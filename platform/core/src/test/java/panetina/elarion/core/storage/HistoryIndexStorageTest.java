package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.HistoryEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HistoryIndexStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void writesMonthlySummaryAndCompactEntries() {
        HistoryIndexStorage storage = new HistoryIndexStorage(LoggerFactory.getLogger("history-index-test"));
        UUID actor = UUID.randomUUID();
        storage.append(tempDir, eventAt("realm", "leader-set", actor, "player",
                actor.toString(), "oak", "2026-06-10T12:00:00Z"));
        storage.append(tempDir, eventAt("title", "granted", actor, "title",
                "diplomat", "oak", "2026-06-11T12:00:00Z"));

        var month = storage.loadRecentMonths(tempDir, 1).getFirst();

        assertEquals("2026-06", month.month());
        assertEquals(2, month.totalEvents());
        assertEquals(1, month.categoryCounts().get("realm"));
        assertEquals(1, month.categoryCounts().get("title"));
        assertEquals(1, month.typeCounts().get("realm:leader-set"));
        assertEquals(2, month.realmCounts().get("oak"));
        assertEquals(2, month.playerCounts().get(actor.toString()));
        assertEquals("title", month.entries().getFirst().category());
        assertTrue(Files.exists(tempDir.resolve("2026-06.summary.json")));
    }

    @Test
    void indexQueriesUseNewestMonthlyFilesFirst() {
        HistoryIndexStorage storage = new HistoryIndexStorage(LoggerFactory.getLogger("history-index-test"));
        storage.append(tempDir, eventAt("realm", "older-match", null, "realm",
                "oak", "oak", "2026-04-10T12:00:00Z"));
        storage.append(tempDir, eventAt("realm", "newer-other", null, "realm",
                "stone", "stone", "2026-06-10T12:00:00Z"));

        assertEquals(0, storage.queryEntries(tempDir,
                entry -> entry.realmId().equals("oak"), 10, 1).size());
        assertEquals(1, storage.queryEntries(tempDir,
                entry -> entry.realmId().equals("oak"), 10, 3).size());
    }

    @Test
    void duplicateEventIdsDoNotDoubleCount() {
        HistoryIndexStorage storage = new HistoryIndexStorage(LoggerFactory.getLogger("history-index-test"));
        HistoryEvent event = eventAt("realm", "relationship-set", null, "realm",
                "oak", "oak", "2026-06-10T12:00:00Z");

        storage.append(tempDir, event);
        storage.flushBlocking();
        storage.append(tempDir, event);
        storage.flushBlocking();

        var month = storage.loadRecentMonths(tempDir, 1).getFirst();
        assertEquals(1, month.totalEvents());
        assertEquals(1, month.categoryCounts().get("realm"));
    }

    @Test
    void matchingReadsSkipKnownNonmatchingMonthlyIndexes() throws Exception {
        HistoryIndexStorage storage = new HistoryIndexStorage(LoggerFactory.getLogger("history-index-test"));
        storage.append(tempDir, eventAt("realm", "older-match", null, "realm",
                "oak", "oak", "2026-04-10T12:00:00Z"));
        storage.append(tempDir, eventAt("realm", "newer-other", null, "realm",
                "stone", "stone", "2026-06-10T12:00:00Z"));
        storage.flushBlocking();

        Files.writeString(tempDir.resolve("2026-06.json"), "{ invalid json");

        var months = storage.loadRecentMonthsMatching(tempDir, 3,
                summary -> summary.realmCounts().containsKey("oak"));

        assertEquals(1, months.size());
        assertEquals("2026-04", months.getFirst().month());
    }

    private static HistoryEvent eventAt(
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String realmId,
            String instant
    ) {
        return new HistoryEvent(
                UUID.randomUUID(),
                Instant.parse(instant).toEpochMilli(),
                category,
                type,
                actorId,
                subjectType,
                subjectId,
                realmId,
                Map.of(),
                null);
    }
}
