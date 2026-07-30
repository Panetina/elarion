package panetina.elarion.addons.underworld.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.underworld.model.BanishmentRecord;
import panetina.elarion.addons.underworld.model.ElarionDeathType;
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

    @Test
    void recoverableNullRowsAndNestedCollectionsRetainValidState() throws Exception {
        String playerId = "00000000-0000-4000-8000-000000000003";
        Files.writeString(root.resolve("state.json"), """
                {
                  "schemaVersion": 3,
                  "corpses": {
                    "corpse-1": {
                      "corpseId": null,
                      "victimId": null,
                      "tombstoneVariant": null,
                      "deathType": null,
                      "protectedVictimItems": [null],
                      "pvpLootItems": null
                    },
                    "discarded": null,
                    "": {}
                  },
                  "sessions": {
                    "%s": {"playerId": null, "corpseId": null, "deathType": null},
                    "discarded": null
                  },
                  "souls": {
                    "%s": {"playerId": null, "fractures": 2},
                    "discarded": null
                  },
                  "recoveryVaults": {
                    "%s": [null, {"itemId": "minecraft:emerald", "count": 1}],
                    "discarded": null
                  },
                  "banishments": {
                    "%s": {"playerId": null, "reason": null},
                    "discarded": null
                  },
                  "afterlifeInventories": {
                    "%s": {"stacks": [null, {"itemId": "minecraft:diamond", "count": 1}]},
                    "discarded": null
                  },
                  "livingInventories": {"discarded": null}
                }
                """.formatted(playerId, playerId, playerId, playerId, playerId));
        UnderworldStorage storage = new UnderworldStorage(LoggerFactory.getLogger("test"), root);

        UnderworldState loaded = storage.load(root);

        assertEquals(1, loaded.corpses.size());
        assertEquals("corpse-1", loaded.corpses.get("corpse-1").corpseId);
        assertEquals(ElarionDeathType.UNKNOWN, loaded.corpses.get("corpse-1").deathType);
        assertTrue(loaded.corpses.get("corpse-1").protectedVictimItems.isEmpty());
        assertTrue(loaded.corpses.get("corpse-1").pvpLootItems.isEmpty());
        assertEquals(playerId, loaded.sessions.get(playerId).playerId);
        assertEquals(ElarionDeathType.UNKNOWN, loaded.sessions.get(playerId).deathType);
        assertEquals(playerId, loaded.souls.get(playerId).playerId);
        assertEquals(1, loaded.recoveryVaults.get(playerId).size());
        assertEquals(playerId, loaded.banishments.get(playerId).playerId);
        assertEquals(1, loaded.afterlifeInventories.get(playerId).stacks.size());
        assertTrue(loaded.livingInventories.isEmpty());
        assertTrue(Files.exists(root.resolve("state.json")));
        try (var files = Files.list(root)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().startsWith("state.json.corrupt-")));
        }
    }

    @Test
    void explicitNullCollectionsLoadAsMutableEmptyState() throws Exception {
        Files.writeString(root.resolve("state.json"), """
                {
                  "schemaVersion": 3,
                  "corpses": null,
                  "sessions": null,
                  "souls": null,
                  "recoveryVaults": null,
                  "banishments": null,
                  "afterlifeInventories": null,
                  "livingInventories": null
                }
                """);
        UnderworldStorage storage = new UnderworldStorage(LoggerFactory.getLogger("test"), root);

        UnderworldState loaded = storage.load(root);

        loaded.recoveryVaults.put("player", new java.util.ArrayList<>());
        assertTrue(loaded.corpses.isEmpty());
        assertTrue(loaded.sessions.isEmpty());
        assertTrue(loaded.souls.isEmpty());
        assertEquals(1, loaded.recoveryVaults.size());
        assertTrue(loaded.banishments.isEmpty());
        assertTrue(loaded.afterlifeInventories.isEmpty());
        assertTrue(loaded.livingInventories.isEmpty());
        assertTrue(Files.exists(root.resolve("state.json")));
    }
}
