package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CoreConfigManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void generatesAndVersionsEveryCoreConfig() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();

        assertTrue(config.realms().containsKey("realm1"));
        assertEquals("A citizen of Elarion.", config.titles().get("citizen").description());
        assertEquals(panetina.elarion.core.model.TitleAcquisitionMode.DEFAULT,
                config.titles().get("citizen").acquisitionMode());
        assertEquals(panetina.elarion.core.model.TitleOwnershipMode.UNLIMITED,
                config.titles().get("citizen").ownershipMode());
        assertTrue(config.titles().get("aquatic").activeEffects().stream()
                .anyMatch(effect -> effect.parameters().get("id").equals("minecraft:water_breathing")));
        assertTrue(config.titleUnlockRules().containsKey("goblin_slayer"));
        assertEquals("modded_goblin_kills", config.titleUnlockRules().get("goblin_slayer").statKey());
        assertTrue(config.progressionRegions().containsKey("maze_end"));
        assertEquals("maze_runner", config.titleUnlockRules().get("maze_runner").titleId());
        assertEquals(64, config.localChatRadius());
        assertEquals(4, config.whisperChatRadius());
        assertEquals(128, config.yellChatRadius());
        assertEquals(300, config.yellChatCooldownSeconds());
        assertTrue(config.historyRecordingPolicy().allows("citizen", "realm-assigned"));
        assertTrue(!config.historyRecordingPolicy().allows("chat", "realm-message"));
        assertEquals(3, config.historyQueryMaxMonths());
        assertEquals(100, config.historyCommandLimitMax());
        assertTrue(config.historyArchiveEnabled());
        assertEquals(8, config.historyArchiveMaxCompletedWeeks());
        assertTrue(config.historyChronicleCategories().contains("realm"));
        assertEquals(8, config.publicHistoryDefaultWeeks());
        assertEquals(50, config.publicHistoryDefaultLimit());
        assertEquals(200, config.publicHistoryMaxLimit());
        assertEquals("Elarion", config.serverIdentity().serverName());
        assertEquals("1 sigil", config.serverIdentity().currencyAmount(1));
        assertTrue(Files.exists(tempDir.resolve("server_identity.yml")));
        assertTrue(Files.exists(tempDir.resolve("ui_theme.yml")));
        assertEquals(0xFFC08A32, config.uiTheme().variant("shrine").borderColor());
        assertEquals(config.uiTheme().variant("default"), config.uiTheme().variant("missing"));
        try (var files = Files.list(tempDir)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                assertTrue(Files.readString(file).contains("config-version: 1"), file.toString());
            }
        }
    }

    @Test
    void rejectsUnknownUiThemeParentAndKeepsPreviousTheme() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();
        int previousBorder = config.uiTheme().variant("shrine").borderColor();
        Path theme = tempDir.resolve("ui_theme.yml");
        Files.writeString(theme, Files.readString(theme, StandardCharsets.UTF_8)
                .replace("extends: \"default\"", "extends: \"missing\""), StandardCharsets.UTF_8);

        assertThrows(ConfigValidationException.class, config::load);
        assertEquals(previousBorder, config.uiTheme().variant("shrine").borderColor());
    }

    @Test
    void reloadsCustomServerIdentityWithoutChangingInternalIds() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();

        Path identity = tempDir.resolve("server_identity.yml");
        Files.writeString(identity, Files.readString(identity, StandardCharsets.UTF_8)
                .replace("server-name: \"Elarion\"", "server-name: \"Asterfall\"")
                .replace("currency-singular: \"Sigil\"", "currency-singular: \"Crown\"")
                .replace("currency-plural: \"Sigils\"", "currency-plural: \"Crowns\""),
                StandardCharsets.UTF_8);

        config.load();

        assertEquals("Asterfall", config.serverIdentity().serverName());
        assertEquals("1 crown", config.serverIdentity().currencyAmount(1));
        assertEquals("8 crowns", config.serverIdentity().currencyAmount(8));
    }

    @Test
    void rejectsInvalidFieldWithPrecisePathAndKeepsPreviousSnapshot() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();
        int previousRealmCount = config.realms().size();
        Path realms = tempDir.resolve("realms.yml");
        String content = Files.readString(realms, StandardCharsets.UTF_8)
                .replace("color: \"green\"", "color: \"gren\"");
        Files.writeString(realms, content, StandardCharsets.UTF_8);

        ConfigValidationException exception =
                assertThrows(ConfigValidationException.class, config::load);

        assertTrue(exception.errors().stream()
                .anyMatch(error -> error.contains("realms.yml.realms.realm1.color")));
        assertEquals(previousRealmCount, config.realms().size());
    }

    @Test
    void rejectsBrokenCrossFileReferences() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();

        Path defaults = tempDir.resolve("citizens-defaults.yml");
        Files.writeString(defaults, Files.readString(defaults, StandardCharsets.UTF_8)
                .replace("title: \"citizen\"", "title: \"ghost_title\""), StandardCharsets.UTF_8);
        Path rewards = tempDir.resolve("rewards.yml");
        Files.writeString(rewards, Files.readString(rewards, StandardCharsets.UTF_8)
                .replace("type: \"message\"", "type: \"mystery\""), StandardCharsets.UTF_8);

        ConfigValidationException exception =
                assertThrows(ConfigValidationException.class, config::load);

        assertTrue(exception.errors().stream()
                .anyMatch(error -> error.contains("citizens-defaults.yml.defaults.title")));
        assertTrue(exception.errors().stream()
                .anyMatch(error -> error.contains("rewards.yml.rewards")));
    }

    @Test
    void acceptsKnownAddonRewardActionsDuringCoreOnlyValidation() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();

        Path rewards = tempDir.resolve("rewards.yml");
        Files.writeString(rewards, """
                config-version: 1

                rewards:
                  test_economy_reward:
                    actions:
                      - type: "currency-reward"
                        amount: 25
                      - type: "realm-currency-reward"
                        realm: "realm1"
                        amount: 50
                      - type: "realm-treasury-grant"
                        realm: "realm1"
                        amount: 10
                """, StandardCharsets.UTF_8);

        config.load();

        assertTrue(config.rewards().containsKey("test_economy_reward"));
    }
}
