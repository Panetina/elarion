package panetina.elarion.core.integration.minecraft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MinecraftBridgeStateStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void pendingAcknowledgementsSurviveRestartUntilConfirmed() {
        MinecraftBridgeStateStorage storage = new MinecraftBridgeStateStorage(LoggerFactory.getLogger("test"));
        MinecraftBridgeStateStorage.State state = MinecraftBridgeStateStorage.State.empty()
                .applied(10)
                .applied(11);

        storage.save(tempDir, state);
        MinecraftBridgeStateStorage.State restored = storage.load(tempDir);

        assertEquals(11, restored.cursor());
        assertEquals(java.util.List.of(10L, 11L), restored.pendingAcknowledgements());
        storage.save(tempDir, restored.acknowledged());
        assertEquals(java.util.List.of(), storage.load(tempDir).pendingAcknowledgements());
    }
}
