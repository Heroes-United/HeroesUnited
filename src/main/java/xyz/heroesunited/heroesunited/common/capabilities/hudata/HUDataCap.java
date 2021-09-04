package xyz.heroesunited.heroesunited.common.capabilities.hudata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.INBTSerializable;
import xyz.heroesunited.heroesunited.common.events.HUDataRegister;
import xyz.heroesunited.heroesunited.util.hudata.HUDataManager;

public class HUDataCap implements IHUDataCap, INBTSerializable<CompoundTag> {

    @CapabilityInject(IHUDataCap.class)
    public static Capability<IHUDataCap> CAPABILITY;
    private final HUDataManager dataManager;

    public HUDataCap(Entity entity) {
        this.dataManager = new HUDataManager();
        MinecraftForge.EVENT_BUS.post(new HUDataRegister(entity, this.dataManager));
    }

    public static IHUDataCap getCap(Entity entity) {
        return entity.getCapability(HUDataCap.CAPABILITY).orElse(null);
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.dataManager.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.dataManager.deserializeNBT(nbt);
    }

    @Override
    public HUDataManager getDataManager() {
        return this.dataManager;
    }
}
