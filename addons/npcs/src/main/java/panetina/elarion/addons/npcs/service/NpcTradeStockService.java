package panetina.elarion.addons.npcs.service;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeStockRecord;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.storage.NpcTradeStockStorage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class NpcTradeStockService {
    public static final int UNLIMITED = -1;
    private static final int MAX_CONSUMED_IDS_PER_RECORD = 512;

    private final Logger logger;
    private final NpcTradeStockStorage storage;
    private final Map<String, NpcTradeStockRecord> stocks = new LinkedHashMap<>();
    private MinecraftServer server;
    private boolean bound;

    public NpcTradeStockService(Logger logger, NpcTradeStockStorage storage) {
        this.logger = logger;
        this.storage = storage;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        this.bound = true;
        stocks.clear();
        stocks.putAll(storage.load(server));
    }

    public synchronized void shutdown() {
        if (bound) storage.save(server, stocks);
    }

    public synchronized int available(PlacedNpcRecord placed, NpcTradeOfferDefinition offer) {
        if (!limited(offer) || placed == null) return UNLIMITED;
        NpcTradeStockRecord record = refreshed(placed, offer, System.currentTimeMillis());
        return Math.max(0, record.remaining());
    }

    public synchronized int maxQuantity(PlacedNpcRecord placed, NpcTradeOfferDefinition offer, int fallbackMax) {
        int available = available(placed, offer);
        if (available == UNLIMITED) return Math.max(1, fallbackMax);
        return Math.max(0, Math.min(Math.max(1, fallbackMax), available));
    }

    public synchronized boolean consume(
            PlacedNpcRecord placed,
            NpcTradeOfferDefinition offer,
            int quantity,
            UUID purchaseId
    ) {
        if (!limited(offer)) return true;
        if (!bound || placed == null || purchaseId == null || quantity < 1) return false;
        long now = System.currentTimeMillis();
        NpcTradeStockRecord record = refreshed(placed, offer, now);
        if (record.consumed(purchaseId)) return true;
        if (record.remaining() < quantity) return false;
        NpcTradeStockRecord updated = record
                .withRemaining(record.remaining() - quantity, record.lastRestockAtMillis())
                .withConsumed(purchaseId, MAX_CONSUMED_IDS_PER_RECORD);
        stocks.put(key(placed, offer), updated);
        persist();
        return true;
    }

    public synchronized boolean supply(
            PlacedNpcRecord placed,
            NpcTradeOfferDefinition offer,
            int quantity,
            UUID saleId
    ) {
        if (!limited(offer)) return true;
        if (!bound || placed == null || saleId == null || quantity < 1) return false;
        long now = System.currentTimeMillis();
        NpcTradeStockRecord record = refreshed(placed, offer, now);
        if (record.consumed(saleId)) return true;
        int updatedRemaining = Math.min(offer.stockLimit(), record.remaining() + quantity);
        NpcTradeStockRecord updated = record
                .withRemaining(updatedRemaining, record.lastRestockAtMillis())
                .withConsumed(saleId, MAX_CONSUMED_IDS_PER_RECORD);
        stocks.put(key(placed, offer), updated);
        persist();
        return true;
    }

    private NpcTradeStockRecord refreshed(PlacedNpcRecord placed, NpcTradeOfferDefinition offer, long now) {
        String key = key(placed, offer);
        NpcTradeStockRecord record = stocks.get(key);
        if (record == null) {
            record = new NpcTradeStockRecord(placed.id(), offer.id(), offer.stockLimit(), now, java.util.List.of());
            stocks.put(key, record);
            persist();
            return record;
        }
        if (offer.stockLimit() < 1 || offer.restockIntervalSeconds() < 1L) return cap(record, offer, now);
        long intervalMillis = offer.restockIntervalSeconds() * 1000L;
        if (intervalMillis <= 0L || now < record.lastRestockAtMillis() + intervalMillis) {
            return cap(record, offer, record.lastRestockAtMillis());
        }
        long periods = Math.max(1L, (now - record.lastRestockAtMillis()) / intervalMillis);
        long amountPerPeriod = offer.restockAmount() < 1 ? offer.stockLimit() : offer.restockAmount();
        long restored = Math.min((long) offer.stockLimit(), record.remaining() + periods * amountPerPeriod);
        long restockAt = record.lastRestockAtMillis() + periods * intervalMillis;
        NpcTradeStockRecord updated = record.withRemaining((int) restored, restockAt);
        stocks.put(key, updated);
        persist();
        return updated;
    }

    private NpcTradeStockRecord cap(NpcTradeStockRecord record, NpcTradeOfferDefinition offer, long restockAt) {
        if (record.remaining() <= offer.stockLimit()) return record;
        NpcTradeStockRecord updated = record.withRemaining(offer.stockLimit(), restockAt);
        stocks.put(NpcTradeStockStorage.key(record.npcId().toString(), record.offerId()), updated);
        persist();
        return updated;
    }

    private void persist() {
        try {
            storage.saveChecked(server, stocks);
        } catch (IllegalStateException exception) {
            logger.error("Failed to persist NPC trade stock state", exception);
            throw exception;
        }
    }

    private static boolean limited(NpcTradeOfferDefinition offer) {
        return offer != null && offer.stockLimit() > 0;
    }

    private static String key(PlacedNpcRecord placed, NpcTradeOfferDefinition offer) {
        return NpcTradeStockStorage.key(placed.id().toString(), offer.id());
    }
}
