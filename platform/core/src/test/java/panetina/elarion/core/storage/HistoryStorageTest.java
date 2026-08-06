package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.service.ElarionTaskService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class HistoryStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void eventsSurviveStorageRoundTrip() {
        HistoryStorage storage = new HistoryStorage(LoggerFactory.getLogger("history-test"));
        UUID actor = UUID.randomUUID();
        HistoryEvent event = HistoryEvent.create(
                "title",
                "unique-discovered",
                actor,
                "title",
                "maze_runner",
                "oak",
                Map.of("location", "indestructible_maze"));

        storage.append(tempDir, event);

        HistoryEvent loaded = storage.loadAll(tempDir).getFirst();
        assertEquals(event.id(), loaded.id());
        assertEquals(actor, loaded.actorId());
        assertEquals("unique-discovered", loaded.type());
        assertEquals("maze_runner", loaded.subjectId());
        assertEquals("indestructible_maze", loaded.metadata().get("location"));
        assertEquals(event.chronicleText(), loaded.chronicleText());
    }

    @Test
    void historyFlushesBatchedWrites() throws IOException {
        ElarionTaskService tasks = new ElarionTaskService(
                LoggerFactory.getLogger("history-tasks"), 1, 1, 10, 10, TimeUnit.MILLISECONDS.toNanos(1));
        HistoryStorage storage = new HistoryStorage(LoggerFactory.getLogger("history-test"));
        storage.setTaskService(tasks);

        for (int index = 0; index < 33; index++) {
            storage.append(tempDir, HistoryEvent.create(
                    "realm",
                    "event-" + index,
                    UUID.randomUUID(),
                    "realm",
                    "oak",
                    "oak",
                    Map.of()));
        }

        storage.flushBlocking();

        assertEquals(33, storage.loadAll(tempDir).size());
        try (var files = Files.list(tempDir)) {
            assertEquals(1L, files.filter(path -> path.getFileName().toString().endsWith(".jsonl")).count());
        }
        tasks.shutdown();
    }

    @Test
    void boundedQueriesOnlyScanNewestMonthlyFiles() {
        HistoryStorage storage = new HistoryStorage(LoggerFactory.getLogger("history-test"));
        storage.append(tempDir, eventAt("realm", "older-match", "oak", "2026-04-10T12:00:00Z"));
        storage.append(tempDir, eventAt("realm", "newer-other", "stone", "2026-06-10T12:00:00Z"));

        assertEquals(0, storage.queryRecent(tempDir,
                event -> event.realmId().equals("oak"), 10, 1).size());
        assertEquals(1, storage.queryRecent(tempDir,
                event -> event.realmId().equals("oak"), 10, 3).size());
    }

    @Test
    void failedFlushRetainsOnlyUnwrittenEntriesForRetry() throws IOException {
        HistoryStorage storage = new HistoryStorage(LoggerFactory.getLogger("history-test"));
        Path blocker = tempDir.resolve("not-a-directory");
        Files.writeString(blocker, "block history directory creation");
        Path blockedHistory = blocker.resolve("history");
        HistoryEvent event = HistoryEvent.create(
                "realm", "retry-after-io-failure", UUID.randomUUID(), "realm", "oak", "oak", Map.of());

        storage.append(blockedHistory, event);

        assertThrows(IllegalStateException.class, storage::flushBlocking);

        Files.delete(blocker);
        storage.flushBlocking();

        assertEquals(List.of(event.id()), storage.loadAll(blockedHistory).stream().map(HistoryEvent::id).toList());
    }

    private static HistoryEvent eventAt(String category, String type, String realmId, String instant) {
        return new HistoryEvent(
                UUID.randomUUID(),
                Instant.parse(instant).toEpochMilli(),
                category,
                type,
                null,
                "realm",
                realmId,
                realmId,
                Map.of(),
                null);
    }
}
