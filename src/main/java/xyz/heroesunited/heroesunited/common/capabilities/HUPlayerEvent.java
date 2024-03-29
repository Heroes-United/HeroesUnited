package xyz.heroesunited.heroesunited.common.capabilities;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import xyz.heroesunited.heroesunited.HeroesUnited;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCap;
import xyz.heroesunited.heroesunited.common.capabilities.ability.HUAbilityCapProvider;
import xyz.heroesunited.heroesunited.common.capabilities.ability.IHUAbilityCap;
import xyz.heroesunited.heroesunited.common.capabilities.hudata.HUDataProvider;
import xyz.heroesunited.heroesunited.common.networking.HUNetworking;
import xyz.heroesunited.heroesunited.common.networking.client.ClientSyncAbilityCap;
import xyz.heroesunited.heroesunited.common.networking.client.ClientSyncHUPlayer;
import xyz.heroesunited.heroesunited.common.objects.items.IAccessory;

public class HUPlayerEvent {

    @SubscribeEvent
    public void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(new ResourceLocation(HeroesUnited.MODID, "huplayer"), new HUPlayerProvider((LivingEntity) event.getObject()));
        }
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(HeroesUnited.MODID, "huability"), new HUAbilityCapProvider((Player) event.getObject()));
        }
        event.addCapability(new ResourceLocation(HeroesUnited.MODID, "hudata"), new HUDataProvider(event.getObject()));
    }

    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            HUPlayer.getCap(event.getEntity()).copy(HUPlayer.getCap(event.getOriginal()));
            HUAbilityCap.getCap(event.getEntity()).copy(HUAbilityCap.getCap(event.getOriginal()));
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player && !event.getEntity().level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            player.getCapability(HUPlayerProvider.CAPABILITY).ifPresent(a -> {
                NonNullList<ItemStack> list = a.getInventory().getItems();
                for (int i = 0; i < list.size(); ++i) {
                    ItemStack stack = list.get(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof IAccessory && ((IAccessory) stack.getItem()).dropAfterDeath(player, stack) == true) {
                        player.drop(stack, true, true);
                        list.set(i, ItemStack.EMPTY);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking e) {
        if (e.getEntity() instanceof ServerPlayer) {
            e.getTarget().getCapability(HUAbilityCap.CAPABILITY).ifPresent(a ->
                    HUNetworking.INSTANCE.sendTo(new ClientSyncAbilityCap(e.getTarget().getId(), a.serializeNBT()), ((ServerPlayer) e.getEntity()).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
            e.getTarget().getCapability(HUPlayerProvider.CAPABILITY).ifPresent(a ->
                    HUNetworking.INSTANCE.sendTo(new ClientSyncHUPlayer(e.getTarget().getId(), a.serializeNBT()), ((ServerPlayer) e.getEntity()).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof ServerPlayer) {
            e.getEntity().getCapability(HUAbilityCap.CAPABILITY).ifPresent(IHUAbilityCap::syncToAll);
            e.getEntity().getCapability(HUPlayerProvider.CAPABILITY).ifPresent(IHUPlayer::syncToAll);
        }
    }
}
