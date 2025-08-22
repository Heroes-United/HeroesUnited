package xyz.heroesunited.heroesunited.common.objects.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationProcessor;
import software.bernie.geckolib.util.RenderUtils;
import xyz.heroesunited.heroesunited.HeroesUnited;
import xyz.heroesunited.heroesunited.client.renderer.GeckoAccessoryRenderer;
import xyz.heroesunited.heroesunited.common.objects.container.EquipmentAccessoriesSlot;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.util.Arrays;
import java.util.function.Consumer;

class ClothesAccessory extends GeckoAccessory {

    private final String id;

    public ClothesAccessory(EquipmentAccessoriesSlot accessorySlot, String id) {
        super(accessorySlot);
        this.id = id;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new GeckoAccessoryRenderer() {

                @Override
                public void actuallyRender(PoseStack poseStack, GeckoAccessory animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

                    for (GeoBone topLevelBone : model.topLevelBones()) {
                        topLevelBone.setHidden(true);
                    }

                    EquipmentAccessoriesSlot slot = ClothesAccessory.this.accessorySlot;

                    if (slot == EquipmentAccessoriesSlot.TSHIRT) {
                        model.getBone("tshirt_body").ifPresent(b -> b.setHidden(false));
                        model.getBone("tshirt_right_arm").ifPresent(b -> b.setHidden(false));
                        model.getBone("tshirt_left_arm").ifPresent(b -> b.setHidden(false));
                    }

                    if (slot == EquipmentAccessoriesSlot.JACKET) {
                        for (String s : Arrays.asList("jacket_head", "body", "right_arm",
                                "left_arm", "jacket_right_leg", "jacket_left_leg")) {
                            model.getBone(s).ifPresent(b -> b.setHidden(false));
                        }
                    }

                    if (slot == EquipmentAccessoriesSlot.PANTS) {
                        model.getBone("right_leg").ifPresent(b -> b.setHidden(false));
                        model.getBone("left_leg").ifPresent(b -> b.setHidden(false));
                    }

                    if (slot == EquipmentAccessoriesSlot.SHOES) {
                        model.getBone("right_leg_boots").ifPresent(b -> b.setHidden(false));
                        model.getBone("left_leg_boots").ifPresent(b -> b.setHidden(false));
                    }
                    super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
                }
            };

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(EntityRendererProvider.Context context, LivingEntityRenderer<? extends LivingEntity, ? extends HumanoidModel<?>> renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, LivingEntity livingEntity, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, int slot) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 24 / 16F, 0.0D);
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        GeckoAccessoryRenderer accessoryRenderer = (GeckoAccessoryRenderer) IClientItemExtensions.of(this).getCustomRenderer();
        ResourceLocation location = getModelFile();
        if (HUPlayerUtil.haveSmallArms(livingEntity)) {
            location = new ResourceLocation(getModelFile().toString().replace(".geo", "_slim.geo"));
        }
        BakedGeoModel model = accessoryRenderer.getGeoModel().getBakedModel(location);
        AnimationProcessor<GeckoAccessory> processor = accessoryRenderer.getGeoModel().getAnimationProcessor();

        for (CoreGeoBone iBone : processor.getRegisteredBones()) {
            if (iBone.getName().contains("head")) {
                RenderUtils.matchModelPartRot(renderer.getModel().head, iBone);
                iBone.updatePosition(renderer.getModel().head.x, -renderer.getModel().head.y, renderer.getModel().head.z);
            }
            if (iBone.getName().contains("body")) {
                RenderUtils.matchModelPartRot(renderer.getModel().body, iBone);
                iBone.updatePosition(renderer.getModel().body.x, -renderer.getModel().body.y, renderer.getModel().body.z);
            }
            if (iBone.getName().contains("right_arm")) {
                RenderUtils.matchModelPartRot(renderer.getModel().rightArm, iBone);
                iBone.updatePosition(renderer.getModel().rightArm.x + 5, 2 - renderer.getModel().rightArm.y, renderer.getModel().rightArm.z);

            }
            if (iBone.getName().contains("left_arm")) {
                RenderUtils.matchModelPartRot(renderer.getModel().leftArm, iBone);
                iBone.updatePosition(renderer.getModel().leftArm.x - 5, 2 - renderer.getModel().leftArm.y, renderer.getModel().leftArm.z);
            }
            if (iBone.getName().contains("right_leg")) {
                RenderUtils.matchModelPartRot(renderer.getModel().rightLeg, iBone);
                iBone.updatePosition(renderer.getModel().rightLeg.x + 2, 12 - renderer.getModel().rightLeg.y, renderer.getModel().rightLeg.z);
            }
            if (iBone.getName().contains("left_leg")) {
                RenderUtils.matchModelPartRot(renderer.getModel().leftLeg, iBone);
                iBone.updatePosition(renderer.getModel().leftLeg.x - 2, 12 - renderer.getModel().leftLeg.y, renderer.getModel().leftLeg.z);
            }
        }


        RenderType type = RenderType.entityTranslucent(this.getTextureFile());

        accessoryRenderer.setInitialProperties(stack);
        accessoryRenderer.actuallyRender(poseStack, (GeckoAccessory) stack.getItem(), model, type,
                bufferIn, bufferIn.getBuffer(type), false, partialTicks, packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -24 / 16F, 0.0D);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureFile() {
        return new ResourceLocation(HeroesUnited.MODID, "textures/accessories/%s.png".formatted(this.id));
    }

    @Override
    public ResourceLocation getModelFile() {
        return new ResourceLocation(HeroesUnited.MODID, "geo/%s.geo.json".formatted(this.id));
    }
}
