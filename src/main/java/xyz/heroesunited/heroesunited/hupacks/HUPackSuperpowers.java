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
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import xyz.heroesunited.heroesunited.HeroesUnited;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.Superpower;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.Level;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCap;
import xyz.heroesunited.heroesunited.common.events.HURegisterSuperpower;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HUPackSuperpowers extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static HUPackSuperpowers INSTANCE;
    private final Map<ResourceLocation, Superpower> registeredSuperpowers = Maps.newHashMap();

    public HUPackSuperpowers() {
        super(GSON, "husuperpowers");
        INSTANCE = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager iResourceManager, ProfilerFiller ProfilerFiller) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            try {
                Superpower superpower = new Superpower(resourcelocation, (JsonObject) entry.getValue());
                this.registeredSuperpowers.put(resourcelocation, superpower);
                MinecraftForge.EVENT_BUS.post(new HURegisterSuperpower(this.registeredSuperpowers));
            } catch (Exception e) {
                HeroesUnited.LOGGER.error("Parsing error loading superpower {}", resourcelocation, e);
            }
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player != null && player.isAlive()) {
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

    public static Map<ResourceLocation, JsonObject> getSuperpowersJSONS() {
        ResourceManager manager = HUPacks.getInstance().getResourceManager();
        Map<ResourceLocation, JsonObject> list = Maps.newHashMap();
        if (manager == null) return list;
        for(ResourceLocation path : manager.listResources("husuperpowers", (p_223379_0_) -> p_223379_0_.endsWith(".json"))) {
            ResourceLocation location = new ResourceLocation(path.getNamespace(), path.getPath().substring("husuperpowers".length() + 1, path.getPath().length() - ".json".length()));

            try {
                JsonElement jsonelement = GsonHelper.fromJson(HUPacks.GSON, new BufferedReader(new InputStreamReader(manager.getResource(path).getInputStream(), StandardCharsets.UTF_8)), JsonElement.class);
                if (jsonelement instanceof JsonObject) {
                    list.put(location, jsonelement.getAsJsonObject());
                }
            } catch (IOException ignored) {
            }
        }

        return list;
    }

    public static HUPackSuperpowers getInstance() {
        return INSTANCE;
    }

    public static Superpower getSuperpower(ResourceLocation location) {
        return getInstance().registeredSuperpowers.get(location);
    }

    public static void setSuperpower(Player player, Superpower superpower) {
        try {
            if (!HUPlayer.getCap(player).getSuperpowerLevels().containsKey(superpower.getRegistryName())) {
                HUPlayer.getCap(player).getSuperpowerLevels().put(superpower.getRegistryName(), new Level());
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
        for (Ability ability : HUAbilityCap.getCap(player).getAbilities().values()) {
            if (ability.getAdditionalData() != null && ability.getAdditionalData().getString("Superpower").equals(location.toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasSuperpower(Player player, Superpower superpower) {
        return hasSuperpower(player, superpower.getRegistryName());
    }

    public static boolean hasSuperpowers(Player player) {
        for (Ability ability : HUAbilityCap.getCap(player).getAbilities().values()) {
            if (ability.getAdditionalData().contains("Superpower")) {
                return true;
            }
        }
        return false;
    }

    public static ResourceLocation getSuperpower(Player player) {
        for (Ability ability : HUAbilityCap.getCap(player).getAbilities().values()) {
            if (ability.getAdditionalData().contains("Superpower")) {
                return new ResourceLocation(ability.getAdditionalData().getString("Superpower"));
            }
        }
        return null;
    }
}
