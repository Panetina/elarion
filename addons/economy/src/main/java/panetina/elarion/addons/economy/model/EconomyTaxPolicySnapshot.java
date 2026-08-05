package panetina.elarion.addons.economy.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record EconomyTaxPolicySnapshot(
        EconomyTaxAuthority authority,
        long revision,
        Map<EconomyTaxCategory, Integer> rates
) {
    public EconomyTaxPolicySnapshot {
        rates = Map.copyOf(new LinkedHashMap<>(rates == null ? Map.of() : rates));
    }
}
