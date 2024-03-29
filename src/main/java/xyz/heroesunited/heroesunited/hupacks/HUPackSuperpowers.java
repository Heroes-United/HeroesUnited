package xyz.heroesunited.heroesunited.hupacks;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import xyz.heroesunited.heroesunited.HeroesUnited;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.Superpower;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.IHUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.Level;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCap;
import xyz.heroesunited.heroesunited.common.capabilities.ability.IHUAbilityCap;
import xyz.heroesunited.heroesunited.common.events.RegisterSuperpowerEvent;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.networking.client.ClientSyncSuperpowers;

import java.util.Map;

public class HUPackSuperpowers extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static HUPackSuperpowers INSTANCE;
    public Map<ResourceLocation, Superpower> registeredSuperpowers = Maps.newHashMap();

    public HUPackSuperpowers() {
        super(GSON, "husuperpowers");
        INSTANCE = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager iResourceManager, ProfilerFiller iProfiler) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            try {
                Superpower superpower = new Superpower(resourcelocation, (JsonObject) entry.getValue());
                this.registeredSuperpowers.put(resourcelocation, superpower);
                MinecraftForge.EVENT_BUS.post(new RegisterSuperpowerEvent(this.registeredSuperpowers));
            } catch (Exception e) {
                HeroesUnited.LOGGER.error("Parsing error loading superpower {}", resourcelocation, e);
            }
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player != null && player.isAlive()) {
                    HUNetworking.INSTANCE.sendTo(new ClientSyncSuperpowers(this.registeredSuperpowers), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
                    ResourceLocation resourceLocation = HUPackSuperpowers.getSuperpower(player);
                    if (resourceLocation != null && registeredSuperpowers.containsKey(resourceLocation)) {
                        HUPackSuperpowers.setSuperpower(player, registeredSuperpowers.get(resourceLocation));
                    }
                }
            }
        }
        HeroesUnited.LOGGER.info("Loaded {} superpowers", this.registeredSuperpowers.size());
    }

    public static Map<ResourceLocation, Superpower> getSuperpowers() {
        Map<ResourceLocation, Superpower> superpowers = Maps.newHashMap();
        for (Map.Entry<ResourceLocation, Superpower> entry : getInstance().registeredSuperpowers.entrySet()) {
            if (!GsonHelper.getAsBoolean(entry.getValue().jsonObject, "hidden", false)) {
                superpowers.put(entry.getKey(), entry.getValue());
            }
        }
        return superpowers;
    }

    public static HUPackSuperpowers getInstance() {
        return INSTANCE == null ? new HUPackSuperpowers() : INSTANCE;
    }

    public static Superpower getSuperpower(ResourceLocation location) {
        return getInstance().registeredSuperpowers.get(location);
    }

    public static Superpower getSuperpowerFrom(Player player) {
        ResourceLocation location = getSuperpower(player);
        if (location != null) {
            return getInstance().registeredSuperpowers.get(location);
        }
        return null;
    }

    public static void setSuperpower(Player player, Superpower superpower) {
        try {
            IHUPlayer huPlayer = HUPlayer.getCap(player);
            if (huPlayer != null && !huPlayer.getSuperpowerLevels().containsKey(superpower.getRegistryName())) {
                huPlayer.getSuperpowerLevels().put(superpower.getRegistryName(), new Level());
            }
            player.getCapability(HUAbilityCap.CAPABILITY).ifPresent(cap -> {
                cap.clearAbilities(ability -> ability.getAdditionalData().contains("Superpower"));
                cap.addAbilities(superpower);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeSuperpower(Player player) {
        try {
            player.getCapability(HUAbilityCap.CAPABILITY).ifPresent(cap -> cap.clearAbilities(ability -> ability.getAdditionalData().contains("Superpower")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasSuperpower(Player player, ResourceLocation location) {
        ResourceLocation superpower = getSuperpower(player);
        if (superpower != null) {
            return superpower.equals(location);
        }
        return false;
    }

    public static boolean hasSuperpowers(Player player) {
        return getSuperpower(player) != null;
    }

    public static ResourceLocation getSuperpower(Player player) {
        IHUAbilityCap abilityCap = HUAbilityCap.getCap(player);
        if (abilityCap != null) {
            for (Ability ability : abilityCap.getAbilities().values()) {
                if (ability.getAdditionalData().contains("Superpower")) {
                    return new ResourceLocation(ability.getAdditionalData().getString("Superpower"));
                }
            }
        }
        return null;
    }
}
