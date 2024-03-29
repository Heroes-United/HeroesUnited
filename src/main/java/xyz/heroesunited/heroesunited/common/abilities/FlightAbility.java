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
    public boolean isFlying(Player player) {
        return this.getEnabled();
    }

    @Override
    public void onDataUpdated(HUData<?> data) {
        super.onDataUpdated(data);
        if (data.getKey().equals("enabled") && ((boolean) data.getValue())) {
            if (this.player instanceof LocalPlayer localPlayer) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> new Runnable() {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void run() {
                        Minecraft.getInstance().getSoundManager().play(new FlyingSoundInstance(FlightAbility.this.getSoundEvent(), localPlayer));
                    }
                });
            }
        }
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ELYTRA_FLYING;
    }
}
