package panetina.elarion.core.model;

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
        text = clean(text);
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
