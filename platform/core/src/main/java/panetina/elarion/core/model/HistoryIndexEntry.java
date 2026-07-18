package panetina.elarion.core.model;

import java.util.Map;
import java.util.UUID;

public record HistoryIndexEntry(
        UUID eventId,
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
    public HistoryIndexEntry {
        eventId = eventId == null ? UUID.randomUUID() : eventId;
        category = clean(category);
        type = clean(type);
        subjectType = clean(subjectType);
        subjectId = clean(subjectId);
        realmId = clean(realmId);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        chronicleText = clean(chronicleText);
    }

    public HistoryIndexEntry(
            UUID eventId,
            long timestamp,
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String realmId,
            String chronicleText
    ) {
        this(eventId, timestamp, category, type, actorId, subjectType, subjectId, realmId, Map.of(), chronicleText);
    }

    public static HistoryIndexEntry from(HistoryEvent event) {
        return new HistoryIndexEntry(
                event.id(),
                event.timestamp(),
                event.category(),
                event.type(),
                event.actorId(),
                event.subjectType(),
                event.subjectId(),
                event.realmId(),
                event.metadata(),
                event.chronicleText());
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
