package xyz.heroesunited.heroesunited.mixin.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.client.renderer.IPlayerModel;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.PlayerPart;

/**
 * This is for triggering the {@link SetupAnimEvent}.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin implements IPlayerModel {
    private float limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch;

    @Shadow @Final private boolean slim;

    @Inject(method = "setupAnim", at = @At(value = "HEAD"))
    private void setRotationAngles(LivingEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entityIn instanceof Player)) return;
        PlayerModel model = (PlayerModel) (Object) this;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        for (PlayerPart bodyPart : PlayerPart.bodyParts()) {
            HUClientUtil.resetModelRenderer(bodyPart.initialModelPart(model));
        }
        model.rightArm.setPos(-5F, this.slim ? 2.5F : 2F, 0F);
        model.rightSleeve.setPos(-5F, this.slim ? 2.5F : 2F, 10F);
        model.leftArm.setPos(5F, this.slim ? 2.5F : 2F, 0F);
        model.leftSleeve.setPos(5F, this.slim ? 2.5F : 2F, 0F);
        model.leftLeg.setPos(1.9F, 12F, 0F);
        model.leftPants.copyFrom(model.leftLeg);
        model.rightLeg.setPos(-1.9F, 12F, 0F);
        model.rightPants.copyFrom(model.rightLeg);
    }

    @Override
    public float limbSwing() {
        return this.limbSwing;
    }

    @Override
    public float limbSwingAmount() {
        return this.limbSwingAmount;
    }

    @Override
    public float ageInTicks() {
        return this.ageInTicks;
    }

    @Override
    public float netHeadYaw() {
        return this.netHeadYaw;
    }

    @Override
    public float headPitch() {
        return this.headPitch;
    }
}