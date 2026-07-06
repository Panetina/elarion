package panetina.elarion.addons.mounts.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;
import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.addons.mounts.model.GeoModelDefinition;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.HashMap;
import java.util.Map;

public final class ElarionMountEntityRenderer extends GeoEntityRenderer<ElarionMountEntity> {
    private static final Identifier EMPTY_TEXTURE =
            Identifier.of("minecraft", "textures/misc/white.png");
    private static final double BLOCKBENCH_PIXEL_SCALE = 1.0D / 16.0D;
    private static final float VISUAL_YAW_RESPONSE = 0.18F;
    private static final float VISUAL_YAW_MAX_STEP = 3.0F;
    private final Map<Integer, VisualYawState> visualStates = new HashMap<>();

    public ElarionMountEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ElarionMountGeoModel());
        shadowRadius = 0.75F;
        addRenderLayer(new ElarionMountRiderLayer(this));
    }

    @Override
    public void render(
            ElarionMountEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {
        GeoModelDefinition model = GeoModelCache.forType(entity.mountType());
        if (model != null) {
            VisualYawState visual = visualState(entity, yaw);
            super.render(entity, visual.visualYaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public void preRender(
            MatrixStack poseStack,
            ElarionMountEntity animatable,
            BakedGeoModel model,
            VertexConsumerProvider bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int renderColor
    ) {
        GeoModelDefinition definition = GeoModelCache.forType(animatable.mountType());
        if (definition != null) {
            GeoModelDefinition.Vec3 anchor = staticPassengerAnchor(animatable.mountType(), definition);
            float scale = animatable.mountType().renderScale();
            poseStack.scale(scale, scale, scale);
            poseStack.translate(
                    -anchor.x() * BLOCKBENCH_PIXEL_SCALE,
                    -anchor.y() * BLOCKBENCH_PIXEL_SCALE,
                    -anchor.z() * BLOCKBENCH_PIXEL_SCALE);
        }
        super.preRender(
                poseStack,
                animatable,
                model,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                renderColor);
    }

    @Override
    public Identifier getTexture(ElarionMountEntity entity) {
        GeoModelDefinition model = GeoModelCache.forType(entity.mountType());
        return model == null ? EMPTY_TEXTURE : model.texture();
    }

    @Override
    public RenderLayer getRenderType(
            ElarionMountEntity animatable,
            Identifier texture,
            VertexConsumerProvider bufferSource,
            float partialTick
    ) {
        return RenderLayer.getEntityCutoutNoCull(texture);
    }

    private GeoModelDefinition.Vec3 staticPassengerAnchor(ElarionMountType type, GeoModelDefinition model) {
        GeoModelDefinition.Vec3 anchor = model.passengerAnchor();
        return new GeoModelDefinition.Vec3(
                type.renderAnchorX(anchor.x()),
                anchor.y(),
                type.renderAnchorZ(anchor.z()));
    }

    private VisualYawState visualState(ElarionMountEntity entity, float yaw) {
        VisualYawState state = visualStates.computeIfAbsent(
                entity.getId(),
                id -> new VisualYawState(yaw));
        state.updateYaw(yaw);
        return state;
    }

    private static final class VisualYawState {
        private float visualYaw;

        private VisualYawState(float visualYaw) {
            this.visualYaw = visualYaw;
        }

        private void updateYaw(float targetYaw) {
            float delta = wrapDegrees(targetYaw - visualYaw);
            float step = Math.max(-VISUAL_YAW_MAX_STEP, Math.min(VISUAL_YAW_MAX_STEP, delta * VISUAL_YAW_RESPONSE));
            if (Math.abs(step) < 0.01F) {
                return;
            }
            visualYaw = wrapDegrees(visualYaw + step);
        }

        private float wrapDegrees(float value) {
            float wrapped = value % 360.0F;
            if (wrapped >= 180.0F) wrapped -= 360.0F;
            if (wrapped < -180.0F) wrapped += 360.0F;
            return wrapped;
        }
    }

}
