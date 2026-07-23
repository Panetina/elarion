package panetina.elarion.core.model;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record CatchTelemetryEvent(
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
    public static final int MAX_METADATA_ENTRIES = 32;
    public static final int MAX_METADATA_KEY_LENGTH = 64;
    public static final int MAX_METADATA_VALUE_LENGTH = 256;

    public CatchTelemetryEvent {
        Objects.requireNonNull(eventId, "eventId");
        if (occurredAt <= 0) {
            throw new IllegalArgumentException("occurredAt must be positive");
        }
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(fishDefinitionId, "fishDefinitionId");
        Objects.requireNonNull(rarityId, "rarityId");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        Map<String, String> copied = metadata == null ? Map.of() : new LinkedHashMap<>(metadata);
        if (copied.size() > MAX_METADATA_ENTRIES) {
            throw new IllegalArgumentException("metadata must contain at most " + MAX_METADATA_ENTRIES + " entries");
        }
        for (Map.Entry<String, String> entry : copied.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || key.isBlank() || key.length() > MAX_METADATA_KEY_LENGTH) {
                throw new IllegalArgumentException(
                        "metadata keys must be nonblank and at most " + MAX_METADATA_KEY_LENGTH + " characters");
            }
            if (value == null || value.length() > MAX_METADATA_VALUE_LENGTH) {
                throw new IllegalArgumentException(
                        "metadata values must be nonnull and at most " + MAX_METADATA_VALUE_LENGTH + " characters");
            }
        }
        metadata = Collections.unmodifiableMap(copied);
    }

    /** Backward-compatible constructor for telemetry sources without rich outcome details. */
    public CatchTelemetryEvent(
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
        this(eventId, occurredAt, actorId, sourceId, fishDefinitionId, rarityId, quantity,
                worldId, dimensionId, biomeId, metadata, null);
    }
}
