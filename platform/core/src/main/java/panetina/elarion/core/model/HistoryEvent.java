package panetina.elarion.core.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record HistoryEvent(
        UUID id,
        long timestamp,
        String category,
        String type,
        UUID actorId,
        String subjectType,
        String subjectId,
        String communityId,
        Map<String, String> metadata
) {
    public HistoryEvent {
        id = id == null ? UUID.randomUUID() : id;
        timestamp = timestamp <= 0 ? Instant.now().toEpochMilli() : timestamp;
        category = clean(category, "general");
        type = clean(type, "event");
        subjectType = clean(subjectType, "");
        subjectId = clean(subjectId, "");
        communityId = clean(communityId, "");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static HistoryEvent create(
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String communityId,
            Map<String, String> metadata
    ) {
        return new HistoryEvent(null, 0, category, type, actorId, subjectType,
                subjectId, communityId, metadata);
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
