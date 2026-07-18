package panetina.elarion.addons.npcs.model;

import java.util.UUID;

public record NpcTradePurchaseRecord(
        UUID purchaseId,
        UUID playerId,
        UUID npcId,
        String nodeId,
        String catalogId,
        long catalogRevision,
        String offerId,
        int quantity,
        long subtotal,
        int taxBasisPoints,
        long tax,
        long total,
        long policyRevision,
        String taxAuthorityLabel,
        NpcTradeDeliveryStack delivery,
        NpcTradePurchaseStatus status,
        UUID economyOperationId,
        UUID economyTransactionId,
        String message,
        long createdAt,
        long updatedAt
) {
    public NpcTradePurchaseRecord {
        if (purchaseId == null) throw new IllegalArgumentException("Purchase ID is required");
        if (playerId == null) throw new IllegalArgumentException("Player ID is required");
        if (npcId == null) throw new IllegalArgumentException("NPC ID is required");
        nodeId = nodeId == null ? "" : nodeId.trim();
        catalogId = catalogId == null ? "" : catalogId.trim();
        offerId = offerId == null ? "" : offerId.trim();
        quantity = Math.max(1, Math.min(64, quantity));
        taxBasisPoints = Math.max(0, Math.min(10_000, taxBasisPoints));
        taxAuthorityLabel = taxAuthorityLabel == null ? "" : taxAuthorityLabel.trim();
        if (delivery == null) throw new IllegalArgumentException("Delivery stack is required");
        status = status == null ? NpcTradePurchaseStatus.PREPARED : status;
        message = message == null ? "" : message.trim();
        if (createdAt < 1L) createdAt = System.currentTimeMillis();
        if (updatedAt < createdAt) updatedAt = createdAt;
    }

    public boolean matchesRequest(
            UUID playerId,
            UUID npcId,
            String nodeId,
            String catalogId,
            long catalogRevision,
            String offerId,
            int quantity
    ) {
        return this.playerId.equals(playerId)
                && this.npcId.equals(npcId)
                && this.nodeId.equals(nodeId == null ? "" : nodeId.trim())
                && this.catalogId.equals(catalogId == null ? "" : catalogId.trim())
                && this.catalogRevision == catalogRevision
                && this.offerId.equals(offerId == null ? "" : offerId.trim())
                && this.quantity == Math.max(1, Math.min(64, quantity));
    }

    public NpcTradePurchaseRecord paid(UUID operationId, UUID transactionId, String message, long now) {
        return new NpcTradePurchaseRecord(purchaseId, playerId, npcId, nodeId, catalogId,
                catalogRevision, offerId, quantity, subtotal, taxBasisPoints, tax, total,
                policyRevision, taxAuthorityLabel, delivery, NpcTradePurchaseStatus.PAID,
                operationId, transactionId, message, createdAt, now);
    }

    public NpcTradePurchaseRecord complete(String message, long now) {
        return new NpcTradePurchaseRecord(purchaseId, playerId, npcId, nodeId, catalogId,
                catalogRevision, offerId, quantity, subtotal, taxBasisPoints, tax, total,
                policyRevision, taxAuthorityLabel, delivery, NpcTradePurchaseStatus.COMPLETE,
                economyOperationId, economyTransactionId, message, createdAt, now);
    }

    public NpcTradePurchaseRecord failed(String message, long now) {
        return new NpcTradePurchaseRecord(purchaseId, playerId, npcId, nodeId, catalogId,
                catalogRevision, offerId, quantity, subtotal, taxBasisPoints, tax, total,
                policyRevision, taxAuthorityLabel, delivery, NpcTradePurchaseStatus.FAILED,
                economyOperationId, economyTransactionId, message, createdAt, now);
    }
}
