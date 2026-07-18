package panetina.elarion.addons.economy.service;

import panetina.elarion.addons.economy.config.EconomyConfig;
import panetina.elarion.addons.economy.config.EconomyServicePriceConfig;
import panetina.elarion.addons.economy.model.EconomyServicePrice;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class EconomyReloadCoordinator {
    private EconomyReloadCoordinator() {
    }

    public static PreparedReload prepare() {
        return prepare(EconomyConfig::load, EconomyServicePriceConfig::load);
    }

    static PreparedReload prepare(
            Supplier<EconomyConfig> economyLoader,
            Supplier<Map<String, EconomyServicePrice>> pricingLoader
    ) {
        EconomyConfig economy = Objects.requireNonNull(economyLoader, "economyLoader").get();
        Map<String, EconomyServicePrice> prices = Objects.requireNonNull(pricingLoader, "pricingLoader").get();
        return new PreparedReload(economy, prices);
    }

    public record PreparedReload(EconomyConfig economy, Map<String, EconomyServicePrice> prices) {
        public PreparedReload {
            economy = Objects.requireNonNull(economy, "economy");
            prices = prices == null ? Map.of() : Map.copyOf(prices);
        }

        public void commit(EconomyTransactionService transactions, EconomyPricingService pricing) {
            Objects.requireNonNull(transactions, "transactions");
            Objects.requireNonNull(pricing, "pricing");
            pricing.reload(prices);
            transactions.reload(economy);
        }
    }
}
