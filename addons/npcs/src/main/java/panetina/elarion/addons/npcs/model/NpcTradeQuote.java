package panetina.elarion.addons.npcs.model;

public record NpcTradeQuote(
        int quantity,
        int maxQuantity,
        long subtotal,
        int taxBasisPoints,
        long tax,
        long total,
        long policyRevision,
        String taxAuthorityLabel,
        boolean valid,
        String message
) {
    public NpcTradeQuote {
        taxAuthorityLabel = taxAuthorityLabel == null ? "" : taxAuthorityLabel;
        message = message == null ? "" : message;
    }
}
