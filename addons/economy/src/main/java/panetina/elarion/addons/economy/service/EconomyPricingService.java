package panetina.elarion.addons.economy.service;

import panetina.elarion.addons.economy.config.EconomyServicePriceConfig;
import panetina.elarion.addons.economy.model.EconomyServicePrice;

import java.util.Map;

public final class EconomyPricingService {
    private volatile Map<String, EconomyServicePrice> definitions = Map.of();

    public EconomyPricingService() {
        reload();
    }

    public void reload() {
        definitions = EconomyServicePriceConfig.load();
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

    public Map<String, EconomyServicePrice> definitions() {
        return definitions;
    }
}
