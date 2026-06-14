package panetina.elarion.addons.angling.service;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.condition.AnglingConditionContext;
import panetina.elarion.addons.angling.condition.AnglingConditionRegistry;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinition;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;
import panetina.elarion.addons.angling.resource.FishDefinitionRepository;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingFishingSessionServiceTest {
    private static final Identifier FISH_ID =
            Identifier.of("elarion_angling", "placeholder_fish_001");

    @Test
    void startsOneSessionPerPlayerAndCompletesIntoTelemetry() {
        Fixture fixture = fixture();
        UUID actorId = UUID.randomUUID();
        var session = fixture.sessions.start(context(actorId), 0, 1_000).orElseThrow();

        assertEquals(1, fixture.sessions.activeCount());
        assertEquals(FISH_ID, session.fishDefinitionId());
        assertThrows(IllegalStateException.class, () ->
                fixture.sessions.start(context(actorId), 0, 1_000));

        fixture.clock.set(150);
        var result = fixture.sessions.complete(actorId, session.sessionId());

        assertEquals(session.eventId(), result.eventId());
        assertEquals(150, result.occurredAt());
        assertEquals(AnglingRarity.PLACEHOLDER_COMMON.id(), result.rarityId());
        assertEquals(List.of(result.toTelemetryEvent()), fixture.emitted);
        assertTrue(fixture.sessions.active(actorId).isEmpty());
    }

    @Test
    void failedEmissionRetainsStableCompletionForRetry() {
        Fixture fixture = fixture();
        UUID actorId = UUID.randomUUID();
        var session = fixture.sessions.start(context(actorId), 0, 1_000).orElseThrow();
        fixture.failEmission.set(true);
        fixture.clock.set(150);

        assertThrows(IllegalStateException.class, () ->
                fixture.sessions.complete(actorId, session.sessionId()));
        var pending = fixture.sessions.active(actorId).orElseThrow();
        assertEquals(150, pending.completionStartedAt());

        fixture.failEmission.set(false);
        fixture.clock.set(175);
        var result = fixture.sessions.complete(actorId, session.sessionId());

        assertEquals(session.eventId(), result.eventId());
        assertEquals(150, result.occurredAt());
        assertEquals(1, fixture.emitted.size());
    }

    @Test
    void selectedDefinitionSurvivesReloadBeforeCompletion() {
        Fixture fixture = fixture();
        UUID actorId = UUID.randomUUID();
        var session = fixture.sessions.start(context(actorId), 0, 1_000).orElseThrow();
        fixture.definitions.publish(new FishDefinitionIndex(List.of()));
        fixture.clock.set(150);

        var result = fixture.sessions.complete(actorId, session.sessionId());

        assertEquals(FISH_ID, result.fishDefinitionId());
        assertEquals(AnglingRarity.PLACEHOLDER_COMMON.id(), result.rarityId());
    }

    @Test
    void noCandidateCreatesNoSession() {
        Fixture fixture = fixture();
        fixture.definitions.publish(new FishDefinitionIndex(List.of()));

        assertTrue(fixture.sessions.start(context(UUID.randomUUID()), 0, 1_000).isEmpty());
        assertEquals(0, fixture.sessions.activeCount());
        assertEquals(0, fixture.sessions.pendingExpirationCount());
    }

    @Test
    void expiryWorkIsBoundedAndCancellationIsDirect() {
        Fixture fixture = fixture();
        UUID firstActor = UUID.randomUUID();
        UUID secondActor = UUID.randomUUID();
        UUID thirdActor = UUID.randomUUID();
        var first = fixture.sessions.start(context(firstActor), 0, 10).orElseThrow();
        fixture.sessions.start(context(secondActor), 0, 10).orElseThrow();
        fixture.sessions.start(context(thirdActor), 0, 10).orElseThrow();
        assertTrue(fixture.sessions.cancel(firstActor, first.sessionId()));
        assertFalse(fixture.sessions.cancel(firstActor, first.sessionId()));

        assertEquals(0, fixture.sessions.expireDue(110, 1));
        assertEquals(2, fixture.sessions.activeCount());
        assertEquals(1, fixture.sessions.expireDue(110, 1));
        assertEquals(1, fixture.sessions.activeCount());
        assertEquals(1, fixture.sessions.expireDue(110, 1));
        assertEquals(0, fixture.sessions.activeCount());
    }

    @Test
    void expiredAndMismatchedCompletionFailsWithoutEmission() {
        Fixture fixture = fixture();
        UUID actorId = UUID.randomUUID();
        var session = fixture.sessions.start(context(actorId), 0, 10).orElseThrow();

        assertThrows(IllegalArgumentException.class, () ->
                fixture.sessions.complete(actorId, UUID.randomUUID()));
        fixture.clock.set(110);
        assertThrows(IllegalStateException.class, () ->
                fixture.sessions.complete(actorId, session.sessionId()));
        assertTrue(fixture.emitted.isEmpty());
        assertTrue(fixture.sessions.active(actorId).isEmpty());
    }

    private static Fixture fixture() {
        FishDefinitionRepository definitions = new FishDefinitionRepository();
        definitions.publish(new FishDefinitionIndex(List.of(new FishDefinition(
                FISH_ID,
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                1,
                List.of()))));
        FishCandidateSelector selector =
                new FishCandidateSelector(definitions, new AnglingConditionRegistry());
        List<CatchTelemetryEvent> emitted = new ArrayList<>();
        AtomicBoolean failEmission = new AtomicBoolean();
        AnglingCatchResolutionService catchResolution = new AnglingCatchResolutionService(
                definitions,
                event -> {
                    if (failEmission.get()) throw new IllegalStateException("test emission failure");
                    emitted.add(event);
                },
                UUID::randomUUID,
                () -> 100);
        AtomicLong clock = new AtomicLong(100);
        AtomicLong sessionCounter = new AtomicLong();
        AnglingFishingSessionService sessions = new AnglingFishingSessionService(
                selector,
                catchResolution,
                () -> new UUID(0, sessionCounter.incrementAndGet()),
                UUID::randomUUID,
                clock::get);
        return new Fixture(definitions, sessions, emitted, failEmission, clock);
    }

    private static AnglingConditionContext context(UUID actorId) {
        return new AnglingConditionContext(
                actorId,
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                Identifier.of("minecraft", "water"),
                null,
                64,
                6_000,
                false,
                false);
    }

    private record Fixture(
            FishDefinitionRepository definitions,
            AnglingFishingSessionService sessions,
            List<CatchTelemetryEvent> emitted,
            AtomicBoolean failEmission,
            AtomicLong clock
    ) {
    }
}
