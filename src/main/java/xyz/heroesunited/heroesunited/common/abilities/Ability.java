package xyz.heroesunited.heroesunited.common.abilities;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCap;
import xyz.heroesunited.heroesunited.common.events.EntitySprintingEvent;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.networking.client.ClientSyncAbility;
import xyz.heroesunited.heroesunited.util.HUJsonUtils;
import xyz.heroesunited.heroesunited.util.hudata.HUDataManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Ability implements INBTSerializable<CompoundTag> {

    public String name;
    public final AbilityType type;
    protected CompoundTag additionalData = new CompoundTag();
    protected final JsonObject jsonObject;
    protected final HUDataManager dataManager = new HUDataManager();
    protected final ConditionManager conditionManager = new ConditionManager(this);
    protected final Player player;
    private IAbilityClientProperties clientProperties;

    public Ability(AbilityType type, Player player, @Nonnull JsonObject jsonObject) {
        this.type = type;
        this.player = player;
        this.jsonObject = jsonObject;
        this.conditionManager.registerConditions(jsonObject);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            initializeClient(properties -> this.clientProperties = properties);
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

    @Nullable
    public List<Component> getHoveredDescription() {
        return getJsonObject().has("description") ? HUJsonUtils.parseDescriptionLines(jsonObject.get("description")) : null;
    }

    public void onActivated(Player player) {
    }

    public void onUpdate(Player player) {
        this.dataManager.set("prev_cooldown", this.dataManager.<Integer>getValue("cooldown"));
        if (this.dataManager.<Integer>getValue("cooldown") > 0) {
            this.dataManager.set("cooldown", this.dataManager.<Integer>getValue("cooldown") - 1);
        }
        this.conditionManager.update(player);

        if (!canActivate(player) && !alwaysActive(player)) {
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
            return GsonHelper.getAsInt(GsonHelper.getAsJsonObject(this.getJsonObject(), "key"), "id");
        }
        return 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("AbilityType", this.type.getRegistryName().toString());
        nbt.put("HUData", this.dataManager.serializeNBT());
        nbt.put("Conditions", this.conditionManager.serializeNBT());
        nbt.put("AdditionalData", additionalData);
        nbt.putString("JsonObject", this.jsonObject.toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.dataManager.deserializeNBT(nbt.getCompound("HUData"));
        this.conditionManager.deserializeNBT(nbt.getCompound("Conditions"));
        this.additionalData = nbt.getCompound("AdditionalData");
    }

    public Component getTitle() {
        if (getJsonObject().has("title")) {
            return Component.Serializer.fromJson(GsonHelper.getAsJsonObject(getJsonObject(), "title"));
        } else {
            return new TranslatableComponent(name);
        }
    }

    public ConditionManager getConditionManager() {
        return conditionManager;
    }

    public CompoundTag getAdditionalData() {
        return additionalData;
    }

    public int getMaxCooldown() {
        return this.dataManager.<Integer>getValue("maxCooldown");
    }

    public boolean getEnabled() {
        return false;
    }

    public boolean isVisible(Player player) {
        return !GsonHelper.getAsBoolean(getJsonObject(), "hidden", false) || !this.conditionManager.isEnabled(player, "isHidden");
    }

    public boolean alwaysActive(Player player) {
        return GsonHelper.getAsBoolean(getJsonObject(), "active", false) && this.conditionManager.isEnabled(player, "alwaysActive");
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
