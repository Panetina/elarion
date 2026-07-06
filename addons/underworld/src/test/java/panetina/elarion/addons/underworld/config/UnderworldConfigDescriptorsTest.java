package panetina.elarion.addons.underworld.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnderworldConfigDescriptorsTest {
    @Test
    void registersReloadableUnderworldCategoriesAndDefaults() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        UnderworldConfigDescriptors.register(registry, UnderworldConfig::defaults);

        ElarionConfigDomain domain = registry.domain("underworld").orElseThrow();

        assertEquals("addons:underworld", domain.ownerModule());
        assertEquals("/e death reload", domain.reloadCommand());
        assertEquals(Set.of("underworld", "corpse", "pvp-loot", "combat-tag", "soul"),
                domain.categories().stream().map(category -> category.id()).collect(Collectors.toSet()));
        assertEquals(33, domain.categories().stream().mapToInt(category -> category.entries().size()).sum());
        assertEquals("elarion:underworld",
                domain.entry("underworld", "world-id").orElseThrow().currentValue());
        assertEquals("0.5", domain.entry("underworld", "spawn-x").orElseThrow().currentValue());
        assertEquals("0.25",
                domain.entry("pvp-loot", "physical-currency-percent").orElseThrow().currentValue());
        assertEquals("elarion:soulbound, elarion:quest_items, elarion:authority_items, elarion:no_pvp_loot",
                domain.entry("pvp-loot", "excluded-item-tags").orElseThrow().currentValue());

        for (var category : domain.categories()) {
            for (ElarionConfigEntry<?> entry : category.entries()) {
                assertTrue(entry.runtimeReloadable());
                assertFalse(entry.restartRequired());
                assertTrue(entry.validateCurrent().isEmpty(), entry.path());
            }
        }
    }

    @Test
    void decimalMetadataRetainsBoundsWhileUsingReadOnlyStrings() {
        ElarionConfigDomain domain = UnderworldConfigDescriptors.domain(UnderworldConfig::defaults);
        ElarionConfigEntry<?> percentage = domain.entry(
                "pvp-loot", "physical-currency-percent").orElseThrow();

        assertEquals("string", percentage.codec().id());
        assertEquals("0.0", percentage.minimum());
        assertEquals("1.0", percentage.maximum());
    }
}
