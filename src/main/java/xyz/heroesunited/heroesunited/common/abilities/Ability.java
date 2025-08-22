package xyz.heroesunited.heroesunited.common.abilities;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCap;
import xyz.heroesunited.heroesunited.common.events.AbilityEvent;
import xyz.heroesunited.heroesunited.common.events.EntitySprintingEvent;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.networking.client.ClientSyncAbility;
import xyz.heroesunited.heroesunited.util.FlyingSoundInstance;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.hudata.HUData;
import xyz.heroesunited.heroesunited.util.hudata.HUDataManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Ability implements INBTSerializable<CompoundTag> {

    public String name;
    public final AbilityType type;
    protected IAbilityClientProperties clientProperties;
    protected CompoundTag additionalData = new CompoundTag();
    protected final JsonObject jsonObject;
    protected final HUDataManager dataManager = new HUDataManager(this);
    protected final ConditionManager conditionManager = new ConditionManager(this);
    protected final Player player;

    public Ability(AbilityType type, Player player, @Nonnull JsonObject jsonObject) {
        this.type = type;
        this.player = player;
        this.jsonObject = jsonObject;
        this.registerData();
        MinecraftForge.EVENT_BUS.post(new AbilityEvent.RegisterData(player, this));
        for (Map.Entry<String, HUData<?>> entry : this.dataManager.getHUDataMap().entrySet()) {
            if (entry.getValue().isJson()) {
                this.dataManager.set(entry.getKey(), entry.getValue().getFromJson(jsonObject));
            }
        }
        this.conditionManager.registerConditions(jsonObject);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            this.initializeClient(properties -> this.clientProperties = properties);
        }
    }

    public IAbilityClientProperties getClientProperties() {
        return this.clientProperties != null ? this.clientProperties : IAbilityClientProperties.DUMMY;
    }

    public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
    }

    public HUDataManager getDataManager() {
        return this.dataManager;
    }

    public void registerData() {
        this.dataManager.register("prev_cooldown", 0);
        this.dataManager.register("cooldown", 0);
        this.dataManager.register("maxCooldown", 0, true);
    }

    public boolean canActivate(Player player) {
        return this.conditionManager.isEnabled(player, "canActivate");
    }

    public void onDataUpdated(HUData<?> data) {
        if (this.player instanceof LocalPlayer localPlayer && this instanceof IFlyingAbility a) {
            if (data.getKey().equals("enabled") && ((boolean) data.getValue())) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> new Runnable() {
                    @OnlyIn(Dist.CLIENT)
                    @Override
                    public void run() {
                        Minecraft.getInstance().getSoundManager().play(new FlyingSoundInstance(a.getSoundEvent(), localPlayer));
                    }
                });
            }
        }
    }

    @Nullable
    public List<Component> getHoveredDescription() {
        return getJsonObject().has("description") ? HUJsonUtils.parseDescriptionLines(jsonObject.get("description")) : null;
    }

    public void onActivated(Player player) {
    }

    public void onUpdate(Player player) {
        this.dataManager.set("prev_cooldown", this.dataManager.getAsInt("cooldown"));
        if (this.dataManager.getAsInt("cooldown") > 0) {
            this.dataManager.set("cooldown", this.dataManager.getAsInt("cooldown") - 1);
        }
        this.conditionManager.update(player);

        if (!canActivate(player) && !alwaysActive()) {
            player.getCapability(HUAbilityCap.CAPABILITY).ifPresent(a -> a.disable(name));
        }
    }

    public void onDeactivated(Player player) {
    }

    public void onKeyInput(Player player, Map<Integer, Boolean> map) {
    }

    public void cancelSprinting(EntitySprintingEvent event) {
    }

    public int getKey() {
        if (getJsonObject().has("key")) {
            JsonObject jsonObject = GsonHelper.getAsJsonObject(this.getJsonObject(), "key");
            if (jsonObject.has("id")) {
                return GsonHelper.getAsInt(jsonObject, "id");
            } else {
                return -1;
            }
        }
        return 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("Name", this.name);
        ResourceLocation abilityType = AbilityType.REGISTRY.get().getKey(this.type);
        if (abilityType != null) {
            nbt.putString("AbilityType", abilityType.toString());
        }
        nbt.put("HUData", this.dataManager.serializeNBT());
        nbt.put("Conditions", this.conditionManager.serializeNBT());
        nbt.put("AdditionalData", additionalData);
        nbt.putString("JsonObject", this.jsonObject.toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.name = nbt.getString("Name");
        this.dataManager.deserializeNBT(nbt.getCompound("HUData"));
        this.conditionManager.deserializeNBT(nbt.getCompound("Conditions"));
        this.additionalData = nbt.getCompound("AdditionalData");
    }

    public Component getTitle() {
        if (getJsonObject().has("title")) {
            return Component.Serializer.fromJson(GsonHelper.getAsJsonObject(getJsonObject(), "title"));
        } else {
            return Component.translatable(name);
        }
    }

    public ConditionManager getConditionManager() {
        return conditionManager;
    }

    public CompoundTag getAdditionalData() {
        return additionalData;
    }

    public int getMaxCooldown() {
        return this.dataManager.getAsInt("maxCooldown");
    }

    public float getCooldownProgress(float partialTicks) {
        return (this.dataManager.getAsInt("prev_cooldown") + (this.dataManager.getAsInt("cooldown") - this.dataManager.getAsInt("prev_cooldown")) * partialTicks) / this.getMaxCooldown();
    }

    public boolean getEnabled() {
        return false;
    }

    public boolean isVisible() {
        if (GsonHelper.getAsBoolean(getJsonObject(), "hidden", false)) {
            return false;
        }
        boolean b = this.conditionManager.isEnabled(this.player, "isHidden");
        return !b;
    }

    public boolean alwaysActive() {
        return GsonHelper.getAsBoolean(getJsonObject(), "active", false) && this.conditionManager.isEnabled(this.player, "alwaysActive");
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public Player getPlayer() {
        return player;
    }

    public void sync(Player player) {
        if (player instanceof ServerPlayer) {
            HUNetworking.INSTANCE.sendTo(new ClientSyncAbility(player.getId(), this.name, this.serializeNBT()), ((ServerPlayer) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public void syncToAll(Player player) {
        this.sync(player);
        for (Player mpPlayer : player.level.players()) {
            if (mpPlayer instanceof ServerPlayer) {
                HUNetworking.INSTANCE.sendTo(new ClientSyncAbility(player.getId(), this.name, this.serializeNBT()), ((ServerPlayer) mpPlayer).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }
}
