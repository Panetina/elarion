package panetina.elarion.core.model;

import java.util.Map;
import java.util.UUID;

public record PublicHistoryEntry(
        UUID eventId,
        long timestamp,
        String source,
        String category,
        String type,
        UUID actorId,
        String subjectType,
        String subjectId,
        String realmId,
        Map<String, String> metadata,
        String text
) {
    public PublicHistoryEntry {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        source = clean(source);
        category = clean(category);
        type = clean(type);
        subjectType = clean(subjectType);
        subjectId = clean(subjectId);
        realmId = clean(realmId);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        text = clean(text);
    }

    public PublicHistoryEntry(
            UUID eventId,
            long timestamp,
            String source,
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String realmId,
            String text
    ) {
        this(eventId, timestamp, source, category, type, actorId, subjectType, subjectId, realmId, Map.of(), text);
    }

    public static PublicHistoryEntry fromArchive(ChronicleEntry entry) {
        return new PublicHistoryEntry(
                entry.eventId(),
                entry.timestamp(),
                "chronicle",
                entry.category(),
                entry.type(),
                entry.actorId(),
                entry.subjectType(),
                entry.subjectId(),
                entry.realmId(),
                entry.metadata(),
                entry.text());
    }

    public static PublicHistoryEntry fromIndex(HistoryIndexEntry entry) {
        return new PublicHistoryEntry(
                entry.eventId(),
                entry.timestamp(),
                "live-index",
                entry.category(),
                entry.type(),
                entry.actorId(),
                entry.subjectType(),
                entry.subjectId(),
                entry.realmId(),
                entry.metadata(),
                entry.chronicleText());
    }

    public boolean involvesPlayer(UUID playerId) {
        if (playerId == null) return false;
        return playerId.equals(actorId)
                || subjectType.equals("player") && subjectId.equals(playerId.toString());
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
