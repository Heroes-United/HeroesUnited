package xyz.heroesunited.heroesunited;

import net.arikia.dev.drpc.DiscordRPC;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import xyz.heroesunited.heroesunited.client.HUClientEventHandler;
import xyz.heroesunited.heroesunited.client.HorasInfo;
import xyz.heroesunited.heroesunited.client.gui.AccessoriesScreen;
import xyz.heroesunited.heroesunited.client.render.model.space.SunModel;
import xyz.heroesunited.heroesunited.client.render.renderer.EnergyBlastRenderer;
import xyz.heroesunited.heroesunited.client.render.renderer.GeckoSuitRenderer;
import xyz.heroesunited.heroesunited.client.render.renderer.HorasRenderer;
import xyz.heroesunited.heroesunited.client.render.renderer.IGeoAbility;
import xyz.heroesunited.heroesunited.client.render.renderer.space.AsteroidsBeltRenderer;
import xyz.heroesunited.heroesunited.client.render.renderer.space.CelestialBodyRenderer;
import xyz.heroesunited.heroesunited.client.render.renderer.space.EarthRenderer;
import xyz.heroesunited.heroesunited.client.render.renderer.space.SunRenderer;
import xyz.heroesunited.heroesunited.common.HUConfig;
import xyz.heroesunited.heroesunited.common.HUEventHandler;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.suit.GeckoSuitItem;
import xyz.heroesunited.heroesunited.common.capabilities.HUCapStorage;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayerEvent;
import xyz.heroesunited.heroesunited.common.capabilities.IHUPlayer;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCap;
import xyz.heroesunited.heroesunited.common.capabilities.ability.IHUAbilityCap;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.objects.HUAttributes;
import xyz.heroesunited.heroesunited.common.objects.HUPaintings;
import xyz.heroesunited.heroesunited.common.objects.HUSounds;
import xyz.heroesunited.heroesunited.common.objects.blocks.HUBlocks;
import xyz.heroesunited.heroesunited.common.objects.container.HUContainers;
import xyz.heroesunited.heroesunited.common.objects.entities.HUEntities;
import xyz.heroesunited.heroesunited.common.objects.entities.Horas;
import xyz.heroesunited.heroesunited.common.objects.items.HUItems;
import xyz.heroesunited.heroesunited.common.planets.CelestialBody;
import xyz.heroesunited.heroesunited.common.planets.CelestialBodies;
import xyz.heroesunited.heroesunited.hupacks.HUPacks;
import xyz.heroesunited.heroesunited.util.HURichPresence;
import xyz.heroesunited.heroesunited.util.data.HUEnglishProvider;

import static xyz.heroesunited.heroesunited.common.objects.HUAttributes.FALL_RESISTANCE;
import static xyz.heroesunited.heroesunited.common.objects.HUAttributes.JUMP_BOOST;

@Mod(HeroesUnited.MODID)
public class HeroesUnited {

