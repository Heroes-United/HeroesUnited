package xyz.heroesunited.heroesunited.common.abilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.heroesunited.heroesunited.client.events.RendererChangeEvent;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCap;
import xyz.heroesunited.heroesunited.common.events.EntitySprintingEvent;
import xyz.heroesunited.heroesunited.common.events.RegisterPlayerControllerEvent;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.networking.client.ClientSyncAbility;
import xyz.heroesunited.heroesunited.common.networking.client.ClientSyncAbilityCreators;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.hudata.HUData;
import xyz.heroesunited.heroesunited.util.hudata.HUDataManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class Ability implements INBTSerializable<CompoundTag> {

    public String name;
    public final AbilityType type;
    protected CompoundTag additionalData = new CompoundTag();
    protected JsonObject jsonObject;
    protected final HUDataManager dataManager = new HUDataManager();
    protected final JsonConditionManager conditionManager = new JsonConditionManager(this);

    public Ability(AbilityType type) {
        this.type = type;
    }

    public HUDataManager getDataManager() {
        return this.dataManager;
    }

    public void registerData() {
        this.dataManager.register("prev_cooldown", 0);
        this.dataManager.register("cooldown", 0);
        this.dataManager.register("maxCooldown", 0, true);
    }

    public boolean canActivate(Player player) {
        return this.conditionManager.isEnabled(player, "canActivate");
    }

    @Nullable
    public List<Component> getHoveredDescription() {
        return getJsonObject() != null && getJsonObject().has("description") ? HUJsonUtils.parseDescriptionLines(jsonObject.get("description")) : null;
    }

    public void onActivated(Player player) {
    }

    public void onUpdate(Player player) {
        this.dataManager.set("prev_cooldown", this.dataManager.<Integer>getValue("cooldown"));
        if (this.dataManager.<Integer>getValue("cooldown") > 0) {
            this.dataManager.set("cooldown", this.dataManager.<Integer>getValue("cooldown") - 1);
        }
        this.conditionManager.update(player);

        if (!canActivate(player) && !alwaysActive(player)) {
            player.getCapability(HUAbilityCap.CAPABILITY).ifPresent(a -> a.disable(name));
        }
    }

    public void onDeactivated(Player player) {
    }

    public void onKeyInput(Player player, Map<Integer, Boolean> map) {
    }

    public void cancelSprinting(EntitySprintingEvent event) {
    }

    public int getKey() {
        if (getJsonObject() != null && getJsonObject().has("key")) {
            return GsonHelper.getAsInt(GsonHelper.getAsJsonObject(this.getJsonObject(), "key"), "id");
        }
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(PlayerRenderer renderer, PoseStack matrix, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @OnlyIn(Dist.CLIENT)
    public void inputUpdate(MovementInputUpdateEvent event) {
    }

    @OnlyIn(Dist.CLIENT)
    public void registerPlayerControllers(RegisterPlayerControllerEvent event) {
    }

    @OnlyIn(Dist.CLIENT)
    public void setupAnim(SetupAnimEvent event) {
    }

    @OnlyIn(Dist.CLIENT)
    public void renderPlayerPre(RenderPlayerEvent.Pre event) {
    }

    @OnlyIn(Dist.CLIENT)
    public void renderPlayerPost(RenderPlayerEvent.Post event) {
    }

    @OnlyIn(Dist.CLIENT)
    public boolean renderFirstPersonArm(PlayerRenderer renderer, PoseStack matrix, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer player, HumanoidArm side) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void rendererChange(RendererChangeEvent event) {
    }

    @OnlyIn(Dist.CLIENT)
    public void drawIcon(PoseStack stack, int x, int y) {
        Minecraft.getInstance().getItemRenderer().blitOffset -= 100f;
        if (getJsonObject() != null && getJsonObject().has("icon")) {
            JsonObject icon = getJsonObject().getAsJsonObject("icon");
            String type = GsonHelper.getAsString(icon, "type");
            if (type.equals("texture")) {
                ResourceLocation texture = new ResourceLocation(GsonHelper.getAsString(icon, "texture"));
                int width = GsonHelper.getAsInt(icon, "width", 16);
                int height = GsonHelper.getAsInt(icon, "height", 16);
                int textureWidth = GsonHelper.getAsInt(icon, "texture_width", 256);
                int textureHeight = GsonHelper.getAsInt(icon, "texture_height", 256);
                RenderSystem.setShaderTexture(0, texture);
                GuiComponent.blit(stack, x, y, GsonHelper.getAsInt(icon, "u", 0), GsonHelper.getAsInt(icon, "v", 0), width, height, textureWidth, textureHeight);
            } else if (type.equals("item")) {
                Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(icon, "item")))), x, y);
            }
        } else {
            Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(new ItemStack(Items.APPLE), x, y);
        }
        Minecraft.getInstance().getItemRenderer().blitOffset += 100f;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("AbilityType", this.type.getRegistryName().toString());
        nbt.put("HUData", this.dataManager.serializeNBT());
        nbt.put("Conditions", this.conditionManager.serializeNBT());
        nbt.put("AdditionalData", additionalData);
        if (this.jsonObject != null) {
            nbt.putString("JsonObject", this.jsonObject.toString());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.dataManager.deserializeNBT(nbt.getCompound("HUData"));
        this.conditionManager.deserializeNBT(nbt.getCompound("Conditions"));
        this.additionalData = nbt.getCompound("AdditionalData");
        if (nbt.contains("JsonObject")) {
            this.jsonObject = JsonParser.parseString(nbt.getString("JsonObject")).getAsJsonObject();
        }
    }

    public Component getTitle() {
        if (getJsonObject() != null && getJsonObject().has("title")) {
            return Component.Serializer.fromJson(GsonHelper.getAsJsonObject(getJsonObject(), "title"));
        } else {
            return new TranslatableComponent(name);
        }
    }

    public JsonConditionManager getConditionManager() {
        return conditionManager;
    }

    public CompoundTag getAdditionalData() {
        return additionalData;
    }

    public int getMaxCooldown(Player player) {
        return this.dataManager.<Integer>getValue("maxCooldown");
    }

    public boolean getEnabled() {
        return false;
    }

    public boolean isHidden(Player player) {
        return getJsonObject() != null && GsonHelper.getAsBoolean(getJsonObject(), "hidden", false) && this.conditionManager.isEnabled(player, "isHidden");
    }

    public boolean alwaysActive(Player player) {
        return getJsonObject() != null && GsonHelper.getAsBoolean(getJsonObject(), "active", false) && this.conditionManager.isEnabled(player, "alwaysActive");
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public Ability setJsonObject(Entity entity, JsonObject jsonObject) {
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            this.conditionManager.registerConditions(jsonObject);
            if (entity != null) {
                for (Map.Entry<String, HUData<?>> entry : this.dataManager.getHUDataMap().entrySet()) {
                    if ( entry.getValue().isJson()) {
                        this.dataManager.set(entry.getKey(), entry.getValue().getFromJson(jsonObject, entry.getKey(), entry.getValue().getDefaultValue()));
                    }
                }
                if (entity instanceof ServerPlayer) {
                    HUNetworking.INSTANCE.sendTo(new ClientSyncAbilityCreators(entity.getId(), name, jsonObject), ((ServerPlayer) entity).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        }
        return this;
    }

    public Ability setAdditionalData(CompoundTag nbt) {
        this.additionalData = nbt;
        return this;
    }

    public void sync(Player player) {
        if (player instanceof ServerPlayer) {
            HUNetworking.INSTANCE.sendTo(new ClientSyncAbility(player.getId(), this.name, this.serializeNBT()), ((ServerPlayer) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public void syncToAll(Player player) {
        this.sync(player);
        for (Player mpPlayer : player.level.players()) {
            if (mpPlayer instanceof ServerPlayer) {
                HUNetworking.INSTANCE.sendTo(new ClientSyncAbility(player.getId(), this.name, this.serializeNBT()), ((ServerPlayer) mpPlayer).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }
}
