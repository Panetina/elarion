package panetina.elarion.addons.mounts.client;

import panetina.elarion.addons.mounts.entity.ElarionMountEntity;
import panetina.elarion.addons.mounts.model.GeoModelDefinition;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class ElarionMountPoseBlender {
    private static final double WALK_RESPONSE = 0.22D;
    private static final double LEAN_RESPONSE = 0.34D;
    private static final double VERTICAL_RESPONSE = 0.36D;
    private static final double BOOST_RESPONSE = 0.18D;

    private final Map<Integer, State> states = new HashMap<>();

    void apply(ElarionMountGeoModel model, ElarionMountEntity mount, AnimationState<ElarionMountEntity> animationState) {
        GeoModelDefinition definition = GeoModelCache.forType(mount.mountType());
        if (definition == null) {
            return;
        }
        State state = states.computeIfAbsent(mount.getId(), ignored -> new State(mount.age + animationState.getPartialTick()));
        double frameTick = mount.age + animationState.getPartialTick();
        state.updateTargets(mount, frameTick);

        double seconds = animationState.getAnimationTick() / 20.0D * mount.animationTimeScale();
        Set<String> bones = definition.animatedBoneNames("idle", "walk", "ascend", "descend", "lean_left", "lean_right");
        for (String boneName : bones) {
            model.getBone(boneName).ifPresent(bone -> applyBone(definition, bone, state, seconds));
        }
    }

    private void applyBone(GeoModelDefinition definition, GeoBone bone, State state, double seconds) {
        bone.saveInitialSnapshot();
        var initial = bone.getInitialSnapshot();
        String name = bone.getName();

        GeoModelDefinition.Vec3 idlePosition = definition.samplePosition("idle", name, seconds);
        GeoModelDefinition.Vec3 walkPosition = definition.samplePosition("walk", name, seconds);
        GeoModelDefinition.Vec3 position = lerp(idlePosition, walkPosition, state.walkWeight)
                .add(definition.samplePosition("lean_left", name, seconds).multiply(state.leanLeftWeight))
                .add(definition.samplePosition("lean_right", name, seconds).multiply(state.leanRightWeight))
                .add(definition.samplePosition("ascend", name, seconds).multiply(state.ascendWeight))
                .add(definition.samplePosition("descend", name, seconds).multiply(state.descendWeight));

        GeoModelDefinition.Vec3 idleRotation = definition.sampleRotation("idle", name, seconds);
        GeoModelDefinition.Vec3 walkRotation = definition.sampleRotation("walk", name, seconds);
        GeoModelDefinition.Vec3 rotation = lerp(idleRotation, walkRotation, state.walkWeight)
                .add(definition.sampleRotation("lean_left", name, seconds).multiply(state.leanLeftWeight))
                .add(definition.sampleRotation("lean_right", name, seconds).multiply(state.leanRightWeight))
                .add(definition.sampleRotation("ascend", name, seconds).multiply(state.ascendWeight))
                .add(definition.sampleRotation("descend", name, seconds).multiply(state.descendWeight));

        GeoModelDefinition.Vec3 idleScale = definition.sampleScale("idle", name, seconds);
        GeoModelDefinition.Vec3 walkScale = definition.sampleScale("walk", name, seconds);
        GeoModelDefinition.Vec3 scale = lerp(idleScale, walkScale, state.walkWeight)
                .add(definition.sampleScale("lean_left", name, seconds).subtract(GeoModelDefinition.Vec3.ONE).multiply(state.leanLeftWeight))
                .add(definition.sampleScale("lean_right", name, seconds).subtract(GeoModelDefinition.Vec3.ONE).multiply(state.leanRightWeight))
                .add(definition.sampleScale("ascend", name, seconds).subtract(GeoModelDefinition.Vec3.ONE).multiply(state.ascendWeight))
                .add(definition.sampleScale("descend", name, seconds).subtract(GeoModelDefinition.Vec3.ONE).multiply(state.descendWeight));

        bone.updatePosition((float) position.x(), (float) position.y(), (float) position.z());
        bone.updateRotation(
                initial.getRotX() + rotationRadiansX(rotation),
                initial.getRotY() + rotationRadiansY(rotation),
                initial.getRotZ() + rotationRadiansZ(rotation));
        bone.updateScale(
                Math.max(0.01F, (float) scale.x()),
                Math.max(0.01F, (float) scale.y()),
                Math.max(0.01F, (float) scale.z()));
    }

    private static float rotationRadiansX(GeoModelDefinition.Vec3 degrees) {
        return (float) Math.toRadians(-degrees.x());
    }

    private static float rotationRadiansY(GeoModelDefinition.Vec3 degrees) {
        return (float) Math.toRadians(-degrees.y());
    }

    private static float rotationRadiansZ(GeoModelDefinition.Vec3 degrees) {
        return (float) Math.toRadians(degrees.z());
    }

    private static GeoModelDefinition.Vec3 lerp(
            GeoModelDefinition.Vec3 start,
            GeoModelDefinition.Vec3 end,
            double amount
    ) {
        double clamped = clamp01(amount);
        return new GeoModelDefinition.Vec3(
                start.x() + (end.x() - start.x()) * clamped,
                start.y() + (end.y() - start.y()) * clamped,
                start.z() + (end.z() - start.z()) * clamped);
    }

    private static double smooth(double current, double target, double response, double tickDelta) {
        double clampedResponse = clamp01(response);
        double amount = 1.0D - Math.pow(1.0D - clampedResponse, Math.max(0.0D, tickDelta));
        return current + (target - current) * amount;
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static final class State {
        private double lastFrameTick;
        private double walkWeight;
        private double leanLeftWeight;
        private double leanRightWeight;
        private double ascendWeight;
        private double descendWeight;
        private double boostWeight;

        private State(double lastFrameTick) {
            this.lastFrameTick = lastFrameTick;
        }

        private void updateTargets(ElarionMountEntity mount, double frameTick) {
            double tickDelta = Math.min(3.0D, Math.max(0.0D, frameTick - lastFrameTick));
            lastFrameTick = frameTick;

            String overlay = mount.overlayAnimation();
            walkWeight = smooth(walkWeight, mount.baseAnimation().equals("walk") ? 1.0D : 0.0D, WALK_RESPONSE, tickDelta);
            leanLeftWeight = smooth(leanLeftWeight, overlayContains(overlay, "lean_left") ? 1.0D : 0.0D, LEAN_RESPONSE, tickDelta);
            leanRightWeight = smooth(leanRightWeight, overlayContains(overlay, "lean_right") ? 1.0D : 0.0D, LEAN_RESPONSE, tickDelta);
            ascendWeight = smooth(ascendWeight, overlayContains(overlay, "ascend") ? 1.0D : 0.0D, VERTICAL_RESPONSE, tickDelta);
            descendWeight = smooth(descendWeight, overlayContains(overlay, "descend") ? 1.0D : 0.0D, VERTICAL_RESPONSE, tickDelta);
            boostWeight = smooth(boostWeight, mount.isBoosting() ? 1.0D : 0.0D, BOOST_RESPONSE, tickDelta);
        }

        private static boolean overlayContains(String overlay, String animation) {
            if (overlay == null || overlay.isBlank() || overlay.equals("none")) {
                return false;
            }
            for (String part : overlay.split("\\+")) {
                if (part.trim().equals(animation)) {
                    return true;
                }
            }
            return false;
        }
    }
}
