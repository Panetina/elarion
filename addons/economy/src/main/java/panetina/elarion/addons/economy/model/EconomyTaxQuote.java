package panetina.elarion.addons.economy.model;

public record EconomyTaxQuote(
        EconomyTaxAuthority authority,
        EconomyTaxCategory category,
        long unitPrice,
        int quantity,
        int maxQuantity,
        long subtotal,
        int taxBasisPoints,
        long tax,
        long total,
        long policyRevision,
        boolean valid,
        String message
) {
    public EconomyTaxQuote {
        message = message == null ? "" : message;
    }
}
