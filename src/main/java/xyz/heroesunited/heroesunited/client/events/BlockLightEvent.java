package xyz.heroesunited.heroesunited.client.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is called when block light updated.
 * Can be used to make dynamic lights
 */
public class BlockLightEvent extends Event {

    private final int defaultValue;
    private int value;
    private final BlockPos pos;
    private final BlockGetter world;

    public BlockLightEvent(int defaultValue, BlockPos pos, BlockGetter world) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.pos = pos;
        this.world = world;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public void setNewValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockGetter getWorld() {
        return world;
    }
}
