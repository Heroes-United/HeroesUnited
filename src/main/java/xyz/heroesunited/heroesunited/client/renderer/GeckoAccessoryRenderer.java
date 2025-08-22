package xyz.heroesunited.heroesunited.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import xyz.heroesunited.heroesunited.common.objects.items.GeckoAccessory;
import xyz.heroesunited.heroesunited.common.objects.items.HUItems;

public class GeckoAccessoryRenderer extends GeoItemRenderer<GeckoAccessory> {

    public GeckoAccessoryRenderer() {
        this(null);
    }

    public GeckoAccessoryRenderer(ResourceLocation modelFile) {
        super(new DefaultedGeoModel<>(new ResourceLocation("")) {
            @Override
            public ResourceLocation getAnimationResource(GeckoAccessory accessory) {
                return accessory.getAnimationFile();
            }

            @Override
            protected String subtype() {
                return "item";
            }

            @Override
            public ResourceLocation getModelResource(GeckoAccessory accessory) {
                return modelFile == null ? accessory.getModelFile() : modelFile;
            }

            @Override
            public ResourceLocation getTextureResource(GeckoAccessory accessory) {
                return accessory.getTextureFile();
            }
        });
    }

    @Override
    public void preRender(PoseStack poseStack, GeckoAccessory animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setInitialProperties(ItemStack stack) {
        this.animatable = (GeckoAccessory)stack.getItem();
        this.currentItemStack = stack;
    }

    @Override
    public RenderType getRenderType(GeckoAccessory animatable, ResourceLocation texture, @org.jetbrains.annotations.Nullable MultiBufferSource bufferSource, float partialTick) {
        if (animatable == HUItems.JASON_MASK.get() || animatable == HUItems.ZEK_GLASSES.get() || animatable == HUItems.MADNESSCOMBAT.get()) {
            return RenderType.entityTranslucent(texture);
        }
        return RenderType.entityCutoutNoCull(texture);
    }
}
