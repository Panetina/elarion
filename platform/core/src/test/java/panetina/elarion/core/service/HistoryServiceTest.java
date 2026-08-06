package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.HistoryChroniclePolicy;
import panetina.elarion.core.model.HistoryIndexEntry;
import panetina.elarion.core.model.HistoryMonthIndex;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HistoryServiceTest {
    @Test
    void publicHistoryWeeksUseTheConfiguredBoundForDefaultAndCallerValues() {
        assertEquals(8, HistoryService.boundedPublicHistoryWeeks(0, 8, 52));
        assertEquals(52, HistoryService.boundedPublicHistoryWeeks(999, 8, 52));
        assertEquals(1, HistoryService.boundedPublicHistoryWeeks(-1, -2, 0));
    }

    @Test
    void recentIndexReadsNeverExceedTheConfiguredMonthWindow() {
        assertEquals(3, HistoryService.boundedRecentIndexMonths(999, 3));
        assertEquals(2, HistoryService.boundedRecentIndexMonths(2, 3));
        assertEquals(1, HistoryService.boundedRecentIndexMonths(0, 0));
    }

    @Test
    void weeklyArchiveFiltersMonthlyIndexesBeforeMaterializingEntries() {
        HistoryIndexEntry eligible = entry("realm", "founding", "2026-06-03T12:00:00Z", true);
        HistoryIndexEntry excluded = entry("citizen", "renamed", "2026-06-04T12:00:00Z", true);
        HistoryIndexEntry outsideWeek = entry("realm", "founding", "2026-06-10T12:00:00Z", true);
        List<HistoryMonthIndex> months = List.of(
                month("2026-05", List.of(outsideWeek)),
                month("2026-06", List.of(eligible, excluded)));
        HistoryChroniclePolicy policy = new HistoryChroniclePolicy(
                java.util.Set.of("realm"), true, java.util.Set.of(), java.util.Set.of());

        var archive = HistoryService.buildArchive(
                months, LocalDate.parse("2026-06-01"), LocalDate.parse("2026-06-08"), ZoneOffset.UTC, policy);

        assertEquals(1, archive.totalEvents());
        assertEquals(eligible.eventId(), archive.entries().getFirst().eventId());
    }

    private static HistoryMonthIndex month(String name, List<HistoryIndexEntry> entries) {
        return new HistoryMonthIndex(name, 0, 0, entries.size(), Map.of(), Map.of(), Map.of(), Map.of(), entries);
    }

    private static HistoryIndexEntry entry(String category, String type, String timestamp, boolean intentional) {
        return new HistoryIndexEntry(UUID.randomUUID(), Instant.parse(timestamp).toEpochMilli(), category, type,
                null, "realm", "oak", "oak",
                intentional ? Map.of("chronicle.intent", "true") : Map.of(), "Chronicle event");
    }
}
