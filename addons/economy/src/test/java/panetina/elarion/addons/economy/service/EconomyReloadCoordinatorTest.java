package panetina.elarion.addons.economy.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.economy.config.EconomyConfig;
import panetina.elarion.addons.economy.model.EconomyGovernorMode;
import panetina.elarion.addons.economy.model.EconomyServicePrice;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class EconomyReloadCoordinatorTest {
    @Test
    void pricingFailurePreventsPreparedReloadFromExisting() {
        EconomyConfig config = config();

        assertThrows(IllegalStateException.class, () -> EconomyReloadCoordinator.prepare(
                () -> config,
                () -> { throw new IllegalStateException("invalid pricing"); }));
    }

    @Test
    void successfulPrepareCopiesBothValidatedSnapshots() {
        EconomyConfig config = config();
        Map<String, EconomyServicePrice> prices = new java.util.LinkedHashMap<>();
        prices.put("portal.ticket", new EconomyServicePrice("portal.ticket", 25, 1, 100));

        EconomyReloadCoordinator.PreparedReload prepared = EconomyReloadCoordinator.prepare(
                () -> config, () -> prices);
        prices.clear();

        assertEquals(config, prepared.economy());
        assertEquals(25L, prepared.prices().get("portal.ticket").base());
    }

    private static EconomyConfig config() {
        return new EconomyConfig(
                1, 300_000L, true, 30L * 86_400_000L, 10_000,
                12, 100, EconomyGovernorMode.MONITOR_ONLY, 7, 10_000,
                false, 86_400_000L, 25, 100L, 1L, 100, 0, 0);
    }
}
