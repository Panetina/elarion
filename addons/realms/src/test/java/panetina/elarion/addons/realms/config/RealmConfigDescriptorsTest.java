package panetina.elarion.addons.realms.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RealmConfigDescriptorsTest {
    @Test
    void registersRestartRequiredProtectionSettings() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        RealmConfigDescriptors.register(registry, RealmProtectionConfig::defaults);

        ElarionConfigDomain domain = registry.domain("realms").orElseThrow();

        assertEquals("addons:realms", domain.ownerModule());
        assertEquals("", domain.reloadCommand());
        assertEquals(Set.of("protection", "blocks"), domain.categories().stream()
                .map(category -> category.id()).collect(java.util.stream.Collectors.toSet()));
        assertEquals("elarion:lobby, elarion:worldheart",
                domain.entry("protection", "shared-world-ids").orElseThrow().defaultDisplayValue());
        assertEquals("1000",
                domain.entry("protection", "feedback-cooldown-millis").orElseThrow().defaultDisplayValue());

        for (var category : domain.categories()) {
            for (ElarionConfigEntry<?> entry : category.entries()) {
                assertFalse(entry.runtimeReloadable());
                assertTrue(entry.restartRequired());
                assertTrue(entry.validateCurrent().isEmpty());
            }
        }
    }

    @Test
    void currentValuesUseTheLoadedSnapshot() {
        AtomicReference<RealmProtectionConfig> current = new AtomicReference<>(new RealmProtectionConfig(
                Set.of("elarion:test", "minecraft:overworld"),
                true,
                false,
                2500L,
                Set.of("minecraft:oak_door"),
                Set.of("minecraft:chest")));
        ElarionConfigDomain domain = RealmConfigDescriptors.domain(current::get);

        assertEquals("elarion:test, minecraft:overworld",
                domain.entry("protection", "shared-world-ids").orElseThrow().currentDisplayValue());
        assertEquals(true,
                domain.entry("protection", "operator-bypass").orElseThrow().currentValue());
        assertEquals(false,
                domain.entry("protection", "protect-explosion-blocks").orElseThrow().currentValue());
        assertEquals(2500L,
                domain.entry("protection", "feedback-cooldown-millis").orElseThrow().currentValue());
        assertEquals("minecraft:oak_door",
                domain.entry("blocks", "extra-ally-interactable-blocks").orElseThrow().currentValue());
        assertEquals("minecraft:chest",
                domain.entry("blocks", "extra-container-blocks").orElseThrow().currentValue());
    }
}
