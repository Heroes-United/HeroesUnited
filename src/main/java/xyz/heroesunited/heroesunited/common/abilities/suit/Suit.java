package xyz.heroesunited.heroesunited.common.abilities.suit;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import software.bernie.geckolib3.core.manager.AnimationData;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.client.render.model.ModelSuit;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.objects.container.EquipmentAccessoriesSlot;
import xyz.heroesunited.heroesunited.hupacks.HUPackLayers;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Suit {

    public static final Map<ResourceLocation, Suit> SUITS = Maps.newHashMap();
    private ResourceLocation registryName = null;
    protected SuitItem helmet, chestplate, legs, boots;

    public Suit(ResourceLocation name) {
        this.setRegistryName(name);
    }

    public Suit(String modid, String name) {
        this(new ResourceLocation(modid, name));
    }

    public void registerItems(IForgeRegistry<Item> e) {
        e.register(helmet = createItem(this, EquipmentSlotType.HEAD));
        e.register(chestplate = createItem(this, EquipmentSlotType.CHEST));
        e.register(legs = createItem(this, EquipmentSlotType.LEGS));
        e.register(boots = createItem(this, EquipmentSlotType.FEET));
    }

    protected SuitItem createItem(Suit suit, EquipmentSlotType slot) {
        return this.createItem(suit, slot, slot.getName());
    }

    protected SuitItem createItem(Suit suit, EquipmentSlotType slot, String name) {
        return (SuitItem) new SuitItem(suit.getSuitMaterial(), slot, new Item.Properties().stacksTo(1).tab(suit.getItemGroup()), suit).setRegistryName(suit.getRegistryName().getNamespace(), suit.getRegistryName().getPath() + "_" + name);
    }

    public boolean canEquip(PlayerEntity player) {
        return true;
    }

    public IArmorMaterial getSuitMaterial() {
        return ArmorMaterial.IRON;
    }

    public ItemGroup getItemGroup() {
        return ItemGroup.TAB_COMBAT;
    }

    public List<ITextComponent> getDescription(ItemStack stack) {
        return null;
    }

    public final ResourceLocation getRegistryName() {
        return registryName;
    }

    public Map<String, Ability> getAbilities(PlayerEntity player) {
        return Maps.newHashMap();
    }

    public void onActivated(PlayerEntity player, EquipmentSlotType slot) {
    }

    public void onUpdate(PlayerEntity player, EquipmentSlotType slot) {
    }

    public void onDeactivated(PlayerEntity player, EquipmentSlotType slot) {
    }

    public void onKeyInput(PlayerEntity player, EquipmentSlotType slot, Map<Integer, Boolean> map) {
    }

    @OnlyIn(Dist.CLIENT)
    public void setRotationAngles(HUSetRotationAnglesEvent event, EquipmentSlotType slot) {
    }

    @OnlyIn(Dist.CLIENT)
    public void renderLayer(LivingRenderer<? extends LivingEntity, ? extends BipedModel<?>> entityRenderer, LivingEntity entity, ItemStack stack, EquipmentSlotType slot, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        HUPackLayers.Layer layer = HUPackLayers.getInstance().getLayer(this.getRegistryName());
        if (layer != null) {
            if (layer.getTexture("cape") != null && slot.equals(EquipmentSlotType.CHEST)) {
                HUClientUtil.renderCape(entityRenderer, entity, matrix, bufferIn, packedLightIn, partialTicks, layer.getTexture("cape"));
            }
            if (layer.getTexture("lights") != null) {
                Suit.getSuitItem(slot, entity).getArmorModel(entity, stack, slot, entityRenderer.getModel()).renderToBuffer(matrix, bufferIn.getBuffer(HUClientUtil.HURenderTypes.getLight(layer.getTexture("lights"))), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            }
        }
    }

    public <B extends SuitItem> void registerControllers(AnimationData data, B suitItem) {
    }

    @OnlyIn(Dist.CLIENT)
    public float getScale(EquipmentSlotType slot) {
        return 0.1F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSmallArms(Entity entity) {
        return HUPlayerUtil.haveSmallArms(entity);
    }

    @OnlyIn(Dist.CLIENT)
    public BipedModel<?> getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlotType slot, BipedModel<?> _default) {
        ModelSuit<?> suitModel = new ModelSuit<>(getScale(slot), isSmallArms(entity));
        switch (slot) {
            case HEAD:
                suitModel.hat.visible = suitModel.head.visible = true;
                break;
            case CHEST:
                suitModel.body.visible = suitModel.jacket.visible = true;
                suitModel.leftSleeve.visible = suitModel.leftArm.visible = true;
                suitModel.rightSleeve.visible = suitModel.rightArm.visible = true;
                break;
            case LEGS:
            case FEET:
                suitModel.leftLeg.visible = suitModel.rightLeg.visible = true;
                break;
        }
        return suitModel;
    }

    @OnlyIn(Dist.CLIENT)
    public String getSuitTexture(ItemStack stack, Entity entity, EquipmentSlotType slot) {
        HUPackLayers.Layer layer = HUPackLayers.getInstance().getLayer(this.getRegistryName());
        if (layer != null) {
            String tex = slot != EquipmentSlotType.LEGS ? layer.getTexture("layer_0").toString() : layer.getTexture("layer_1").toString();
            if (slot.equals(EquipmentSlotType.CHEST) && isSmallArms(entity) && layer.getTexture("smallArms") != null)
                tex = layer.getTexture("smallArms").toString();
            return tex;
        } else {
            String tex = slot != EquipmentSlotType.LEGS ? "layer_0" : "layer_1";
            if (slot == EquipmentSlotType.CHEST && isSmallArms(entity)) tex = "smallarms";
            return this.getRegistryName().getNamespace() + ":textures/suits/" + this.getRegistryName().getPath() + "/" + tex + ".png";
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderFirstPersonArm(PlayerRenderer renderer, MatrixStack matrix, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity player, HandSide side, ItemStack stack, SuitItem suitItem) {
        ModelSuit<AbstractClientPlayerEntity> suitModel = new ModelSuit<>(getScale(suitItem.getSlot()), isSmallArms(player));
        suitModel.renderArm(side, matrix, bufferIn.getBuffer(RenderType.entityTranslucent(new ResourceLocation(suitItem.getArmorTexture(stack, player, suitItem.getSlot(), null)))), packedLightIn, renderer.getModel());
    }

    public final void setRegistryName(ResourceLocation name) {
        if (getRegistryName() != null)
            throw new IllegalStateException("Attempted to set registry name with existing registry name! New: " + name.toString() + " Old: " + getRegistryName());

        this.registryName = GameData.checkPrefix(name.toString(), true);
    }

    public SuitItem getHelmet() {
        return helmet;
    }

    public SuitItem getChestplate() {
        return chestplate;
    }

    public SuitItem getLegs() {
        return legs;
    }

    public SuitItem getBoots() {
        return boots;
    }

    protected SuitItem getItemBySlot(EquipmentSlotType slot) {
        if (slot == EquipmentSlotType.HEAD) {
            return getHelmet();
        }
        if (slot == EquipmentSlotType.CHEST) {
            return getChestplate();
        }
        if (slot == EquipmentSlotType.LEGS) {
            return getLegs();
        }
        return getBoots();
    }

    public boolean hasArmorOn(LivingEntity entity) {
        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            if (slot.getType() == EquipmentSlotType.Group.ARMOR) {
                if (getItemBySlot(slot) != null && (entity.getItemBySlot(slot).isEmpty() || entity.getItemBySlot(slot).getItem() != getItemBySlot(slot))) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<EquipmentAccessoriesSlot> getSlotForHide(EquipmentSlotType slot) {
        List<EquipmentAccessoriesSlot> list = new ArrayList<>();
        for (int i = 0; i <= 8; ++i) {
            list.add(EquipmentAccessoriesSlot.getFromSlotIndex(i));
        }
        return list;
    }

    public static void registerSuit(Suit suit) {
        Suit.SUITS.put(suit.getRegistryName(), suit);
        suit.registerItems(ForgeRegistries.ITEMS);
    }

    public static SuitItem getSuitItem(EquipmentSlotType slot, LivingEntity entity) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.getItem() instanceof SuitItem) {
            SuitItem suitItem = (SuitItem) stack.getItem();
            if (suitItem.getSlot().equals(slot)) {
                return suitItem;
            }
        }
        return null;
    }

    public static Suit getSuit(LivingEntity entity) {
        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            if (slot.getType() == EquipmentSlotType.Group.ARMOR) {
                Item item = entity.getItemBySlot(slot).getItem();
                if (item instanceof SuitItem) {
                    SuitItem suitItem = (SuitItem) item;
                    if (suitItem.getSuit().hasArmorOn(entity)) {
                        return suitItem.getSuit();
                    }
                }
            }
        }
        return null;
    }

    public boolean canBreathOnSpace() {
        return false;
    }

    public void serializeNBT(CompoundNBT nbt, ItemStack stack) {

    }

    public void deserializeNBT(CompoundNBT nbt, ItemStack stack) {

    }
}