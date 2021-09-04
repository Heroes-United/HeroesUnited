package xyz.heroesunited.heroesunited.common.networking.client;

import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCap;

import java.util.function.Supplier;

public class ClientEnableAbility {

    public int entityId;
    public String name;
    public CompoundTag nbt;

    public ClientEnableAbility(int entityId, String name, CompoundTag nbt) {
        this.entityId = entityId;
        this.name = name;
        this.nbt = nbt;
    }

    public ClientEnableAbility(FriendlyByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.name = buffer.readUtf(32767);
        this.nbt = buffer.readNbt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeUtf(this.name);
        buffer.writeNbt(this.nbt);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
            if (entity instanceof AbstractClientPlayer) {
                entity.getCapability(HUAbilityCap.CAPABILITY).ifPresent(cap -> {
                    Ability ability = AbilityType.ABILITIES.get().getValue(new ResourceLocation(this.nbt.getString("AbilityType"))).create(this.name);
                    if (ability != null) {
                        if (this.nbt.contains("JsonObject")) {
                            ability.setJsonObject(entity, new JsonParser().parse(this.nbt.getString("JsonObject")).getAsJsonObject());
                        }
                        cap.enable(this.name, ability);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}