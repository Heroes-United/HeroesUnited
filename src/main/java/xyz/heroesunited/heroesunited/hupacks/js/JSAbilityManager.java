package xyz.heroesunited.heroesunited.hupacks.js;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import xyz.heroesunited.heroesunited.HeroesUnited;
import xyz.heroesunited.heroesunited.client.events.SetupAnimEvent;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.IAbilityClientProperties;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JSAbilityManager extends JSReloadListener {

    private final List<AbilityType> types = new ArrayList<>();

    public JSAbilityManager(IEventBus bus) {
        super("huabilities", new NashornScriptEngineFactory());
        bus.addGenericListener(AbilityType.class, this::registerAbilityTypes);
    }

    @Override
    public void apply(Map<ResourceLocation, NashornScriptEngine> map, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        for (Map.Entry<ResourceLocation, NashornScriptEngine> entry : map.entrySet()) {
            try {
                types.add(new AbilityType((type, player, json) -> new JSAbility(type, player, json, entry.getValue())).setRegistryName(entry.getKey()));
            } catch (Throwable throwable) {
                HeroesUnited.LOGGER.error("Couldn't read hupack ability {}", entry.getKey(), throwable);
            }
        }
    }

    public void registerAbilityTypes(RegistryEvent.Register<AbilityType> event) {
        for (AbilityType type : types) {
            event.getRegistry().register(type);
        }
    }

    public static class JSAbility extends JSONAbility {

        private final NashornScriptEngine engine;

        public JSAbility(AbilityType type, Player player, JsonObject jsonObject, NashornScriptEngine engine) {
            super(type, player, jsonObject);
            this.engine = engine;
            try {
                this.engine.invokeFunction("registerData", this);
            } catch (ScriptException | NoSuchMethodException ignored) {
            }
        }

        @Override
        public boolean canActivate(Player player) {
            try {
                return (boolean) engine.invokeFunction("canActivate", player, this);
            } catch (ScriptException | NoSuchMethodException e) {
                return super.canActivate(player);
            }
        }

        @Override
        public void onUpdate(Player player) {
            super.onUpdate(player);
            try {
                engine.invokeFunction("update", player, this);
            } catch (ScriptException | NoSuchMethodException ignored) {
            }
        }

        @Override
        public void onKeyInput(Player player, Map<Integer, Boolean> map) {
            super.onKeyInput(player, map);
            try {
                engine.invokeFunction("keyInput", player, this, map);
            } catch (ScriptException | NoSuchMethodException ignored) {
            }
        }

        @Override
        public boolean getEnabled() {
            try {
                return (boolean) engine.invokeFunction("enabled", this);
            } catch (ScriptException | NoSuchMethodException e) {
                return super.getEnabled();
            }
        }

        @Override
        public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
            super.initializeClient(consumer);
            consumer.accept(new IAbilityClientProperties() {
                @Override
                public void setupAnim(SetupAnimEvent event) {
                    try {
                        engine.invokeFunction("setupAnim", event, this);
                    } catch (ScriptException | NoSuchMethodException ignored) {
                    }
                }
            });
        }
    }
}