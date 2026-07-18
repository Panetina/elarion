package panetina.elarion.addons.npcs.model;

import java.util.List;

public record NpcTradeEscrowResult(
        boolean successful,
        String message,
        List<NpcTradeEscrowStack> escrow,
        int removedCount
) {
    public NpcTradeEscrowResult {
        message = message == null ? "" : message;
        escrow = escrow == null ? List.of() : List.copyOf(escrow);
        removedCount = Math.max(0, removedCount);
    }

    public static NpcTradeEscrowResult failure(String message) {
        return new NpcTradeEscrowResult(false, message, List.of(), 0);
    }

    public static NpcTradeEscrowResult success(List<NpcTradeEscrowStack> escrow, int removedCount) {
        return new NpcTradeEscrowResult(true, "Items escrowed.", escrow, removedCount);
    }
}
