package xyz.heroesunited.heroesunited.common.abilities;

import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import xyz.heroesunited.heroesunited.HeroesUnited;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = HeroesUnited.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AbilityType extends ForgeRegistryEntry<AbilityType> {

    public static final DeferredRegister<AbilityType> ABILITY_TYPES = DeferredRegister.create(AbilityType.class, HeroesUnited.MODID);
    public static final Lazy<IForgeRegistry<AbilityType>> ABILITIES = Lazy.of(ABILITY_TYPES.makeRegistry("ability_types", () -> new RegistryBuilder<AbilityType>().setType(AbilityType.class).setIDRange(0, 2048)));

    private final Supplier<Ability> supplier;

    public AbilityType(Supplier<Ability> supplier) {
        this.supplier = supplier;
    }

    public AbilityType(Supplier<Ability> supplier, String modid, String name) {
        this.supplier = supplier;
        this.setRegistryName(modid, name);
    }

    public Ability create(String id) {
        Ability a = this.supplier.get();
        a.name = id;
        return a;
    }

    public static final AbilityType ATTRIBUTE_MODIFIER = register("attribute_modifier", AttributeModifierAbility::new);
    public static final AbilityType FLIGHT = register("flight", FlightAbility::new);
    public static final AbilityType SLOW_MO = register("slow_mo", SlowMoAbility::new);
    public static final AbilityType GECKO = register("gecko", GeckoAbility::new);
    public static final AbilityType HIDE_BODY_PARTS = register("hide_body_parts", HideBodyPartsAbility::new);
    public static final AbilityType ROTATE_PARTS = register("rotate_parts", RotatePartsAbility::new);
    public static final AbilityType SIZE_CHANGE = register("size_change", SizeChangeAbility::new);
    public static final AbilityType COMMAND = register("command", CommandAbility::new);
    public static final AbilityType DAMAGE_IMMUNITY = register("damage_immunity", DamageImmunityAbility::new);
    public static final AbilityType POTION_EFFECT = register("potion_effect", PotionEffectAbility::new);
    public static final AbilityType ENERGY_LASER = register("energy_laser", EnergyLaserAbility::new);
    public static final AbilityType HEAT_VISION = register("heat_vision", HeatVisionAbility::new);
    public static final AbilityType OXYGEN = register("oxygen", OxygenAbility::new);
    public static final AbilityType PROJECTILE = register("projectile", ProjectileAbility::new);
    public static final AbilityType HIDE_LAYER = register("hide_layer", HideLayerAbility::new);
    public static final AbilityType PARACHUTE = register("parachute", ParachuteAbility::new);

    private static AbilityType register(String name, Supplier<Ability> ability) {
        AbilityType type = new AbilityType(ability);
        ABILITY_TYPES.register(name, () -> type);
        return type;
    }
}