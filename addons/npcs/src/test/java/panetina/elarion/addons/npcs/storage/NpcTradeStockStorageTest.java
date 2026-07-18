package panetina.elarion.addons.npcs.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.service.NpcTradeStockService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcTradeStockStorageTest {
    @TempDir
    Path root;

    @Test
    void stockConsumesAndReplaysPurchaseIdIdempotently() {
        NpcTradeStockService service = new NpcTradeStockService(
                LoggerFactory.getLogger("npc-stock-test"),
                new NpcTradeStockStorage(LoggerFactory.getLogger("npc-stock-test"), root));
        service.bind(null);
        PlacedNpcRecord npc = placed();
        NpcTradeOfferDefinition offer = offer(3);
        UUID purchaseId = UUID.randomUUID();

        assertEquals(3, service.available(npc, offer));
        assertTrue(service.consume(npc, offer, 2, purchaseId));
        assertEquals(1, service.available(npc, offer));
        assertTrue(service.consume(npc, offer, 2, purchaseId));
        assertEquals(1, service.available(npc, offer));
        service.shutdown();

        NpcTradeStockStorage storage = new NpcTradeStockStorage(
                LoggerFactory.getLogger("npc-stock-test"), root);
        var loaded = storage.load(null);

        assertEquals(1, loaded.values().iterator().next().remaining());
        assertTrue(Files.exists(root.resolve("trade-stock.json")));
    }

    @Test
    void stockSupplyReplaysSaleIdIdempotentlyAndCapsAtLimit() {
        NpcTradeStockService service = new NpcTradeStockService(
                LoggerFactory.getLogger("npc-stock-test"),
                new NpcTradeStockStorage(LoggerFactory.getLogger("npc-stock-test"), root));
        service.bind(null);
        PlacedNpcRecord npc = placed();
        NpcTradeOfferDefinition offer = offer(5);

        assertTrue(service.consume(npc, offer, 4, UUID.randomUUID()));
        assertEquals(1, service.available(npc, offer));

        UUID saleId = UUID.randomUUID();
        assertTrue(service.supply(npc, offer, 3, saleId));
        assertEquals(4, service.available(npc, offer));
        assertTrue(service.supply(npc, offer, 3, saleId));
        assertEquals(4, service.available(npc, offer));

        assertTrue(service.supply(npc, offer, 99, UUID.randomUUID()));
        assertEquals(5, service.available(npc, offer));
        service.shutdown();

        NpcTradeStockStorage storage = new NpcTradeStockStorage(
                LoggerFactory.getLogger("npc-stock-test"), root);
        var loaded = storage.load(null);

        assertEquals(5, loaded.values().iterator().next().remaining());
    }

    @Test
    void unsupportedSchemaFailsClosedWithoutReplacingFile() throws Exception {
        Path file = root.resolve("trade-stock.json");
        String unsupported = "{\"schemaVersion\":99,\"stocks\":[]}";
        Files.writeString(file, unsupported, StandardCharsets.UTF_8);
        NpcTradeStockStorage storage = new NpcTradeStockStorage(
                LoggerFactory.getLogger("npc-stock-test"), root);

        assertThrows(IllegalStateException.class, () -> storage.load(null));
        assertEquals(unsupported, Files.readString(file));
    }

    private static PlacedNpcRecord placed() {
        return new PlacedNpcRecord(UUID.randomUUID(), "trader", "trader", UUID.randomUUID(),
                "minecraft:overworld", 0.0D, 64.0D, 0.0D, 0.0F, 0.0F,
                "", "", "", "", UUID.randomUUID(), 1L);
    }

    private static NpcTradeOfferDefinition offer(int stockLimit) {
        return new NpcTradeOfferDefinition("ticket", "buy", "Ticket", "",
                "minecraft:paper", 1, "", List.of(), List.of(), 0,
                "portal.ticket", 10L, stockLimit, 0, 0L, true);
    }
}
