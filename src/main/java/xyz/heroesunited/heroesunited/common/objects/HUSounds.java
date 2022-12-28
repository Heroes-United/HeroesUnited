package xyz.heroesunited.heroesunited.common.objects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.heroesunited.heroesunited.HeroesUnited;

public class HUSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HeroesUnited.MODID);

    public static final SoundEvent FLYING = register("flying");

    private static SoundEvent register(String name) {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(new ResourceLocation(HeroesUnited.MODID, name));
        SOUNDS.register(name, () -> soundEvent);
        return soundEvent;
    }
}
