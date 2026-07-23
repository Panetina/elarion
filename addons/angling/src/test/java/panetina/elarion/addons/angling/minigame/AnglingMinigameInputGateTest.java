package panetina.elarion.addons.angling.minigame;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.network.AnglingMinigameInputAction;
import panetina.elarion.addons.angling.network.AnglingMinigameInputPayload;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingMinigameInputGateTest {
    private final UUID session = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();
    private final AnglingMinigameInputGate gate = new AnglingMinigameInputGate(session, actor, 9, 100, 200);

    @Test
    void acceptsOnlyOrderedPhysicalEdges() {
        assertEquals(AnglingMinigameInputGate.Result.ACCEPTED, accept(0, AnglingMinigameInputAction.PRESS, 101));
        assertTrue(gate.isPressed());
        assertEquals(AnglingMinigameInputGate.Result.IMPOSSIBLE_TRANSITION,
                accept(1, AnglingMinigameInputAction.PRESS, 101));
        assertEquals(AnglingMinigameInputGate.Result.ACCEPTED,
                accept(1, AnglingMinigameInputAction.RELEASE, 102));
        assertFalse(gate.isPressed());
        assertEquals(1, gate.lastSequence());
    }

    @Test
    void rejectsReplayGapAndWrongOwnership() {
        assertEquals(AnglingMinigameInputGate.Result.WRONG_ACTOR,
                gate.accept(UUID.randomUUID(), payload(session, 9, 0, AnglingMinigameInputAction.PRESS), 101));
        assertEquals(AnglingMinigameInputGate.Result.WRONG_SESSION,
                gate.accept(actor, payload(UUID.randomUUID(), 9, 0, AnglingMinigameInputAction.PRESS), 101));
        assertEquals(AnglingMinigameInputGate.Result.WRONG_BOBBER,
                gate.accept(actor, payload(session, 10, 0, AnglingMinigameInputAction.PRESS), 101));
        assertEquals(AnglingMinigameInputGate.Result.OUT_OF_ORDER,
                accept(2, AnglingMinigameInputAction.PRESS, 101));
        assertEquals(AnglingMinigameInputGate.Result.ACCEPTED,
                accept(0, AnglingMinigameInputAction.PRESS, 101));
        assertEquals(AnglingMinigameInputGate.Result.REPLAYED,
                accept(0, AnglingMinigameInputAction.PRESS, 102));
    }

    @Test
    void boundsPerTickInputAndAllowsRetryOnLaterTick() {
        for (int sequence = 0; sequence < 4; sequence++) {
            AnglingMinigameInputAction action = sequence % 2 == 0
                    ? AnglingMinigameInputAction.PRESS : AnglingMinigameInputAction.RELEASE;
            assertEquals(AnglingMinigameInputGate.Result.ACCEPTED, accept(sequence, action, 101));
        }
        assertEquals(AnglingMinigameInputGate.Result.RATE_LIMITED,
                accept(4, AnglingMinigameInputAction.PRESS, 101));
        assertEquals(AnglingMinigameInputGate.Result.ACCEPTED,
                accept(4, AnglingMinigameInputAction.PRESS, 102));
    }

    @Test
    void slidingWindowForgetsOnlyExpiredBuckets() {
        int sequence = 0;
        for (long tick = 101; tick <= 110; tick++) {
            for (int edge = 0; edge < 4; edge++) {
                AnglingMinigameInputAction action = sequence % 2 == 0
                        ? AnglingMinigameInputAction.PRESS : AnglingMinigameInputAction.RELEASE;
                assertEquals(AnglingMinigameInputGate.Result.ACCEPTED, accept(sequence++, action, tick));
            }
        }
        assertEquals(AnglingMinigameInputGate.Result.RATE_LIMITED,
                accept(sequence, AnglingMinigameInputAction.PRESS, 111));
        assertEquals(AnglingMinigameInputGate.Result.ACCEPTED,
                accept(sequence, AnglingMinigameInputAction.PRESS, 121));
    }

    @Test
    void abandonAndExpiryAreTerminal() {
        assertEquals(AnglingMinigameInputGate.Result.ACCEPTED,
                accept(0, AnglingMinigameInputAction.ABANDON, 101));
        assertTrue(gate.isClosed());
        assertEquals(AnglingMinigameInputGate.Result.CLOSED,
                accept(1, AnglingMinigameInputAction.PRESS, 102));

        AnglingMinigameInputGate expiring = new AnglingMinigameInputGate(session, actor, 9, 10, 2);
        assertEquals(AnglingMinigameInputGate.Result.EXPIRED,
                expiring.accept(actor, payload(session, 9, 0, AnglingMinigameInputAction.PRESS), 13));
        assertTrue(expiring.isClosed());
    }

    private AnglingMinigameInputGate.Result accept(int sequence, AnglingMinigameInputAction action, long tick) {
        return gate.accept(actor, payload(session, 9, sequence, action), tick);
    }

    private static AnglingMinigameInputPayload payload(
            UUID session, int bobber, int sequence, AnglingMinigameInputAction action
    ) {
        return new AnglingMinigameInputPayload(session, bobber, sequence, action);
    }
}
