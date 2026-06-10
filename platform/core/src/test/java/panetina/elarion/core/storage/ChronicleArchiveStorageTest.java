package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.ChronicleArchive;
import panetina.elarion.core.model.ChronicleEntry;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ChronicleArchiveStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void weeklyArchiveIsImmutableOnceWritten() {
        ChronicleArchiveStorage storage =
                new ChronicleArchiveStorage(LoggerFactory.getLogger("chronicle-test"));
        ChronicleArchive first = archive("first");
        ChronicleArchive second = archive("second");

        storage.saveIfAbsent(tempDir, first);
        storage.saveIfAbsent(tempDir, second);

        ChronicleArchive loaded = storage.loadRecent(tempDir, 1).getFirst();
        assertEquals("first", loaded.entries().getFirst().text());
    }

    private static ChronicleArchive archive(String text) {
        ChronicleEntry entry = new ChronicleEntry(
                UUID.randomUUID(),
                1L,
                "realm",
                "event",
                null,
                "realm",
                "oak",
                "oak",
                text);
        return new ChronicleArchive(UUID.randomUUID(), "2026-06-01", "2026-06-08",
                2L, 1, Map.of("realm", 1), Map.of("realm:event", 1),
                Map.of("oak", 1), Map.of(), List.of(entry));
    }
}
