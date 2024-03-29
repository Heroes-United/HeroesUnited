package xyz.heroesunited.heroesunited.common.capabilities;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.compress.utils.Lists;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;
import xyz.heroesunited.heroesunited.common.events.RegisterPlayerControllerEvent;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.networking.client.ClientSyncHUPlayer;
import xyz.heroesunited.heroesunited.common.networking.client.ClientTriggerPlayerAnim;
import xyz.heroesunited.heroesunited.common.objects.container.AccessoriesInventory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class HUPlayer implements IHUPlayer {
    public final AccessoriesInventory inventory;
    protected final LivingEntity livingEntity;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, false);
    protected Map<ResourceLocation, Level> superpowerLevels;
    private int theme;
    private float flightAmount, flightAmountO, slowMo = 20F;
    private boolean intangible;
    protected ResourceLocation animationFile;
    private final PlayerGeoModel modelProvider = new PlayerGeoModel();

    public HUPlayer(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
        this.superpowerLevels = Maps.newHashMap();
        this.inventory = new AccessoriesInventory(livingEntity);
    }

    @Nullable
    public static IHUPlayer getCap(Entity entity) {
        return entity.getCapability(HUPlayerProvider.CAPABILITY).orElse(null);
    }

    @Override
    public void updateFlyAmount() {
        this.flightAmountO = this.flightAmount;
        if (livingEntity.isSprinting()) {
            this.flightAmount = Math.min(1.0F, this.flightAmount + 0.07F);
        } else {
            this.flightAmount = Math.max(0.0F, this.flightAmount - 0.07F);
        }
    }

    @Override
    public float getFlightAmount(float partialTicks) {
        return Mth.lerp(partialTicks, this.flightAmountO, this.flightAmount);
    }

    @Override
    public Map<ResourceLocation, Level> getSuperpowerLevels() {
        return superpowerLevels;
    }

    @Override
    public void triggerAnim(@Nullable String controllerName, String animName, ResourceLocation animationFile) {
        this.animationFile = animationFile;
        this.registerNewPlayerControllers();
        if (this.livingEntity.getLevel().isClientSide()) {
            var controller = getController(controllerName);
            if (controller != null) {
                controller.tryTriggerAnimation(animName);
            }
        } else {
            HUNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.livingEntity), new ClientTriggerPlayerAnim(this.livingEntity.getId(), controllerName, animName, animationFile));
        }
    }

    @Override
    public float getSlowMoSpeed() {
        return slowMo;
    }

    @Override
    public void setSlowMoSpeed(float slowMo) {
        this.slowMo = slowMo;
        this.syncToAll();
    }

    @Override
    public boolean isIntangible() {
        return intangible;
    }

    @Override
    public void setIntangible(boolean intangible) {
        this.intangible = intangible;
        this.syncToAll();
    }

    @Override
    public int getTheme() {
        return theme;
    }

    @Override
    public void setTheme(int theme) {
        this.theme = theme;
    }

    @Override
    public IHUPlayer copy(IHUPlayer cap) {
        this.deserializeNBT(cap.serializeNBT());
        this.theme = cap.getTheme();
        this.inventory.copy(cap.getInventory());
        this.slowMo = 20F;
        this.sync();
        return this;
    }

    @Override
    public IHUPlayer sync() {
        if (livingEntity instanceof ServerPlayer) {
            HUNetworking.INSTANCE.sendTo(new ClientSyncHUPlayer(livingEntity.getId(), this.serializeNBT()), ((ServerPlayer) livingEntity).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
        return this;
    }

    @Override
    public IHUPlayer syncToAll() {
        this.sync();
        for (Player player : this.livingEntity.level.players()) {
            if (player instanceof ServerPlayer) {
                HUNetworking.INSTANCE.sendTo(new ClientSyncHUPlayer(this.livingEntity.getId(), this.serializeNBT()), ((ServerPlayer) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        }
        return this;
    }

    @Override
    public AccessoriesInventory getInventory() {
        return inventory;
    }

    @Override
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    @Override
    public PlayerGeoModel getAnimatedModel() {
        return modelProvider;
    }

    @Override
    public AnimationController getController(String controllerName) {
        return getAnimatableInstanceCache().getManagerForId(this.livingEntity.getUUID().hashCode()).getAnimationControllers().get(controllerName);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        CompoundTag levels = new CompoundTag();
        superpowerLevels.forEach((resourceLocation, level) -> levels.put(resourceLocation.toString(), level.writeNBT()));
        nbt.put("levels", levels);
        nbt.putFloat("SlowMo", this.slowMo);
        nbt.putBoolean("Intangible", this.intangible);
        nbt.putInt("Theme", this.theme);
        if (this.animationFile != null) {
            nbt.putString("AnimationFile", this.animationFile.toString());
        }
        ContainerHelper.saveAllItems(nbt, this.inventory.getItems());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag levels = nbt.getCompound("levels");
        superpowerLevels.clear();
        for (String key : levels.getAllKeys()) {
            superpowerLevels.put(new ResourceLocation(key), Level.readFromNBT(levels.getCompound(key)));
        }
        if (nbt.contains("Intangible")) {
            this.intangible = nbt.getBoolean("Intangible");
        }
        if (nbt.contains("SlowMo")) {
            this.slowMo = nbt.getFloat("SlowMo");
        }
        if (nbt.contains("Theme")) {
            this.theme = nbt.getInt("Theme");
        }
        if (nbt.contains("AnimationFile")) {
            this.animationFile = new ResourceLocation(nbt.getString("AnimationFile"));
        }
        inventory.getItems().clear();
        ContainerHelper.loadAllItems(nbt, inventory.getItems());

        this.registerNewPlayerControllers();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        if (livingEntity instanceof Player) {
            List<AnimationController<? extends GeoAnimatable>> animationControllers = Lists.newArrayList();
            MinecraftForge.EVENT_BUS.post(new RegisterPlayerControllerEvent(this, (Player) livingEntity, animationControllers));
            animationControllers.forEach(controllers::add);
        }
    }

    public void registerNewPlayerControllers() {
        if (livingEntity instanceof Player) {
            List<AnimationController<? extends GeoAnimatable>> animationControllers = Lists.newArrayList();
            MinecraftForge.EVENT_BUS.post(new RegisterPlayerControllerEvent(this, (Player) livingEntity, animationControllers));
            AnimatableManager manager = this.cache.getManagerForId(this.livingEntity.getUUID().hashCode());
            for (AnimationController<? extends GeoAnimatable> controller : animationControllers) {
                if (!manager.getAnimationControllers().containsKey(controller.getName())) {
                    manager.addController(controller);
                }
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object object) {
        return ((Entity) object).tickCount + Minecraft.getInstance().getFrameTime();
    }
}
