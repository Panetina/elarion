package panetina.elarion.addons.npcs.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.model.NpcTradeDeliveryStack;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseRecord;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseStatus;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcTradePurchaseStorageTest {
    @TempDir
    Path root;

    @Test
    void purchaseJournalRoundTrips() {
        UUID id = UUID.randomUUID();
        NpcTradePurchaseRecord record = record(id).paid(UUID.randomUUID(), UUID.randomUUID(), "Paid", 2L);
        LinkedHashMap<UUID, NpcTradePurchaseRecord> records = new LinkedHashMap<>();
        records.put(id, record);
        NpcTradePurchaseStorage storage = new NpcTradePurchaseStorage(
                LoggerFactory.getLogger("npc-purchase-test"), root);

        storage.saveChecked(null, records);
        var loaded = storage.load(null);

        assertEquals(NpcTradePurchaseStatus.PAID, loaded.get(id).status());
        assertEquals("minecraft:cobblestone", loaded.get(id).delivery().itemId());
        assertTrue(Files.exists(root.resolve("trade-purchases.json")));
    }

    @Test
    void unsupportedSchemaFailsClosedWithoutReplacingFile() throws Exception {
        Path file = root.resolve("trade-purchases.json");
        String unsupported = "{\"schemaVersion\":99,\"purchases\":[]}";
        Files.writeString(file, unsupported, StandardCharsets.UTF_8);
        NpcTradePurchaseStorage storage = new NpcTradePurchaseStorage(
                LoggerFactory.getLogger("npc-purchase-test"), root);

        assertThrows(IllegalStateException.class, () -> storage.load(null));
        assertEquals(unsupported, Files.readString(file));
    }

    private static NpcTradePurchaseRecord record(UUID id) {
        return new NpcTradePurchaseRecord(
                id, UUID.randomUUID(), UUID.randomUUID(), "trade", "catalog",
                1L, "stone", 2, 50L, 100, 1L, 51L, 3L,
                "Worldheart tax",
                new NpcTradeDeliveryStack("minecraft:cobblestone", 2, "", List.of(), List.of(), 0),
                NpcTradePurchaseStatus.PREPARED, null, null, "", 1L, 1L);
    }
}
