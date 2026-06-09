package panetina.elarion.addons.worlds.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorldsConfigManagerTest {
    @TempDir
    Path temp;

    @Test
    void generatesAndLoadsDefaults() {
        WorldsConfigManager config = new WorldsConfigManager(
                LoggerFactory.getLogger("worlds-config-test"), temp.resolve("worlds.yml"));
        config.load();

        assertEquals(3, config.worlds().size());
        assertEquals(0.25, config.worlds().get("community_world_1")
                .blockRules().getFirst().retainChance());
        assertTrue(Files.exists(config.path()));
    }

    @Test
    void rejectsUnknownFieldsAndInvalidChances() throws Exception {
        Path file = temp.resolve("worlds.yml");
        Files.writeString(file, """
                config-version: 1
                defaults: {}
                worlds:
                  broken:
                    id: "elarion:broken"
                    surprise: true
                    block-abundance:
                      minecraft:iron_ore:
                        retain-chance: 2.0
                """);
        WorldsConfigManager config = new WorldsConfigManager(
                LoggerFactory.getLogger("worlds-config-test"), file);

        WorldsConfigException exception = assertThrows(WorldsConfigException.class, config::load);
        assertTrue(exception.errors().stream().anyMatch(error -> error.contains("surprise: unknown field")));
        assertTrue(exception.errors().stream().anyMatch(error -> error.contains("expected 0.0 through 1.0")));
    }
}
