package panetina.elarion.addons.economy.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.economy.model.EconomyGovernorMode;
import panetina.elarion.addons.economy.model.EconomyServicePrice;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EconomyConfigDescriptorsTest {
    @Test
    void registersEconomyDomain() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();

        EconomyConfigDescriptors.register(registry, this::config, this::prices);

        assertTrue(registry.domain("economy").isPresent());
        assertEquals("Economy", registry.domain("economy").orElseThrow().label());
    }

    @Test
    void domainExposesCurrentConfigAndServicePriceValues() {
        ElarionConfigDomain domain = EconomyConfigDescriptors.domain(this::config, this::prices);

        assertEquals("economy", domain.id());
        assertEquals("addons:economy", domain.ownerModule());
        assertEquals("/e economy reload", domain.reloadCommand());
        assertEquals(4, domain.categories().size());
        assertTrue(domain.files().contains("config/elarion/addons/economy/economy.yml"));
        assertTrue(domain.files().contains("config/elarion/addons/economy/service_prices.yml"));

        var snapshotInterval = domain.entry("persistence", "persistence.snapshot-interval-seconds").orElseThrow();
        assertEquals("450", snapshotInterval.currentDisplayValue());
        assertEquals("300", snapshotInterval.defaultDisplayValue());
        assertEquals("10", snapshotInterval.minimum());
        assertTrue(snapshotInterval.runtimeReloadable());

        var forceWrites = domain.entry("persistence", "persistence.force-journal-writes").orElseThrow();
        assertEquals("false", forceWrites.currentDisplayValue());
        assertEquals("true", forceWrites.defaultDisplayValue());

        var governorMode = domain.entry("governor", "governor.mode").orElseThrow();
        assertEquals("SUGGEST_ONLY", governorMode.currentDisplayValue());
        assertTrue(governorMode.choices().contains("AUTO_FULL"));

        var customBase = domain.entry("service-prices", "service-prices.custom.service.base").orElseThrow();
        assertEquals("9", customBase.currentDisplayValue());
        assertEquals("9", customBase.defaultDisplayValue());

        var netherMaximum = domain.entry("service-prices",
                "service-prices.portal_ticket.nether.maximum").orElseThrow();
        assertEquals("80", netherMaximum.currentDisplayValue());
        assertEquals("60", netherMaximum.defaultDisplayValue());
        assertEquals("0", netherMaximum.minimum());
    }

    private EconomyConfig config() {
        return new EconomyConfig(
                1,
                450_000L,
                false,
                6,
                75,
                EconomyGovernorMode.SUGGEST_ONLY,
                14,
                2_000);
    }

    private Map<String, EconomyServicePrice> prices() {
        Map<String, EconomyServicePrice> values = new LinkedHashMap<>();
        values.put("portal_ticket.nether", new EconomyServicePrice("portal_ticket.nether", 40L, 20L, 80L));
        values.put("custom.service", new EconomyServicePrice("custom.service", 9L, 3L, 12L));
        return values;
    }
}
