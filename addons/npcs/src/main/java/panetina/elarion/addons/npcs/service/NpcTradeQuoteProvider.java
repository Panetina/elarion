package panetina.elarion.addons.npcs.service;

import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeQuote;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;

public interface NpcTradeQuoteProvider {
    NpcTradeQuote quote(PlacedNpcRecord npc, NpcTradeOfferDefinition offer, int quantity);

    static NpcTradeQuoteProvider unavailable() {
        return (npc, offer, quantity) -> new NpcTradeQuote(
                Math.max(1, quantity), 1, 0L, 0, 0L, 0L, 0L, "",
                false, "Economy services are unavailable.");
    }
}
