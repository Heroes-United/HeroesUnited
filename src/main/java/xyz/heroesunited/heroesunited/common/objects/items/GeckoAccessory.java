package xyz.heroesunited.heroesunited.common.objects.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;
import xyz.heroesunited.heroesunited.client.renderer.GeckoAccessoryRenderer;
import xyz.heroesunited.heroesunited.common.objects.container.EquipmentAccessoriesSlot;

import java.util.function.Consumer;

public class GeckoAccessory extends DefaultAccessoryItem implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GeckoAccessory(EquipmentAccessoriesSlot accessorySlot) {
        super(new Properties(), accessorySlot);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new GeckoAccessoryRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    public ResourceLocation getTextureFile() {
        return new ResourceLocation(this.registryName().getNamespace(), String.format("textures/accessories/%s.png", this.registryName().getPath()));
    }

    public ResourceLocation getModelFile() {
        return new ResourceLocation(this.registryName().getNamespace(), String.format("geo/%s.geo.json", this.registryName().getPath()));
    }

    public ResourceLocation getAnimationFile() {
        return new ResourceLocation(this.registryName().getNamespace(), String.format("animations/%s.animation.json", this.registryName().getPath()));
    }

    private ResourceLocation registryName() {
        return ForgeRegistries.ITEMS.getKey(this);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(EntityRendererProvider.Context context, LivingEntityRenderer<? extends LivingEntity, ? extends HumanoidModel<?>> renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, LivingEntity livingEntity, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, int slot) {
        if (stack.getItem() == HUItems.HOKAGE_CAPE.get()) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 24 / 16F, 0.0D);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            this.renderHokageCape(renderer, poseStack, bufferIn, packedLightIn, livingEntity, partialTicks, stack);
            poseStack.popPose();
            return;
        }
        if (EquipmentAccessoriesSlot.wristAccessories().contains(accessorySlot)) {
            HumanoidArm side = slot == EquipmentAccessoriesSlot.LEFT_WRIST.getSlot() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            ItemTransforms.TransformType transformType = side == HumanoidArm.LEFT ? ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
            if (stack.getItem() == HUItems.BOOSTED_GEAR.get() || stack.getItem() == HUItems.FINN_ARM.get() || stack.getItem() == HUItems.MADNESSCLAW.get()) {
                transformType = ItemTransforms.TransformType.HEAD;
            }

            poseStack.pushPose();
            renderer.getModel().translateToHand(side, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.translate((side == HumanoidArm.LEFT ? -1 : 1) / 16.0F, 0.125D, -0.625D);
            Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(livingEntity, stack, transformType, side == HumanoidArm.LEFT, poseStack, bufferIn, packedLightIn);
            poseStack.popPose();
        }
        if (this.accessorySlot.equals(EquipmentAccessoriesSlot.GLOVES)) {
            for (HumanoidArm side : HumanoidArm.values()) {
                ItemTransforms.TransformType transformType = side == HumanoidArm.LEFT ? ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
                poseStack.pushPose();
                renderer.getModel().translateToHand(side, poseStack);
                if (stack.getItem() == HUItems.SMALLGILLY.get()) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    poseStack.scale(0.625F, -0.625F, -0.625F);
                    poseStack.translate(side == HumanoidArm.LEFT ? -0.6 : -0.4, -0.35D, -0.625D);
                    ResourceLocation modelFile = new ResourceLocation(this.registryName().getNamespace(), String.format("geo/%s.geo.json", this.registryName().getPath() + (side == HumanoidArm.LEFT ? "" : "_v2")));
                    GeckoAccessoryRenderer accessoryRenderer = new GeckoAccessoryRenderer(modelFile);

                    RenderType renderType = accessoryRenderer.getRenderType(this, accessoryRenderer.getTextureLocation(this), bufferIn, Minecraft.getInstance().getFrameTime());
                    VertexConsumer buffer = ItemRenderer.getFoilBufferDirect(bufferIn, renderType, false, stack.hasFoil());
                    accessoryRenderer.defaultRender(poseStack, this, bufferIn, renderType, buffer,
                            0, Minecraft.getInstance().getFrameTime(), packedLightIn);
                } else {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    poseStack.translate((side == HumanoidArm.LEFT ? -1 : 1) / 16.0F, 0.125D, -0.625D);
                    Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(livingEntity, stack, transformType, side == HumanoidArm.LEFT, poseStack, bufferIn, packedLightIn);
                }
                poseStack.popPose();
            }
        }

        if (this.accessorySlot.equals(EquipmentAccessoriesSlot.TSHIRT) || this.accessorySlot.equals(EquipmentAccessoriesSlot.JACKET) || this.accessorySlot.equals(EquipmentAccessoriesSlot.BELT)) {
            poseStack.pushPose();
            renderer.getModel().body.translateAndRotate(poseStack);
            poseStack.translate(0.0D, -0.25D, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.625F, -0.625F, -0.625F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.HEAD, packedLightIn, OverlayTexture.NO_OVERLAY, poseStack, bufferIn, 0);
            poseStack.popPose();
        }

