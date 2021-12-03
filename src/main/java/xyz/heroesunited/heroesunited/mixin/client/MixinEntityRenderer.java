package xyz.heroesunited.heroesunited.mixin.client;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.heroesunited.heroesunited.HeroesUnited;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z", at = @At(value = "HEAD"), cancellable = true)
    public <T extends Entity> void onShouldRender(T entity, Frustum p_225626_2_, double p_225626_3_, double p_225626_5_, double p_225626_7_, CallbackInfoReturnable<Boolean> ci) {
        if (entity.level.dimension().equals(HeroesUnited.SPACE)) {
            ci.setReturnValue(true);
        }
    }
}
