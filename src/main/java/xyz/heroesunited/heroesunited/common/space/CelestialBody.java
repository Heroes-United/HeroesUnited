package xyz.heroesunited.heroesunited.common.space;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

public class CelestialBody extends ForgeRegistryEntry<CelestialBody> {
    public static IForgeRegistry<CelestialBody> CELESTIAL_BODIES;

    protected Vec3 coordinates;


    public CelestialBody(Vec3 coordinates) {
        this.coordinates = coordinates;
    }


    public Vec3 getCoordinates() {
        return coordinates;
    }

    public void tick() {
    }

    public void entityInside(Entity entity) {
    }

    public CompoundTag writeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putDouble("x", coordinates.x);
        compound.putDouble("y", coordinates.y);
        compound.putDouble("z", coordinates.z);
        return compound;
    }

    public AABB getHitbox() {
        return new AABB(BlockPos.ZERO, BlockPos.ZERO);
    }

    public void readNBT(CompoundTag nbt) {
        coordinates = new Vec3(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
    }
}
