package xyz.heroesunited.heroesunited.common.abilities.suit;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.geo.exception.GeckoLibException;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.IAbilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SuitItem extends ArmorItem implements IAbilityProvider, IAnimatable {

    protected final AnimationFactory factory = new AnimationFactory(this);
    protected final Suit suit;

    public SuitItem(ArmorMaterial materialIn, EquipmentSlot slot, Properties builder, Suit suit) {
        super(materialIn, slot, builder);
        this.suit = suit;
    }

    @Nullable
    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return super.getEquipmentSlot(stack);
    }

    @Nonnull
    public Suit getSuit() {
        return suit;
    }

    @Override
    public Map<String, Ability> getAbilities(Player player) {
        Map<String, Ability> map = Maps.newHashMap();
        suit.getAbilities(player).forEach((id, a) -> {
            a.getAdditionalData().putString("Suit", suit.getRegistryName().toString());
            if (suit instanceof JsonSuit && a.getJsonObject().has("slot")) {
                a.getAdditionalData().putString("Slot", GsonHelper.getAsString(a.getJsonObject(), "slot"));
            }
            map.put(id, a);
        });
        return map;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level p_77624_2_, List<Component> tooltip, TooltipFlag p_77624_4_) {
        if (getSuit().getDescription(stack) != null) tooltip.addAll(getSuit().getDescription(stack));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, itemSlot, isSelected);
        if (getSuit() instanceof JsonSuit) {
            JsonSuit suit = ((JsonSuit) getSuit());
            if (suit.getConditionManager().getConditions().isEmpty()) {
                suit.getConditionManager().registerConditions(suit.getJsonObject());
            }
        }
    }

    @Override
    public void onArmorTick(ItemStack item, Level world, Player player) {
        if (!getSuit().canEquip(player)) {
            ItemStack stack = player.getItemBySlot(slot);
            player.getInventory().add(stack);
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IItemRenderProperties() {

            @Override
            public <A extends HumanoidModel<?>> A getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot armorSlot, A _default) {
                try {
                    GeoArmorRenderer renderer = getArmorRenderer();
                    renderer.setCurrentItem(entity, stack, armorSlot);
                    renderer.applyEntityStats(_default).applySlot(armorSlot);
                    return (A) renderer;
                } catch (GeckoLibException | IllegalArgumentException e) {
                    if (stack != ItemStack.EMPTY) {
                        if (stack.getItem() instanceof SuitItem) {
                            HumanoidModel model = getSuit().getArmorModel(entity, stack, armorSlot, _default);
                            model.copyPropertiesTo(_default);
                            return (A) model;
                        }
                    }
                    return null;
                }
            }
        });
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        try {
            ResourceLocation location = getArmorRenderer().getTextureLocation((ArmorItem) stack.getItem());
            if (Minecraft.getInstance().getResourceManager().hasResource(location)) {
                return location.toString();
            } else {
                throw new GeckoLibException(location,
                        "Could not find texture. If you are getting this with a built mod, please just restart your game.");
            }
        } catch (GeckoLibException | IllegalArgumentException e) {
            return getSuit().getSuitTexture(stack, entity, slot);
        }
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        getSuit().serializeNBT(nbt, stack);
        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null) {
            getSuit().deserializeNBT(nbt, stack);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        ItemStack armorStack = playerIn.getItemBySlot(slot);
        if (getSuit().canEquip(playerIn) && armorStack.isEmpty()) {
            playerIn.setItemSlot(slot, stack.copy());
            stack.setCount(0);
            return InteractionResultHolder.sidedSuccess(stack, worldIn.isClientSide());
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    public GeoArmorRenderer getArmorRenderer() {
        Class<? extends ArmorItem> clazz = this.getClass();
        return GeoArmorRenderer.getRenderer(clazz);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, Entity entity) {
        if (entity instanceof Player) {
            return super.canEquip(stack, slot, entity) && getSuit().canEquip((Player) entity);
        }
        return super.canEquip(stack, slot, entity);
    }

    public void registerControllers(AnimationData data) {
        getSuit().registerControllers(data, this);
    }

    public AnimationFactory getFactory() {
        return this.factory;
    }
}