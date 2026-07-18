package panetina.elarion.addons.npcs.model;

import java.util.UUID;

public record NpcTradeSaleSettlement(
        boolean successful,
        String message,
        UUID operationId,
        UUID transactionId
) {
    public NpcTradeSaleSettlement {
        message = message == null ? "" : message;
    }

    public static NpcTradeSaleSettlement failure(String message) {
        return new NpcTradeSaleSettlement(false, message, null, null);
    }
}
