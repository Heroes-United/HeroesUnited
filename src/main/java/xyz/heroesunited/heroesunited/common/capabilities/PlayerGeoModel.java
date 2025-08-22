package xyz.heroesunited.heroesunited.common.capabilities;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.molang.LazyVariable;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.core.object.DataTicket;
import software.bernie.geckolib.model.DefaultedGeoModel;
import xyz.heroesunited.heroesunited.HeroesUnited;
import xyz.heroesunited.heroesunited.util.PlayerPart;

import javax.annotation.Nullable;

public class PlayerGeoModel extends DefaultedGeoModel<IHUPlayer> {

    public static final DataTicket<ModelData> PLAYER_MODEL_DATA = new DataTicket<>("player_model_data", ModelData.class);

    @Nullable
    private ModelData modelData;

    public PlayerGeoModel() {
        super(new ResourceLocation(HeroesUnited.MODID, "player"));
    }

    @Override
    protected String subtype() {
        return "entity";
    }

    @Override
    public ResourceLocation getTextureResource(IHUPlayer o) {
        return ((LocalPlayer) ((HUPlayer) o).livingEntity).getSkinTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(IHUPlayer o) {
        return ((HUPlayer) o).animationFile != null ? ((HUPlayer) o).animationFile : new ResourceLocation(HeroesUnited.MODID, "animations/player.animation.json");
    }

    @Override
    public void setCustomAnimations(IHUPlayer entity, long instanceId, AnimationState<IHUPlayer> customPredicate) {
        if (customPredicate != null) {
            this.modelData = customPredicate.getData(PLAYER_MODEL_DATA);
        }
        super.setCustomAnimations(entity, instanceId, customPredicate);
    }

    @Override
    public boolean crashIfBoneMissing() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMolangQueries(IHUPlayer animatable, double animTime) {
        super.applyMolangQueries(animatable, animTime);
        MolangParser parser = MolangParser.INSTANCE;
        if (animatable != null && this.modelData != null) {
            for (PlayerPart part : PlayerPart.bodyParts()) {
                String name = part == PlayerPart.CHEST ? "body" : part.name().toLowerCase();
                ModelPart renderer = part.initialModelPart(this.modelData.model);
                parser.setMemoizedValue(String.format("player.%s.x_rot", name), () -> renderer.xRot / Math.PI * 180.0);
                parser.setMemoizedValue(String.format("player.%s.y_rot", name), () -> renderer.yRot / Math.PI * 180.0);
                parser.setMemoizedValue(String.format("player.%s.z_rot", name), () -> renderer.zRot / Math.PI * 180.0);

                parser.setMemoizedValue(String.format("player.%s.x", name), () -> switch (part) {
                    case RIGHT_ARM -> renderer.x + 5;
                    case LEFT_ARM -> renderer.x - 5;
                    case RIGHT_LEG -> renderer.x + 2;
                    case LEFT_LEG -> renderer.x - 2;
                    default -> renderer.x;
                });
                parser.setMemoizedValue(String.format("player.%s.y", name), () -> switch (part) {
                    case RIGHT_ARM, LEFT_ARM -> 2 - renderer.y;
                    case RIGHT_LEG, LEFT_LEG -> 12 - renderer.y;
                    default -> -renderer.y;
                });
                parser.setMemoizedValue(String.format("player.%s.z", name), () -> renderer.z);
            }
            parser.setMemoizedValue("player.leftIsMainArm", () -> animatable.getLivingEntity().getMainArm() == HumanoidArm.LEFT ? 1 : 0);
            parser.setMemoizedValue("player.x_rot", () -> animatable.getLivingEntity().getXRot());
            parser.setMemoizedValue("player.y_rot", () -> animatable.getLivingEntity().getYRot());
            parser.setMemoizedValue("player.limbSwing", () -> this.modelData.limbSwing);
            parser.setMemoizedValue("player.limbSwingAmount", () -> this.modelData.limbSwingAmount);
            parser.setMemoizedValue("player.ageInTicks", () -> this.modelData.ageInTicks);
            parser.setMemoizedValue("player.headPitch", () -> this.modelData.headPitch);
            parser.setMemoizedValue("player.netHeadYaw", () -> this.modelData.netHeadYaw);
        }
    }

    public static void registerMolangQueries() {
        MolangParser parser = MolangParser.INSTANCE;

        for (PlayerPart part : PlayerPart.bodyParts()) {
            String name = part.name().toLowerCase();
            parser.register(new LazyVariable(String.format("player.%s.x_rot", name), 0));
            parser.register(new LazyVariable(String.format("player.%s.y_rot", name), 0));
            parser.register(new LazyVariable(String.format("player.%s.z_rot", name), 0));

            parser.register(new LazyVariable(String.format("player.%s.x", name), 0));
            parser.register(new LazyVariable(String.format("player.%s.y", name), 0));
            parser.register(new LazyVariable(String.format("player.%s.z", name), 0));
        }

        parser.register(new LazyVariable("player.leftIsMainArm", 0));
        parser.register(new LazyVariable("player.x_rot", 0));
        parser.register(new LazyVariable("player.y_rot", 0));
        parser.register(new LazyVariable("player.limbSwing", 0));
        parser.register(new LazyVariable("player.limbSwingAmount", 0));
        parser.register(new LazyVariable("player.ageInTicks", 0));
        parser.register(new LazyVariable("player.headPitch", 0));
        parser.register(new LazyVariable("player.netHeadYaw", 0));
    }

    public static class ModelData {
        public final PlayerModel<?> model;
        public final float limbSwing, limbSwingAmount, ageInTicks, headPitch, netHeadYaw;

        public ModelData(PlayerModel<?> model) {
            this(model, 0, 0, 0, 0, 0);
        }

        public ModelData(PlayerModel<?> model, float limbSwing, float limbSwingAmount, float ageInTicks, float headPitch, float netHeadYaw) {
            this.model = model;
            this.limbSwing = limbSwing;
            this.limbSwingAmount = limbSwingAmount;
            this.ageInTicks = ageInTicks;
            this.headPitch = headPitch;
            this.netHeadYaw = netHeadYaw;
        }
    }
}
