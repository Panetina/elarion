package panetina.elarion.addons.npcs.integration;

import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.model.EconomyTradeDirection;
import panetina.elarion.addons.economy.model.EconomyTradePriceRequest;
import panetina.elarion.addons.economy.model.EconomyTradePriceQuote;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;
import panetina.elarion.addons.economy.model.EconomyTaxCategory;
import panetina.elarion.addons.npcs.model.NpcTaxJurisdictionKind;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeQuote;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.service.NpcTradeQuoteProvider;

import java.util.Map;

public final class EconomyNpcTradeQuoteProvider implements NpcTradeQuoteProvider {
    @Override
    public NpcTradeQuote quote(PlacedNpcRecord npc, NpcTradeOfferDefinition offer, int quantity) {
        EconomyTaxAuthority authority = npc.taxJurisdictionKind() == NpcTaxJurisdictionKind.REALM
                ? EconomyTaxAuthority.realm(npc.taxJurisdictionId(), npc.worldId())
                : EconomyTaxAuthority.worldheart(npc.worldId());
        EconomyTradeDirection direction = "sell".equals(offer.direction())
                ? EconomyTradeDirection.SELL : EconomyTradeDirection.BUY;
        EconomyTradePriceQuote quote = ElarionEconomyApi.get().quoteTradePrice(new EconomyTradePriceRequest(
                direction,
                authority,
                EconomyTaxCategory.NPC_TRADE,
                offer.priceKey(),
                false,
                offer.price(),
                quantity,
                "sell".equals(offer.direction()) ? Math.max(1, offer.maxQuantity()) : 64,
                -1,
                offer.stockLimit(),
                "",
                offer.id(),
                "elarion_npcs",
                Map.of("itemId", offer.itemId(), "direction", offer.direction())));
        String label = authority.kind().name().equals("REALM")
                ? "Realm tax" : "Worldheart tax";
        long total = direction == EconomyTradeDirection.SELL ? quote.totalPayout() : quote.totalCost();
        return new NpcTradeQuote(quote.quantity(), quote.maxQuantity(), quote.subtotal(),
                quote.feeOrTaxBasisPoints(), quote.feeOrTax(), total, quote.policyRevision(),
                label, quote.valid(), quote.message());
    }
}
