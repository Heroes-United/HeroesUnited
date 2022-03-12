package xyz.heroesunited.heroesunited.common.abilities;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class JSONAbility extends Ability {

    protected ActionType actionType;

    public JSONAbility(AbilityType type, Player player, JsonObject jsonObject) {
        super(type, player, jsonObject);
        if (jsonObject.has("key")) {
            this.actionType = ActionType.getById(GsonHelper.getAsString(GsonHelper.getAsJsonObject(jsonObject, "key"), "pressType", "toggle"));
        } else {
            this.actionType = ActionType.CONSTANT;
        }
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register("enabled", false);
    }

    @Override
    public void onUpdate(Player player) {
        super.onUpdate(player);
        if (this.actionType != ActionType.ACTION) {
            this.action(player);
        } else {
            if (this.dataManager.getAsBoolean("enabled")) {
                this.setEnabled(player, false);
            }
        }
        if (this.actionType == ActionType.CONSTANT && !this.dataManager.getAsBoolean("enabled")) {
            this.setEnabled(player, true);
        }

        for (Map.Entry<String, Boolean> entry : this.conditionManager.getMethodConditions().entrySet()) {
            if (entry.getKey().equals("canBeEnabled") && !entry.getValue()) {
                this.setEnabled(player, false);
            }
        }
    }

    @Override
    public void onDeactivated(Player player) {
        super.onDeactivated(player);
        this.setEnabled(player, false);
        this.action(player);
    }

    public void action(Player player) {

    }

    @Override
    public void onKeyInput(Player player, Map<Integer, Boolean> map) {
        super.onKeyInput(player, map);
        if (this.getKey() == 0) return;
        if (map.get(this.getKey())) {
            if (this.actionType == ActionType.TOGGLE) {
                this.setEnabled(player, !this.dataManager.getAsBoolean("enabled"));
            } else {
                this.setEnabled(player, true);
            }
        } else {
            if (this.actionType == ActionType.HELD) {
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

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        if (this.actionType != null) {
            nbt.putString("actionType", this.actionType.name().toLowerCase());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        if (nbt.contains("actionType")) {
            this.actionType = ActionType.getById(nbt.getString("actionType"));
        }
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
