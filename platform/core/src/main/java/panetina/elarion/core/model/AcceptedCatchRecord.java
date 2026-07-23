package panetina.elarion.core.model;

import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;

public record AcceptedCatchRecord(
        int schemaVersion,
        UUID eventId,
        long occurredAt,
        UUID actorId,
        Identifier sourceId,
        Identifier fishDefinitionId,
        Identifier rarityId,
        long quantity,
        Identifier worldId,
        Identifier dimensionId,
        Identifier biomeId,
        Map<String, String> metadata,
        CatchTelemetryDetails details
) {
    public static final int CURRENT_SCHEMA_VERSION = 2;

    public AcceptedCatchRecord {
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException("unsupported catch record schema version: " + schemaVersion);
        }
        CatchTelemetryEvent validated = new CatchTelemetryEvent(
                eventId,
                occurredAt,
                actorId,
                sourceId,
                fishDefinitionId,
                rarityId,
                quantity,
                worldId,
                dimensionId,
                biomeId,
                metadata,
                details);
        metadata = validated.metadata();
    }

    /** Backward-compatible source constructor. New records are always persisted as schema 2. */
    public AcceptedCatchRecord(
            int schemaVersion,
            UUID eventId,
            long occurredAt,
            UUID actorId,
            Identifier sourceId,
            Identifier fishDefinitionId,
            Identifier rarityId,
            long quantity,
            Identifier worldId,
            Identifier dimensionId,
            Identifier biomeId,
            Map<String, String> metadata
    ) {
        this(schemaVersion, eventId, occurredAt, actorId, sourceId, fishDefinitionId, rarityId, quantity,
                worldId, dimensionId, biomeId, metadata, null);
    }

    public static AcceptedCatchRecord from(CatchTelemetryEvent event) {
        return new AcceptedCatchRecord(
                CURRENT_SCHEMA_VERSION,
                event.eventId(),
                event.occurredAt(),
                event.actorId(),
                event.sourceId(),
                event.fishDefinitionId(),
                event.rarityId(),
                event.quantity(),
                event.worldId(),
                event.dimensionId(),
                event.biomeId(),
                event.metadata(),
                event.details());
    }
}
