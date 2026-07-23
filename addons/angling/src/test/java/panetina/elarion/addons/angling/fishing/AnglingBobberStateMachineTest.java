package panetina.elarion.addons.angling.fishing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingBobberStateMachineTest {
    @Test
    void biteCannotStartUntilStrictlyAfterMinimum() {
        var machine = new AnglingBobberStateMachine(2, 10, 1.0, false);
        assertEquals(AnglingBobberStateMachine.Transition.STARTED_BOBBING,
                machine.tick(true, () -> 0.0));
        assertEquals(AnglingBobberStateMachine.Transition.NONE, machine.tick(true, () -> 0.0));
        assertEquals(AnglingBobberStateMachine.Transition.NONE, machine.tick(true, () -> 0.0));
        assertEquals(AnglingBobberStateMachine.Transition.BITE_STARTED, machine.tick(true, () -> 0.0));
        assertEquals(AnglingBobberState.BITING, machine.state());
    }

    @Test
    void maximumForcesBiteWithFrozenStrictInequalities() {
        var machine = new AnglingBobberStateMachine(1, 2, 0.0, false);
        machine.tick(true, () -> 0.9);
        machine.tick(true, () -> 0.9);
        machine.tick(true, () -> 0.9);
        assertEquals(AnglingBobberStateMachine.Transition.BITE_STARTED,
                machine.tick(true, () -> 0.9));
        assertEquals(3, machine.ticksInFluid());
    }

    @Test
    void biteExpiresOnlyAfterEightyTickWindow() {
        var machine = bitingMachine();
        for (int tick = 1; tick <= AnglingBobberStateMachine.BITE_WINDOW_TICKS; tick++) {
            assertEquals(AnglingBobberStateMachine.Transition.NONE, machine.tick(true, () -> 0.9));
        }
        assertFalse(machine.biteExpired());
        assertEquals(AnglingBobberStateMachine.Transition.BITE_EXPIRED,
                machine.tick(true, () -> 0.9));
        assertTrue(machine.biteExpired());
        assertFalse(machine.reel());
    }

    @Test
    void reelTransitionsBitingToFishingOnce() {
        var machine = bitingMachine();
        assertTrue(machine.reel());
        assertEquals(AnglingBobberState.FISHING, machine.state());
        assertFalse(machine.reel());
    }

    @Test
    void voidBobberBeginsBobbingAfterFiftyFlightTicks() {
        var machine = new AnglingBobberStateMachine(1, 2, 0, true);
        for (int tick = 1; tick <= AnglingBobberStateMachine.VOID_FLIGHT_TICKS; tick++) {
            assertEquals(AnglingBobberStateMachine.Transition.NONE, machine.tick(false, () -> 0.9));
        }
        assertEquals(AnglingBobberStateMachine.Transition.STARTED_BOBBING,
                machine.tick(false, () -> 0.9));
    }

    @Test
    void ordinaryBobberReturnsToFlyingWhenFluidEnds() {
        var machine = new AnglingBobberStateMachine(1, 2, 0, false);
        machine.tick(true, () -> 0.9);
        assertEquals(AnglingBobberStateMachine.Transition.STARTED_FLYING,
                machine.tick(false, () -> 0.9));
        assertEquals(AnglingBobberState.FLYING, machine.state());
    }

    @Test
    void rejectsUnboundedOrInvalidTiming() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnglingBobberStateMachine(2, 1, 0, false));
        assertThrows(IllegalArgumentException.class,
                () -> new AnglingBobberStateMachine(1, 2, Double.NaN, false));
    }

    private static AnglingBobberStateMachine bitingMachine() {
        var machine = new AnglingBobberStateMachine(1, 2, 1, false);
        machine.tick(true, () -> 0.0);
        machine.tick(true, () -> 0.0);
        machine.tick(true, () -> 0.0);
        assertEquals(AnglingBobberState.BITING, machine.state());
        return machine;
    }
}
