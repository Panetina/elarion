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
        assertEquals("An Ember of Elarion.", config.titles().get("citizen").description());
        assertEquals(panetina.elarion.core.model.TitleAcquisitionMode.DEFAULT,
                config.titles().get("citizen").acquisitionMode());
        assertEquals(panetina.elarion.core.model.TitleOwnershipMode.UNLIMITED,
                config.titles().get("citizen").ownershipMode());
        assertEquals(0xFFC9C9C9, config.titles().get("citizen").colorArgb());
        assertEquals(0xFFFFD36A, config.titles().get("government_monarch").colorArgb());
        assertEquals(0xFFC084FF, config.titles().get("government_synod_member").colorArgb());
        assertEquals(0xFFFFA83D, config.titles().get("dragon_slayer").colorArgb());
        assertTrue(config.titles().get("aquatic").activeEffects().stream()
                .anyMatch(effect -> effect.parameters().get("id").equals("minecraft:water_breathing")));
        assertEquals(panetina.elarion.core.model.TitleOwnershipMode.ONE_PER_PLAYER,
                config.titles().get("aquatic").ownershipMode());
        assertEquals("Monarch", config.titles().get("government_monarch").displayName());
        assertEquals("Holy Priest", config.titles().get("government_high_cleric").displayName());
        assertEquals("Councilor", config.titles().get("government_councilor").displayName());
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
        assertTrue(!config.nicknameRejectContainingProtectedName());
        assertTrue(Files.exists(tempDir.resolve("server_identity.yml")));
        assertTrue(Files.exists(tempDir.resolve("ui_theme.yml")));
        assertEquals(0xFFC08A32, config.uiTheme().variant("shrine").borderColor());
        assertEquals(100, config.uiTheme().fontScalePercent());
        assertEquals(config.uiTheme().variant("default"), config.uiTheme().variant("missing"));
        try (var files = Files.list(tempDir)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                assertTrue(Files.readString(file).contains("config-version: 1"), file.toString());
            }
        }
    }

    @Test
    void reloadsBoundedServerWideFontScaleAndKeepsPreviousValueOnFailure() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();
        Path theme = tempDir.resolve("ui_theme.yml");
        String original = Files.readString(theme, StandardCharsets.UTF_8);

        Files.writeString(theme, original.replace("font-scale-percent: 100", "font-scale-percent: 125"),
                StandardCharsets.UTF_8);
        config.load();
        assertEquals(125, config.uiTheme().fontScalePercent());

        Files.writeString(theme, original.replace("font-scale-percent: 100", "font-scale-percent: 151"),
                StandardCharsets.UTF_8);
        ConfigValidationException exception = assertThrows(ConfigValidationException.class, config::load);
        assertTrue(exception.errors().stream().anyMatch(error -> error.contains("font-scale-percent")));
        assertEquals(125, config.uiTheme().fontScalePercent());
    }

    @Test
    void reloadsTitleColorsAndRejectsInvalidHexColors() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();
        Path titles = tempDir.resolve("titles.yml");
        String original = Files.readString(titles, StandardCharsets.UTF_8);

        Files.writeString(titles, original.replace("color: \"#C9C9C9\"", "color: \"#44AAFF\""),
                StandardCharsets.UTF_8);
        config.load();
        assertEquals(0xFF44AAFF, config.titles().get("citizen").colorArgb());

        Files.writeString(titles, original.replace("color: \"#C9C9C9\"", "color: \"gold\""),
                StandardCharsets.UTF_8);
        ConfigValidationException exception = assertThrows(ConfigValidationException.class, config::load);
        assertTrue(exception.errors().stream().anyMatch(error -> error.contains("titles.yml.titles.citizen.color")));
        assertEquals(0xFF44AAFF, config.titles().get("citizen").colorArgb());
    }

    @Test
    void migratesMissingKnownTitleColorsWithoutOverwritingCustomColors() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();
        Path titles = tempDir.resolve("titles.yml");
        String content = Files.readString(titles, StandardCharsets.UTF_8)
                .replace("color: \"#C9C9C9\"", "")
                .replace("color: \"#FFD36A\"", "color: \"#FFEEDD\"");
        Files.writeString(titles, content, StandardCharsets.UTF_8);

        config.load();

        String migrated = Files.readString(titles, StandardCharsets.UTF_8);
        assertEquals(0xFFC9C9C9, config.titles().get("citizen").colorArgb());
        assertEquals(0xFFFFEEDD, config.titles().get("government_monarch").colorArgb());
        assertTrue(migrated.contains("citizen:"));
        assertTrue(migrated.contains("color: \"#C9C9C9\""));
        assertTrue(migrated.contains("color: \"#FFEEDD\""));
    }

    @Test
    void migratesOldCitizenDefaultTitleColorWithoutOverwritingCustomColors() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();
        Path titles = tempDir.resolve("titles.yml");
        String original = Files.readString(titles, StandardCharsets.UTF_8);

        Files.writeString(titles, original.replace("color: \"#C9C9C9\"", "color: \"#D19B42\""),
                StandardCharsets.UTF_8);
        config.load();
        assertEquals(0xFFC9C9C9, config.titles().get("citizen").colorArgb());
        assertTrue(Files.readString(titles, StandardCharsets.UTF_8).contains("color: \"#C9C9C9\""));

        Files.writeString(titles, original.replace("color: \"#C9C9C9\"", "color: \"#44AAFF\""),
                StandardCharsets.UTF_8);
        config.load();
        assertEquals(0xFF44AAFF, config.titles().get("citizen").colorArgb());
        assertTrue(Files.readString(titles, StandardCharsets.UTF_8).contains("color: \"#44AAFF\""));
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

    @Test
    void migratesMissingGovernmentAuthorityTitlesIntoExistingTitleConfig() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();
        Path titles = tempDir.resolve("titles.yml");
        String content = Files.readString(titles, StandardCharsets.UTF_8);
        int start = content.indexOf("  government_monarch:");
        int end = content.indexOf("  government_heir:", start);
        assertTrue(start > 0 && end > start);
        Files.writeString(titles, content.substring(0, start) + content.substring(end), StandardCharsets.UTF_8);

        config.load();

        assertEquals("Monarch", config.titles().get("government_monarch").displayName());
        assertTrue(Files.readString(titles, StandardCharsets.UTF_8).contains("government_monarch:"));
    }
}
