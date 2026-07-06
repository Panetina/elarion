package panetina.elarion.addons.mounts.client;

import net.minecraft.client.option.Perspective;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import panetina.elarion.addons.mounts.entity.ElarionMountEntity;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

public final class ElarionMountRiderLayer extends GeoRenderLayer<ElarionMountEntity> {
    public ElarionMountRiderLayer(GeoRenderer<ElarionMountEntity> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(
            MatrixStack poseStack,
            ElarionMountEntity animatable,
            GeoBone bone,
            RenderLayer renderType,
            VertexConsumerProvider bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        if (!bone.getName().equals("p_passenger") || animatable.getPassengerList().isEmpty()) {
            return;
        }
        for (Entity passenger : animatable.getPassengerList()) {
            if (isHiddenFirstPersonRider(passenger)) {
                continue;
            }
            poseStack.push();
            ElarionMountType.RiderSeatProfile profile = animatable.mountType().riderSeatProfile();
            float inverseScale = 1.0F / animatable.mountType().renderScale();
            poseStack.scale(inverseScale, inverseScale, inverseScale);
            poseStack.translate(profile.visualXOffset(), profile.visualYOffset(), profile.visualZOffset());
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(profile.visualYawOffset()));
            ElarionMountRiderStatueRenderer.render(
                    passenger,
                    poseStack,
                    bufferSource,
                    packedLight,
                    partialTick);
            poseStack.pop();
        }
    }

    private boolean isHiddenFirstPersonRider(Entity passenger) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player == passenger && client.options.getPerspective() == Perspective.FIRST_PERSON;
    }
}
