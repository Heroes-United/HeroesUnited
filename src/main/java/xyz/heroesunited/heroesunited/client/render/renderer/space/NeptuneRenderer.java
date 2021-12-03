package xyz.heroesunited.heroesunited.client.render.renderer.space;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import xyz.heroesunited.heroesunited.HeroesUnited;

public class NeptuneRenderer extends PlanetRenderer {
    public NeptuneRenderer(ModelPart planet) {
        super(planet);
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(HeroesUnited.MODID, "textures/planets/neptune.png");
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffers, int packedLight, float partialTicks) {

        matrixStack.scale(2.95F, 2.95F, 2.95F);
        matrixStack.translate(0, -1, 0);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation()));
        planetModel.prepareModel(partialTicks);
        planetModel.renderToBuffer(matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
    }

    @Override
    protected RenderType getRenderType() {
        return RenderType.entityTranslucent(getTextureLocation());
    }
}
