package panetina.elarion.addons.worlds.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.worlds.model.WorldType;
import panetina.elarion.addons.worlds.model.WorldBorderDefinition;

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

        assertEquals(5, config.worlds().size());
        assertEquals(WorldType.VOID, config.worlds().get("lobby").type());
        assertEquals(WorldType.VOID, config.worlds().get("underworld").type());
        assertEquals("elarion:underworld", config.worlds().get("underworld").id());
        assertEquals(0.25, config.worlds().get("realm_world_1")
                .blockRules().getFirst().retainChance());
        assertTrue(Files.exists(config.path()));
    }

    @Test
    void storesAndRemovesCommandWorldsInWorldsYaml() throws Exception {
        WorldsConfigManager config = new WorldsConfigManager(
                LoggerFactory.getLogger("worlds-config-test"), temp.resolve("worlds.yml"));
        config.load();

        config.create("jail", WorldType.VOID, 42);
        assertEquals(WorldType.VOID, config.worlds().get("jail").type());
        assertTrue(Files.readString(config.path()).contains("jail:"));
        assertEquals("elarion:jail", config.remove("jail").id());
        assertTrue(!Files.readString(config.path()).contains("elarion:jail"));
        assertTrue(config.worlds().containsKey("lobby"));
        assertEquals(null, config.remove("lobby"));
        assertTrue(Files.notExists(temp.resolve("created-worlds.yml")));
    }

    @Test
    void persistsBorderChangesInTheWorldDefinition() throws Exception {
        WorldsConfigManager config = new WorldsConfigManager(
                LoggerFactory.getLogger("worlds-config-test"), temp.resolve("worlds.yml"));
        config.load();
        WorldBorderDefinition border = new WorldBorderDefinition(12, 34, 567, 6, 0.4, 8, 9);

        config.updateBorder("elarion:realm_world_1", border);

        WorldsConfigManager reloaded = new WorldsConfigManager(
                LoggerFactory.getLogger("worlds-config-test"), temp.resolve("worlds.yml"));
        reloaded.load();
        assertEquals(border, reloaded.worlds().get("realm_world_1").border());
        assertTrue(!Files.readString(config.path()).contains("*id"));
    }

    @Test
    void rejectsUnknownFieldsAndInvalidChances() throws Exception {
        Path file = temp.resolve("worlds.yml");
        Files.writeString(file, """
                config-version: 4
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

    @Test
    void migratesSchemaTwoAndMergesLegacyCreatedWorlds() throws Exception {
        Path file = temp.resolve("worlds.yml");
        Files.writeString(file, """
                config-version: 3
                destinations:
                  default: lobby
                  lobby: elarion:lobby
                  overworld: minecraft:overworld
                existing-worlds:
                  overworld:
                    id: minecraft:overworld
                defaults: {}
                worlds:
                  old_world:
                    id: "elarion:old_world"
                    seed: 99
                    gamerules: { keepInventory: true }
                """);
        Files.writeString(temp.resolve("created-worlds.yml"), """
                worlds:
                  jail:
                    id: elarion:jail
                    type: VOID
                """);
        WorldsConfigManager config = new WorldsConfigManager(
                LoggerFactory.getLogger("worlds-config-test"), file);

        config.load();

        assertEquals(3, config.worlds().size());
        assertEquals(99, config.worlds().get("old_world").seed());
        assertEquals("true", config.worlds().get("old_world").gameRules().get("keepInventory"));
        assertEquals(WorldType.OVERWORLD, config.worlds().get("old_world").type());
        assertEquals(WorldType.VOID, config.worlds().get("jail").type());
        assertTrue(Files.exists(temp.resolve("worlds.yml.bak-v3")));
        assertTrue(Files.notExists(temp.resolve("created-worlds.yml")));
        String migrated = Files.readString(file);
        assertTrue(migrated.contains("config-version: 4"));
        assertTrue(!migrated.contains("existing-worlds"));
        assertTrue(!migrated.contains("destinations:"));
        assertTrue(!migrated.contains("overworld:"));
        assertTrue(!migrated.contains("default:"));
    }
}
