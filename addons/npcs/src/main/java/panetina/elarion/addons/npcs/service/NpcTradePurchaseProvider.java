package panetina.elarion.addons.npcs.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseRecord;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseSettlement;
import panetina.elarion.addons.npcs.model.NpcTradeQuote;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;

public interface NpcTradePurchaseProvider {
    NpcTradePurchaseSettlement settle(
            ServerPlayerEntity player,
            PlacedNpcRecord npc,
            NpcTradeOfferDefinition offer,
            NpcTradeQuote quote,
            NpcTradePurchaseRecord record
    );

    static NpcTradePurchaseProvider unavailable() {
        return (player, npc, offer, quote, record) ->
                NpcTradePurchaseSettlement.failure("Economy services are unavailable.");
    }
}
