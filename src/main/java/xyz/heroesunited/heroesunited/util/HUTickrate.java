package xyz.heroesunited.heroesunited.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.IHUPlayer;

public class HUTickrate {

    public static long SERVER_TICK = 50;
    public static float CLIENT_TICK = 20;

    public static void tick(Player player, LogicalSide side) {
        float tickrate = 20F;
        for (Player player1 : player.level.players()) {
            IHUPlayer hu = HUPlayer.getCap(player1);
            if (player1.isAlive() && hu != null && hu.getSlowMoSpeed() != 20) {
                tickrate = hu.getSlowMoSpeed();
            }
        }

        if (side.isClient() && CLIENT_TICK != tickrate) {
            ObfuscationReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getInstance(), new Timer(tickrate, 0L), "f_90991_");
            CLIENT_TICK = tickrate;
        }
        if (side.isServer() && HUTickrate.SERVER_TICK != (long) (1000L / tickrate)) {
            HUTickrate.SERVER_TICK = (long) (1000L / tickrate);
        }
    }
}
