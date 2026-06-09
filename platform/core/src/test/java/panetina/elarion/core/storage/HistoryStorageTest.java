package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.HistoryEvent;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    }
}
