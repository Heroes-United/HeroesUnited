package xyz.heroesunited.heroesunited.util;

import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import xyz.heroesunited.heroesunited.HeroesUnited;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.suit.Suit;
import xyz.heroesunited.heroesunited.common.space.Planet;

import java.util.Objects;

public class HUPlayerUtil {

    public static boolean canBreath(LivingEntity entity) {
        boolean canBreath = !entity.level.dimension().equals(HeroesUnited.SPACE);
        if (Planet.PLANETS_MAP.containsKey(entity.level.dimension())) {
            Planet planet = Planet.PLANETS_MAP.get(entity.level.dimension());
            canBreath = planet.hasOxygen();
        }

        if (entity instanceof PlayerEntity) {
            for (Ability a : AbilityHelper.getAbilities(entity)) {
                if (Objects.equals(a.type, AbilityType.OXYGEN) && !canBreath) {
                    canBreath = a.getEnabled();
                    break;
                }
            }
        }

        Suit suit = Suit.getSuit(entity);
        if (suit != null && !canBreath) {
            canBreath = suit.canBreathOnSpace();
        }

        return canBreath;
    }

    public static void playSoundToAll(World world, Vector3d vec, double range, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        for (PlayerEntity player : world.getEntitiesOfClass(PlayerEntity.class, getCollisionBoxWithRange(vec, range))) {
            if (player instanceof ServerPlayerEntity && sound.getRegistryName() != null) {
                ((ServerPlayerEntity) player).connection.send(new SPlaySoundPacket(sound.getRegistryName(), category, new Vector3d(vec.x, vec.y, vec.z), volume, pitch));
            }
        }
    }

    public static boolean haveSmallArms(Entity entity) {
        if (entity instanceof AbstractClientPlayerEntity) {
            return ((AbstractClientPlayerEntity) entity).getModelName().equalsIgnoreCase("slim");
        }
        return false;
    }

    public static AxisAlignedBB getCollisionBoxWithRange(Vector3d posVc3d, double range) {
        return new AxisAlignedBB(new BlockPos(posVc3d.x - range, posVc3d.y - range, posVc3d.z - range), new BlockPos(posVc3d.x + range, posVc3d.y + range, posVc3d.z + range));
    }

    public static Vector3d getPlayerPos(PlayerEntity player) {
        return new Vector3d(player.getX(), player.getY(), player.getZ());
    }

    public static void makeLaserLooking(PlayerEntity player, double distance, float strength) {
        RayTraceResult rtr = getPosLookingAt(player, distance);
        if (rtr != null && !player.level.isClientSide) {
            if (rtr.getType() == RayTraceResult.Type.ENTITY) {
                EntityRayTraceResult ertr = (EntityRayTraceResult) rtr;
                if (ertr.getEntity() != player) {
                    ertr.getEntity().setSecondsOnFire((int) (strength * 5));
                    ertr.getEntity().hurt(DamageSource.mobAttack(player), strength * 2);
                }
            } else if (rtr.getType() == RayTraceResult.Type.BLOCK) {
                BlockPos pos = new BlockPos(rtr.getLocation());
                for (Direction dir : Direction.values()) {
                    BlockPos blockPos = new BlockPos(pos.relative(dir.getOpposite()));
                    if (player.level.isEmptyBlock(blockPos)) {
                        player.level.setBlock(blockPos, Blocks.FIRE.defaultBlockState(), 11);
                    }
                }
            }
        }
    }

    public static RayTraceResult getPosLookingAt(PlayerEntity player, double distance) {
        Vector3d startPos = player.position().add(0, player.getEyeHeight(), 0);
        Vector3d endPos = player.position().add(0, player.getEyeHeight(), 0).add(player.getLookAngle().scale(distance));

        for (int i = 0; i < distance * 2; i++) {
            float scale = i / 2F;
            Vector3d pos = startPos.add(endPos.subtract(startPos).scale(scale / distance));
            BlockPos bpos = new BlockPos(pos);
            boolean block = !player.level.getBlockState(bpos).canOcclude() && player.level.getBlockState(bpos).getBlock() instanceof AbstractGlassBlock;
            if ((player.level.getBlockState(bpos).canOcclude() && !player.level.isEmptyBlock(bpos)) || block) {
                return new BlockRayTraceResult(pos, Direction.UP, bpos, false);
            } else {
                Vector3d min = pos.add(0.25F, 0.25F, 0.25F);
                Vector3d max = pos.add(-0.25F, -0.25F, -0.25F);
                for (Entity entity : player.level.getEntities(player, new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z))) {
                    return new EntityRayTraceResult(entity);
                }
            }
        }
        return null;
    }

    public static void setSuitForPlayer(PlayerEntity player, Suit suit) {
        Item helmet = suit.getHelmet();
        Item chest = suit.getChestplate();
        Item legs = suit.getLegs();
        Item feet = suit.getBoots();
        if (helmet != null) {
            if (player.getItemBySlot(EquipmentSlotType.HEAD).isEmpty()) {
                player.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(helmet));
            } else player.addItem(new ItemStack(helmet));
            player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
        if (chest != null) {
            if (player.getItemBySlot(EquipmentSlotType.CHEST).isEmpty()) {
                player.setItemSlot(EquipmentSlotType.CHEST, new ItemStack(chest));
            } else player.addItem(new ItemStack(chest));
            player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
        if (legs != null) {
            if (player.getItemBySlot(EquipmentSlotType.LEGS).isEmpty()) {
                player.setItemSlot(EquipmentSlotType.LEGS, new ItemStack(legs));
            } else player.addItem(new ItemStack(legs));
            player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
        if (feet != null) {
            if (player.getItemBySlot(EquipmentSlotType.FEET).isEmpty()) {
                player.setItemSlot(EquipmentSlotType.FEET, new ItemStack(feet));
            } else player.addItem(new ItemStack(feet));
            player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }
}
