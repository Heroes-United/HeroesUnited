package xyz.heroesunited.heroesunited.common.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.heroesunited.heroesunited.HeroesUnited;
import xyz.heroesunited.heroesunited.common.networking.client.*;
import xyz.heroesunited.heroesunited.common.networking.server.*;

public class HUNetworking {

    public static SimpleChannel INSTANCE;
    private static int ID = 0;

    public static int NextID() {
        return ID++;
    }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(HeroesUnited.MODID, "networking"), () -> "1.0", s -> true, s -> true);
        //Client
        INSTANCE.registerMessage(NextID(), ClientSyncHUPlayer.class, ClientSyncHUPlayer::toBytes, ClientSyncHUPlayer::new, ClientSyncHUPlayer::handle);
        INSTANCE.registerMessage(NextID(), ClientSyncAbilityCap.class, ClientSyncAbilityCap::toBytes, ClientSyncAbilityCap::new, ClientSyncAbilityCap::handle);
        INSTANCE.registerMessage(NextID(), ClientSetAnimation.class, ClientSetAnimation::toBytes, ClientSetAnimation::new, ClientSetAnimation::handle);
        INSTANCE.registerMessage(NextID(), ClientSyncHUData.class, ClientSyncHUData::toBytes, ClientSyncHUData::new, ClientSyncHUData::handle);
        INSTANCE.registerMessage(NextID(), ClientDisableAbility.class, ClientDisableAbility::toBytes, ClientDisableAbility::new, ClientDisableAbility::handle);
        INSTANCE.registerMessage(NextID(), ClientEnableAbility.class, ClientEnableAbility::toBytes, ClientEnableAbility::new, ClientEnableAbility::handle);
        INSTANCE.registerMessage(NextID(), ClientSyncAbilities.class, ClientSyncAbilities::toBytes, ClientSyncAbilities::new, ClientSyncAbilities::handle);
        INSTANCE.registerMessage(NextID(), ClientSyncSuperpowers.class, ClientSyncSuperpowers::toBytes, ClientSyncSuperpowers::new, ClientSyncSuperpowers::handle);
        INSTANCE.registerMessage(NextID(), ClientSyncAbility.class, ClientSyncAbility::toBytes, ClientSyncAbility::new, ClientSyncAbility::handle);
        INSTANCE.registerMessage(NextID(), ClientSyncCelestialBody.class, ClientSyncCelestialBody::toBytes, ClientSyncCelestialBody::new, ClientSyncCelestialBody::handle);
        INSTANCE.registerMessage(NextID(), ClientOpenAccessoriesScreen.class, ClientOpenAccessoriesScreen::toBytes, ClientOpenAccessoriesScreen::new, ClientOpenAccessoriesScreen::handle);
        //Server
        INSTANCE.registerMessage(NextID(), ServerSetPlayerAnimation.class, ServerSetPlayerAnimation::toBytes, ServerSetPlayerAnimation::new, ServerSetPlayerAnimation::handle);
        INSTANCE.registerMessage(NextID(), ServerSetTheme.class, ServerSetTheme::toBytes, ServerSetTheme::new, ServerSetTheme::handle);
        INSTANCE.registerMessage(NextID(), ServerSyncAbility.class, ServerSyncAbility::toBytes, ServerSyncAbility::new, ServerSyncAbility::handle);
        INSTANCE.registerMessage(NextID(), ServerHorasPlayerSetDimension.class, ServerHorasPlayerSetDimension::toBytes, ServerHorasPlayerSetDimension::new, ServerHorasPlayerSetDimension::handle);
        INSTANCE.registerMessage(NextID(), ServerKeyInput.class, ServerKeyInput::toBytes, ServerKeyInput::new, ServerKeyInput::handle);
        INSTANCE.registerMessage(NextID(), ServerToggleAbility.class, ServerToggleAbility::toBytes, ServerToggleAbility::new, ServerToggleAbility::handle);
        INSTANCE.registerMessage(NextID(), ServerOpenAccessoriesInv.class, ServerOpenAccessoriesInv::toBytes, ServerOpenAccessoriesInv::new, ServerOpenAccessoriesInv::handle);
    }
}
