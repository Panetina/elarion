package panetina.elarion.addons.npcs.service;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeCatalogDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeQuote;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.network.NpcTradeOfferPayload;
import panetina.elarion.addons.npcs.network.NpcTradeQuotePayload;
import panetina.elarion.addons.npcs.network.NpcTradeQuoteRequestPayload;
import panetina.elarion.addons.npcs.network.NpcTradeSnapshotPayload;

import java.util.ArrayList;
import java.util.List;

public final class NpcTradeSnapshotService {
    private final NpcDefinitionService definitions;
    private final NpcTradeQuoteProvider quotes;
    private final NpcTradeStockService stocks;

    public NpcTradeSnapshotService(
            NpcDefinitionService definitions,
            NpcTradeQuoteProvider quotes,
            NpcTradeStockService stocks
    ) {
        this.definitions = definitions;
        this.quotes = quotes;
        this.stocks = stocks;
    }

    public NpcTradeSnapshotPayload snapshot(
            ServerPlayerEntity player,
            PlacedNpcRecord placed,
            NpcDefinition npc,
            String nodeId
    ) {
        java.util.UUID npcId = placed.id();
        if (npc.tradeCatalog().isBlank()) {
            return new NpcTradeSnapshotPayload(npcId, nodeId, "", 0L, List.of(),
                    "This trader has no catalog configured.");
        }
        NpcTradeCatalogDefinition catalog = definitions.trade(npc.tradeCatalog()).orElse(null);
        if (catalog == null) {
            return new NpcTradeSnapshotPayload(npcId, nodeId, npc.tradeCatalog(), 0L, List.of(),
                    "This trader's catalog is unavailable.");
        }
        List<NpcTradeOfferPayload> offers = new ArrayList<>();
        for (NpcTradeOfferDefinition offer : catalog.offers()) {
            if (offers.size() >= NpcTradeSnapshotPayload.MAX_OFFERS) break;
            offers.add(offer(player, placed, offer));
        }
        String empty = offers.isEmpty() ? "No goods are currently listed." : "";
        return new NpcTradeSnapshotPayload(npcId, nodeId, catalog.id(), catalog.hashCode(), offers, empty);
    }

    public NpcTradeQuotePayload quote(
            ServerPlayerEntity player,
            PlacedNpcRecord placed,
            NpcDefinition npc,
            NpcTradeQuoteRequestPayload request
    ) {
        NpcTradeCatalogDefinition catalog = definitions.trade(npc.tradeCatalog()).orElse(null);
        if (catalog == null || !catalog.id().equals(request.catalogId()) || catalog.hashCode() != request.catalogRevision()) {
            return invalidQuote(request, "The trade catalog changed. Reopen the trader.");
        }
        NpcTradeOfferDefinition offer = catalog.offers().stream()
                .filter(candidate -> candidate.id().equals(request.offerId()))
                .findFirst().orElse(null);
        if (offer == null || !offer.enabled()) return invalidQuote(request, "That offer is unavailable.");
        int stockMax = maxQuantity(player, placed, offer);
        if (stockMax < 1) return invalidQuote(request, "buy".equals(offer.direction())
                ? "That offer is out of stock." : "You have no matching items to sell.");
        int quantity = Math.max(1, Math.min(request.quantity(), stockMax));
        NpcTradeQuote quote = quotes.quote(placed, offer, quantity);
        int maxQuantity = Math.min(quote.maxQuantity(), stockMax);
        return new NpcTradeQuotePayload(placed.id(), request.nodeId(), catalog.id(), request.catalogRevision(),
                offer.id(), quote.quantity(), maxQuantity, quote.subtotal(), quote.taxBasisPoints(),
                quote.tax(), quote.total(), quote.policyRevision(), quote.taxAuthorityLabel(),
                quote.valid(), quote.message());
    }

    private NpcTradeOfferPayload offer(
            ServerPlayerEntity player, PlacedNpcRecord placed, NpcTradeOfferDefinition definition
    ) {
        boolean itemAvailable = NpcTradeItemStacks.itemAvailable(definition);
        int stockMax = maxQuantity(player, placed, definition);
        ItemStack stack = NpcTradeItemStacks.preview(player, definition);
        NpcTradeQuote quote = stockMax < 1
                ? new NpcTradeQuote(1, 1, 0L, 0, 0L, 0L, 0L, "", false, "Out of stock.")
                : quotes.quote(placed, definition, 1);
        int maxQuantity = stockMax < 1 ? 1 : Math.min(quote.maxQuantity(), stockMax);
        String disabledReason = !definition.enabled()
                ? "This offer is disabled."
                : stockMax < 1
                ? "buy".equals(definition.direction()) ? "Out of stock." : "No matching items."
                : !itemAvailable
                ? "Required item content is unavailable."
                : !quote.valid()
                ? quote.message()
                : "";
        return new NpcTradeOfferPayload(
                definition.id(), definition.direction(), definition.label(), definition.subtitle(),
                definition.price(), quote.quantity(), maxQuantity, quote.subtotal(),
                quote.taxBasisPoints(), quote.tax(), quote.total(), quote.policyRevision(),
                quote.taxAuthorityLabel(), quote.valid() && itemAvailable && definition.enabled() && stockMax != 0,
                disabledReason, "buy".equals(definition.direction()) ? stocks.available(placed, definition) : stockMax,
                stack);
    }

    private int maxQuantity(ServerPlayerEntity player, PlacedNpcRecord placed, NpcTradeOfferDefinition offer) {
        if ("sell".equals(offer.direction())) {
            return Math.min(Math.max(1, offer.maxQuantity()), NpcTradeInventoryEscrow.maxSellable(player, offer));
        }
        return stocks.maxQuantity(placed, offer, 64);
    }

    private static NpcTradeQuotePayload invalidQuote(NpcTradeQuoteRequestPayload request, String message) {
        return new NpcTradeQuotePayload(request.npcId(), request.nodeId(), request.catalogId(),
                request.catalogRevision(), request.offerId(), request.quantity(), 1,
                0L, 0, 0L, 0L, 0L, "", false, message);
    }

}
