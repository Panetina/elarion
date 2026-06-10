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
        String realmId,
        Map<String, String> metadata,
        String chronicleText
) {
    public HistoryEvent {
        id = id == null ? UUID.randomUUID() : id;
        timestamp = timestamp <= 0 ? Instant.now().toEpochMilli() : timestamp;
        category = clean(category, "general");
        type = clean(type, "event");
        subjectType = clean(subjectType, "");
        subjectId = clean(subjectId, "");
        realmId = clean(realmId, "");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        chronicleText = clean(chronicleText, defaultChronicleText(category, type, subjectType, subjectId, realmId));
    }

    public static HistoryEvent create(
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String realmId,
            Map<String, String> metadata
    ) {
        return new HistoryEvent(null, 0, category, type, actorId, subjectType,
                subjectId, realmId, metadata, null);
    }

    public static HistoryEvent create(
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String realmId,
            Map<String, String> metadata,
            String chronicleText
    ) {
        return new HistoryEvent(null, 0, category, type, actorId, subjectType,
                subjectId, realmId, metadata, chronicleText);
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String defaultChronicleText(
            String category,
            String type,
            String subjectType,
            String subjectId,
            String realmId
    ) {
        String realm = realmId == null || realmId.isBlank() ? "Elarion" : "the Realm of " + realmId;
        String subject = subjectId == null || subjectId.isBlank()
                ? "an event"
                : (subjectType == null || subjectType.isBlank() ? subjectId : subjectType + " " + subjectId);
        return "In " + realm + ", " + subject + " was marked as " + clean(type, "event")
                + " within " + clean(category, "general") + ".";
    }
}