        if (this.accessorySlot.equals(EquipmentAccessoriesSlot.HELMET)) {
            poseStack.pushPose();
            renderer.getModel().head.translateAndRotate(poseStack);
            poseStack.translate(0.0D, -0.25D, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.625F, -0.625F, -0.625F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.HEAD, packedLightIn, OverlayTexture.NO_OVERLAY, poseStack, bufferIn, 0);
            poseStack.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderHokageCape(LivingEntityRenderer<? extends LivingEntity, ? extends HumanoidModel<?>> renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, LivingEntity livingEntity, float partialTicks, ItemStack stack) {
        GeckoAccessoryRenderer accessoryRenderer = new GeckoAccessoryRenderer();
        BakedGeoModel model = accessoryRenderer.getGeoModel().getBakedModel(getModelFile());

        GeoBone headBone = model.getBone("armorHead").orElse(null);
        if (headBone != null) {
            RenderUtils.matchModelPartRot(renderer.getModel().head, headBone);
            headBone.updatePosition(renderer.getModel().head.x, -renderer.getModel().head.y, renderer.getModel().head.z);
        }

        GeoBone bodyBone = model.getBone("armorBody").orElse(null);
        if (bodyBone != null) {
            RenderUtils.matchModelPartRot(renderer.getModel().body, bodyBone);
            bodyBone.updatePosition(renderer.getModel().body.x, -renderer.getModel().body.y, renderer.getModel().body.z);
        }

        GeoBone rightArmBone = model.getBone("armorRightArm").orElse(null);
        if (rightArmBone != null) {
            RenderUtils.matchModelPartRot(renderer.getModel().rightArm, rightArmBone);
            rightArmBone.updatePosition(renderer.getModel().rightArm.x + 5, 2 - renderer.getModel().rightArm.y, renderer.getModel().rightArm.z);
        }

        GeoBone leftArmBone = model.getBone("armorLeftArm").orElse(null);
        if (leftArmBone != null) {
            RenderUtils.matchModelPartRot(renderer.getModel().leftArm, leftArmBone);
            leftArmBone.updatePosition(renderer.getModel().leftArm.x - 5, 2 - renderer.getModel().leftArm.y, renderer.getModel().leftArm.z);
        }

        if (livingEntity instanceof AbstractClientPlayer player) {
            double d0 = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX());
            double d1 = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY());
            double d2 = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ());
            float f = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO);
            double d3 = Mth.sin(f * ((float) Math.PI / 180F));
            double d4 = -Mth.cos(f * ((float) Math.PI / 180F));
            float f1 = Mth.clamp((float) d1 * 10.0F, -6.0F, 32.0F);
            float f2 = Mth.clamp((float) (d0 * d3 + d2 * d4) * 100.0F, 0.0F, 150.0F);

            f1 = f1 + Mth.sin(Mth.lerp(partialTicks, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * Mth.lerp(partialTicks, player.oBob, player.bob);

            GeoBone cape = model.getBone("cape").orElse(null);
            float rot = Mth.clamp(f2 + f1, 0F, 80F);
            if (cape != null) {
                cape.setRotX((float) Math.toRadians(-rot));
                cape.setPosY(0);
                cape.setPosZ(0);
                if (player.isCrouching()) {
                    cape.setRotX(0.5F + cape.getRotX());
                    cape.setPosY(2.2F);
                    cape.setPosZ(-1F);
                }
            }
            rot = 0.15F + rot / 80F;
            if (player.isCrouching()) {
                rot += 0.15F;
            }

            GeoBone right = model.getBone("right").orElse(null);
            if (right != null) {
                right.setRotX(rot * (float) Math.toRadians(-9.5));
                right.setRotY(rot * (float) Math.toRadians(-59));
                right.setRotZ(rot * (float) Math.toRadians(8.8));
            }

            GeoBone left = model.getBone("left").orElse(null);
            if (left != null) {
                left.setRotX(rot * (float) Math.toRadians(-9.5));
                left.setRotY(rot * (float) Math.toRadians(59));
                left.setRotZ(rot * (float) Math.toRadians(-8.8));
            }
        }


        RenderType type = RenderType.entityTranslucent(this.getTextureFile());
        accessoryRenderer.setInitialProperties(stack);
        accessoryRenderer.actuallyRender(poseStack, (GeckoAccessory) stack.getItem(), model, type,
                bufferIn, bufferIn.getBuffer(type), false, partialTicks, packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean renderDefaultModel() {
        return false;
    }

    @Override
    public ResourceLocation getTexture(ItemStack stack, LivingEntity livingEntity, EquipmentAccessoriesSlot slot) {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        if (this == HUItems.BOBO_ACCESSORY.get()) {
            controllers.add(new AnimationController<>(this, "controller", 20, event -> {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("animation.bobo"));
                return PlayState.CONTINUE;
            }));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
