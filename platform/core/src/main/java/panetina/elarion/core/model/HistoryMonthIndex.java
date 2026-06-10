package panetina.elarion.core.model;

import java.util.List;
import java.util.Map;

public record HistoryMonthIndex(
        String month,
        long firstTimestamp,
        long lastTimestamp,
        int totalEvents,
        Map<String, Integer> categoryCounts,
        Map<String, Integer> typeCounts,
        Map<String, Integer> realmCounts,
        Map<String, Integer> playerCounts,
        List<HistoryIndexEntry> entries
) {
    public HistoryMonthIndex {
        month = month == null ? "" : month.trim();
        categoryCounts = categoryCounts == null ? Map.of() : Map.copyOf(categoryCounts);
        typeCounts = typeCounts == null ? Map.of() : Map.copyOf(typeCounts);
        realmCounts = realmCounts == null ? Map.of() : Map.copyOf(realmCounts);
        playerCounts = playerCounts == null ? Map.of() : Map.copyOf(playerCounts);
        entries = entries == null ? List.of() : List.copyOf(entries);
    }
}
