package panetina.elarion.addons.mounts.entity;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

final class ElarionMountFlightController {
    static final float LEAN_DEADZONE_TURN_INTENT = 0.08F;
    private static final double FORWARD_INPUT_RESPONSE = 0.34D;
    private static final double TURN_INPUT_RESPONSE = 0.26D;
    private static final double VERTICAL_INPUT_RESPONSE = 0.30D;
    private static final double BOOST_INPUT_RESPONSE = 0.22D;
    private static final double VERTICAL_SPEED_RESPONSE = 0.24D;
    private static final double VERTICAL_STOP_RESPONSE = 0.16D;
    private static final double VERTICAL_RELEASE_DEADZONE = 0.18D;

    private ElarionMountFlightController() {
    }

    static Step step(State state, ElarionMountFlightInput input, ElarionMountType.MovementProfile profile, float currentYaw) {
        double forwardIntent = ElarionMountAnimationLogic.smoothToward(
                state.smoothedForwardIntent(),
                input.forward(),
                FORWARD_INPUT_RESPONSE);
        double turnIntent = ElarionMountAnimationLogic.smoothToward(
                state.smoothedTurnIntent(),
                input.turnIntent(),
                TURN_INPUT_RESPONSE);
        double verticalIntent = ElarionMountAnimationLogic.smoothToward(
                state.smoothedVerticalIntent(),
                ElarionMountAnimationLogic.verticalIntentForInputs(input.sneak(), input.jump()),
                VERTICAL_INPUT_RESPONSE);
        double boostIntent = ElarionMountAnimationLogic.smoothToward(
                state.smoothedBoostIntent(),
                input.boost() && forwardIntent > 0.05D ? 1.0D : 0.0D,
                BOOST_INPUT_RESPONSE);

        float yawStep = MathHelper.clamp(
                (float) turnIntent * (float) profile.turnResponse(),
                (float) -profile.turnDegrees(),
                (float) profile.turnDegrees());
        float nextYaw = MathHelper.wrapDegrees(currentYaw + yawStep);

        double flightSpeed = state.flightSpeed();
        if (forwardIntent > 0.05D) {
            double maxSpeed = lerp(profile.maxForwardSpeed(), profile.boostedForwardSpeed(), boostIntent);
            double acceleration = profile.acceleration() * lerp(1.0D, 1.18D, boostIntent);
            flightSpeed = Math.min(maxSpeed, flightSpeed + acceleration * forwardIntent);
        } else if (forwardIntent < -0.05D) {
            flightSpeed = Math.max(profile.maxReverseSpeed(), flightSpeed + profile.brake() * forwardIntent);
        } else {
            flightSpeed *= profile.horizontalDrag();
            if (Math.abs(flightSpeed) < 0.01D) {
                flightSpeed = 0.0D;
            }
        }

        double verticalTarget = verticalTarget(verticalIntent, profile);
        double verticalResponse = verticalTarget == 0.0D ? VERTICAL_STOP_RESPONSE : VERTICAL_SPEED_RESPONSE;
        double verticalSpeed = ElarionMountAnimationLogic.smoothToward(
                state.verticalSpeed(),
                verticalTarget,
                verticalResponse);
        if (verticalTarget == 0.0D) {
            verticalSpeed *= profile.verticalDrag();
            if (Math.abs(verticalSpeed) < 0.01D) {
                verticalSpeed = 0.0D;
            }
        }

        double yawRadians = Math.toRadians(nextYaw);
        Vec3d horizontalVelocity = new Vec3d(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians)).multiply(flightSpeed);
        boolean boostActive = boostIntent > 0.12D && forwardIntent > 0.05D;
        String baseAnimation = Math.abs(flightSpeed) > 0.025D ? "walk" : "idle";
        String overlayAnimation = combineOverlays(
                verticalOverlay(verticalIntent),
                leanOverlay(turnIntent, boostActive));

        State next = new State(
                flightSpeed,
                verticalSpeed,
                forwardIntent,
                turnIntent,
                verticalIntent,
                boostIntent);
        return new Step(
                next,
                yawStep,
                horizontalVelocity,
                verticalSpeed,
                boostActive,
                baseAnimation,
                overlayAnimation,
                (float) turnIntent);
    }

    static double verticalTarget(double verticalIntent, ElarionMountType.MovementProfile profile) {
        if (verticalIntent > VERTICAL_RELEASE_DEADZONE) {
            return profile.ascendSpeed() * Math.min(1.0D, verticalIntent);
        }
        if (verticalIntent < -VERTICAL_RELEASE_DEADZONE) {
            return -profile.descendSpeed() * Math.min(1.0D, -verticalIntent);
        }
        return 0.0D;
    }

    private static String verticalOverlay(double verticalIntent) {
        if (verticalIntent > VERTICAL_RELEASE_DEADZONE) {
            return "ascend";
        }
        if (verticalIntent < -VERTICAL_RELEASE_DEADZONE) {
            return "descend";
        }
        return "none";
    }

    private static String leanOverlay(double turnIntent, boolean boostActive) {
        float leanDeadzone = boostActive ? LEAN_DEADZONE_TURN_INTENT * 1.7F : LEAN_DEADZONE_TURN_INTENT;
        if (turnIntent > leanDeadzone) {
            return "lean_right";
        }
        if (turnIntent < -leanDeadzone) {
            return "lean_left";
        }
        return "none";
    }

    private static String combineOverlays(String verticalOverlay, String leanOverlay) {
        boolean hasVertical = verticalOverlay != null && !verticalOverlay.equals("none");
        boolean hasLean = leanOverlay != null && !leanOverlay.equals("none");
        if (hasVertical && hasLean) {
            return verticalOverlay + "+" + leanOverlay;
        }
        if (hasVertical) {
            return verticalOverlay;
        }
        return hasLean ? leanOverlay : "none";
    }

    private static double lerp(double start, double end, double amount) {
        double clamped = Math.max(0.0D, Math.min(1.0D, amount));
        return start + (end - start) * clamped;
    }

    record State(
            double flightSpeed,
            double verticalSpeed,
            double smoothedForwardIntent,
            double smoothedTurnIntent,
            double smoothedVerticalIntent,
            double smoothedBoostIntent
    ) {
        static State zero() {
            return new State(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    record Step(
            State state,
            float yawStep,
            Vec3d horizontalVelocity,
            double verticalSpeed,
            boolean boostActive,
            String baseAnimation,
            String overlayAnimation,
            float turnIntent
    ) {
    }
}
