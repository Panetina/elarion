package panetina.elarion.addons.mounts.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import panetina.elarion.addons.mounts.mixin.LivingEntityRendererAccessor;
import panetina.elarion.addons.mounts.mixin.PlayerEntityRendererAccessor;

import java.util.List;

final class ElarionMountRiderStatueRenderer {
    private static final int WHITE = 0xFFFFFFFF;
    private static final float SEATED_ARM_PITCH = -0.62831855F;
    private static final float SEATED_ARM_YAW = 0.08F;
    private static final float SEATED_ARM_ROLL = 0.04F;
    private static final float SEATED_LEG_PITCH = -1.4137167F;
    private static final float SEATED_LEG_YAW = 0.31415927F;
    private static final float SEATED_LEG_ROLL = 0.07853982F;

    private ElarionMountRiderStatueRenderer() {
    }

    static void render(
            Entity passenger,
            MatrixStack poseStack,
            VertexConsumerProvider bufferSource,
            int packedLight,
            float partialTick
    ) {
        if (!(passenger instanceof AbstractClientPlayerEntity player)) {
            return;
        }
        EntityRenderer<? super AbstractClientPlayerEntity> renderer =
                MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof PlayerEntityRenderer playerRenderer)) {
            return;
        }

        ElarionMountRiderRenderContext.renderMountedRider(() -> renderStaticPlayer(
                player,
                playerRenderer,
                poseStack,
                bufferSource,
                packedLight,
                partialTick));
    }

    private static void renderStaticPlayer(
            AbstractClientPlayerEntity player,
            PlayerEntityRenderer renderer,
            MatrixStack poseStack,
            VertexConsumerProvider bufferSource,
            int packedLight,
            float partialTick
    ) {
        PlayerEntityModel<AbstractClientPlayerEntity> model = renderer.getModel();
        PlayerRenderStateSnapshot snapshot = PlayerRenderStateSnapshot.capture(player);
        try {
            snapshot.applyFrozen(player);
            applyStaticPose(model);

            poseStack.push();
            poseStack.translate(0.0D, 1.5D, 0.0D);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            VertexConsumer consumer = bufferSource.getBuffer(model.getLayer(renderer.getTexture(player)));
            model.render(poseStack, consumer, packedLight, OverlayTexture.DEFAULT_UV, WHITE);
            renderFeatures(player, renderer, model, poseStack, bufferSource, packedLight);
            poseStack.pop();

            if (MinecraftClient.getInstance().player != player) {
                ((PlayerEntityRendererAccessor) renderer).elarionMounts$renderLabelIfPresent(
                        player,
                        player.getDisplayName(),
                        poseStack,
                        bufferSource,
                        packedLight,
                        partialTick);
            }
        } finally {
            snapshot.restore(player);
            applyStaticPose(model);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void renderFeatures(
            AbstractClientPlayerEntity player,
            PlayerEntityRenderer renderer,
            PlayerEntityModel<AbstractClientPlayerEntity> model,
            MatrixStack poseStack,
            VertexConsumerProvider bufferSource,
            int packedLight
    ) {
        List<FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>> features =
                ((LivingEntityRendererAccessor<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) renderer)
                        .elarionMounts$features();
        for (FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> feature : features) {
            applyStaticPose(model);
            feature.render(poseStack, bufferSource, packedLight, player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    private static void applyStaticPose(PlayerEntityModel<AbstractClientPlayerEntity> model) {
        model.setVisible(true);
        model.child = false;
        model.riding = true;
        model.handSwingProgress = 0.0F;
        model.sneaking = false;
        model.leaningPitch = 0.0F;
        model.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
        model.rightArmPose = BipedEntityModel.ArmPose.EMPTY;

        resetPart(model.head);
        resetPart(model.hat);
        resetPart(model.body);
        setArm(model.rightArm, -SEATED_ARM_YAW, SEATED_ARM_ROLL);
        setArm(model.leftArm, SEATED_ARM_YAW, -SEATED_ARM_ROLL);
        setLeg(model.rightLeg, SEATED_LEG_YAW, SEATED_LEG_ROLL);
        setLeg(model.leftLeg, -SEATED_LEG_YAW, -SEATED_LEG_ROLL);

        model.rightSleeve.copyTransform(model.rightArm);
        model.leftSleeve.copyTransform(model.leftArm);
        model.rightPants.copyTransform(model.rightLeg);
        model.leftPants.copyTransform(model.leftLeg);
        model.jacket.copyTransform(model.body);
    }

    private static void resetPart(ModelPart part) {
        part.pitch = 0.0F;
        part.yaw = 0.0F;
        part.roll = 0.0F;
    }

    private static void setArm(ModelPart part, float yaw, float roll) {
        part.pitch = SEATED_ARM_PITCH;
        part.yaw = yaw;
        part.roll = roll;
    }

    private static void setLeg(ModelPart part, float yaw, float roll) {
        part.pitch = SEATED_LEG_PITCH;
        part.yaw = yaw;
        part.roll = roll;
    }

    private record PlayerRenderStateSnapshot(
            float yaw,
            float pitch,
            float bodyYaw,
            float headYaw
    ) {
        private static PlayerRenderStateSnapshot capture(AbstractClientPlayerEntity player) {
            return new PlayerRenderStateSnapshot(
                    player.getYaw(),
                    player.getPitch(),
                    player.bodyYaw,
                    player.headYaw);
        }

        private void applyFrozen(AbstractClientPlayerEntity player) {
            player.setYaw(0.0F);
            player.setPitch(0.0F);
            player.bodyYaw = 0.0F;
            player.headYaw = 0.0F;
        }

        private void restore(AbstractClientPlayerEntity player) {
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.bodyYaw = bodyYaw;
            player.headYaw = headYaw;
        }
    }
}
