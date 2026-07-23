package panetina.elarion.addons.npcs.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeCatalogDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeDeliveryStack;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseRecord;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseSettlement;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseStatus;
import panetina.elarion.addons.npcs.model.NpcTradeQuote;
import panetina.elarion.addons.npcs.model.NpcTradeSaleRecord;
import panetina.elarion.addons.npcs.model.NpcTradeSaleSettlement;
import panetina.elarion.addons.npcs.model.NpcTradeSaleStatus;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.network.NpcTradePurchaseRequestPayload;
import panetina.elarion.addons.npcs.network.NpcTradePurchaseResultPayload;
import panetina.elarion.addons.npcs.storage.NpcTradePurchaseStorage;
import panetina.elarion.addons.npcs.storage.NpcTradeSaleStorage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class NpcTradePurchaseService {
    private static final UUID ZERO_UUID = new UUID(0L, 0L);
    private final Logger logger;
    private final NpcDefinitionService definitions;
    private final NpcTradeQuoteProvider quotes;
    private final NpcTradePurchaseProvider purchases;
    private final NpcTradeSaleProvider sales;
    private final NpcTradeStockService stocks;
    private final NpcTradePurchaseStorage storage;
    private final NpcTradeSaleStorage saleStorage;
    private final Map<UUID, NpcTradePurchaseRecord> journal = new LinkedHashMap<>();
    private final Map<UUID, NpcTradeSaleRecord> saleJournal = new LinkedHashMap<>();
    private MinecraftServer server;
    private boolean bound;

    public NpcTradePurchaseService(
            Logger logger,
            NpcDefinitionService definitions,
            NpcTradeQuoteProvider quotes,
            NpcTradePurchaseProvider purchases,
            NpcTradeSaleProvider sales,
            NpcTradeStockService stocks,
            NpcTradePurchaseStorage storage,
            NpcTradeSaleStorage saleStorage
    ) {
        this.logger = logger;
        this.definitions = definitions;
        this.quotes = quotes;
        this.purchases = purchases;
        this.sales = sales;
        this.stocks = stocks;
        this.storage = storage;
        this.saleStorage = saleStorage;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        this.bound = true;
        journal.clear();
        journal.putAll(storage.load(server));
        saleJournal.clear();
        saleJournal.putAll(saleStorage.load(server));
    }

    public synchronized void shutdown() {
        if (bound) storage.save(server, journal);
        if (bound) saleStorage.save(server, saleJournal);
    }

    public synchronized int resetAllPlayerState() {
        int changed = journal.size() + saleJournal.size();
        journal.clear();
        saleJournal.clear();
        if (bound) {
            storage.save(server, journal);
            saleStorage.save(server, saleJournal);
        }
        return changed;
    }

    public synchronized void reconcilePlayer(ServerPlayerEntity player) {
        if (!bound || player == null) return;
        for (NpcTradePurchaseRecord record : journal.values().stream()
                .filter(record -> record.playerId().equals(player.getUuid()))
                .filter(record -> record.status() == NpcTradePurchaseStatus.PAID)
                .toList()) {
            completeDelivery(player, record, "Recovered pending trade delivery.");
        }
    }

    public synchronized NpcTradePurchaseResultPayload purchase(
            ServerPlayerEntity player,
            PlacedNpcRecord placed,
            NpcDefinition npc,
            NpcTradePurchaseRequestPayload request
    ) {
        if (!bound) return failure(request, "NPC trade purchases are not ready.");
        if (player == null || placed == null || npc == null || request.purchaseId().equals(ZERO_UUID)) {
            return failure(request, "Invalid purchase request.");
        }
        NpcTradePurchaseRecord existing = journal.get(request.purchaseId());
        if (existing != null && existing.status() != NpcTradePurchaseStatus.PREPARED) {
            return replayOrReject(player, request, existing);
        }
        if (existing != null && !existing.matchesRequest(player.getUuid(), request.npcId(), request.nodeId(),
                request.catalogId(), request.catalogRevision(), request.offerId(), request.quantity())) {
            return failure(request, "Purchase ID was already used for another request.");
        }

        NpcTradeCatalogDefinition catalog = definitions.trade(npc.tradeCatalog()).orElse(null);
        if (catalog == null || !catalog.id().equals(request.catalogId())
                || catalog.hashCode() != request.catalogRevision()) {
            return failure(request, "The trade catalog changed. Reopen the trader.");
        }
        NpcTradeOfferDefinition offer = catalog.offers().stream()
                .filter(candidate -> candidate.id().equals(request.offerId()))
                .findFirst().orElse(null);
        if (offer != null && "sell".equals(offer.direction())) {
            return sell(player, placed, catalog, offer, request);
        }
        if (offer == null || !offer.enabled()) return failure(request, "That offer is unavailable.");
        if (!NpcTradeItemStacks.itemAvailable(offer)) return failure(request, "Required item content is unavailable.");
        int stockMax = stocks.maxQuantity(placed, offer, 64);
        if (stockMax < 1) return failure(request, "That offer is out of stock.");
        if (request.quantity() > stockMax) return failure(request, "Only " + stockMax + " remain in stock.");
        NpcTradeQuote quote = quotes.quote(placed, offer, request.quantity());
        if (!quote.valid()) return failure(request, quote.message().isBlank() ? "That purchase is unavailable." : quote.message());

        NpcTradeDeliveryStack delivery;
        try {
            delivery = NpcTradeDeliveryStack.from(offer, quote.quantity());
        } catch (ArithmeticException exception) {
            return failure(request, "That purchase is too large.");
        }
        long now = System.currentTimeMillis();
        NpcTradePurchaseRecord prepared = existing == null
                ? new NpcTradePurchaseRecord(
                request.purchaseId(), player.getUuid(), placed.id(), request.nodeId(),
                catalog.id(), catalog.hashCode(), offer.id(), quote.quantity(),
                quote.subtotal(), quote.taxBasisPoints(), quote.tax(), quote.total(),
                quote.policyRevision(), quote.taxAuthorityLabel(), delivery,
                NpcTradePurchaseStatus.PREPARED, null, null, "", now, now)
                : existing;
        if (existing == null) {
            journal.put(prepared.purchaseId(), prepared);
            persist();
        }

        NpcTradePurchaseSettlement settlement = purchases.settle(player, placed, offer, quote, prepared);
        if (!settlement.successful()) {
            NpcTradePurchaseRecord failed = prepared.failed(settlement.message(), System.currentTimeMillis());
            journal.put(failed.purchaseId(), failed);
            persist();
            return result(failed, false, settlement.message());
        }

        NpcTradePurchaseRecord paid = prepared.paid(
                settlement.operationId(), settlement.transactionId(), settlement.message(), System.currentTimeMillis());
        if (!stocks.consume(placed, offer, quote.quantity(), paid.purchaseId())) {
            NpcTradePurchaseRecord failed = prepared.failed("That offer is out of stock.", System.currentTimeMillis());
            journal.put(failed.purchaseId(), failed);
            persist();
            return result(failed, false, failed.message());
        }
        journal.put(paid.purchaseId(), paid);
        persist();
        return completeDelivery(player, paid, "Purchase complete.");
    }

    private NpcTradePurchaseResultPayload replayOrReject(
            ServerPlayerEntity player,
            NpcTradePurchaseRequestPayload request,
            NpcTradePurchaseRecord record
    ) {
        if (!record.matchesRequest(player.getUuid(), request.npcId(), request.nodeId(), request.catalogId(),
                request.catalogRevision(), request.offerId(), request.quantity())) {
            return failure(request, "Purchase ID was already used for another request.");
        }
        return switch (record.status()) {
            case COMPLETE -> result(record, true, record.message().isBlank()
                    ? "Purchase already completed." : record.message());
            case PAID -> completeDelivery(player, record, "Recovered pending trade delivery.");
            case FAILED -> result(record, false, record.message().isBlank()
                    ? "Purchase failed." : record.message());
            case PREPARED -> result(record, false, "Purchase was prepared but not paid. Try again with a new request.");
        };
    }

    private NpcTradePurchaseResultPayload sell(
            ServerPlayerEntity player,
            PlacedNpcRecord placed,
            NpcTradeCatalogDefinition catalog,
            NpcTradeOfferDefinition offer,
            NpcTradePurchaseRequestPayload request
    ) {
        NpcTradeSaleRecord existing = saleJournal.get(request.purchaseId());
        if (existing != null && !existing.matchesRequest(player.getUuid(), request.npcId(), request.nodeId(),
                request.catalogId(), request.catalogRevision(), request.offerId(), request.quantity())) {
            return failure(request, "Sale ID was already used for another request.");
        }
        if (!offer.enabled()) return failure(request, "That offer is unavailable.");
        if (!NpcTradeItemStacks.itemAvailable(offer)) return failure(request, "Required item content is unavailable.");
        int maxSellable = Math.min(offer.maxQuantity(), NpcTradeInventoryEscrow.maxSellable(player, offer));
        if (existing == null || existing.status() == NpcTradeSaleStatus.PREPARED) {
            if (maxSellable < 1) return failure(request, "You have no matching items to sell.");
            if (request.quantity() > maxSellable) return failure(request, "You can only sell " + maxSellable + ".");
        }
        NpcTradeQuote quote = quotes.quote(placed, offer, request.quantity());
        if (!quote.valid()) {
            return failure(request, quote.message().isBlank() ? "That sale is unavailable." : quote.message());
        }
        if (existing != null && existing.status() == NpcTradeSaleStatus.ITEMS_ESCROWED) {
            return finishEscrowedSale(player, placed, catalog, offer, quote, existing);
        }
        if (existing != null && existing.status() == NpcTradeSaleStatus.PAID) {
            return completePaidSale(placed, catalog, offer, existing);
        }
        if (existing != null && existing.status() == NpcTradeSaleStatus.STOCK_UPDATED) {
            NpcTradeSaleRecord complete = existing.complete("Sale complete.", System.currentTimeMillis());
            saleJournal.put(complete.saleId(), complete);
            persistSales();
            return saleResult(complete, true, "Sale complete.");
        }
        if (existing != null && existing.status() != NpcTradeSaleStatus.PREPARED) {
            return replayOrRejectSale(player, request, existing);
        }
        long now = System.currentTimeMillis();
        NpcTradeSaleRecord prepared = existing == null
                ? new NpcTradeSaleRecord(
                request.purchaseId(), player.getUuid(), placed.id(), request.nodeId(), catalog.id(),
                catalog.hashCode(), offer.id(), quote.quantity(), quote.subtotal(), quote.taxBasisPoints(),
                quote.tax(), quote.total(), quote.policyRevision(), quote.taxAuthorityLabel(), java.util.List.of(),
                NpcTradeSaleStatus.PREPARED, null, null, "", now, now)
                : existing;
        if (existing == null) {
            saleJournal.put(prepared.saleId(), prepared);
            persistSales();
        }

        var escrow = NpcTradeInventoryEscrow.escrow(player, offer, quote.quantity());
        if (!escrow.successful()) {
            NpcTradeSaleRecord failed = prepared.failed(escrow.message(), System.currentTimeMillis());
            saleJournal.put(failed.saleId(), failed);
            persistSales();
            return saleResult(failed, false, escrow.message());
        }

        NpcTradeSaleRecord escrowed = prepared.escrowed(
                escrow.escrow(), "Items escrowed.", System.currentTimeMillis());
        saleJournal.put(escrowed.saleId(), escrowed);
        persistSales();

        return finishEscrowedSale(player, placed, catalog, offer, quote, escrowed);
    }

    private NpcTradePurchaseResultPayload finishEscrowedSale(
            ServerPlayerEntity player,
            PlacedNpcRecord placed,
            NpcTradeCatalogDefinition catalog,
            NpcTradeOfferDefinition offer,
            NpcTradeQuote quote,
            NpcTradeSaleRecord escrowed
    ) {
        NpcTradeSaleSettlement settlement = sales.settle(player, placed, offer, quote, escrowed);
        if (!settlement.successful()) {
            boolean restored = NpcTradeInventoryEscrow.restore(player, escrowed.escrow());
            NpcTradeSaleRecord failed = restored
                    ? escrowed.restored(settlement.message(), System.currentTimeMillis())
                    : escrowed.failed(settlement.message().isBlank()
                    ? "Sale payout failed and items remain in escrow." : settlement.message(), System.currentTimeMillis());
            saleJournal.put(failed.saleId(), failed);
            persistSales();
            return saleResult(failed, false, restored
                    ? "Sale failed; items were restored." : failed.message());
        }

        NpcTradeSaleRecord paid = escrowed.paid(
                settlement.operationId(), settlement.transactionId(), settlement.message(), System.currentTimeMillis());
        saleJournal.put(paid.saleId(), paid);
        persistSales();
        return completePaidSale(placed, catalog, offer, paid);
    }

    private NpcTradePurchaseResultPayload completePaidSale(
            PlacedNpcRecord placed,
            NpcTradeCatalogDefinition catalog,
            NpcTradeOfferDefinition offer,
            NpcTradeSaleRecord paid
    ) {
        String stockMessage = updateSaleStock(placed, catalog, offer, paid);
        if (!stockMessage.isBlank()) {
            return saleResult(paid, false, stockMessage);
        }
        NpcTradeSaleRecord stocked = paid.stockUpdated("Sale stock updated.", System.currentTimeMillis());
        saleJournal.put(stocked.saleId(), stocked);
        persistSales();
        NpcTradeSaleRecord complete = stocked.complete("Sale complete.", System.currentTimeMillis());
        saleJournal.put(complete.saleId(), complete);
        persistSales();
        return saleResult(complete, true, "Sale complete.");
    }

    private String updateSaleStock(
            PlacedNpcRecord placed,
            NpcTradeCatalogDefinition catalog,
            NpcTradeOfferDefinition offer,
            NpcTradeSaleRecord paid
    ) {
        if (!"placed_npc".equals(offer.stockDestination())) return "";
        NpcTradeOfferDefinition destination = catalog.offers().stream()
                .filter(candidate -> candidate.id().equals(offer.destinationOfferId()))
                .findFirst()
                .orElse(null);
        if (destination == null || !"buy".equals(destination.direction())) {
            return "Sale paid, but destination stock is unavailable. Reopen after config is fixed.";
        }
        return stocks.supply(placed, destination, paid.quantity(), paid.saleId())
                ? ""
                : "Sale paid, but destination stock could not be updated. Try again shortly.";
    }

    private NpcTradePurchaseResultPayload replayOrRejectSale(
            ServerPlayerEntity player,
            NpcTradePurchaseRequestPayload request,
            NpcTradeSaleRecord record
    ) {
        if (!record.matchesRequest(player.getUuid(), request.npcId(), request.nodeId(), request.catalogId(),
                request.catalogRevision(), request.offerId(), request.quantity())) {
            return failure(request, "Sale ID was already used for another request.");
        }
        return switch (record.status()) {
            case COMPLETE -> saleResult(record, true, record.message().isBlank()
                    ? "Sale already completed." : record.message());
            case ITEMS_ESCROWED, PAID, STOCK_UPDATED -> saleResult(record, false,
                    "Sale recovery is pending. Reopen the trader shortly.");
            case FAILED -> saleResult(record, false, record.message().isBlank()
                    ? "Sale failed." : record.message());
            case RESTORED -> saleResult(record, false, record.message().isBlank()
                    ? "Sale failed; items were restored." : record.message());
            case PREPARED -> saleResult(record, false,
                    "Sale was prepared but not escrowed. Try again with a new request.");
        };
    }

    private NpcTradePurchaseResultPayload completeDelivery(
            ServerPlayerEntity player,
            NpcTradePurchaseRecord record,
            String message
    ) {
        NpcTradeItemStacks.deliver(player, record.delivery());
        NpcTradePurchaseRecord complete = record.complete(message, System.currentTimeMillis());
        journal.put(complete.purchaseId(), complete);
        persist();
        return result(complete, true, message);
    }

    private void persist() {
        try {
            storage.saveChecked(server, journal);
        } catch (IllegalStateException exception) {
            logger.error("Failed to persist NPC trade purchase journal", exception);
            throw exception;
        }
    }

    private void persistSales() {
        try {
            saleStorage.saveChecked(server, saleJournal);
        } catch (IllegalStateException exception) {
            logger.error("Failed to persist NPC trade sale journal", exception);
            throw exception;
        }
    }

    private static NpcTradePurchaseResultPayload result(
            NpcTradePurchaseRecord record,
            boolean successful,
            String message
    ) {
        return new NpcTradePurchaseResultPayload(record.purchaseId(), record.npcId(), record.nodeId(),
                record.offerId(), record.quantity(), record.subtotal(), record.tax(), record.total(),
                successful, message == null || message.isBlank() ? record.message() : message);
    }

    private static NpcTradePurchaseResultPayload failure(NpcTradePurchaseRequestPayload request, String message) {
        return new NpcTradePurchaseResultPayload(request.purchaseId(), request.npcId(), request.nodeId(),
                request.offerId(), request.quantity(), 0L, 0L, 0L, false, message);
    }

    private static NpcTradePurchaseResultPayload saleResult(
            NpcTradeSaleRecord record,
            boolean successful,
            String message
    ) {
        return new NpcTradePurchaseResultPayload(record.saleId(), record.npcId(), record.nodeId(),
                record.offerId(), record.quantity(), record.grossPayout(), record.fee(), record.netPayout(),
                successful, message == null || message.isBlank() ? record.message() : message);
    }
}
