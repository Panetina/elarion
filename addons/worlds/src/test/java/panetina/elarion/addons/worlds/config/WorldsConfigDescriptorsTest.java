package panetina.elarion.addons.worlds.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.worlds.model.WorldBorderDefinition;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorldsConfigDescriptorsTest {
    @TempDir
    Path temp;

    @Test
    void registersWorldsDomain() {
        WorldsConfigManager config = loadedConfig();
        ElarionConfigRegistry registry = new ElarionConfigRegistry();

        WorldsConfigDescriptors.register(registry, config);

        assertTrue(registry.domain("worlds").isPresent());
        assertEquals("Worlds", registry.domain("worlds").orElseThrow().label());
    }

    @Test
    void domainExposesLoadedWorldsConfigSnapshot() {
        WorldsConfigManager config = loadedConfig();

        ElarionConfigDomain domain = WorldsConfigDescriptors.domain(config);

        assertEquals("worlds", domain.id());
        assertEquals("addons:worlds", domain.ownerModule());
        assertEquals("/e world reload", domain.reloadCommand());
        assertEquals(2, domain.categories().size());
        assertTrue(domain.files().contains("config/elarion/addons/worlds/worlds.yml"));

        assertEquals("4", domain.entry("general", "config-version").orElseThrow().currentDisplayValue());
        assertEquals("lobby", domain.entry("general", "lobby.destination").orElseThrow().currentDisplayValue());
        assertEquals("true", domain.entry("general",
                "lobby.enforce-for-unassigned").orElseThrow().currentDisplayValue());
        assertEquals("5", domain.entry("general", "worlds.count").orElseThrow().currentDisplayValue());
        assertTrue(domain.entry("general", "worlds.keys").orElseThrow()
                .currentDisplayValue().contains("underworld"));

        var lobbyType = domain.entry("worlds", "worlds.lobby.type").orElseThrow();
        assertEquals("VOID", lobbyType.currentDisplayValue());
        assertTrue(lobbyType.choices().contains("NETHER"));

        assertEquals("12", domain.entry("worlds", "worlds.underworld.platform-radius")
                .orElseThrow().currentDisplayValue());
        assertEquals("2", domain.entry("worlds", "worlds.realm_world_1.block-abundance.count")
                .orElseThrow().currentDisplayValue());
        assertTrue(domain.entry("worlds", "worlds.lobby.border").orElseThrow()
                .currentDisplayValue().contains("size=32.0"));
    }

    @Test
    void worldEntriesReadCurrentManagerValues() {
        WorldsConfigManager config = loadedConfig();
        ElarionConfigDomain domain = WorldsConfigDescriptors.domain(config);
        var border = domain.entry("worlds", "worlds.realm_world_1.border").orElseThrow();

        config.updateBorder("elarion:realm_world_1",
                new WorldBorderDefinition(1, 2, 345, 6, 0.4, 8, 9));

        assertTrue(border.currentDisplayValue().contains("size=345.0"));
        assertTrue(border.defaultDisplayValue().contains("size=10000.0"));
    }

    private WorldsConfigManager loadedConfig() {
        WorldsConfigManager config = new WorldsConfigManager(
                LoggerFactory.getLogger("worlds-config-descriptor-test"),
                temp.resolve("worlds.yml"));
        config.load();
        return config;
    }
}
