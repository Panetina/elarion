package panetina.elarion.core.model;

import java.util.Map;

/** Compact per-month routing metadata for bounded public-history index reads. */
public record HistoryMonthSummary(
        String month,
        long firstTimestamp,
        long lastTimestamp,
        int totalEvents,
        Map<String, Integer> categoryCounts,
        Map<String, Integer> typeCounts,
        Map<String, Integer> realmCounts,
        Map<String, Integer> playerCounts
) {
    public HistoryMonthSummary {
        month = month == null ? "" : month.trim();
        totalEvents = Math.max(0, totalEvents);
        categoryCounts = categoryCounts == null ? Map.of() : Map.copyOf(categoryCounts);
        typeCounts = typeCounts == null ? Map.of() : Map.copyOf(typeCounts);
        realmCounts = realmCounts == null ? Map.of() : Map.copyOf(realmCounts);
        playerCounts = playerCounts == null ? Map.of() : Map.copyOf(playerCounts);
    }

    public static HistoryMonthSummary from(HistoryMonthIndex index) {
        return new HistoryMonthSummary(index.month(), index.firstTimestamp(), index.lastTimestamp(), index.totalEvents(),
                index.categoryCounts(), index.typeCounts(), index.realmCounts(), index.playerCounts());
    }
}
