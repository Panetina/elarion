package panetina.elarion.addons.npcs.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.model.NpcTradeEnchantmentDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeEscrowStack;
import panetina.elarion.addons.npcs.model.NpcTradeSaleRecord;
import panetina.elarion.addons.npcs.model.NpcTradeSaleStatus;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcTradeSaleStorageTest {
    @TempDir
    Path root;

    @Test
    void saleJournalRoundTripsEscrowedItems() {
        UUID id = UUID.randomUUID();
        NpcTradeSaleRecord record = record(id).escrowed(List.of(escrow()), "Escrowed", 2L);
        LinkedHashMap<UUID, NpcTradeSaleRecord> records = new LinkedHashMap<>();
        records.put(id, record);
        NpcTradeSaleStorage storage = new NpcTradeSaleStorage(
                LoggerFactory.getLogger("npc-sale-test"), root);

        storage.saveChecked(null, records);
        var loaded = storage.load(null);

        assertEquals(NpcTradeSaleStatus.ITEMS_ESCROWED, loaded.get(id).status());
        assertEquals("minecraft:cobblestone", loaded.get(id).escrow().getFirst().itemId());
        assertEquals("stone-stack-fingerprint", loaded.get(id).escrow().getFirst().fingerprint());
        assertTrue(Files.exists(root.resolve("trade-sales.json")));
    }

    @Test
    void saleJournalRoundTripsEncodedEscrowPayload() {
        UUID id = UUID.randomUUID();
        NpcTradeEscrowStack encoded = new NpcTradeEscrowStack(
                "minecraft:diamond_chestplate",
                1,
                "",
                List.of(),
                List.of(),
                0,
                "encoded-fingerprint",
                "base64-stack-payload",
                7,
                "Inventory slot 8");
        NpcTradeSaleRecord record = record(id).escrowed(List.of(encoded), "Escrowed", 2L);
        LinkedHashMap<UUID, NpcTradeSaleRecord> records = new LinkedHashMap<>();
        records.put(id, record);
        Path encodedRoot = root.resolve("encoded-sale");
        NpcTradeSaleStorage storage = new NpcTradeSaleStorage(
                LoggerFactory.getLogger("npc-sale-test"), encodedRoot);

        storage.saveChecked(null, records);
        NpcTradeEscrowStack loaded = storage.load(null).get(id).escrow().getFirst();

        assertEquals("base64-stack-payload", loaded.stackNbt());
        assertEquals(7, loaded.sourceSlot());
        assertEquals("Inventory slot 8", loaded.sourceLabel());
    }

    @Test
    void saleReplayStatesRemainExplicitAcrossRestartLoads() {
        UUID id = UUID.randomUUID();
        UUID operation = UUID.randomUUID();
        UUID transaction = UUID.randomUUID();
        NpcTradeSaleRecord record = record(id)
                .escrowed(List.of(escrow()), "Escrowed", 2L)
                .paid(operation, transaction, "Paid", 3L)
                .stockUpdated("Stock updated", 4L);
        LinkedHashMap<UUID, NpcTradeSaleRecord> records = new LinkedHashMap<>();
        records.put(id, record);
        NpcTradeSaleStorage storage = new NpcTradeSaleStorage(
                LoggerFactory.getLogger("npc-sale-test"), root);

        storage.saveChecked(null, records);
        NpcTradeSaleStorage restarted = new NpcTradeSaleStorage(
                LoggerFactory.getLogger("npc-sale-test"), root);
        NpcTradeSaleRecord loaded = restarted.load(null).get(id);

        assertEquals(NpcTradeSaleStatus.STOCK_UPDATED, loaded.status());
        assertEquals(operation, loaded.economyOperationId());
        assertEquals(transaction, loaded.economyTransactionId());
        assertEquals("Stock updated", loaded.message());
        assertEquals(NpcTradeSaleStatus.COMPLETE, loaded.complete("Complete", 5L).status());
        assertEquals(NpcTradeSaleStatus.RESTORED, loaded.restored("Restored", 5L).status());
    }

    @Test
    void requestMatchingUsesStableServerAuthoredFields() {
        NpcTradeSaleRecord record = record(UUID.randomUUID());

        assertTrue(record.matchesRequest(
                record.playerId(), record.npcId(), "trade", "worldheart_trader",
                7L, "cobblestone_buyback", 64));
    }

    @Test
    void unsupportedSchemaFailsClosedWithoutReplacingFile() throws Exception {
        Path file = root.resolve("trade-sales.json");
        String unsupported = "{\"schemaVersion\":99,\"sales\":[]}";
        Files.writeString(file, unsupported, StandardCharsets.UTF_8);
        NpcTradeSaleStorage storage = new NpcTradeSaleStorage(
                LoggerFactory.getLogger("npc-sale-test"), root);

        assertThrows(IllegalStateException.class, () -> storage.load(null));
        assertEquals(unsupported, Files.readString(file));
    }

    private static NpcTradeSaleRecord record(UUID id) {
        return new NpcTradeSaleRecord(
                id, UUID.randomUUID(), UUID.randomUUID(), "trade", "worldheart_trader",
                7L, "cobblestone_buyback", 64, 64L, 0, 0L, 64L, 3L,
                "Worldheart tax", List.of(), NpcTradeSaleStatus.PREPARED,
                null, null, "", 1L, 1L);
    }

    private static NpcTradeEscrowStack escrow() {
        return new NpcTradeEscrowStack(
                "minecraft:cobblestone",
                64,
                "",
                List.of("Clean stone only."),
                List.of(new NpcTradeEnchantmentDefinition("minecraft:unbreaking", 1)),
                0,
                "stone-stack-fingerprint");
    }
}
