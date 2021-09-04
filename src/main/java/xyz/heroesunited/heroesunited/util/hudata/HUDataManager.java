package xyz.heroesunited.heroesunited.util.hudata;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.networking.client.ClientSyncHUData;

import java.util.Map;

public class HUDataManager implements INBTSerializable<CompoundTag> {

    protected Map<String, HUData<?>> dataMap = Maps.newHashMap();

    public <T> void register(String id, T defaultValue) {
        dataMap.put(id, new HUData<>(id, defaultValue, false));
    }

    public <T> void register(String id, T defaultValue, boolean json) {
        dataMap.put(id, new HUData<>(id, defaultValue, json));
    }

    public <T> void set(String id, T value) {
        HUData<T> data = getData(id);
        if (!value.equals(data.getValue())) {
            data.setValue(value);
            data.setDirty(true);
        }
    }

    public <T> void assignValue(HUData<T> data, CompoundTag nbt) {
        if (data != null) {
            T value = (T) data.deserializeNBT(nbt, data.getKey(), data.getDefaultValue());
            if (!value.equals(data.getValue())) {
                data.setValue(value);
                data.setDirty(false);
            }
        }
    }

    public <T> T getValue(String id) {
        HUData<T> data = getData(id);
        return data.getValue();
    }

    public <T> HUData<T> getData(String id) {
        return (HUData<T>) dataMap.get(id);
    }

    public Map<String, HUData<?>> getHUDataMap() {
        return this.dataMap;
    }

    public void syncToAll(Player player, String abilityName) {
        for (HUData<?> value : this.dataMap.values()) {
            if (value.isDirty() && !player.level.isClientSide) {
                HUNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new ClientSyncHUData(player.getId(), abilityName, value.getKey(), serializeNBT()));
                value.setDirty(false);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        for (HUData data : dataMap.values()) {
            if (data.getValue() != null) {
                data.serializeNBT(nbt, data.getKey(), data.getValue());
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (HUData data : dataMap.values()) {
            data.setValue(data.deserializeNBT(nbt, data.getKey(), data.getDefaultValue()));
        }
    }
}
