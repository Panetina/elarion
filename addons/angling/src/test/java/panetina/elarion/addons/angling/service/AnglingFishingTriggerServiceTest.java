package panetina.elarion.addons.angling.service;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.condition.AnglingConditionContext;
import panetina.elarion.addons.angling.condition.AnglingConditionRegistry;
import panetina.elarion.addons.angling.model.AnglingConditionId;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinition;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;
import panetina.elarion.addons.angling.resource.FishDefinitionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingFishingTriggerServiceTest {
    @Test
    void newCastReplacesStaleSessionAndSuccessfulCatchEmitsOnce() {
        var fixture = fixture();
        UUID actorId = UUID.randomUUID();
        var first = fixture.triggers.begin(context(actorId), 0).orElseThrow();

        var second = fixture.triggers.begin(context(actorId), 0).orElseThrow();
        assertFalse(first.sessionId().equals(second.sessionId()));
        assertEquals(second, fixture.sessions.active(actorId).orElseThrow());

        var completed = fixture.triggers.complete(actorId).orElseThrow();
        var result = completed.result();
        assertEquals(second.eventId(), result.eventId());
        assertEquals(context(actorId).baitId(), completed.baitId());
        assertEquals(List.of(result.toTelemetryEvent()), fixture.emitted);
        assertTrue(fixture.sessions.active(actorId).isEmpty());
        assertTrue(fixture.triggers.complete(actorId).isEmpty());
    }

    @Test
    void repeatedFishingTicksReuseTheExistingSelection() {
        var fixture = fixture();
        UUID actorId = UUID.randomUUID();
        var first = fixture.triggers.beginIfAbsent(context(actorId), 0).orElseThrow();

        var second = fixture.triggers.beginIfAbsent(context(actorId), 999).orElseThrow();

        assertEquals(first, second);
        assertEquals(1, fixture.sessions.activeCount());
    }

    @Test
    void unloadCancelsSessionWithoutEmittingTelemetry() {
        var fixture = fixture();
        UUID actorId = UUID.randomUUID();
        fixture.triggers.begin(context(actorId), 0).orElseThrow();

        assertTrue(fixture.triggers.cancel(actorId));
        assertTrue(fixture.sessions.active(actorId).isEmpty());
        assertTrue(fixture.emitted.isEmpty());
        assertFalse(fixture.triggers.cancel(actorId));
    }

    @Test
    void cancellationAllowsFreshCastAfterReconnect() {
        var fixture = fixture();
        UUID actorId = UUID.randomUUID();
        var first = fixture.triggers.begin(context(actorId), 0).orElseThrow();

        assertTrue(fixture.triggers.cancel(actorId));
        var second = fixture.triggers.beginIfAbsent(context(actorId), 0).orElseThrow();

        assertFalse(first.sessionId().equals(second.sessionId()));
        assertEquals(second, fixture.sessions.active(actorId).orElseThrow());
        assertEquals(1, fixture.sessions.activeCount());
        assertTrue(fixture.emitted.isEmpty());
    }

    private static Fixture fixture() {
        FishDefinitionRepository definitions = new FishDefinitionRepository();
        definitions.publish(new FishDefinitionIndex(List.of(new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                1,
                List.of(AnglingConditionId.of("placeholder_condition_001"))))));
        AnglingConditionRegistry conditions = new AnglingConditionRegistry();
        conditions.register(
                AnglingConditionId.of("placeholder_condition_001"),
                (definition, context) -> true);
        var emitted = new ArrayList<panetina.elarion.core.model.CatchTelemetryEvent>();
        var resolution = new AnglingCatchResolutionService(
                definitions,
                emitted::add,
                UUID::randomUUID,
                () -> 1_000L);
        var sessions = new AnglingFishingSessionService(
                new FishCandidateSelector(definitions, conditions),
                resolution);
        return new Fixture(sessions, new AnglingFishingTriggerService(sessions), emitted);
    }

    private static AnglingConditionContext context(UUID actorId) {
        return new AnglingConditionContext(
                actorId,
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                Identifier.of("minecraft", "water"),
                Identifier.of("elarion_angling", "placeholder_bait_item"),
                63,
                6_000,
                false,
                false);
    }

    private record Fixture(
            AnglingFishingSessionService sessions,
            AnglingFishingTriggerService triggers,
            List<panetina.elarion.core.model.CatchTelemetryEvent> emitted
    ) {
    }
}
