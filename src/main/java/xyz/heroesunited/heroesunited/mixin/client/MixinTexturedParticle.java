package xyz.heroesunited.heroesunited.mixin.client;

import net.minecraft.client.particle.TexturedParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.heroesunited.heroesunited.HeroesUnited;

@Mixin(TexturedParticle.class)
public abstract class MixinTexturedParticle {

    @Shadow public abstract float getQuadSize(float p_217561_1_);

    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraft/client/renderer/ActiveRenderInfo;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/TexturedParticle;getQuadSize(F)F"))
    private float changeQuadSize(TexturedParticle texturedParticle, float partialTicks) {
        if (((AccessorParticle) this).getLevel().dimension().equals(HeroesUnited.SPACE)) {
            return this.getQuadSize(partialTicks) *0.01F;
        }
        return this.getQuadSize(partialTicks);
    }
}
