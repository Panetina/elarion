package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record MetricUpdateBatch(
        Identifier sourceSystem,
        String sourcePartition,
        long sequence,
        UUID eventId,
        UUID actorId,
        long occurredAt,
        Identifier realmId,
        List<MetricUpdate> updates
) {
    public static final int MAX_UPDATES = 64;
    public static final int MAX_PARTITION_LENGTH = 128;

    public MetricUpdateBatch {
        Objects.requireNonNull(sourceSystem, "sourceSystem");
        Objects.requireNonNull(sourcePartition, "sourcePartition");
        if (sourcePartition.isBlank() || sourcePartition.length() > MAX_PARTITION_LENGTH) {
            throw new IllegalArgumentException("metric source partition is invalid");
        }
        if (sequence <= 0 || occurredAt <= 0) throw new IllegalArgumentException("sequence and occurredAt must be positive");
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(actorId, "actorId");
        updates = List.copyOf(Objects.requireNonNull(updates, "updates"));
        if (updates.isEmpty() || updates.size() > MAX_UPDATES || updates.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("metric batch updates are empty or unbounded");
        }
    }
}
