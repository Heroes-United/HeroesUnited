package xyz.heroesunited.heroesunited.util;

import com.google.gson.*;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class HUJsonUtils {

    public static List<String> getStringsFromArray(JsonArray jsonArray) {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            list.add(jsonArray.get(i).getAsString());
        }

        return list;
    }

    public static Color getColor(JsonObject json) {
        if (json != null && json.has("color")) {
            JsonArray jsonColor = JSONUtils.getAsJsonArray(json, "color");
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

    public static List<ITextComponent> parseDescriptionLines(JsonElement jsonElement) {
        List<ITextComponent> lines = new ArrayList<>();

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                lines.addAll(parseDescriptionLines(jsonArray.get(i)));
            }
        } else if (jsonElement.isJsonObject()) {
            lines.add(ITextComponent.Serializer.fromJson(jsonElement));
        } else if (jsonElement.isJsonPrimitive()) {
            lines.add(new StringTextComponent(jsonElement.getAsString()));
        }

        return lines;
    }

    public static ItemGroup getItemGroup(JsonObject json, String memberName) {
        if (json.has(memberName) && json.get(memberName).isJsonPrimitive()) {
            return Arrays.stream(ItemGroup.TABS).filter(itemGroup -> json.get(memberName).getAsString().equalsIgnoreCase(itemGroup.getRecipeFolderName().toLowerCase())).findFirst().orElse(null);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find an item");
        }
    }

    public static IArmorMaterial parseArmorMaterial(JsonObject json) {
        int[] damageReductionAmountArray = new int[4];
        JsonArray dmgReduction = JSONUtils.getAsJsonArray(json, "damage_reduction");
        if (dmgReduction.size() != 4)
            throw new JsonParseException("The damage_reduction must contain 4 entries, one for each armor part!");
        IntStream.range(0, dmgReduction.size()).forEach(i -> damageReductionAmountArray[i] = dmgReduction.get(i).getAsInt());
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(JSONUtils.getAsString(json, "equip_sound", "")));
        Ingredient repairMaterial = json.has("repair_material") ? Ingredient.fromJson(json.get("repair_material")) : Ingredient.EMPTY;

        return new HUArmorMaterial("", JSONUtils.getAsInt(json, "max_damage_factor", 0), damageReductionAmountArray, JSONUtils.getAsInt(json, "enchantibility", 0), soundEvent, JSONUtils.getAsFloat(json, "toughness", 0), JSONUtils.getAsFloat(json, "knockback_resistance", 0), repairMaterial);
    }

    public static Suit getSuit(ResourceLocation location) {
        return Suit.SUITS.get(location);
    }

    public static int getIndexOfItem(Item item, NonNullList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static void rotatePartOfModel(ModelRenderer modelRenderer, String xyz, float angle, boolean player) {
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

    public static void translatePivotOfModel(ModelRenderer modelRenderer, String xyz, float value, boolean player) {
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
