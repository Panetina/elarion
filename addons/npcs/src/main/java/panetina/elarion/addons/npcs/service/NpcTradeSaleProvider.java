package panetina.elarion.addons.npcs.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeQuote;
import panetina.elarion.addons.npcs.model.NpcTradeSaleRecord;
import panetina.elarion.addons.npcs.model.NpcTradeSaleSettlement;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;

public interface NpcTradeSaleProvider {
    NpcTradeSaleSettlement settle(
            ServerPlayerEntity player,
            PlacedNpcRecord npc,
            NpcTradeOfferDefinition offer,
            NpcTradeQuote quote,
            NpcTradeSaleRecord record
    );

    static NpcTradeSaleProvider unavailable() {
        return (player, npc, offer, quote, record) ->
                NpcTradeSaleSettlement.failure("Economy services are unavailable.");
    }
}
