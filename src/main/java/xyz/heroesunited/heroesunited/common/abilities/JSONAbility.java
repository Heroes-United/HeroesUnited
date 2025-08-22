package xyz.heroesunited.heroesunited.common.abilities;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class JSONAbility extends Ability {

    public JSONAbility(AbilityType type, Player player, JsonObject jsonObject) {
        super(type, player, jsonObject);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register("enabled", false);
    }

    @Override
    public void onUpdate(Player player) {
        super.onUpdate(player);
        if (!this.conditionManager.isEnabled(player, "canBeEnabled")) {
            this.setEnabled(player, false);
        }

        if (this.dataManager.getAsBoolean("enabled")) {
            if (this.getActionType() == ActionType.ACTION) {
                this.setEnabled(player, false);
            } else {
                this.action(player);
            }
        } else {
            if (this.getActionType() == ActionType.CONSTANT) {
                this.setEnabled(player, true);
            }
        }
    }

    @Override
    public void onDeactivated(Player player) {
        super.onDeactivated(player);
        this.setEnabled(player, false);
    }

    public void action(Player player) {

    }

    @Override
    public void onKeyInput(Player player, Map<Integer, Boolean> map) {
        super.onKeyInput(player, map);
        if (this.getKey() == 0) return;
        if (map.get(this.getKey())) {
            if (this.getActionType() == ActionType.TOGGLE) {
                this.setEnabled(player, !this.dataManager.getAsBoolean("enabled"));
            } else {
                this.setEnabled(player, true);
            }
        } else {
            if (this.getActionType() == ActionType.HELD) {
                this.setEnabled(player, false);
            }
        }
    }

    public void setEnabled(Player player, boolean enabled) {
        boolean b = !enabled || this.conditionManager.isEnabled(player, "canBeEnabled");
        if (this.dataManager.getAsBoolean("enabled") != enabled && b && this.dataManager.getAsInt("cooldown") == 0) {
            this.dataManager.set("enabled", enabled);
            this.action(player);
            player.refreshDimensions();
            if (!enabled && this.getMaxCooldown() != 0) {
                this.dataManager.set("cooldown", this.getMaxCooldown());
            }
        }
    }

    @Override
    public boolean getEnabled() {
        return this.dataManager.getAsBoolean("enabled");
    }

    public ActionType getActionType() {
        if (this.getJsonObject().has("key")) {
            return ActionType.getById(GsonHelper.getAsString(GsonHelper.getAsJsonObject(this.getJsonObject(), "key"), "pressType", "toggle"));
        }
        return ActionType.CONSTANT;
    }

    public enum ActionType {
        CONSTANT,
        ACTION,
        TOGGLE,
        HELD;

        static ActionType getById(String id) {
            for (ActionType type : values()) {
                if (type.name().toLowerCase().equals(id)) {
                    return type;
                }
            }
            return null;
        }
    }
}
