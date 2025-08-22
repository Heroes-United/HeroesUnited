package xyz.heroesunited.heroesunited.common.abilities;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.common.objects.HUSounds;

public interface IFlyingAbility {

    default Ability ability()
    {
        return (Ability) this;
    }

    default boolean isFlying(Player player) {
        return ability().getEnabled();
    }

    default boolean renderFlying(Player player) {
        return GsonHelper.getAsBoolean(this.ability().getJsonObject(), "render", true);
    }

    default boolean rotateArms(Player player) {
        return GsonHelper.getAsBoolean(this.ability().getJsonObject(), "rotateArms", false);
    }

    default boolean setDefaultRotationAngles(SetupAnimEvent event) {
        return GsonHelper.getAsBoolean(this.ability().getJsonObject(), "setDefaultRotationAngles", true);
    }

    default float getDegreesForWalk(Player player) {
        return GsonHelper.getAsFloat(this.ability().getJsonObject(), "degrees_for_sprint", 22.5F);
    }

    default float getDegreesForSprint(Player player) {
        return GsonHelper.getAsFloat(this.ability().getJsonObject(), "degrees_for_walk", 90F + player.getXRot());
    }

    default SoundEvent getSoundEvent() {
        return HUSounds.FLYING;
    }

    static IFlyingAbility getFlyingAbility(Player player) {
        return AbilityHelper.getListOfType(IFlyingAbility.class, AbilityHelper.getAbilities(player)).stream().findFirst().orElse(null);
    }
}
