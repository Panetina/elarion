package panetina.elarion.addons.npcs.integration;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.model.EconomyOperationKey;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;
import panetina.elarion.addons.economy.model.EconomyTaxCategory;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.npcs.model.NpcTaxJurisdictionKind;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeQuote;
import panetina.elarion.addons.npcs.model.NpcTradeSaleRecord;
import panetina.elarion.addons.npcs.model.NpcTradeSaleSettlement;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.service.NpcTradeSaleProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EconomyNpcTradeSaleProvider implements NpcTradeSaleProvider {
    private static final String OPERATION_OWNER = "elarion_npcs:trade_sell";

    @Override
    public NpcTradeSaleSettlement settle(
            ServerPlayerEntity player,
            PlacedNpcRecord npc,
            NpcTradeOfferDefinition offer,
            NpcTradeQuote quote,
            NpcTradeSaleRecord record
    ) {
        ElarionEconomyApi economy = ElarionEconomyApi.get();
        EconomyTaxAuthority authority = npc.taxJurisdictionKind() == NpcTaxJurisdictionKind.REALM
                ? EconomyTaxAuthority.realm(npc.taxJurisdictionId(), npc.worldId())
                : EconomyTaxAuthority.worldheart(npc.worldId());
        EconomyOperationKey operation = new EconomyOperationKey(OPERATION_OWNER, record.saleId());
        TransactionResult result = economy.payPlayerBalanceRewardOnce(
                player.getUuid(),
                operation,
                quote.total(),
                "NPC trade sale: " + offer.id(),
                "elarion_npcs",
                metadata(npc, offer, quote, authority));
        return new NpcTradeSaleSettlement(
                result.successful(),
                result.message(),
                operation.operationId(),
                result.transaction() == null ? null : result.transaction().id());
    }

    private static Map<String, String> metadata(
            PlacedNpcRecord npc,
            NpcTradeOfferDefinition offer,
            NpcTradeQuote quote,
            EconomyTaxAuthority authority
    ) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("npcId", npc.id().toString());
        metadata.put("npcDefinition", npc.definitionId());
        metadata.put("offerId", offer.id());
        metadata.put("quantity", Integer.toString(quote.quantity()));
        metadata.put("itemId", offer.itemId());
        metadata.put("grossPayout", Long.toString(quote.subtotal()));
        metadata.put("fee", Long.toString(quote.tax()));
        metadata.put("netPayout", Long.toString(quote.total()));
        metadata.put("feeBasisPoints", Integer.toString(quote.taxBasisPoints()));
        metadata.put("taxCategory", EconomyTaxCategory.NPC_TRADE.name());
        metadata.put("taxAuthorityKind", authority.kind().name());
        metadata.put("taxAuthorityId", authority.id());
        metadata.put("policyRevision", Long.toString(quote.policyRevision()));
        return Map.copyOf(metadata);
    }
}
