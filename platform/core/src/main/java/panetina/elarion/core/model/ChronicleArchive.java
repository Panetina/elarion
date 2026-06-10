package panetina.elarion.core.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ChronicleArchive(
        UUID id,
        String weekStart,
        String weekEnd,
        long generatedAt,
        int totalEvents,
        Map<String, Integer> categoryCounts,
        Map<String, Integer> typeCounts,
        Map<String, Integer> realmCounts,
        Map<String, Integer> playerCounts,
        List<ChronicleEntry> entries
) {
    public ChronicleArchive {
        id = id == null ? UUID.randomUUID() : id;
        weekStart = clean(weekStart);
        weekEnd = clean(weekEnd);
        categoryCounts = categoryCounts == null ? Map.of() : Map.copyOf(categoryCounts);
        typeCounts = typeCounts == null ? Map.of() : Map.copyOf(typeCounts);
        realmCounts = realmCounts == null ? Map.of() : Map.copyOf(realmCounts);
        playerCounts = playerCounts == null ? Map.of() : Map.copyOf(playerCounts);
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
