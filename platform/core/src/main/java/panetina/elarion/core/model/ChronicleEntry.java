package panetina.elarion.core.model;

import java.util.Map;
import java.util.UUID;

public record ChronicleEntry(
        UUID eventId,
        long timestamp,
        String category,
        String type,
        UUID actorId,
        String subjectType,
        String subjectId,
        String realmId,
        Map<String, String> metadata,
        String text
) {
    public ChronicleEntry {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        category = clean(category);
        type = clean(type);
        subjectType = clean(subjectType);
        subjectId = clean(subjectId);
        realmId = clean(realmId);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        text = clean(text);
    }

    public ChronicleEntry(
            UUID eventId,
            long timestamp,
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String realmId,
            String text
    ) {
        this(eventId, timestamp, category, type, actorId, subjectType, subjectId, realmId, Map.of(), text);
    }

    public static ChronicleEntry from(HistoryIndexEntry entry) {
        return new ChronicleEntry(
                entry.eventId(),
                entry.timestamp(),
                entry.category(),
                entry.type(),
                entry.actorId(),
                entry.subjectType(),
                entry.subjectId(),
                entry.realmId(),
                entry.metadata(),
                entry.chronicleText());
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
