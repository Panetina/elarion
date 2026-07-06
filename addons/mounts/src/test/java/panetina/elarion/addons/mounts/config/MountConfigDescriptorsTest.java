package panetina.elarion.addons.mounts.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MountConfigDescriptorsTest {
    @Test
    void registersRestartRequiredCollectionTextForEveryMount() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        MountConfigDescriptors.register(registry, MountCollectionTextConfig::defaults);

        ElarionConfigDomain domain = registry.domain("mounts").orElseThrow();

        assertEquals("addons:mounts", domain.ownerModule());
        assertEquals("", domain.reloadCommand());
        assertEquals(List.of("config/elarion/addons/mounts/collection.yml"), domain.files());
        assertEquals(1, domain.categories().size());
        assertEquals(2 + ElarionMountType.values().length * 4,
                domain.category("collection-text").orElseThrow().entries().size());
        assertEquals(ElarionMountType.values().length,
                domain.entry("collection-text", "mounts.count").orElseThrow().currentValue());
        assertEquals(
                "airship, bee, chinese_dragon, ghast, hot_air_balloon, scifi_bike, wyvern",
                domain.entry("collection-text", "mounts.ids").orElseThrow().currentValue());

        for (ElarionMountType type : ElarionMountType.values()) {
            for (String field : List.of("locked-row", "unlocked-row", "locked-detail", "unlocked-detail")) {
                ElarionConfigEntry<?> entry = domain.entry(
                        "collection-text", "mounts." + type.id() + "." + field).orElseThrow();
                assertFalse(entry.runtimeReloadable());
                assertTrue(entry.restartRequired());
                assertTrue(entry.validateCurrent().isEmpty());
            }
        }
    }

    @Test
    void exposesCanonicalRealmAndProgressionTextDefaults() {
        ElarionConfigDomain domain = MountConfigDescriptors.domain(MountCollectionTextConfig::defaults);

        assertEquals("Realm vendor: {realm}", domain.entry(
                "collection-text", "mounts.airship.locked-row").orElseThrow().currentValue());
        assertEquals("Future collection reward.", domain.entry(
                "collection-text", "mounts.bee.locked-row").orElseThrow().currentValue());
        assertEquals("Ready to summon with R.", domain.entry(
                "collection-text", "mounts.wyvern.unlocked-row").orElseThrow().currentValue());
    }
}
