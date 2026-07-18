package panetina.elarion.addons.economy.model;

public record EconomyTradePriceQuote(
        EconomyTradeDirection direction,
        EconomyTaxAuthority authority,
        EconomyTaxCategory taxCategory,
        String priceKey,
        long unitPrice,
        int quantity,
        int maxQuantity,
        long subtotal,
        int feeOrTaxBasisPoints,
        long feeOrTax,
        long totalCost,
        long totalPayout,
        long policyRevision,
        long priceRevision,
        EconomyTradePriceSource priceSource,
        boolean valid,
        String message
) {
    public EconomyTradePriceQuote {
        direction = direction == null ? EconomyTradeDirection.BUY : direction;
        taxCategory = taxCategory == null ? EconomyTaxCategory.NPC_TRADE : taxCategory;
        priceKey = priceKey == null ? "" : priceKey.trim();
        quantity = Math.max(1, quantity);
        maxQuantity = Math.max(1, Math.min(64, maxQuantity));
        feeOrTaxBasisPoints = Math.max(0, Math.min(10_000, feeOrTaxBasisPoints));
        priceSource = priceSource == null ? EconomyTradePriceSource.UNAVAILABLE : priceSource;
        message = message == null ? "" : message;
    }
}
