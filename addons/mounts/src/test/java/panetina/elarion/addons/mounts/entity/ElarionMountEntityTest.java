package panetina.elarion.addons.mounts.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElarionMountEntityTest {
    @Test
    void verticalInputMapsToMatchingAnimationOverlay() {
        assertEquals("ascend", ElarionMountAnimationLogic.verticalOverlayForInputs(false, true));
        assertEquals("descend", ElarionMountAnimationLogic.verticalOverlayForInputs(true, false));
        assertEquals("ascend", ElarionMountAnimationLogic.verticalOverlayForInputs(true, true));
        assertEquals("none", ElarionMountAnimationLogic.verticalOverlayForInputs(false, false));
    }

    @Test
    void verticalIntentMatchesControlDirection() {
        assertEquals(1.0D, ElarionMountAnimationLogic.verticalIntentForInputs(false, true));
        assertEquals(-1.0D, ElarionMountAnimationLogic.verticalIntentForInputs(true, false));
        assertEquals(1.0D, ElarionMountAnimationLogic.verticalIntentForInputs(true, true));
        assertEquals(0.0D, ElarionMountAnimationLogic.verticalIntentForInputs(false, false));
    }

    @Test
    void smoothingMovesTowardTargetWithoutSnapping() {
        double first = ElarionMountAnimationLogic.smoothToward(0.0D, 1.0D, 0.25D);
        double second = ElarionMountAnimationLogic.smoothToward(first, 1.0D, 0.25D);

        assertTrue(first > 0.0D && first < 1.0D);
        assertTrue(second > first && second < 1.0D);
        assertEquals(1.0D, ElarionMountAnimationLogic.smoothToward(0.0D, 1.0D, 1.0D));
    }

    @Test
    void holdingForwardKeepsMovementIntentAndSpeed() {
        ElarionMountFlightController.State state = ElarionMountFlightController.State.zero();
        ElarionMountType.MovementProfile profile = ElarionMountType.SCIFI_BIKE.movementProfile();
        ElarionMountFlightController.Step step = null;
        for (int tick = 0; tick < 200; tick++) {
            step = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(1.0F, 0.0F, false, false, false, 0.0F),
                    profile,
                    0.0F);
            state = step.state();
        }

        assertTrue(state.smoothedForwardIntent() > 0.95D);
        assertTrue(state.flightSpeed() > profile.maxForwardSpeed() * 0.8D);
        assertEquals("walk", step.baseAnimation());
    }

    @Test
    void releasingForwardDecaysMovement() {
        ElarionMountFlightController.State state = ElarionMountFlightController.State.zero();
        ElarionMountType.MovementProfile profile = ElarionMountType.BEE.movementProfile();
        for (int tick = 0; tick < 60; tick++) {
            state = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(1.0F, 0.0F, false, false, false, 0.0F),
                    profile,
                    0.0F).state();
        }
        double speedBeforeRelease = state.flightSpeed();
        for (int tick = 0; tick < 120; tick++) {
            state = ElarionMountFlightController.step(
                    state,
                    ElarionMountFlightInput.neutral(),
                    profile,
                    0.0F).state();
        }

        assertTrue(state.smoothedForwardIntent() < 0.05D);
        assertTrue(state.flightSpeed() < speedBeforeRelease * 0.25D);
    }

    @Test
    void holdingJumpKeepsVerticalIntentAndSpeed() {
        ElarionMountFlightController.State state = ElarionMountFlightController.State.zero();
        ElarionMountType.MovementProfile profile = ElarionMountType.CHINESE_DRAGON.movementProfile();
        ElarionMountFlightController.Step step = null;
        for (int tick = 0; tick < 200; tick++) {
            step = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(0.0F, 0.0F, true, false, false, 0.0F),
                    profile,
                    0.0F);
            state = step.state();
        }

        assertTrue(state.smoothedVerticalIntent() > 0.95D);
        assertTrue(state.verticalSpeed() > profile.ascendSpeed() * 0.8D);
        assertEquals("ascend", step.overlayAnimation());
    }

    @Test
    void turnInputTriggersLeanOverlay() {
        ElarionMountFlightController.State state = ElarionMountFlightController.State.zero();
        ElarionMountType.MovementProfile profile = ElarionMountType.WYVERN.movementProfile();
        ElarionMountFlightController.Step right = null;
        for (int tick = 0; tick < 60; tick++) {
            right = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(1.0F, 0.0F, false, false, false, 1.0F),
                    profile,
                    0.0F);
            state = right.state();
        }

        assertEquals("lean_right", right.overlayAnimation());

        ElarionMountFlightController.Step left = null;
        for (int tick = 0; tick < 100; tick++) {
            left = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(1.0F, 0.0F, false, false, false, -1.0F),
                    profile,
                    0.0F);
            state = left.state();
        }

        assertEquals("lean_left", left.overlayAnimation());
    }

    @Test
    void verticalAndTurnInputsComposeOverlays() {
        ElarionMountFlightController.State state = ElarionMountFlightController.State.zero();
        ElarionMountType.MovementProfile profile = ElarionMountType.CHINESE_DRAGON.movementProfile();
        ElarionMountFlightController.Step ascendingTurn = null;
        for (int tick = 0; tick < 80; tick++) {
            ascendingTurn = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(1.0F, 0.0F, true, false, false, 1.0F),
                    profile,
                    0.0F);
            state = ascendingTurn.state();
        }

        assertEquals("ascend+lean_right", ascendingTurn.overlayAnimation());

        ElarionMountFlightController.Step descendingTurn = null;
        for (int tick = 0; tick < 120; tick++) {
            descendingTurn = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(1.0F, 0.0F, false, true, false, -1.0F),
                    profile,
                    0.0F);
            state = descendingTurn.state();
        }

        assertEquals("descend+lean_left", descendingTurn.overlayAnimation());
    }

    @Test
    void releasingJumpEasesVerticalSpeedTowardZero() {
        ElarionMountFlightController.State state = ElarionMountFlightController.State.zero();
        ElarionMountType.MovementProfile profile = ElarionMountType.WYVERN.movementProfile();
        for (int tick = 0; tick < 80; tick++) {
            state = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(0.0F, 0.0F, true, false, false, 0.0F),
                    profile,
                    0.0F).state();
        }
        double speedBeforeRelease = state.verticalSpeed();
        for (int tick = 0; tick < 80; tick++) {
            state = ElarionMountFlightController.step(
                    state,
                    ElarionMountFlightInput.neutral(),
                    profile,
                    0.0F).state();
        }

        assertTrue(speedBeforeRelease > 0.0D);
        assertTrue(Math.abs(state.verticalSpeed()) < speedBeforeRelease * 0.25D);
    }

    @Test
    void boostRequiresBoostInputAndForwardMovement() {
        ElarionMountFlightController.State state = ElarionMountFlightController.State.zero();
        ElarionMountType.MovementProfile profile = ElarionMountType.AIRSHIP.movementProfile();
        for (int tick = 0; tick < 80; tick++) {
            state = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(1.0F, 0.0F, true, false, false, 0.0F),
                    profile,
                    0.0F).state();
        }
        assertEquals(0.0D, state.smoothedBoostIntent(), 0.001D);

        ElarionMountFlightController.Step boosted = null;
        for (int tick = 0; tick < 80; tick++) {
            boosted = ElarionMountFlightController.step(
                    state,
                    new ElarionMountFlightInput(1.0F, 0.0F, false, false, true, 0.0F),
                    profile,
                    0.0F);
            state = boosted.state();
        }

        assertTrue(state.smoothedBoostIntent() > 0.95D);
        assertTrue(boosted.boostActive());
    }
}
