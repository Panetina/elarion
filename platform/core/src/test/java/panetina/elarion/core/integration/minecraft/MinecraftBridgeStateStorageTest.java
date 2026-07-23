package panetina.elarion.core.integration.minecraft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MinecraftBridgeStateStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void pendingAcknowledgementsSurviveRestartUntilConfirmed() {
        UUID bridgeManaged = UUID.fromString("38e6b73e-c699-4db2-9dbe-95c714c5c30f");
        MinecraftBridgeStateStorage storage = new MinecraftBridgeStateStorage(LoggerFactory.getLogger("test"));
        MinecraftBridgeStateStorage.State state = MinecraftBridgeStateStorage.State.empty()
                .applied(10)
                .applied(11)
                .bridgeAdded(bridgeManaged);

        storage.save(tempDir, state);
        MinecraftBridgeStateStorage.State restored = storage.load(tempDir);

        assertEquals(11, restored.cursor());
        assertEquals(java.util.List.of(10L, 11L), restored.pendingAcknowledgements());
        assertEquals(true, restored.isBridgeManaged(bridgeManaged));
        storage.save(tempDir, restored.acknowledged());
        MinecraftBridgeStateStorage.State acknowledged = storage.load(tempDir);
        assertEquals(java.util.List.of(), acknowledged.pendingAcknowledgements());
        assertEquals(true, acknowledged.isBridgeManaged(bridgeManaged));
    }

    @Test
    void bridgeOwnershipCanBeReleasedWithoutChangingCursorOrAcknowledgements() {
        UUID bridgeManaged = UUID.fromString("38e6b73e-c699-4db2-9dbe-95c714c5c30f");
        MinecraftBridgeStateStorage.State state = MinecraftBridgeStateStorage.State.empty()
                .applied(5)
                .bridgeAdded(bridgeManaged)
                .bridgeRemoved(bridgeManaged);

        assertEquals(5, state.cursor());
        assertEquals(java.util.List.of(5L), state.pendingAcknowledgements());
        assertEquals(false, state.isBridgeManaged(bridgeManaged));
    }
}
