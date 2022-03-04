package xyz.heroesunited.heroesunited.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import xyz.heroesunited.heroesunited.client.renderer.IHUModelPart;

import java.util.List;

public enum PlayerPart {

    HEAD, HEAD_WEAR(HEAD),
    CHEST, CHEST_WEAR(CHEST),
    RIGHT_ARM, RIGHT_ARM_WEAR(RIGHT_ARM),
    LEFT_ARM, LEFT_ARM_WEAR(LEFT_ARM),
    RIGHT_LEG, RIGHT_LEG_WEAR(RIGHT_LEG),
    LEFT_LEG, LEFT_LEG_WEAR(LEFT_LEG);

    public final PlayerPart parent;

    PlayerPart() {
        this.parent = null;
    }

    PlayerPart(PlayerPart parent) {
        this.parent = parent;
    }

    public void setVisibility(PlayerModel<?> model, boolean visible) {
        this.setVisibility(model, visible, 1F);
    }

    public void setVisibility(PlayerModel<?> model, boolean visible, float size) {
        ModelPart modelRenderer = this.modelPart(model);
        if (modelRenderer != null) {
            if (bodyParts().contains(this)) {
                modelRenderer.visible = visible;
            } else {
                if (size == 0.0F) {
                    modelRenderer.visible = visible;
                } else {
                    if (!visible) {
                        if (this == PlayerPart.HEAD_WEAR) {
                            ((IHUModelPart) (Object) modelRenderer).setSize(new CubeDeformation(size * -0.499F));
                        } else {
                            ((IHUModelPart) (Object) modelRenderer).setSize(new CubeDeformation(size * -0.249F));
                        }
                    }
                }
            }
        }
    }

    public ModelPart modelPart(PlayerModel<?> model) {
        return switch (this) {
            case HEAD_WEAR -> model.hat;
            case CHEST_WEAR -> model.jacket;
            case RIGHT_ARM_WEAR -> model.rightSleeve;
            case LEFT_ARM_WEAR -> model.leftSleeve;
            case RIGHT_LEG_WEAR -> model.rightPants;
            case LEFT_LEG_WEAR -> model.leftPants;
            default -> initialModelPart(model);
        };
    }

    public ModelPart initialModelPart(PlayerModel<?> model) {
        return switch (this) {
            case HEAD -> model.head;
            case CHEST -> model.body;
            case RIGHT_ARM -> model.rightArm;
            case LEFT_ARM -> model.leftArm;
            case RIGHT_LEG -> model.rightLeg;
            case LEFT_LEG -> model.leftLeg;
            default -> null;
        };
    }

    public static PlayerPart byName(String name) {
        for (PlayerPart playerPart : values()) {
            if (name.equalsIgnoreCase(playerPart.name().toLowerCase())) {
                return playerPart;
            }
        }
        return null;
    }

    public PlayerPart getParent() {
        for (PlayerPart value : values()) {
            if (value.parent == this) {
                return value.parent;
            }
        }
        return null;
    }

    public static List<PlayerPart> bodyParts() {
        return ImmutableList.of(HEAD, CHEST, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG);
    }

    public static List<PlayerPart> wearParts() {
        return ImmutableList.of(HEAD_WEAR, CHEST_WEAR, RIGHT_ARM_WEAR, LEFT_ARM_WEAR, RIGHT_LEG_WEAR, LEFT_LEG_WEAR);
    }

}