    public static final RegistryKey<World> SPACE = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(HeroesUnited.MODID,"space"));
    public static final String MODID = "heroesunited";
    public static final Logger LOGGER = LogManager.getLogger();

    public HeroesUnited() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            GeckoLib.initialize();
            bus.addListener(this::clientSetup);
            MinecraftForge.EVENT_BUS.register(new HUClientEventHandler());
        });

        HUPacks.init();

        HUAttributes.ATTRIBUTES.register(bus);
        HUSounds.SOUNDS.register(bus);
        HUEntities.ENTITIES.register(bus);
        HUBlocks.BLOCKS.register(bus);
        HUItems.ITEMS.register(bus);
        HUPaintings.PAINTINGS.register(bus);
        HUContainers.CONTAINERS.register(bus);

        MinecraftForge.EVENT_BUS.register(new HUEventHandler());
        MinecraftForge.EVENT_BUS.register(new HUPlayerEvent());
        bus.addListener(this::onRegisterNewRegistries);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HUConfig.CLIENT_SPEC);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            CelestialBodyRenderer.registerRenderer(new EarthRenderer(), CelestialBodies.EARTH);
            CelestialBodyRenderer.registerRenderer(new SunRenderer(), CelestialBodies.SUN);
            CelestialBodyRenderer.registerRenderer(new AsteroidsBeltRenderer(), CelestialBodies.ASTEROIDS_BELT);
            ModelBakery.UNREFERENCED_TEXTURES.add(SunModel.SUN_TEXTURE_MATERIAL);
        });
    }

    static {
        AnimationController.addModelFetcher((IAnimatable o) -> {
            if (o instanceof IHUPlayer) {
                return ((IHUPlayer) o).getAnimatedModel();
            }
            return null;
        });
        AnimationController.addModelFetcher((IAnimatable o) -> {
            if (o instanceof IGeoAbility) {
                return ((IGeoAbility) o).getGeoModel();
            }
            return null;
        });
    }

    public void onRegisterNewRegistries(RegistryEvent.NewRegistry e) {
        AbilityType.ABILITIES = new RegistryBuilder<AbilityType>().setName(new ResourceLocation(HeroesUnited.MODID, "ability_types")).setType(AbilityType.class).setIDRange(0, 2048).create();
        CelestialBody.CELESTIAL_BODIES = new RegistryBuilder<CelestialBody>().setName(new ResourceLocation(HeroesUnited.MODID, "celestial_bodies")).setType(CelestialBody.class).setIDRange(0, Integer.MAX_VALUE).create();
    }

    @SubscribeEvent
    public void gatherData(final GatherDataEvent e) {
        e.getGenerator().addProvider(new HUEnglishProvider(e.getGenerator()));
    }

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(IHUPlayer.class, new HUCapStorage<>(), () -> new HUPlayer(null));
        CapabilityManager.INSTANCE.register(IHUAbilityCap.class, new HUCapStorage<>(), () -> new HUAbilityCap(null));

        HUNetworking.registerMessages();
        LOGGER.info(MODID + ": common is ready!");
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
        HUPacks.HUPackFinder.createFoldersAndLoadThemes();
        ClientRegistry.registerEntityShader(Horas.class, new ResourceLocation(MODID, "shaders/post/horas.json"));
        RenderingRegistry.registerEntityRenderingHandler(HUEntities.HORAS, HorasRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(HUEntities.ENERGY_BLAST, EnergyBlastRenderer::new);
        GeoArmorRenderer.registerArmorRenderer(GeckoSuitItem.class, new GeckoSuitRenderer());
        ScreenManager.register(HUContainers.ACCESSORIES, AccessoriesScreen::new);

        new HorasInfo.DimensionInfo("Overworld", "Default      Dimension", new ResourceLocation("overworld"), new ResourceLocation(MODID, "textures/gui/horas/dimensions/overworld.png"));
        new HorasInfo.DimensionInfo("Nether", "Default      Dimension", new ResourceLocation("the_nether"), new ResourceLocation(MODID, "textures/gui/horas/dimensions/the_nether.png"));
        new HorasInfo.DimensionInfo("End", "Default      Dimension", new ResourceLocation("the_end"), new ResourceLocation(MODID, "textures/gui/horas/dimensions/the_end.png"));

        if (!HURichPresence.isHiddenRPC()) {
            HURichPresence.getPresence().setDiscordRichPresence("In the Menus", null, HURichPresence.MiniLogos.NONE, null);
        }

        LOGGER.info(MODID + ": client is ready!");
    }

    @SubscribeEvent
    public void entityAttribute(final EntityAttributeCreationEvent event) {
        event.put(HUEntities.HORAS, Horas.createMobAttributes().build());
    }

    @SubscribeEvent
    public void entityAttributeModification(final EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (type == EntityType.PLAYER) {
                if (!event.has(type, FALL_RESISTANCE)) {
                    event.add(type, FALL_RESISTANCE, 0);
                }
                if (!event.has(type, JUMP_BOOST)) {
                    event.add(type, JUMP_BOOST, 0);
                }
            }
        }
    }
}