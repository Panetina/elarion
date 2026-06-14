package panetina.elarion.addons.angling.service;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinition;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;
import panetina.elarion.addons.angling.resource.FishDefinitionRepository;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AnglingCatchResolutionServiceTest {
    private static final Identifier FISH_ID =
            Identifier.of("elarion_angling", "placeholder_fish_001");

    @Test
    void resolvesKnownDefinitionAndEmitsStableTechnicalTelemetry() {
        FishDefinitionRepository definitions = definitions();
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        List<CatchTelemetryEvent> emitted = new ArrayList<>();
        AnglingCatchResolutionService service = new AnglingCatchResolutionService(
                definitions,
                emitted::add,
                () -> eventId,
                () -> 1234L);

        var result = service.resolveAndEmit(
                actorId,
                FISH_ID,
                2,
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"));

        assertEquals(eventId, result.eventId());
        assertEquals(1234L, result.occurredAt());
        assertEquals(AnglingCatchResolutionService.SOURCE_ID, result.sourceId());
        assertEquals(FISH_ID, result.fishDefinitionId());
        assertEquals(AnglingRarity.PLACEHOLDER_COMMON.id(), result.rarityId());
        assertEquals(List.of(result.toTelemetryEvent()), emitted);
    }

    @Test
    void unknownDefinitionAndInvalidQuantityEmitNothing() {
        List<CatchTelemetryEvent> emitted = new ArrayList<>();
        AnglingCatchResolutionService service = new AnglingCatchResolutionService(
                definitions(),
                emitted::add,
                UUID::randomUUID,
                () -> 1L);

        assertThrows(IllegalArgumentException.class, () -> service.resolveAndEmit(
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "placeholder_missing"),
                1,
                null,
                null,
                null));
        assertThrows(IllegalArgumentException.class, () -> service.resolveAndEmit(
                UUID.randomUUID(),
                FISH_ID,
                0,
                null,
                null,
                null));
        assertEquals(List.of(), emitted);
    }

    @Test
    void createdResultCanBeRetriedWithTheSameEventIdentity() {
        List<CatchTelemetryEvent> emitted = new ArrayList<>();
        AnglingCatchResolutionService service = new AnglingCatchResolutionService(
                definitions(),
                emitted::add,
                UUID::randomUUID,
                () -> 1L);
        var result = service.createResult(
                UUID.randomUUID(),
                FISH_ID,
                1,
                null,
                null,
                null);

        service.emit(result);
        service.emit(result);

        assertEquals(2, emitted.size());
        assertEquals(emitted.get(0), emitted.get(1));
    }

    @Test
    void productionConstructorEmitsThroughCoreEventBus() {
        ElarionEventBus events = new ElarionEventBus();
        List<CatchTelemetryEvent> emitted = new ArrayList<>();
        events.onCatchTelemetry(emitted::add);
        AnglingCatchResolutionService service =
                new AnglingCatchResolutionService(definitions(), events);

        var result = service.resolveAndEmit(
                UUID.randomUUID(),
                FISH_ID,
                1,
                null,
                null,
                null);

        assertEquals(List.of(result.toTelemetryEvent()), emitted);
    }

    private static FishDefinitionRepository definitions() {
        FishDefinitionRepository repository = new FishDefinitionRepository();
        repository.publish(new FishDefinitionIndex(List.of(new FishDefinition(
                FISH_ID,
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of()))));
        return repository;
    }
}
