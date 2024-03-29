package xyz.heroesunited.heroesunited.hupacks.js;

import com.google.common.collect.Maps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import xyz.heroesunited.heroesunited.HeroesUnited;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class JSReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, NashornScriptEngine>> {
    protected final NashornScriptEngineFactory manager;
    private final String directory;

    public JSReloadListener(String directory, NashornScriptEngineFactory manager) {
        this.manager = manager;
        this.directory = directory;
    }

    @Override
    public Map<ResourceLocation, NashornScriptEngine> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, NashornScriptEngine> map = Maps.newHashMap();
        FileToIdConverter filetoidconverter = js(this.directory);

        for(Map.Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(manager).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceLocation location = filetoidconverter.fileToId(resourcelocation);

            try (
                    InputStream inputstream = manager.getResource(resourcelocation).get().open();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
            ) {
                NashornScriptEngine engine = (NashornScriptEngine) this.manager.getScriptEngine();
                engine.put("path", location.toString());
                try {
                    engine.eval(reader);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                map.put(location, engine);
            } catch (IOException jsonparseexception) {
                HeroesUnited.LOGGER.error("Couldn't parse data file {} from {}", location, resourcelocation, jsonparseexception);
            }
        }

        return map;
    }

    public static FileToIdConverter js(String pName) {
        return new FileToIdConverter(pName, ".js");
    }
}
