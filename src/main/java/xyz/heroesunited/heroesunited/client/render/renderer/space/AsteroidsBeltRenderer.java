package xyz.heroesunited.heroesunited.client.render.renderer.space;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import xyz.heroesunited.heroesunited.client.render.model.space.StarModel;

import java.util.Random;

public class AsteroidsBeltRenderer extends CelestialBodyRenderer {

    private ModelRenderer[] asteroids = new ModelRenderer[10000];
    private float counter = 0;

    public AsteroidsBeltRenderer() {
        Random random = new Random();
        for (int i = 0; i < asteroids.length; i++) {
            Vector3d asteroidPosition = new Vector3d(10000, 0, 0).yRot(7 * i);
            asteroids[i] = new ModelRenderer(64, 64, 0, 0);
            asteroids[i].addBox(0,0,0,
                    random.nextFloat() + random.nextInt(50) + 1,
                    random.nextFloat() + random.nextInt(50) + 1,
                    random.nextFloat() + random.nextInt(50) + 1);
            asteroids[i].setPos(((float) asteroidPosition.x) + (random.nextBoolean() ? random.nextFloat() : -random.nextFloat()) *2,
                    ((float) asteroidPosition.y) + (random.nextBoolean() ? random.nextFloat() : -random.nextFloat())*2,
                    ((float) asteroidPosition.z) + (random.nextBoolean() ? random.nextFloat() : -random.nextFloat())*2);
            asteroids[i].xRot = (float) Math.toRadians(random.nextInt(360));
            asteroids[i].yRot = (float) Math.toRadians(random.nextInt(360));
            asteroids[i].zRot = (float) Math.toRadians(random.nextInt(360));
        }
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return new ResourceLocation("textures/block/brown_terracotta.png");
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffers, int packedLight, float partialTicks) {
        IVertexBuilder buffer = buffers.getBuffer(RenderType.entitySolid(getTextureLocation()));
        if (counter < 360) {
            counter += 0.05;
        } else {
            counter = 0;
        }
        matrixStack.mulPose(new Quaternion(0, counter, 0, true));
        for (int i = 0; i < asteroids.length; i++) {
            asteroids[i].render(matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}
