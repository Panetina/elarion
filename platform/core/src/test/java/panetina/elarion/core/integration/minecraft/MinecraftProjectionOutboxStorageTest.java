package panetina.elarion.core.integration.minecraft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MinecraftProjectionOutboxStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void persistsEventsAndCoalescesUnsentState() {
        MinecraftProjectionOutboxStorage storage = new MinecraftProjectionOutboxStorage(
                LoggerFactory.getLogger("test"));
        MinecraftProjectionOutboxStorage.State state = MinecraftProjectionOutboxStorage.State.empty()
                .enqueue(projection(MinecraftProjectionProtocol.Mode.STATE, "realm", "ashlands", "old"))
                .enqueue(projection(MinecraftProjectionProtocol.Mode.EVENT, "chronicle", "event-one", "first"))
                .enqueue(projection(MinecraftProjectionProtocol.Mode.STATE, "realm", "ashlands", "new"));

        storage.save(tempDir, state);
        MinecraftProjectionOutboxStorage.State restored = storage.load(tempDir);

        assertEquals(2, restored.pending().size());
        assertEquals(1, restored.pending().getFirst().sequence());
        assertEquals("new", restored.pending().getFirst().payload().get("value"));
        assertEquals(2, restored.pending().getLast().sequence());
    }

    @Test
    void acknowledgedEntriesDoNotReturnAfterRestart() {
        MinecraftProjectionOutboxStorage storage = new MinecraftProjectionOutboxStorage(
                LoggerFactory.getLogger("test"));
        MinecraftProjectionOutboxStorage.State state = MinecraftProjectionOutboxStorage.State.empty()
                .enqueue(projection(MinecraftProjectionProtocol.Mode.EVENT, "chronicle", "one", "first"))
                .enqueue(projection(MinecraftProjectionProtocol.Mode.EVENT, "chronicle", "two", "second"));

        storage.save(tempDir, state.acknowledged(1));

        assertEquals(1, storage.load(tempDir).pending().size());
        assertEquals(2, storage.load(tempDir).pending().getFirst().sequence());
    }

    private static MinecraftProjectionProtocol.Projection projection(
            MinecraftProjectionProtocol.Mode mode,
            String kind,
            String entityId,
            String value
    ) {
        return new MinecraftProjectionProtocol.Projection(
                1, mode, kind, entityId, "", MinecraftProjectionProtocol.Visibility.PUBLIC,
                1, System.currentTimeMillis(), Map.of("value", value));
    }
}
