package xyz.heroesunited.heroesunited.common.objects.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.heroesunited.heroesunited.HeroesUnited;

public class HUItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HeroesUnited.MODID);

    public static final Item TITANIUM_INGOT = register("titanium_ingot", new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final Item HEROES_UNITED = register("heroes_united", new Item(new Item.Properties().stacksTo(1)));
    public static final Item HORAS = register("horas", new HorasItem(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC)));
    public static final ComicItem COMIC_ITEM = registerSpecial("comic", new ComicItem());

    public static final TheOneRingAccessory THE_ONE_RING_ACCESSORY = register("the_one_ring", new TheOneRingAccessory());
    public static final ArcReactorAccessory ARC_REACTOR_ACCESSORY = register("arc_reactor", new ArcReactorAccessory());
    public static final BoboAccessory BOBO_ACCESSORY = register("bobo", new BoboAccessory());

    private static <T extends Item> T register(String name, T item) {
        ITEMS.register(name, () -> item);
        return item;
    }

    private static <T extends Item> T registerSpecial(String name, T item) {
        if (FMLEnvironment.production || ModList.get().getMods().stream().filter(modInfo -> modInfo.getModId().equals("huben10") || modInfo.getModId().equals("hugeneratorrex") || modInfo.getModId().equals("hudannyphantom")).count() >= 3) {
            return register(name, item);
        } else {
            return null;
        }
    }
}
