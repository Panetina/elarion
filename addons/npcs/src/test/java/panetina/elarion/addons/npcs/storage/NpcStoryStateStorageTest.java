package panetina.elarion.addons.npcs.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.model.NpcStoryStateRecord;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcStoryStateStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsStoryState() {
        NpcStoryStateStorage storage = new NpcStoryStateStorage(
                LoggerFactory.getLogger("npc-story-storage-test"), tempDir);
        UUID playerId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        NpcStoryStateRecord state = new NpcStoryStateRecord(playerId, npcId,
                Set.of("trusted"), Set.of("dialogue/root/pledge"), "allied", "returning", 123L);

        storage.save(null, Map.of(NpcStoryStateStorage.key(playerId, npcId), state));

        assertEquals(state, storage.load(null).get(NpcStoryStateStorage.key(playerId, npcId)));
    }

    @Test
    void missingStateDefaultsEmpty() {
        NpcStoryStateStorage storage = new NpcStoryStateStorage(
                LoggerFactory.getLogger("npc-story-storage-test"), tempDir);

        assertTrue(storage.load(null).isEmpty());
    }
}
