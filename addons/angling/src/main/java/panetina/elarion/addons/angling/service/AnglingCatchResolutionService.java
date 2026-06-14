package panetina.elarion.addons.angling.service;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.model.AnglingCatchResult;
import panetina.elarion.addons.angling.model.FishDefinition;
import panetina.elarion.addons.angling.resource.FishDefinitionRepository;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class AnglingCatchResolutionService {
    public static final Identifier SOURCE_ID = Identifier.of("elarion_angling", "fishing");

    private final FishDefinitionRepository definitions;
    private final Consumer<CatchTelemetryEvent> telemetryEmitter;
    private final Supplier<UUID> eventIds;
    private final LongSupplier clock;

    public AnglingCatchResolutionService(
            FishDefinitionRepository definitions,
            ElarionEventBus events
    ) {
        this(definitions, events::emitCatchTelemetry, UUID::randomUUID, System::currentTimeMillis);
    }

    AnglingCatchResolutionService(
            FishDefinitionRepository definitions,
            Consumer<CatchTelemetryEvent> telemetryEmitter,
            Supplier<UUID> eventIds,
            LongSupplier clock
    ) {
        this.definitions = Objects.requireNonNull(definitions, "definitions");
        this.telemetryEmitter = Objects.requireNonNull(telemetryEmitter, "telemetryEmitter");
        this.eventIds = Objects.requireNonNull(eventIds, "eventIds");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public AnglingCatchResult createResult(
            UUID actorId,
            Identifier fishDefinitionId,
            long quantity,
            Identifier worldId,
            Identifier dimensionId,
            Identifier biomeId
    ) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(fishDefinitionId, "fishDefinitionId");
        return createResult(
                Objects.requireNonNull(eventIds.get(), "generated eventId"),
                clock.getAsLong(),
                actorId,
                fishDefinitionId,
                quantity,
                worldId,
                dimensionId,
                biomeId);
    }

    AnglingCatchResult createResult(
            UUID eventId,
            long occurredAt,
            UUID actorId,
            Identifier fishDefinitionId,
            long quantity,
            Identifier worldId,
            Identifier dimensionId,
            Identifier biomeId
    ) {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(fishDefinitionId, "fishDefinitionId");
        FishDefinition definition = definitions.current().get(fishDefinitionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown Angling fish definition: " + fishDefinitionId));
        return createResolvedResult(
                eventId,
                occurredAt,
                actorId,
                definition.id(),
                definition.rarity().id(),
                quantity,
                worldId,
                dimensionId,
                biomeId);
    }

    AnglingCatchResult createResolvedResult(
            UUID eventId,
            long occurredAt,
            UUID actorId,
            Identifier fishDefinitionId,
            Identifier rarityId,
            long quantity,
            Identifier worldId,
            Identifier dimensionId,
            Identifier biomeId
    ) {
        return new AnglingCatchResult(
                eventId,
                occurredAt,
                actorId,
                SOURCE_ID,
                fishDefinitionId,
                rarityId,
                quantity,
                worldId,
                dimensionId,
                biomeId);
    }

    public void emit(AnglingCatchResult result) {
        telemetryEmitter.accept(Objects.requireNonNull(result, "result").toTelemetryEvent());
    }

    public AnglingCatchResult resolveAndEmit(
            UUID actorId,
            Identifier fishDefinitionId,
            long quantity,
            Identifier worldId,
            Identifier dimensionId,
            Identifier biomeId
    ) {
        AnglingCatchResult result = createResult(
                actorId,
                fishDefinitionId,
                quantity,
                worldId,
                dimensionId,
                biomeId);
        emit(result);
        return result;
    }
}
