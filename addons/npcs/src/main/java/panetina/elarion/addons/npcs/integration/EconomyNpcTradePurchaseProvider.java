package panetina.elarion.addons.npcs.integration;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyOperationKey;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;
import panetina.elarion.addons.economy.model.EconomyTaxCategory;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.npcs.model.NpcTaxJurisdictionKind;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseRecord;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseSettlement;
import panetina.elarion.addons.npcs.model.NpcTradeQuote;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.service.NpcTradePurchaseProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EconomyNpcTradePurchaseProvider implements NpcTradePurchaseProvider {
    private static final String OPERATION_OWNER = "elarion_npcs:trade_buy";

    @Override
    public NpcTradePurchaseSettlement settle(
            ServerPlayerEntity player,
            PlacedNpcRecord npc,
            NpcTradeOfferDefinition offer,
            NpcTradeQuote quote,
            NpcTradePurchaseRecord record
    ) {
        ElarionEconomyApi economy = ElarionEconomyApi.get();
        EconomyTaxAuthority authority = npc.taxJurisdictionKind() == NpcTaxJurisdictionKind.REALM
                ? EconomyTaxAuthority.realm(npc.taxJurisdictionId(), npc.worldId())
                : EconomyTaxAuthority.worldheart(npc.worldId());
        EconomyAccount destination = economy.taxDestination(authority);
        EconomyOperationKey operation = new EconomyOperationKey(OPERATION_OWNER, record.purchaseId());
        TransactionResult result = economy.payPhysicalOnlyOnce(
                player,
                operation,
                destination,
                quote.total(),
                "NPC trade purchase: " + offer.id(),
                "elarion_npcs",
                metadata(npc, offer, quote, authority));
        return new NpcTradePurchaseSettlement(
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
        metadata.put("subtotal", Long.toString(quote.subtotal()));
        metadata.put("tax", Long.toString(quote.tax()));
        metadata.put("taxBasisPoints", Integer.toString(quote.taxBasisPoints()));
        metadata.put("taxCategory", EconomyTaxCategory.NPC_TRADE.name());
        metadata.put("taxAuthorityKind", authority.kind().name());
        metadata.put("taxAuthorityId", authority.id());
        metadata.put("policyRevision", Long.toString(quote.policyRevision()));
        return Map.copyOf(metadata);
    }
}
