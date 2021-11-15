package xyz.heroesunited.heroesunited.common.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.util.KeyMap;

/**
 * Fired when player pressed keybinding
 */

public abstract class HUAbilityEvent extends PlayerEvent {

    private final Ability ability;

    public HUAbilityEvent(PlayerEntity player, Ability ability) {
        super(player);
        this.ability = ability;
    }

    public Ability getAbility() {
        return ability;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    public static class Enabled extends HUAbilityEvent {
        public Enabled(PlayerEntity player, Ability ability) {
            super(player, ability);
        }
    }

    public static class Disabled extends HUAbilityEvent {
        public Disabled(PlayerEntity player, Ability ability) {
            super(player, ability);
        }
    }

    public static class KeyInput extends HUAbilityEvent {
        private final KeyMap map;

        public KeyInput(PlayerEntity player, Ability ability, KeyMap map) {
            super(player, ability);
            this.map = map;
        }

        public boolean isPressed(KeyMap.HUKeys key) {
            return this.map.get(key.index);
        }
    }
}
