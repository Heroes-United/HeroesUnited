package xyz.heroesunited.heroesunited.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.heroesunited.heroesunited.common.events.BlockCollisionEvent;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {

    @Shadow @Final protected boolean hasCollision;

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At(value = "RETURN"), cancellable = true)
    public void onGetCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (state != null && context instanceof EntityCollisionContext context1 && context1.getEntity() != null) {
            BlockCollisionEvent event = new BlockCollisionEvent(context1.getEntity().level, pos, state, context1.getEntity());
            MinecraftForge.EVENT_BUS.post(event);
            cir.setReturnValue(!event.isCanceled() && this.hasCollision ? state.getShape(worldIn, pos) : Shapes.empty());
        }
    }
}
