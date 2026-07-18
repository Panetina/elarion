package panetina.elarion.addons.economy.service;

import panetina.elarion.addons.economy.config.EconomyServicePriceConfig;
import panetina.elarion.addons.economy.model.EconomyServicePrice;
import panetina.elarion.addons.economy.model.EconomyTaxQuote;
import panetina.elarion.addons.economy.model.EconomyTradeDirection;
import panetina.elarion.addons.economy.model.EconomyTradePriceQuote;
import panetina.elarion.addons.economy.model.EconomyTradePriceRequest;
import panetina.elarion.addons.economy.model.EconomyTradePriceSource;

import java.util.Map;

public final class EconomyPricingService {
    private volatile Map<String, EconomyServicePrice> definitions = Map.of();

    public EconomyPricingService() {
        reload();
    }

    public EconomyPricingService(Map<String, EconomyServicePrice> definitions) {
        this.definitions = definitions == null ? Map.of() : Map.copyOf(definitions);
    }

    public void reload() {
        reload(EconomyServicePriceConfig.load());
    }

    public void reload(Map<String, EconomyServicePrice> preparedDefinitions) {
        definitions = preparedDefinitions == null ? Map.of() : Map.copyOf(preparedDefinitions);
    }

    public EconomyServicePrice definition(String priceId) {
        EconomyServicePrice definition = definitions.get(priceId);
        if (definition == null) throw new IllegalArgumentException("Unknown Economy service price " + priceId);
        return definition;
    }

    public long currentPrice(String priceId) {
        // Dynamic Governor state will be layered here without changing consumers.
        return definition(priceId).base();
    }

    public EconomyTradePriceQuote quoteTradePrice(
            EconomyTradePriceRequest request,
            EconomyTaxPolicyService taxPolicies
    ) {
        if (request == null) return invalid(null, 1L, 1, "Trade price request is required.");
        if (taxPolicies == null || request.authority() == null) {
            return invalid(request, 0L, request.maxQuantity(), "Trade price policy is unavailable.");
        }
        PriceResolution price = resolvePrice(request);
        if (!price.valid()) return invalid(request, 0L, request.maxQuantity(), price.message());
        EconomyTaxQuote quote = taxPolicies.quote(
                request.authority(), request.taxCategory(), price.unitPrice(), request.quantity(), request.maxQuantity());
        if (!quote.valid()) {
            return new EconomyTradePriceQuote(request.direction(), request.authority(), request.taxCategory(),
                    request.priceKey(), price.unitPrice(), quote.quantity(), quote.maxQuantity(), 0L,
                    quote.taxBasisPoints(), 0L, 0L, 0L, quote.policyRevision(), price.revision(),
                    price.source(), false, quote.message());
        }
        long totalCost = request.direction() == EconomyTradeDirection.BUY ? quote.total() : 0L;
        long totalPayout = request.direction() == EconomyTradeDirection.SELL
                ? Math.max(0L, quote.subtotal() - quote.tax()) : 0L;
        return new EconomyTradePriceQuote(request.direction(), request.authority(), request.taxCategory(),
                request.priceKey(), price.unitPrice(), quote.quantity(), quote.maxQuantity(), quote.subtotal(),
                quote.taxBasisPoints(), quote.tax(), totalCost, totalPayout, quote.policyRevision(),
                price.revision(), price.source(), true, "");
    }

    public Map<String, EconomyServicePrice> definitions() {
        return definitions;
    }

    private PriceResolution resolvePrice(EconomyTradePriceRequest request) {
        if (!request.priceKey().isBlank()) {
            EconomyServicePrice definition = definitions.get(request.priceKey());
            if (definition != null) {
                return new PriceResolution(true, definition.base(), definition.hashCode(),
                        EconomyTradePriceSource.SERVICE_PRICE, "");
            }
            if (request.requirePriceKey()) {
                return new PriceResolution(false, 0L, 0L, EconomyTradePriceSource.UNAVAILABLE,
                        "Unknown trade price key " + request.priceKey() + ".");
            }
        }
        if (request.fixedUnitPriceFallback() > 0L) {
            return new PriceResolution(true, request.fixedUnitPriceFallback(), 0L,
                    EconomyTradePriceSource.FIXED, "");
        }
        return new PriceResolution(false, 0L, 0L, EconomyTradePriceSource.UNAVAILABLE,
                "Trade price is unavailable.");
    }

    private static EconomyTradePriceQuote invalid(
            EconomyTradePriceRequest request,
            long unitPrice,
            int maxQuantity,
            String message
    ) {
        return new EconomyTradePriceQuote(
                request == null ? EconomyTradeDirection.BUY : request.direction(),
                request == null ? null : request.authority(),
                request == null ? null : request.taxCategory(),
                request == null ? "" : request.priceKey(),
                unitPrice,
                request == null ? 1 : request.quantity(),
                maxQuantity,
                0L,
                0,
                0L,
                0L,
                0L,
                0L,
                0L,
                EconomyTradePriceSource.UNAVAILABLE,
                false,
                message);
    }

    private record PriceResolution(
            boolean valid,
            long unitPrice,
            long revision,
            EconomyTradePriceSource source,
            String message
    ) {
    }
}
