package xyz.heroesunited.heroesunited.common.abilities;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.IHUPlayer;

public class SlowMoAbility extends JSONAbility {

    public SlowMoAbility(AbilityType type) {
        super(type);
    }

    @Override
    public void action(Player player) {
        IHUPlayer cap = HUPlayer.getCap(player);
        if (cap != null) {
            cap.setSlowMoSpeed(getEnabled() ? GsonHelper.getAsFloat(getJsonObject(), "speed", 6F) : 20F);
        }
    }
}
