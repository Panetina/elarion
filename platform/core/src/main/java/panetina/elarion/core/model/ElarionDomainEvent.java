package panetina.elarion.core.model;

import java.util.Map;
import java.util.UUID;

/**
 * Immutable cross-addon lifecycle event for bounded, event-driven integrations.
 *
 * <p>Domain owners emit these events after authoritative state changes. The
 * event is an integration signal, not canonical state and not an automatic
 * notification.</p>
 */
public record ElarionDomainEvent(
        String sourceSystem,
        String eventType,
        UUID actorId,
        String realmId,
        String subjectType,
        String subjectId,
        long occurredAt,
        Map<String, String> metadata
) {
    public ElarionDomainEvent {
        sourceSystem = clean(sourceSystem);
        eventType = clean(eventType);
        realmId = clean(realmId);
        subjectType = clean(subjectType);
        subjectId = clean(subjectId);
        occurredAt = occurredAt > 0L ? occurredAt : System.currentTimeMillis();
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        if (sourceSystem.isBlank()) throw new IllegalArgumentException("Domain event sourceSystem is required.");
        if (eventType.isBlank()) throw new IllegalArgumentException("Domain event eventType is required.");
    }

    public static ElarionDomainEvent of(
            String sourceSystem,
            String eventType,
            UUID actorId,
            String realmId,
            String subjectType,
            String subjectId,
            Map<String, String> metadata
    ) {
        return new ElarionDomainEvent(sourceSystem, eventType, actorId, realmId,
                subjectType, subjectId, System.currentTimeMillis(), metadata);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
