package xyz.heroesunited.heroesunited.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import xyz.heroesunited.heroesunited.HeroesUnited;
import xyz.heroesunited.heroesunited.client.events.*;
import xyz.heroesunited.heroesunited.client.gui.AbilitiesScreen;
import xyz.heroesunited.heroesunited.client.renderer.GeckoSuitRenderer;
import xyz.heroesunited.heroesunited.client.renderer.space.CelestialBodyRenderer;
import xyz.heroesunited.heroesunited.common.abilities.*;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;
import xyz.heroesunited.heroesunited.common.abilities.suit.SuitItem;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayerProvider;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCap;
import xyz.heroesunited.heroesunited.common.events.AbilityEvent;
import xyz.heroesunited.heroesunited.common.events.RegisterPlayerControllerEvent;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.networking.server.ServerAbilityKeyInput;
import xyz.heroesunited.heroesunited.common.networking.server.ServerKeyInput;
import xyz.heroesunited.heroesunited.common.networking.server.ServerOpenAccessoriesInv;
import xyz.heroesunited.heroesunited.common.networking.server.ServerToggleAbility;
import xyz.heroesunited.heroesunited.common.objects.container.EquipmentAccessoriesSlot;
import xyz.heroesunited.heroesunited.common.objects.items.HUItems;
import xyz.heroesunited.heroesunited.common.objects.items.IAccessory;
import xyz.heroesunited.heroesunited.common.space.CelestialBodies;
import xyz.heroesunited.heroesunited.common.space.CelestialBody;
import xyz.heroesunited.heroesunited.hupacks.HUPackLayers;
import xyz.heroesunited.heroesunited.hupacks.HUPackSuperpowers;
import xyz.heroesunited.heroesunited.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {

    public static final KeyMapping ABILITIES_SCREEN = new KeyMapping(HeroesUnited.MODID + ".key.abilities_screen", GLFW.GLFW_KEY_H, "key.categories." + HeroesUnited.MODID);
    public static final KeyMapping ACCESSORIES_SCREEN = new KeyMapping(HeroesUnited.MODID + ".key.accessories_screen", GLFW.GLFW_KEY_J, "key.categories." + HeroesUnited.MODID);
    public static final List<AbilityKeyBinding> ABILITY_KEYS = new ArrayList<>();
    public static final KeyMap KEY_MAP = new KeyMap();

    @SubscribeEvent
    public void mouseScroll(InputEvent.MouseScrollingEvent e) {
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_ALT) ||
                InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_ALT)) {
            if (AbilityOverlay.getAbilities(Minecraft.getInstance().player).size() > AbilityOverlay.getCurrentDisplayedAbilities(Minecraft.getInstance().player).size()) {
                if (e.getScrollDelta() > 0) {
                    AbilityOverlay.INDEX--;
                } else {
                    AbilityOverlay.INDEX++;
                }
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void keyInput(InputEvent.Key e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (e.getModifiers() == GLFW.GLFW_MOD_ALT) {
            List<Ability> abilities = AbilityOverlay.getCurrentDisplayedAbilities(mc.player);
            for (int i = 0; i < abilities.size(); i++) {
                int key = GLFW.GLFW_KEY_1 + i;
                if (key == e.getKey() && e.getAction() == GLFW.GLFW_PRESS) {
                    Ability ability = abilities.get(i);
                    if (!ability.alwaysActive()) {
                        HUNetworking.INSTANCE.send(PacketDistributor.SERVER.noArg(), new ServerToggleAbility(ability.name));
                        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    mc.options.keyHotbarSlots[i].release();
                }
            }
        }

        if (ABILITIES_SCREEN.consumeClick()) {
            if (!HUPackSuperpowers.hasSuperpowers(mc.player) || !GsonHelper.getAsBoolean(HUPackSuperpowers.getSuperpowerFrom(mc.player).jsonObject, "block_screen", false)) {
                mc.player.level.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.NEUTRAL, 1, 0);
                mc.setScreen(new AbilitiesScreen());
            }
        }
        if (ACCESSORIES_SCREEN.consumeClick()) {
            HUNetworking.INSTANCE.sendToServer(new ServerOpenAccessoriesInv(mc.player.getId()));
        }
    }

    @SubscribeEvent
    public void huRender(RendererChangeEvent event) {
        AbilityHelper.getAbilities(event.getEntity()).forEach(ability -> ability.getClientProperties().rendererChange(event));
    }

    @SubscribeEvent
    public void onWorldLastRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.dimension().equals(HeroesUnited.SPACE)) {
            PoseStack matrixStack = event.getPoseStack();
            matrixStack.pushPose();

            MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

            Vec3 view = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            matrixStack.translate(-view.x(), -view.y(), -view.z());

            for (CelestialBody celestialBody : CelestialBodies.REGISTRY.get().getValues()) {
                matrixStack.pushPose();
                matrixStack.translate(celestialBody.getCoordinates().x, celestialBody.getCoordinates().y, celestialBody.getCoordinates().z);
                matrixStack.mulPose(HUClientUtil.quatFromXYZ(0, 0, 180, true));
                CelestialBodyRenderer celestialBodyRenderer = CelestialBodyRenderer.getRenderer(celestialBody);
                celestialBodyRenderer.render(matrixStack, buffers, LevelRenderer.getLightColor(Minecraft.getInstance().level, new BlockPos(celestialBody.getCoordinates())), event.getPartialTick());

                VertexConsumer buffer = buffers.getBuffer(RenderType.LINES);
                matrixStack.popPose();
                if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes())
                    LevelRenderer.renderLineBox(matrixStack, buffer, celestialBody.getBoundingBox(), 1, 1, 1, 1);
            }

            matrixStack.popPose();
            RenderSystem.disableDepthTest();
            buffers.endBatch();
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.END || mc.screen != null || mc.player == null || mc.player.isSpectator()) return;

        KeyMap oldKeyMap = new KeyMap();
        oldKeyMap.putAll(KEY_MAP);
        for (Map.Entry<Integer, Boolean> e : KEY_MAP.entrySet()) {
            KeyMapping keyBind = KEY_MAP.getKeyMapping(e.getKey());
            if (e.getValue()) {
                if (!keyBind.isDown()) {
                    KEY_MAP.put(e.getKey(), false);
                }
            } else {
                if (keyBind.isDown()) {
                    KEY_MAP.put(e.getKey(), true);
                }
            }
        }
        if (!KEY_MAP.equals(oldKeyMap)) {
            HUNetworking.INSTANCE.sendToServer(new ServerKeyInput(KEY_MAP));
            mc.player.getCapability(HUAbilityCap.CAPABILITY).ifPresent(cap -> {
                cap.onKeyInput(KEY_MAP);

                cap.getActiveAbilities().values().forEach((ability) -> {
                    if (ability != null) {
                        KeyMap keyMap;
                        if (ability.getKey() != -1) {
                            keyMap = KEY_MAP;
                        } else {
                            keyMap = new KeyMap();
                            keyMap.put(-1, KEY_MAP.get(AbilityOverlay.getCurrentDisplayedAbilities(mc.player).indexOf(ability) + 1));
                        }
                        if (!MinecraftForge.EVENT_BUS.post(new AbilityEvent.KeyInput(mc.player, ability, KEY_MAP, keyMap))) {
                            ability.onKeyInput(mc.player, keyMap);
                            HUNetworking.INSTANCE.sendToServer(new ServerAbilityKeyInput(ability.name, KEY_MAP, keyMap));
                        }
                    }
                });
            });
        }
    }

    @SubscribeEvent
    public void renderEntityPre(RenderLivingEvent.Pre event) {
        if (event.getEntity().level.dimension().equals(HeroesUnited.SPACE)) {
            event.getPoseStack().pushPose();
            if (event.getEntity() instanceof Player && event.getEntity().isCrouching()) {
                event.getPoseStack().translate(0, 0.125D, 0);
            }
            event.getPoseStack().scale(0.01F, 0.01F, 0.01F);
        }
    }

    @SubscribeEvent
    public void renderEntityPost(RenderLivingEvent.Post event) {
        if (event.getEntity().level.dimension().equals(HeroesUnited.SPACE)) {
            event.getPoseStack().popPose();
        }
    }

    @SubscribeEvent
    public void renderShadowSize(ChangeShadowSizeEvent event) {
        for (SizeChangeAbility a : AbilityHelper.getListOfType(SizeChangeAbility.class, AbilityHelper.getAbilities(event.getEntity()))) {
            if (a.changeSizeInRender()) {
                event.setNewSize(event.getDefaultSize() * a.getSize());
            }
        }
    }

    @SubscribeEvent
    public void renderPlayer(RenderPlayerEvent event) {
        for (SizeChangeAbility a : AbilityHelper.getListOfType(SizeChangeAbility.class, AbilityHelper.getAbilities(event.getEntity()))) {
            if (a.changeSizeInRender()) {
                if (event instanceof RenderPlayerEvent.Pre) {
                    event.getPoseStack().pushPose();
                    float size = a.getRenderSize(event.getPartialTick());
                    event.getPoseStack().scale(size, size, size);
                }
                if (event instanceof RenderPlayerEvent.Post) {
                    event.getPoseStack().popPose();
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderHULayer(RenderLayerEvent.Accessories<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> event) {
        for (HideLayerAbility ability : AbilityHelper.getListOfType(HideLayerAbility.class, AbilityHelper.getAbilities(event.getLivingEntity()))) {
            if (ability.layerNameIs("accessories")) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void hideLayers(HideLayerEvent event) {
        if (event.getEntity() instanceof Player) {
            for (HideLayerAbility a : AbilityHelper.getListOfType(HideLayerAbility.class, AbilityHelper.getAbilities(event.getEntity()))) {
                if (a.layerNameIs("armor")) {
                    event.blockLayers(HumanoidArmorLayer.class, ElytraLayer.class);
                }
                if (a.layerNameIs("head")) {
                    event.blockLayer(CustomHeadLayer.class);
                }
                if (a.layerNameIs("arrow")) {
                    event.getLayers().forEach(layer -> {
                        if (layer instanceof StuckInBodyLayer) {
                            event.blockLayer(layer.getClass());
                        }
                    });
                }
                if (a.layerNameIs("held_item")) {
                    event.getLayers().forEach(layer -> {
                        if (layer instanceof ItemInHandLayer) {
                            event.blockLayer(layer.getClass());
                        }
                    });
                }
                if (a.layerNameIs("heroesunited")) {
                    event.blockLayer(HULayerRenderer.class);
                }
                if (a.layerNameIs("player")) {
                    event.getLayers().forEach(layer -> {
                       if (layer.getParentModel() instanceof PlayerModel) {
                           event.blockLayer(layer.getClass());
                       }
                    });
                }
                if (a.layerNameIs("all")) {
                    event.getLayers().forEach(layer -> event.blockLayer(layer.getClass()));
                }
            }
        }
    }

    @SubscribeEvent
    public void setDiscordPresence(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Player) || Minecraft.getInstance().player == null || Minecraft.getInstance().player.getUUID() != event.getEntity().getUUID())
            return;
        HURichPresence.getPresence().setDiscordRichPresence("Playing Heroes United");
    }

    @SubscribeEvent
    public void renderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        player.getCapability(HUPlayerProvider.CAPABILITY).ifPresent(cap -> {
            IFlyingAbility ability = IFlyingAbility.getFlyingAbility(player);
            if (ability != null && ability.isFlying(player) && ability.renderFlying(player)) {
                if (!player.isOnGround() && !player.isSwimming()) {
                    if (!(player.getFallFlyingTicks() > 4) && !player.isVisuallySwimming()) {
                        double d0 = Mth.lerp(event.getPartialTick(), player.xCloakO, player.xCloak) - Mth.lerp(event.getPartialTick(), player.xo, player.getX());
                        double d1 = Mth.lerp(event.getPartialTick(), player.yCloakO, player.yCloak) - Mth.lerp(event.getPartialTick(), player.yo, player.getY());
                        double d2 = Mth.lerp(event.getPartialTick(), player.zCloakO, player.zCloak) - Mth.lerp(event.getPartialTick(), player.zo, player.getZ());
                        float distance = Mth.sqrt((float) (d0 * d0 + d1 * d1 + d2 * d2));
                        float defaultRotation = Mth.clamp(distance, 0.0F, 1.0F) * ability.getDegreesForWalk(player);

                        event.getPoseStack().pushPose();
                        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(-player.getYRot()));
                        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(Mth.clamp(defaultRotation + (cap.getFlightAmount(event.getPartialTick()) * ability.getDegreesForSprint(player)), 0, ability.getDegreesForSprint(player))));
                        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(player.getYRot()));
                    }
                }
            }
        });
        if (GlidingAbility.getInstance(event.getEntity()) != null && GlidingAbility.getInstance(event.getEntity()).canGliding(event.getEntity())) {
            event.getPoseStack().pushPose();
            event.getPoseStack().mulPose(HUClientUtil.quatFromXYZ(0, -event.getEntity().getYRot(), 0, true));
            event.getPoseStack().mulPose(HUClientUtil.quatFromXYZ(90F + event.getEntity().getXRot(), 0, 0, true));
            event.getPoseStack().mulPose(HUClientUtil.quatFromXYZ(0, event.getEntity().getYRot(), 0, true));
        }
        AbilityHelper.getAbilityMap(event.getEntity()).values().forEach(ability -> ability.getClientProperties().renderPlayerPre(event));
    }

    @SubscribeEvent
    public void renderPlayerPost(RenderPlayerEvent.Post event) {
        AbilityHelper.getAbilityMap(event.getEntity()).values().forEach(ability -> ability.getClientProperties().renderPlayerPost(event));
        IFlyingAbility ability = IFlyingAbility.getFlyingAbility(event.getEntity());
        event.getEntity().getCapability(HUPlayerProvider.CAPABILITY).ifPresent(cap -> {
            if (ability != null && ability.isFlying(event.getEntity()) && ability.renderFlying(event.getEntity())) {
                if (!event.getEntity().isOnGround() && !event.getEntity().isSwimming()) {
                    if (!(event.getEntity().getFallFlyingTicks() > 4) && !event.getEntity().isVisuallySwimming()) {
                        event.getPoseStack().popPose();
                    }
                }
            }
        });
        if (GlidingAbility.getInstance(event.getEntity()) != null && GlidingAbility.getInstance(event.getEntity()).canGliding(event.getEntity())) {
            event.getPoseStack().popPose();
        }
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockScreenEffectEvent event) {
        event.getPlayer().getCapability(HUPlayerProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.isIntangible()) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public void registerControllers(RegisterPlayerControllerEvent event) {
        AbilityHelper.getAbilityMap(event.getEntity()).values().forEach(ability -> ability.getClientProperties().registerPlayerControllers(event));
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void setupAnim(SetupAnimEvent event) {
        Player player = event.getEntity();
        PlayerModel<?> model = event.getPlayerModel();
        AbilityHelper.getAbilities(event.getEntity()).forEach(ability -> ability.getClientProperties().setupAnim(event));
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            SuitItem suitItem = Suit.getSuitItem(equipmentSlot, player);
            if (suitItem != null) {
                suitItem.getSuit().setupAnim(event, equipmentSlot);
            }
        }

        AbilityHelper.getAbilityMap(player).values().forEach(ability -> ability.getClientProperties().setAlwaysRotationAngles(event));
        player.getCapability(HUPlayerProvider.CAPABILITY).ifPresent(cap -> {
            for (int slot = 0; slot <= 8; ++slot) {
                ItemStack stack = cap.getInventory().getItem(slot);
                if (stack.getItem() instanceof IAccessory accessory) {
                    if (accessory.getHiddenParts(false) != null) {
                        for (PlayerPart part : accessory.getHiddenParts(false)) {
                            part.setVisibility(model, false, accessory.getPlayerWearSize(stack));
                        }
                    }
                }
            }

            if (!cap.getInventory().getItem(EquipmentAccessoriesSlot.JACKET.getSlot()).isEmpty() &&
                    cap.getInventory().getItem(EquipmentAccessoriesSlot.JACKET.getSlot()).getItem() == HUItems.EMILIA_CAPE.get()) {
                model.leftLeg.xRot /= 100F;
                model.rightLeg.xRot /= 100F;
                model.rightLeg.yRot = model.leftLeg.yRot = model.body.yRot;
                model.rightArm.setRotation(0, model.body.yRot, 0);
                model.leftArm.setRotation(0, model.body.yRot, 0);
                model.rightSleeve.copyFrom(model.rightArm);
                model.leftSleeve.copyFrom(model.leftArm);
                model.rightPants.copyFrom(model.rightLeg);
                model.leftPants.copyFrom(model.leftLeg);

                if (player.isCrouching()) {
                    model.body.xRot = model.body.y = 0.0F;
                    model.rightLeg.z = model.leftLeg.z = 0.1F;
                    model.rightLeg.y = model.leftLeg.y = 12.0F;
                    model.head.y = 1.2F;
                    model.leftArm.y = model.rightArm.y = 2.0F;
                }
            }

            IFlyingAbility ability = IFlyingAbility.getFlyingAbility(player);
            if (ability != null && ability.isFlying(player) && ability.renderFlying(player) && ability.setDefaultRotationAngles(event)) {
                if (!player.isOnGround() && !player.isSwimming() && player.isSprinting()) {
                    float flightAmount = cap.getFlightAmount(event.getPartialTicks());
                    float armRotations = ability.rotateArms(player) ? (float) Math.toRadians(180F) : 0F;
                    float delta = Mth.sin(player.tickCount / 10F) / 100F;

                    model.head.xRot = model.rotlerpRad(flightAmount, model.head.xRot, (-(float) Math.PI / 4F));
                    if (ability.rotateArms(player)) {
                        model.leftArm.xRot = armRotations;
                        model.rightArm.xRot = armRotations;

                        model.leftArm.zRot = model.rotlerpRad(flightAmount, model.leftArm.zRot, 0);
                        model.rightArm.zRot = model.rotlerpRad(flightAmount, model.rightArm.zRot, 0);
                    } else {
                        model.leftArm.xRot = model.rotlerpRad(flightAmount, model.leftArm.xRot, armRotations);
                        model.rightArm.xRot = model.rotlerpRad(flightAmount, model.rightArm.xRot, armRotations);

                        model.leftArm.zRot = model.rotlerpRad(flightAmount, model.leftArm.zRot, (float) Math.toRadians(-11.25F - delta));
                        model.rightArm.zRot = model.rotlerpRad(flightAmount, model.rightArm.zRot, (float) Math.toRadians(11.25F + delta));

                    }

                    model.leftArm.yRot = model.rotlerpRad(flightAmount, model.leftArm.yRot, 0);
                    model.rightArm.yRot = model.rotlerpRad(flightAmount, model.rightArm.yRot, 0);

                    model.leftLeg.xRot = model.rotlerpRad(flightAmount, model.leftLeg.xRot, 0);
                    model.rightLeg.xRot = model.rotlerpRad(flightAmount, model.rightLeg.xRot, 0);

                    ModelPart mainLeg = player.getMainArm() == HumanoidArm.LEFT ? model.leftLeg : model.rightLeg;
                    mainLeg.z = model.rotlerpRad(flightAmount, mainLeg.z, delta-0.25F);
                }
            }
        });
    }

    @SubscribeEvent
    public void onInputUpdate(MovementInputUpdateEvent event) {
        AbilityHelper.getAbilityMap(event.getEntity()).values().forEach(ability -> ability.getClientProperties().inputUpdate(event));
    }

    @SubscribeEvent
    public void renderPlayerHandPost(RenderPlayerHandEvent.Post event) {
        AbilityHelper.getAbilityMap(event.getEntity()).values().forEach(ability -> ability.getClientProperties().renderAlwaysFirstPersonArm(Minecraft.getInstance().getEntityModels(), event.getRenderer(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getEntity(), event.getSide()));
    }

    @SubscribeEvent
    public void renderPlayerLayers(RenderLayerEvent.Armor.Post event) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = event.getLivingEntity().getItemBySlot(slot);
            if (itemStack.getItem() instanceof SuitItem suitItem && event.getRenderer().getModel() instanceof HumanoidModel<? extends LivingEntity> model) {
                HUPackLayers.Layer layer = HUPackLayers.getInstance().getLayer(suitItem.getSuit().getRegistryName());
                if (layer != null) {
                    if (layer.getTexture("cape") != null && slot.equals(EquipmentSlot.CHEST)) {
                        HUClientUtil.renderCape(model, event.getLivingEntity(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTicks(), layer.getTexture("cape"));
                    }
                    var itemModel = IClientItemExtensions.of(itemStack).getHumanoidArmorModel(event.getLivingEntity(), itemStack, slot, model);
                    if (!(itemModel instanceof GeckoSuitRenderer<?>) && layer.getTexture("lights") != null) {
                        itemModel.renderToBuffer(event.getPoseStack(), event.getMultiBufferSource().getBuffer(HUClientUtil.HURenderTypes.getLight(layer.getTexture("lights"))), event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void renderPlayerLayers(RenderLayerEvent.Player event) {
        AbilityHelper.getAbilityMap(event.getPlayer()).values().forEach(ability -> ability.getClientProperties().renderAlways(event.getContext(), event.getRenderer(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPlayer(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getPartialTicks(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch()));
    }

    @SubscribeEvent
    public void renderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) return;
        AbstractClientPlayer player = mc.player;
        boolean canceled = false;
        for (BasicLaserAbility a : AbilityHelper.getListOfType(BasicLaserAbility.class, AbilityHelper.getAbilities(player))) {
            float alpha = a.getAlpha(event.getPartialTick());
            if (alpha == 0) continue;
            Color color = HUJsonUtils.getColor(a.getJsonObject());
            HitResult hitResult = HUPlayerUtil.getPosLookingAt(player, a.getDataManager().getAsFloat("distance"));
            double distance = player.getEyePosition().distanceTo(hitResult.getLocation());
            AABB box = new AABB(0.1F, -0.25, 0, 0, -0.25, -distance).inflate(0.03125D);
            if (a instanceof EnergyLaserAbility) {
                event.getPoseStack().pushPose();
                event.getPoseStack().translate(((EnergyLaserAbility) a).isLeftArm(player) ? -0.3F : 0.3F, 0, 0);
                HUClientUtil.renderFilledBox(event.getPoseStack(), event.getMultiBufferSource().getBuffer(HUClientUtil.HURenderTypes.LASER), box, 1F, 1F, 1F, alpha, event.getPackedLight());
                HUClientUtil.renderFilledBox(event.getPoseStack(), event.getMultiBufferSource().getBuffer(HUClientUtil.HURenderTypes.LASER), box.inflate(0.03125D), color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, alpha * 0.5F, event.getPackedLight());
                canceled = true;
                event.getPoseStack().popPose();
            }
            if (a instanceof HeatVisionAbility) {
                event.getPoseStack().pushPose();
                if (a.getDataManager().getAsString("type").equals("cyclop")) {
                    box = new AABB(-0.15F, -0.1F, 0.5F, 0.15F, -0.1F, -distance).inflate(0.03125D);
                    HUClientUtil.renderFilledBox(event.getPoseStack(), event.getMultiBufferSource().getBuffer(HUClientUtil.HURenderTypes.LASER), box, 1F, 1F, 1F, alpha, event.getPackedLight());
                    HUClientUtil.renderFilledBox(event.getPoseStack(), event.getMultiBufferSource().getBuffer(HUClientUtil.HURenderTypes.LASER), box.inflate(0.03125D), color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, alpha * 0.5F, event.getPackedLight());
                }
                if (a.getDataManager().getAsString("type").equals("default")) {
                    for (int i = 0; i < 2; i++) {
                        event.getPoseStack().pushPose();
                        event.getPoseStack().translate(i == 0 ? 0.2F : -0.3F, 0.25, 0);
                        HUClientUtil.renderFilledBox(event.getPoseStack(), event.getMultiBufferSource().getBuffer(HUClientUtil.HURenderTypes.LASER), box, 1F, 1F, 1F, alpha, event.getPackedLight());
                        HUClientUtil.renderFilledBox(event.getPoseStack(), event.getMultiBufferSource().getBuffer(HUClientUtil.HURenderTypes.LASER), box.inflate(0.03125D), color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, alpha * 0.5F, event.getPackedLight());
                        event.getPoseStack().popPose();
                    }
                }
                if (a.getEnabled()) {
                    event.setCanceled(true);
                }
                event.getPoseStack().popPose();
                return;
            }
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(equipmentSlot);
            if (stack.getItem() instanceof SuitItem suitItem) {
                if (suitItem.getSlot().equals(equipmentSlot)) {
                    if (suitItem.renderWithoutArm()) {
                        suitItem.renderFirstPersonArm(mc.getEntityModels(), null, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), player, player.getMainArm(), stack);
                    }
                }
            }
        }
        for (Ability ability : AbilityHelper.getAbilityMap(player).values()) {
            if (ability.getClientProperties() instanceof GeoAbilityClientProperties<?> properties) {
                properties.getGeoRenderer().doAnimationProcess();
            }
        }

        event.setCanceled(canceled);
    }

    public static class AbilityKeyBinding extends KeyMapping {

        public final int index;

        public AbilityKeyBinding(String description, int keyCode, int index) {
            super(description, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, keyCode, "key.categories." + HeroesUnited.MODID);
            this.index = index;
        }
    }
}
