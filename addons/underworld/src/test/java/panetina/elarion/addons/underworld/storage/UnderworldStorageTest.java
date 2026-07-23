package panetina.elarion.addons.underworld.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.underworld.model.BanishmentRecord;
import panetina.elarion.addons.underworld.model.InventorySnapshot;
import panetina.elarion.addons.underworld.model.StoredItemStack;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnderworldStorageTest {
    @TempDir
    Path root;

    @Test
    void legacyStateLoadsAndRewritesWithCurrentSchema() throws Exception {
        Files.writeString(root.resolve("state.json"), "{\"corpses\":{},\"sessions\":{},\"souls\":{}}");
        UnderworldStorage storage = new UnderworldStorage(LoggerFactory.getLogger("test"), root);

        UnderworldState loaded = storage.load(root);
        storage.save(root, loaded);

        assertEquals(UnderworldState.CURRENT_SCHEMA_VERSION, loaded.schemaVersion);
        assertTrue(loaded.recoveryVaults.isEmpty());
        assertTrue(loaded.banishments.isEmpty());
        assertTrue(Files.readString(root.resolve("state.json")).contains("\"schemaVersion\": 3"));
    }

    @Test
    void banishmentRoundTripsWithReasonAndPermanentExpiry() {
        UnderworldState state = new UnderworldState();
        BanishmentRecord record = new BanishmentRecord();
        record.playerId = "00000000-0000-4000-8000-000000000001";
        record.playerName = "Rulebreaker";
        record.issuedBy = "Admin";
        record.reason = "Cheating";
        record.issuedAt = 100L;
        record.expiresAt = 0L;
        state.banishments.put(record.playerId, record);
        UnderworldStorage storage = new UnderworldStorage(LoggerFactory.getLogger("test"), root);

        storage.save(root, state);
        UnderworldState loaded = storage.load(root);

        BanishmentRecord restored = loaded.banishments.get(record.playerId);
        assertEquals("Rulebreaker", restored.playerName);
        assertEquals("Cheating", restored.reason);
        assertTrue(restored.permanent());
    }

    @Test
    void afterlifeAndLivingInventoriesRoundTripSeparately() {
        UnderworldState state = new UnderworldState();
        InventorySnapshot afterlife = new InventorySnapshot();
        afterlife.stacks.add(new StoredItemStack("minecraft:emerald", 3));
        afterlife.selectedHotbarSlot = 2;
        afterlife.experienceLevel = 4;
        InventorySnapshot living = new InventorySnapshot();
        living.stacks.add(new StoredItemStack("minecraft:diamond", 1));
        String playerId = "00000000-0000-4000-8000-000000000002";
        state.afterlifeInventories.put(playerId, afterlife);
        state.livingInventories.put(playerId, living);
        UnderworldStorage storage = new UnderworldStorage(LoggerFactory.getLogger("test"), root);

        storage.save(root, state);
        UnderworldState loaded = storage.load(root);

        assertEquals("minecraft:emerald", loaded.afterlifeInventories.get(playerId).stacks.getFirst().itemId);
        assertEquals(2, loaded.afterlifeInventories.get(playerId).selectedHotbarSlot);
        assertEquals("minecraft:diamond", loaded.livingInventories.get(playerId).stacks.getFirst().itemId);
    }

    @Test
    void unsupportedFutureSchemaIsQuarantined() throws Exception {
        Path stateFile = root.resolve("state.json");
        Files.writeString(stateFile, "{\"schemaVersion\":99,\"corpses\":{}}");
        UnderworldStorage storage = new UnderworldStorage(LoggerFactory.getLogger("test"), root);

        UnderworldState loaded = storage.load(root);

        assertEquals(UnderworldState.CURRENT_SCHEMA_VERSION, loaded.schemaVersion);
        assertTrue(loaded.corpses.isEmpty());
        assertFalse(Files.exists(stateFile));
        try (var files = Files.list(root)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().startsWith("state.json.corrupt-")));
        }
    }
}
