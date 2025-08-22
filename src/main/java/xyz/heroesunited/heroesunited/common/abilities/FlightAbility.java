package xyz.heroesunited.heroesunited.common.abilities;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import xyz.heroesunited.heroesunited.util.FlyingSoundInstance;
import xyz.heroesunited.heroesunited.util.hudata.HUData;

public class FlightAbility extends JSONAbility implements IFlyingAbility {

    public FlightAbility(AbilityType type, Player player, JsonObject jsonObject) {
        super(type, player, jsonObject);
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ELYTRA_FLYING;
    }
}
