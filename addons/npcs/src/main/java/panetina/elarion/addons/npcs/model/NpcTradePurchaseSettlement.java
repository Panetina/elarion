package panetina.elarion.addons.npcs.model;

import java.util.UUID;

public record NpcTradePurchaseSettlement(
        boolean successful,
        String message,
        UUID operationId,
        UUID transactionId
) {
    public NpcTradePurchaseSettlement {
        message = message == null ? "" : message;
    }

    public static NpcTradePurchaseSettlement failure(String message) {
        return new NpcTradePurchaseSettlement(false, message, null, null);
    }
}
