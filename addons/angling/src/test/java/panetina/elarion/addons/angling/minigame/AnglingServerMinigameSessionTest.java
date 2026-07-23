package panetina.elarion.addons.angling.minigame;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.network.AnglingMinigameInputAction;
import panetina.elarion.addons.angling.network.AnglingMinigameInputPayload;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingServerMinigameSessionTest {
    private final UUID sessionId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();

    @Test
    void seedProducesDeterministicServerLayout() {
        AnglingServerMinigameSpec spec = spec(100, 0, 5, 0, 20, List.of(
                spot(AnglingSweetspotBehaviorType.NORMAL, 20, 10),
                spot(AnglingSweetspotBehaviorType.AQUA, 20, 10)));
        var first = session(spec, 55L).snapshot();
        var second = session(spec, 55L).snapshot();
        var different = session(spec, 56L).snapshot();

        assertEquals(first.sweetspots(), second.sweetspots());
        assertNotEquals(first.sweetspots(), different.sweetspots());
    }

    @Test
    void clientCanSubmitInputsButCannotSubmitOutcome() {
        AnglingServerMinigameSession session = session(spec(100, 0, 5, 0, 20,
                List.of(spot(AnglingSweetspotBehaviorType.NORMAL, 512, 200))), 1L);

        assertEquals(AnglingMinigameInputGate.Result.ACCEPTED,
                session.acceptInput(actorId, payload(0, AnglingMinigameInputAction.PRESS), 101));
        assertEquals(220.0F, session.snapshot().progress());
        assertEquals(1, session.snapshot().totalHits());
        assertTrue(session.snapshot().perfect());

        for (long tick = 102; tick < 110 && session.status() == AnglingServerMinigameStatus.ACTIVE; tick++) {
            session.tick(tick);
        }
        assertEquals(AnglingServerMinigameStatus.SUCCEEDED, session.status());

        assertEquals(AnglingMinigameInputGate.Result.CLOSED,
                session.acceptInput(actorId, payload(1, AnglingMinigameInputAction.ABANDON), 110));
        assertEquals(AnglingServerMinigameStatus.SUCCEEDED, session.status());
    }

    @Test
    void missAndHoldRepeatAreComputedByServer() {
        AnglingServerMinigameSession miss = session(spec(100, 0, 7, 0, 20, List.of()), 1L);
        assertEquals(AnglingMinigameInputGate.Result.ACCEPTED,
                miss.acceptInput(actorId, payload(0, AnglingMinigameInputAction.PRESS), 101));
        assertEquals(13.0F, miss.snapshot().progress());
        assertFalse(miss.snapshot().perfect());
        assertEquals(0, miss.snapshot().consecutiveHits());

        AnglingServerMinigameSession hold = session(spec(10_000, 0, 0, 0, 20,
                List.of(spot(AnglingSweetspotBehaviorType.NORMAL, 512, 1))), 1L);
        hold.acceptInput(actorId, payload(0, AnglingMinigameInputAction.PRESS), 101);
        hold.tick(107);
        assertEquals(1, hold.snapshot().totalHits());
        hold.tick(108);
        assertEquals(2, hold.snapshot().totalHits());
    }

    @Test
    void layerSelectionIsBoundedAndServerOwned() {
        var layers = new AnglingNativeModifier.MultiLayer(2, "");
        AnglingServerMinigameSpec spec = new AnglingServerMinigameSpec(
                100, 0, 0, 0, 0, 20, false,
                List.of(layers), List.of(spot(AnglingSweetspotBehaviorType.NORMAL, 20, 1)));
        AnglingServerMinigameSession session = session(spec, 1L);

        session.acceptInput(actorId, payload(0, AnglingMinigameInputAction.LAYER_PREVIOUS), 101);
        assertEquals(0, session.snapshot().pointerLayer());
        session.acceptInput(actorId, payload(1, AnglingMinigameInputAction.LAYER_NEXT), 101);
        session.acceptInput(actorId, payload(2, AnglingMinigameInputAction.LAYER_NEXT), 101);
        session.acceptInput(actorId, payload(3, AnglingMinigameInputAction.LAYER_NEXT), 101);
        assertEquals(2, session.snapshot().pointerLayer());
        assertEquals(2, session.snapshot().maximumLayers());
    }

    @Test
    void onHitModifierIsBoundedRuntimeStateForLaterMisses() {
        var freeze = new AnglingNativeModifier.FreezeOnMiss(40, 10, "");
        var leaf = new AnglingServerMinigameSpec.Sweetspot(
                AnglingSweetspotBehaviorType.LEAF, texture(), 512, 1, false,
                0, 0, 0xff00ff00, List.of(freeze));
        AnglingServerMinigameSession session = session(new AnglingServerMinigameSpec(
                100, 10, 0, 0, 0, 20, false, List.of(), List.of(leaf)), 1L);

        session.acceptInput(actorId, payload(0, AnglingMinigameInputAction.PRESS), 101);
        assertEquals(10.0F, session.snapshot().pointerSpeed());
        session.acceptInput(actorId, payload(1, AnglingMinigameInputAction.RELEASE), 101);
        session.acceptInput(actorId, payload(2, AnglingMinigameInputAction.PRESS), 102);
        session.tick(103);
        assertTrue(session.snapshot().pointerSpeed() < 10.0F);
    }

    @Test
    void catchUpAndLifetimeAreBounded() {
        AnglingServerMinigameSession session = session(spec(100, 0, 0, 0, 20, List.of()), 1L);
        session.tick(121);
        assertEquals(AnglingServerMinigameStatus.EXPIRED, session.status());
        long revision = session.snapshot().revision();
        session.tick(122);
        assertEquals(revision, session.snapshot().revision());
    }

    @Test
    void equipmentRuntimeModifiersRemainServerComputed() {
        AnglingServerMinigameSession protectedFreeze = session(new AnglingServerMinigameSpec(
                100, 10, 0, 0, 0, 20, false,
                List.of(new AnglingNativeModifier.FreezeOnMiss(40, 10, ""),
                        new AnglingNativeModifier.PreventFrozen("")), List.of()), 1L);
        protectedFreeze.acceptInput(actorId, payload(0, AnglingMinigameInputAction.PRESS), 101);
        protectedFreeze.tick(102);
        assertEquals(10.0F, protectedFreeze.snapshot().pointerSpeed());

        AnglingServerMinigameSession flipping = session(new AnglingServerMinigameSpec(
                100, 0, 0, 0, 0, 20, false,
                List.of(new AnglingNativeModifier.FlipEveryHit("")),
                List.of(spot(AnglingSweetspotBehaviorType.NORMAL, 512, 1))), 1L);
        flipping.acceptInput(actorId, payload(0, AnglingMinigameInputAction.PRESS), 101);
        assertEquals(-1, flipping.snapshot().pointerRotation());

        AnglingServerMinigameSession treasure = session(new AnglingServerMinigameSpec(
                100, 0, 0, 0, 0, 20, true,
                List.of(new AnglingNativeModifier.SpawnTreasureOnHit(2, "")),
                List.of(spot(AnglingSweetspotBehaviorType.NORMAL, 512, 1))), 1L);
        treasure.acceptInput(actorId, payload(0, AnglingMinigameInputAction.PRESS), 101);
        treasure.acceptInput(actorId, payload(1, AnglingMinigameInputAction.RELEASE), 101);
        treasure.acceptInput(actorId, payload(2, AnglingMinigameInputAction.PRESS), 102);
        assertEquals(3, treasure.snapshot().sweetspots().size());

        AnglingServerMinigameSession neverLose = session(new AnglingServerMinigameSpec(
                100, 0, 0, 0, 0, 0, false,
                List.of(new AnglingNativeModifier.NeverLose("")), List.of()), 1L);
        neverLose.tick(101);
        assertEquals(20.0F, neverLose.snapshot().progress());
        assertEquals(AnglingServerMinigameStatus.ACTIVE, neverLose.status());
    }

    @Test
    void treasureFlipAndLowProgressBounceMatchReferenceSemantics() {
        var flippingTreasure = new AnglingServerMinigameSpec.Sweetspot(
                AnglingSweetspotBehaviorType.TREASURE, texture(), 512, 10, true,
                0, 0, 0xff00ff00, List.of());
        AnglingServerMinigameSession treasure = session(new AnglingServerMinigameSpec(
                100, 0, 0, 0, 0, 20, true, List.of(), List.of(flippingTreasure)), 1L);
        treasure.acceptInput(actorId, payload(0, AnglingMinigameInputAction.PRESS), 101);
        assertEquals(-1, treasure.snapshot().pointerRotation());
        assertEquals(10, treasure.snapshot().treasureProgress());

        AnglingServerMinigameSession bounce = session(new AnglingServerMinigameSpec(
                100, 0, 0, 0, 0, 1, false,
                List.of(new AnglingNativeModifier.BounceBack("")), List.of()), 1L);
        bounce.tick(101);
        bounce.tick(102);
        assertEquals(2.0F, bounce.snapshot().progress());
        assertTrue(bounce.snapshot().smoothedProgress() >= 4.0F);
        assertEquals(AnglingServerMinigameStatus.ACTIVE, bounce.status());
    }

    @Test
    void angularOverlapHandlesZeroBoundary() {
        assertTrue(AnglingServerMinigameSession.overlaps(359, 1, 3));
        assertFalse(AnglingServerMinigameSession.overlaps(350, 10, 10));
    }

    private AnglingServerMinigameSession session(AnglingServerMinigameSpec spec, long seed) {
        return new AnglingServerMinigameSession(sessionId, actorId, 9, 100, 200, seed, spec);
    }

    private AnglingMinigameInputPayload payload(int sequence, AnglingMinigameInputAction action) {
        return new AnglingMinigameInputPayload(sessionId, 9, sequence, action);
    }

    private static AnglingServerMinigameSpec spec(
            int hp, float speed, float penalty, float decay, float initial,
            List<AnglingServerMinigameSpec.Sweetspot> sweetspots
    ) {
        return new AnglingServerMinigameSpec(
                hp, speed, penalty, decay, 0, initial, false, List.of(), sweetspots);
    }

    private static AnglingServerMinigameSpec.Sweetspot spot(
            AnglingSweetspotBehaviorType behavior, int size, int reward
    ) {
        return new AnglingServerMinigameSpec.Sweetspot(
                behavior, texture(), size, reward, false, 0, 0, 0xff00ff00, List.of());
    }

    private static Identifier texture() {
        return Identifier.of("elarion_angling", "textures/gui/minigame/spots/normal.png");
    }
}
