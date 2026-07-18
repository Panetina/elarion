package panetina.elarion.addons.npcs.model;

import java.util.List;
import java.util.UUID;

public record NpcTradeSaleRecord(
        UUID saleId,
        UUID playerId,
        UUID npcId,
        String nodeId,
        String catalogId,
        long catalogRevision,
        String offerId,
        int quantity,
        long grossPayout,
        int feeBasisPoints,
        long fee,
        long netPayout,
        long policyRevision,
        String taxAuthorityLabel,
        List<NpcTradeEscrowStack> escrow,
        NpcTradeSaleStatus status,
        UUID economyOperationId,
        UUID economyTransactionId,
        String message,
        long createdAt,
        long updatedAt
) {
    public NpcTradeSaleRecord {
        if (saleId == null) throw new IllegalArgumentException("Sale ID is required");
        if (playerId == null) throw new IllegalArgumentException("Player ID is required");
        if (npcId == null) throw new IllegalArgumentException("NPC ID is required");
        nodeId = nodeId == null ? "" : nodeId.trim();
        catalogId = catalogId == null ? "" : catalogId.trim();
        offerId = offerId == null ? "" : offerId.trim();
        quantity = Math.max(1, Math.min(64, quantity));
        feeBasisPoints = Math.max(0, Math.min(10_000, feeBasisPoints));
        taxAuthorityLabel = taxAuthorityLabel == null ? "" : taxAuthorityLabel.trim();
        escrow = escrow == null ? List.of() : List.copyOf(escrow);
        status = status == null ? NpcTradeSaleStatus.PREPARED : status;
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

    public NpcTradeSaleRecord escrowed(
            List<NpcTradeEscrowStack> escrow,
            String message,
            long now
    ) {
        return new NpcTradeSaleRecord(saleId, playerId, npcId, nodeId, catalogId,
                catalogRevision, offerId, quantity, grossPayout, feeBasisPoints, fee,
                netPayout, policyRevision, taxAuthorityLabel, escrow,
                NpcTradeSaleStatus.ITEMS_ESCROWED, economyOperationId,
                economyTransactionId, message, createdAt, now);
    }

    public NpcTradeSaleRecord paid(UUID operationId, UUID transactionId, String message, long now) {
        return new NpcTradeSaleRecord(saleId, playerId, npcId, nodeId, catalogId,
                catalogRevision, offerId, quantity, grossPayout, feeBasisPoints, fee,
                netPayout, policyRevision, taxAuthorityLabel, escrow,
                NpcTradeSaleStatus.PAID, operationId, transactionId, message, createdAt, now);
    }

    public NpcTradeSaleRecord stockUpdated(String message, long now) {
        return new NpcTradeSaleRecord(saleId, playerId, npcId, nodeId, catalogId,
                catalogRevision, offerId, quantity, grossPayout, feeBasisPoints, fee,
                netPayout, policyRevision, taxAuthorityLabel, escrow,
                NpcTradeSaleStatus.STOCK_UPDATED, economyOperationId, economyTransactionId,
                message, createdAt, now);
    }

    public NpcTradeSaleRecord complete(String message, long now) {
        return new NpcTradeSaleRecord(saleId, playerId, npcId, nodeId, catalogId,
                catalogRevision, offerId, quantity, grossPayout, feeBasisPoints, fee,
                netPayout, policyRevision, taxAuthorityLabel, escrow,
                NpcTradeSaleStatus.COMPLETE, economyOperationId, economyTransactionId,
                message, createdAt, now);
    }

    public NpcTradeSaleRecord failed(String message, long now) {
        return new NpcTradeSaleRecord(saleId, playerId, npcId, nodeId, catalogId,
                catalogRevision, offerId, quantity, grossPayout, feeBasisPoints, fee,
                netPayout, policyRevision, taxAuthorityLabel, escrow,
                NpcTradeSaleStatus.FAILED, economyOperationId, economyTransactionId,
                message, createdAt, now);
    }

    public NpcTradeSaleRecord restored(String message, long now) {
        return new NpcTradeSaleRecord(saleId, playerId, npcId, nodeId, catalogId,
                catalogRevision, offerId, quantity, grossPayout, feeBasisPoints, fee,
                netPayout, policyRevision, taxAuthorityLabel, escrow,
                NpcTradeSaleStatus.RESTORED, economyOperationId, economyTransactionId,
                message, createdAt, now);
    }
}
