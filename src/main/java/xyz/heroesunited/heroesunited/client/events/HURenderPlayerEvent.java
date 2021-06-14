package xyz.heroesunited.heroesunited.client.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;

public abstract class HURenderPlayerEvent extends HUSetRotationAnglesEvent {

    private final PlayerRenderer renderer;
    private final MatrixStack stack;
    private final IRenderTypeBuffer buffers;
    private final IVertexBuilder builder;
    private final int light, overlay;

    public HURenderPlayerEvent(PlayerEntity playerEntity, PlayerRenderer renderer, MatrixStack stack, IRenderTypeBuffer buffers, IVertexBuilder builder, int light, int overlay, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super(playerEntity, renderer.getModel(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.renderer = renderer;
        this.stack = stack;
        this.buffers = buffers;
        this.light = light;
        this.builder = builder;
        this.overlay = overlay;
    }

    public LivingRenderer getRenderer() {
        return renderer;
    }

    public MatrixStack getMatrixStack() {
        return stack;
    }

    public IRenderTypeBuffer getBuffers() {
        return buffers;
    }

    public IVertexBuilder getBuilder() {
        return builder;
    }

    public int getLight() {
        return light;
    }

    public int getOverlay() {
        return overlay;
    }

    @Cancelable
    public static class Pre extends HURenderPlayerEvent {

        public Pre(PlayerEntity playerEntity, PlayerRenderer renderer, MatrixStack stack, IRenderTypeBuffer buffers, IVertexBuilder builder, int light, int overlay, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super(playerEntity, renderer, stack, buffers, builder, light, overlay, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    public static class Post extends HURenderPlayerEvent {

        public Post(PlayerEntity playerEntity, PlayerRenderer renderer, MatrixStack stack, IRenderTypeBuffer buffers, IVertexBuilder builder, int light, int overlay, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super(playerEntity, renderer, stack, buffers, builder, light, overlay, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

}