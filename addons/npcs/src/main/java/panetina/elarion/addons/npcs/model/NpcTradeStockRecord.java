package panetina.elarion.addons.npcs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record NpcTradeStockRecord(
        UUID npcId,
        String offerId,
        int remaining,
        long lastRestockAtMillis,
        List<UUID> consumedPurchaseIds
) {
    public NpcTradeStockRecord {
        offerId = offerId == null ? "" : offerId.trim();
        remaining = Math.max(0, remaining);
        lastRestockAtMillis = Math.max(0L, lastRestockAtMillis);
        consumedPurchaseIds = consumedPurchaseIds == null
                ? List.of()
                : consumedPurchaseIds.stream()
                .filter(id -> id != null && !id.equals(new UUID(0L, 0L)))
                .distinct()
                .toList();
    }

    public NpcTradeStockRecord withRemaining(int value, long restockAtMillis) {
        return new NpcTradeStockRecord(npcId, offerId, value, restockAtMillis, consumedPurchaseIds);
    }

    public NpcTradeStockRecord withConsumed(UUID purchaseId, int maxConsumedIds) {
        if (purchaseId == null || purchaseId.equals(new UUID(0L, 0L))) return this;
        if (consumedPurchaseIds.contains(purchaseId)) return this;
        List<UUID> ids = new ArrayList<>(consumedPurchaseIds);
        ids.add(purchaseId);
        while (ids.size() > Math.max(1, maxConsumedIds)) ids.remove(0);
        return new NpcTradeStockRecord(npcId, offerId, remaining, lastRestockAtMillis, ids);
    }

    public boolean consumed(UUID purchaseId) {
        return purchaseId != null && consumedPurchaseIds.contains(purchaseId);
    }
}
