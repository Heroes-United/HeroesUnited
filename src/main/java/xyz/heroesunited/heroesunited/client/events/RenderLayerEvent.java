package xyz.heroesunited.heroesunited.client.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Just event.
 */
public abstract class RenderLayerEvent<T extends LivingEntity, M extends HumanoidModel<T>> extends Event {

    protected final T livingEntity;
    protected final LivingEntityRenderer<T, M> renderer;
    protected final PoseStack matrixStack;
    protected final MultiBufferSource bufferIn;
    protected final int packedLightIn;
    protected float limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch;

    public RenderLayerEvent(LivingEntityRenderer<T, M> renderer, T livingEntity, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLightIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.livingEntity = livingEntity;
        this.renderer = renderer;
        this.matrixStack = matrixStack;
        this.bufferIn = bufferIn;
        this.packedLightIn = packedLightIn;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.partialTicks = partialTicks;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
    }

    public T getLivingEntity() {
        return livingEntity;
    }

    public LivingEntityRenderer<T, M> getRenderer() {
        return renderer;
    }

    public PoseStack getPoseStack() {
        return matrixStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return bufferIn;
    }

    public int getPackedLight() {
        return packedLightIn;
    }

    public float getLimbSwing() {
        return limbSwing;
    }

    public float getLimbSwingAmount() {
        return limbSwingAmount;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public float getAgeInTicks() {
        return ageInTicks;
    }

    public float getNetHeadYaw() {
        return netHeadYaw;
    }

    public float getHeadPitch() {
        return headPitch;
    }

    @Cancelable
    public static class Accessories<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayerEvent<T, M> {

        private final EntityModelSet modelSet;

        public Accessories(EntityModelSet modelSet, LivingEntityRenderer<T, M> renderer, T entity, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLightIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            super(renderer, entity, matrixStack, bufferIn, packedLightIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            this.modelSet = modelSet;
        }

        public EntityModelSet getModelSet() {
            return modelSet;
        }
    }

    public static class Player extends RenderLayerEvent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

        private final EntityRendererProvider.Context context;

        public Player(EntityRendererProvider.Context context, PlayerRenderer renderer, AbstractClientPlayer player, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLightIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            super(renderer, player, matrixStack, bufferIn, packedLightIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            this.context = context;
        }

        public EntityRendererProvider.Context getContext() {
            return context;
        }

        @Override
        public PlayerRenderer getRenderer() {
            return (PlayerRenderer) renderer;
        }

        public AbstractClientPlayer getPlayer() {
            return livingEntity;
        }
    }

    public static class Armor<T extends LivingEntity> extends RenderLayerEvent<T, HumanoidModel<T>> {

        public Armor(LivingEntityRenderer renderer, T livingEntity, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLightIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            super(renderer, livingEntity, matrixStack, bufferIn, packedLightIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }

        @Cancelable
        public static class Pre extends Armor {

            public Pre(LivingEntityRenderer renderer, LivingEntity livingEntity, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLightIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                super(renderer, livingEntity, matrixStack, bufferIn, packedLightIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }

        public static class Post extends Armor {

            public Post(LivingEntityRenderer renderer, LivingEntity livingEntity, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLightIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                super(renderer, livingEntity, matrixStack, bufferIn, packedLightIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }

        public static class ArmorVisibility extends Event {

            private final HumanoidModel armorModel;
            private final EquipmentSlot slot;

            public ArmorVisibility(HumanoidModel modelIn, EquipmentSlot slotIn) {
                this.armorModel = modelIn;
                this.slot = slotIn;
            }

            public HumanoidModel getArmorModel() {
                return armorModel;
            }

            public EquipmentSlot getSlot() {
                return slot;
            }

        }
    }
}
