package panetina.elarion.addons.npcs.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.model.NpcRelationshipRecord;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcRelationshipStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsRelationshipRecords() {
        NpcRelationshipStorage storage = new NpcRelationshipStorage(
                LoggerFactory.getLogger("npc-relationship-storage-test"), tempDir);
        UUID playerId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        NpcRelationshipRecord record = new NpcRelationshipRecord(playerId, npcId, 42, 123L);

        storage.save(null, Map.of(NpcRelationshipStorage.key(playerId, npcId), record));

        Map<String, NpcRelationshipRecord> loaded = storage.load(null);
        assertEquals(1, loaded.size());
        assertEquals(record, loaded.get(NpcRelationshipStorage.key(playerId, npcId)));
    }

    @Test
    void missingStateDefaultsEmpty() {
        NpcRelationshipStorage storage = new NpcRelationshipStorage(
                LoggerFactory.getLogger("npc-relationship-storage-test"), tempDir);

        assertTrue(storage.load(null).isEmpty());
    }
}
