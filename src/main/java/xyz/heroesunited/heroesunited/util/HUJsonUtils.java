package xyz.heroesunited.heroesunited.util;

import com.google.common.collect.Lists;
import com.google.gson.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class HUJsonUtils {
    private static Map<String, ArmorMaterial> ARMOR_MATERIALS = new HashMap<>();

    static {
        for (ArmorMaterials armorMaterial : ArmorMaterials.values()) {
            addArmorMaterial(armorMaterial.name(), armorMaterial);
        }
    }

    public static Color getColor(JsonObject json) {
        if (json != null && json.has("color")) {
            JsonArray jsonColor = GsonHelper.getAsJsonArray(json, "color");
            if (jsonColor.size() == 3) {
                return new Color(jsonColor.get(0).getAsFloat() / 255F, jsonColor.get(1).getAsFloat() / 255F, jsonColor.get(2).getAsFloat() / 255F, 1);
            } else {
                if (jsonColor.size() != 4)
                    throw new JsonParseException("The color must contain 4 entries, one for each color!");
                return new Color(jsonColor.get(0).getAsFloat() / 255F, jsonColor.get(1).getAsFloat() / 255F, jsonColor.get(2).getAsFloat() / 255F, jsonColor.get(3).getAsFloat() / 255F);
            }
        }
        return Color.RED;
    }

    public static List<Component> parseDescriptionLines(JsonElement jsonElement) {
        List<Component> lines = Lists.newArrayList();

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                lines.addAll(parseDescriptionLines(jsonArray.get(i)));
            }
        } else if (jsonElement.isJsonObject()) {
            lines.add(Component.Serializer.fromJson(jsonElement));
        } else if (jsonElement.isJsonPrimitive()) {
            lines.add(new TextComponent(jsonElement.getAsString()));
        }

        return lines;
    }

    public static CreativeModeTab getItemGroup(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return getItemGroup(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find an item");
        }
    }

    private static CreativeModeTab getItemGroup(JsonElement json, String memberName) {
        if (json.isJsonPrimitive()) {
            String name = json.getAsString();
            for (CreativeModeTab itemGroup : CreativeModeTab.TABS) {
                if (name.equalsIgnoreCase(itemGroup.getRecipeFolderName().toLowerCase())) {
                    return itemGroup;
                }
            }
            return null;
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be an item, was " + json.toString());
        }
    }

    public static ArmorMaterial getArmorMaterial(String name) {
        return ARMOR_MATERIALS.get(name.toLowerCase());
    }

    public static ArmorMaterial addArmorMaterial(String name, ArmorMaterial armorMaterial) {
        ARMOR_MATERIALS.put(name.toLowerCase(), armorMaterial);
        return armorMaterial;
    }

    public static ArmorMaterial parseArmorMaterial(JsonObject json, boolean requireName) {
        String name = requireName ? GsonHelper.getAsString(json, "name") : "";
        int[] damageReductionAmountArray = new int[4];
        JsonArray dmgReduction = GsonHelper.getAsJsonArray(json, "damage_reduction");
        if (dmgReduction.size() != 4)
            throw new JsonParseException("The damage_reduction must contain 4 entries, one for each armor part!");
        IntStream.range(0, dmgReduction.size()).forEach(i -> damageReductionAmountArray[i] = dmgReduction.get(i).getAsInt());
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(GsonHelper.getAsString(json, "equip_sound", "")));
        Ingredient repairMaterial = json.has("repair_material") ? Ingredient.fromJson(json.get("repair_material")) : Ingredient.EMPTY;

        return new HUArmorMaterial(name, GsonHelper.getAsInt(json, "max_damage_factor", 0), damageReductionAmountArray, GsonHelper.getAsInt(json, "enchantibility", 0), soundEvent, GsonHelper.getAsFloat(json, "toughness", 0), GsonHelper.getAsFloat(json, "knockback_resistance", 0), repairMaterial);
    }

    public static Suit getSuit(String modid, String name) {
        return Suit.SUITS.get(new ResourceLocation(modid, name));
    }

    public static Suit getSuit(ResourceLocation location) {
        return Suit.SUITS.get(location);
    }

    public static ResourceLocation getAsResourceLocation(JsonObject jsonObject, String string) {
        return new ResourceLocation(GsonHelper.getAsString(jsonObject, string));
    }

    public static int getIndexOfItem(Item item, NonNullList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static void rotatePartOfModel(ModelPart modelRenderer, String xyz, float angle, boolean player) {
        switch (xyz) {
            case "x":
                modelRenderer.xRot = (player ? modelRenderer.xRot : 0) + (float) Math.toRadians(angle);
                break;
            case "y":
                modelRenderer.yRot = (player ? modelRenderer.yRot : 0) + (float) Math.toRadians(angle);
                break;
            case "z":
                modelRenderer.zRot = (player ? modelRenderer.zRot : 0) + (float) Math.toRadians(angle);
                break;
        }
    }

    public static void translatePivotOfModel(ModelPart modelRenderer, String xyz, float value, boolean player) {
        switch (xyz) {
            case "x":
                modelRenderer.x = (player ? modelRenderer.x : 0) + value;
                break;
            case "y":
                modelRenderer.y = (player ? modelRenderer.y : 0) + value;
                break;
            case "z":
                modelRenderer.z = (player ? modelRenderer.z : 0) + value;
                break;
        }
    }
}
