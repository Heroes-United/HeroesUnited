package xyz.heroesunited.heroesunited.client.render.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import xyz.heroesunited.heroesunited.common.abilities.suit.JsonSuit;
import xyz.heroesunited.heroesunited.common.abilities.suit.SuitItem;
import xyz.heroesunited.heroesunited.hupacks.HUPackLayers;

public class GeckoSuitModel<T extends SuitItem> extends AnimatedGeoModel<T> {
    @Override
    public ResourceLocation getModelLocation(T item) {
        ResourceLocation res = new ResourceLocation(item.getSuit().getRegistryName().getNamespace(), "geo/" + item.getSuit().getRegistryName().getPath() + ".geo.json");
        if (getLayer(item, "texture") != null) {
            return getLayer(item, "texture");
        }
        if (item.getSuit() instanceof JsonSuit && ((JsonSuit) item.getSuit()).getJsonObject() != null)
            return new ResourceLocation(GsonHelper.getAsString(((JsonSuit) item.getSuit()).getJsonObject(), "model", res.toString()));
        return res;
    }

    @Override
    public ResourceLocation getTextureLocation(T item) {
        if (getLayer(item, "texture") != null) {
            return getLayer(item, "texture");
        }
        if (item.getSuit() instanceof JsonSuit && ((JsonSuit) item.getSuit()).getJsonObject() != null && ((JsonSuit) item.getSuit()).getJsonObject().has("texture")) {
            return new ResourceLocation(GsonHelper.getAsString(((JsonSuit) item.getSuit()).getJsonObject(), "texture"));
        } else return new ResourceLocation(item.getSuit().getRegistryName().getNamespace(), "textures/suits/" + item.getSuit().getRegistryName().getPath() + ".png");
    }

    public ResourceLocation getLayer(T item, String type) {
        HUPackLayers.Layer layer = HUPackLayers.getInstance().getLayer(item.getSuit().getRegistryName());
        if (layer != null && layer.getTexture(type) != null) {
            return layer.getTexture(type);
        }
        return null;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(T item) {
        return new ResourceLocation(item.getSuit().getRegistryName().getNamespace(), "animations/" + item.getSuit().getRegistryName().getPath() + ".animation.json");
    }
}