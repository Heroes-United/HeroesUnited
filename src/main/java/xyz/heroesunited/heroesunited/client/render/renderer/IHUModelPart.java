package xyz.heroesunited.heroesunited.client.render.renderer;

import net.minecraft.client.model.geom.builders.CubeDeformation;

public interface IHUModelPart {

    void resetSize();

    void setSize(CubeDeformation size);

    CubeDeformation size();
}
