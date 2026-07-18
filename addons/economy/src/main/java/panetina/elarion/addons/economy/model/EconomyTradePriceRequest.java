package panetina.elarion.addons.economy.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record EconomyTradePriceRequest(
        EconomyTradeDirection direction,
        EconomyTaxAuthority authority,
        EconomyTaxCategory taxCategory,
        String priceKey,
        boolean requirePriceKey,
        long fixedUnitPriceFallback,
        int quantity,
        int maxQuantity,
        int stockRemaining,
        int stockLimit,
        String catalogId,
        String offerId,
        String sourceSystem,
        Map<String, String> context
) {
    private static final int MAX_CONTEXT_ENTRIES = 16;

    public EconomyTradePriceRequest {
        direction = direction == null ? EconomyTradeDirection.BUY : direction;
        taxCategory = taxCategory == null ? EconomyTaxCategory.NPC_TRADE : taxCategory;
        priceKey = priceKey == null ? "" : priceKey.trim();
        maxQuantity = Math.max(1, Math.min(64, maxQuantity));
        stockRemaining = stockRemaining < 0 ? -1 : stockRemaining;
        stockLimit = Math.max(0, stockLimit);
        catalogId = catalogId == null ? "" : catalogId.trim();
        offerId = offerId == null ? "" : offerId.trim();
        sourceSystem = sourceSystem == null ? "" : sourceSystem.trim();
        context = boundedContext(context);
    }

    private static Map<String, String> boundedContext(Map<String, String> values) {
        if (values == null || values.isEmpty()) return Map.of();
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (result.size() >= MAX_CONTEXT_ENTRIES) break;
            String key = entry.getKey() == null ? "" : entry.getKey().trim();
            if (key.isBlank() || key.length() > 64) continue;
            String value = entry.getValue() == null ? "" : entry.getValue().trim();
            result.put(key, value.length() > 128 ? value.substring(0, 128) : value);
        }
        return Map.copyOf(result);
    }
}
