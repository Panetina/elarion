package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConfigRegistryTest {
    @TempDir
    Path tempDir;

    @Test
    void registersDomainsInStableOrderAndRejectsDuplicates() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(testDomain("worlds"));
        registry.registerDomain(testDomain("core"));

        assertEquals(List.of("core", "worlds"),
                registry.domains().stream().map(ElarionConfigDomain::id).toList());
        assertThrows(IllegalArgumentException.class, () -> registry.registerDomain(testDomain("core")));
    }

    @Test
    void exposesCoreDescriptorValuesAndReloadedCurrentValues() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        CoreConfigDescriptors.register(registry, config);

        ElarionConfigDomain core = registry.domain("core").orElseThrow();
        assertEquals("platform:core", core.ownerModule());
        assertTrue(core.files().contains("config/elarion/core/ui_theme.yml"));
        assertTrue(core.files().contains("config/elarion/core/server_identity.yml"));
        assertTrue(core.files().contains("config/elarion/core/realms.yml"));
        assertTrue(core.files().contains("config/elarion/core/titles.yml"));
        assertTrue(core.files().contains("config/elarion/core/title-progression.yml"));
        assertTrue(core.files().contains("config/elarion/core/rewards.yml"));
        assertTrue(core.files().contains("config/elarion/core/chat.yml"));
        assertTrue(core.files().contains("config/elarion/core/identity.yml"));
        assertTrue(core.files().contains("config/elarion/core/history.yml"));

        ElarionConfigEntry<?> fontScale = core.entry("ui_theme", "defaults.font-scale-percent").orElseThrow();
        assertEquals("Font Scale", fontScale.label());
        assertEquals("100", fontScale.defaultDisplayValue());
        assertEquals("100", fontScale.currentDisplayValue());
        assertEquals("100", fontScale.minimum());
        assertEquals("150", fontScale.maximum());
        assertTrue(fontScale.runtimeReloadable());

        Path theme = tempDir.resolve("ui_theme.yml");
        Files.writeString(theme, Files.readString(theme, StandardCharsets.UTF_8)
                .replace("font-scale-percent: 100", "font-scale-percent: 125"), StandardCharsets.UTF_8);
        config.load();

        assertEquals("125", fontScale.currentDisplayValue());
        assertTrue(fontScale.validateCurrent().isEmpty());

        Path identity = tempDir.resolve("server_identity.yml");
        Files.writeString(identity, Files.readString(identity, StandardCharsets.UTF_8)
                .replace("server-name: \"Elarion\"", "server-name: \"Asterfall\""), StandardCharsets.UTF_8);
        config.load();

        ElarionConfigEntry<?> serverName = core.entry("server_identity", "identity.server-name").orElseThrow();
        assertEquals("Elarion", serverName.defaultDisplayValue());
        assertEquals("Asterfall", serverName.currentDisplayValue());

        assertEquals("14", core.entry("citizens", "activity.inactivity-days")
                .orElseThrow().currentDisplayValue());
        assertEquals("64", core.entry("chat", "local.radius").orElseThrow().currentDisplayValue());
        assertEquals("false", core.entry("identity", "protection.reject-containing-protected-name")
                .orElseThrow().currentDisplayValue());
        assertEquals("chat", core.entry("history", "recording.disabled-categories")
                .orElseThrow().currentDisplayValue());
        assertEquals("200", core.entry("history", "public-query.max-limit")
                .orElseThrow().currentDisplayValue());
        assertEquals("3", core.entry("realms", "realms.count").orElseThrow().currentDisplayValue());
        assertEquals("realm1, realm2, realm3",
                core.entry("realms", "realms.ids").orElseThrow().currentDisplayValue());
        assertEquals("Wilderness I",
                core.entry("realms", "realms.realm1.display-name").orElseThrow().currentDisplayValue());
        assertEquals("green",
                core.entry("realms", "realms.realm1.color").orElseThrow().currentDisplayValue());
        assertEquals("REALM",
                core.entry("realms", "realms.realm1.visibility-scope").orElseThrow().currentDisplayValue());
        assertEquals("elarion:realm_world_1",
                core.entry("realms", "realms.realm1.spawn.world").orElseThrow().currentDisplayValue());
        assertEquals("-367",
                core.entry("realms", "realms.realm1.spawn.x").orElseThrow().currentDisplayValue());
        assertEquals("75",
                core.entry("realms", "realms.realm1.spawn.y").orElseThrow().currentDisplayValue());
        assertEquals("138",
                core.entry("realms", "realms.realm1.spawn.z").orElseThrow().currentDisplayValue());
        assertEquals("3000",
                core.entry("realms", "realms.realm2.spawn.x").orElseThrow().currentDisplayValue());
        assertEquals("128",
                core.entry("realms", "realms.realm2.spawn.y").orElseThrow().currentDisplayValue());
        assertEquals("3920",
                core.entry("realms", "realms.realm2.spawn.z").orElseThrow().currentDisplayValue());
        assertEquals("6061",
                core.entry("realms", "realms.realm3.spawn.x").orElseThrow().currentDisplayValue());
        assertEquals("84",
                core.entry("realms", "realms.realm3.spawn.y").orElseThrow().currentDisplayValue());
        assertEquals("5122",
                core.entry("realms", "realms.realm3.spawn.z").orElseThrow().currentDisplayValue());
        assertEquals("15", core.entry("titles", "titles.count").orElseThrow().currentDisplayValue());
        assertEquals("Ember",
                core.entry("titles", "titles.citizen.display-name").orElseThrow().currentDisplayValue());
        assertEquals("#C9C9C9",
                core.entry("titles", "titles.citizen.color").orElseThrow().currentDisplayValue());
        assertEquals("#FFD36A",
                core.entry("titles", "titles.government_monarch.color").orElseThrow().currentDisplayValue());
        assertEquals("DEFAULT",
                core.entry("titles", "titles.citizen.acquisition-mode").orElseThrow().currentDisplayValue());
        assertEquals("UNLIMITED",
                core.entry("titles", "titles.citizen.ownership-mode").orElseThrow().currentDisplayValue());
        assertEquals("1",
                core.entry("titles", "titles.aquatic.active-effects.count").orElseThrow().currentDisplayValue());
        assertTrue(core.entry("titles", "titles.aquatic.active-effects.summary")
                .orElseThrow().currentDisplayValue().contains("minecraft:water_breathing"));
        assertEquals("1", core.entry("title_progression", "regions.count")
                .orElseThrow().currentDisplayValue());
        assertEquals("elarion:worldheart",
                core.entry("title_progression", "regions.maze_end.world").orElseThrow().currentDisplayValue());
        assertEquals("4", core.entry("title_progression", "rules.count")
                .orElseThrow().currentDisplayValue());
        assertEquals("1000",
                core.entry("title_progression", "rules.goblin_slayer.threshold")
                        .orElseThrow().currentDisplayValue());
        assertEquals("#elarion:goblins, minecraft:zombie",
                core.entry("title_progression", "rules.goblin_slayer.entities")
                        .orElseThrow().currentDisplayValue());
        assertTrue(core.entry("title_progression", "rules.aquatic.continuous")
                .orElseThrow().currentDisplayValue().contains("duration=3 minecraft_days"));
        assertEquals("2", core.entry("rewards", "rewards.count").orElseThrow().currentDisplayValue());
        assertEquals("starter_diamonds, welcome",
                core.entry("rewards", "rewards.ids").orElseThrow().currentDisplayValue());
        assertEquals("1",
                core.entry("rewards", "rewards.welcome.actions.count").orElseThrow().currentDisplayValue());
        assertEquals("message",
                core.entry("rewards", "rewards.welcome.actions.0.type").orElseThrow().currentDisplayValue());
        assertEquals("text=Welcome to Asterfall.",
                core.entry("rewards", "rewards.welcome.actions.0.parameters")
                        .orElseThrow().currentDisplayValue());
        assertEquals("item, message",
                core.entry("rewards", "rewards.starter_diamonds.actions.types")
                        .orElseThrow().currentDisplayValue());
        assertEquals("count=3, id=minecraft:diamond",
                core.entry("rewards", "rewards.starter_diamonds.actions.0.parameters")
                        .orElseThrow().currentDisplayValue());

        Path chat = tempDir.resolve("chat.yml");
        Files.writeString(chat, Files.readString(chat, StandardCharsets.UTF_8)
                .replace("radius: 64", "radius: 80"), StandardCharsets.UTF_8);
        Path activity = tempDir.resolve("activity.yml");
        Files.writeString(activity, Files.readString(activity, StandardCharsets.UTF_8)
                .replace("inactivity-days: 14", "inactivity-days: 21"), StandardCharsets.UTF_8);
        Path realms = tempDir.resolve("realms.yml");
        Files.writeString(realms, Files.readString(realms, StandardCharsets.UTF_8)
                .replace("display-name: \"Wilderness I\"", "display-name: \"Asterfall\"")
                .replace("color: \"green\"", "color: \"red\"")
                .replace("visibility-scope: \"REALM\"", "visibility-scope: \"GLOBAL\"")
                .replace("world: \"elarion:realm_world_1\"", "world: \"elarion:asterfall\"")
                .replace("y: 75", "y: 72"), StandardCharsets.UTF_8);
        Path titles = tempDir.resolve("titles.yml");
        Files.writeString(titles, Files.readString(titles, StandardCharsets.UTF_8)
                .replace("display-name: \"Ember\"", "display-name: \"Resident\"")
                .replace("color: \"#C9C9C9\"", "color: \"#44AAFF\"")
                .replace("description: \"An Ember of %server%.\"",
                        "description: \"A resident of %server%.\""), StandardCharsets.UTF_8);
        Path titleProgression = tempDir.resolve("title-progression.yml");
        Files.writeString(titleProgression, Files.readString(titleProgression, StandardCharsets.UTF_8)
                .replace("world: \"elarion:worldheart\"", "world: \"elarion:maze\"")
                .replace("threshold: 1000", "threshold: 2000")
                .replace("duration: 3", "duration: 4"), StandardCharsets.UTF_8);
        Path rewards = tempDir.resolve("rewards.yml");
        Files.writeString(rewards, Files.readString(rewards, StandardCharsets.UTF_8)
                .replace("text: \"Welcome to %server%.\"", "text: \"Welcome back to %server%.\"")
                .replace("count: 3", "count: 5"), StandardCharsets.UTF_8);
        config.load();

        assertEquals("80", core.entry("chat", "local.radius").orElseThrow().currentDisplayValue());
        assertEquals("21", core.entry("citizens", "activity.inactivity-days")
                .orElseThrow().currentDisplayValue());
        assertEquals("Asterfall",
                core.entry("realms", "realms.realm1.display-name").orElseThrow().currentDisplayValue());
        assertEquals("red",
                core.entry("realms", "realms.realm1.color").orElseThrow().currentDisplayValue());
        assertEquals("GLOBAL",
                core.entry("realms", "realms.realm1.visibility-scope").orElseThrow().currentDisplayValue());
        assertEquals("elarion:asterfall",
                core.entry("realms", "realms.realm1.spawn.world").orElseThrow().currentDisplayValue());
        assertEquals("72",
                core.entry("realms", "realms.realm1.spawn.y").orElseThrow().currentDisplayValue());
        assertEquals("Resident",
                core.entry("titles", "titles.citizen.display-name").orElseThrow().currentDisplayValue());
        assertEquals("A resident of Asterfall.",
                core.entry("titles", "titles.citizen.description").orElseThrow().currentDisplayValue());
        assertEquals("#44AAFF",
                core.entry("titles", "titles.citizen.color").orElseThrow().currentDisplayValue());
        assertEquals("elarion:maze",
                core.entry("title_progression", "regions.maze_end.world").orElseThrow().currentDisplayValue());
        assertEquals("2000",
                core.entry("title_progression", "rules.goblin_slayer.threshold")
                        .orElseThrow().currentDisplayValue());
        assertTrue(core.entry("title_progression", "rules.aquatic.continuous")
                .orElseThrow().currentDisplayValue().contains("duration=4 minecraft_days"));
        assertEquals("text=Welcome back to Asterfall.",
                core.entry("rewards", "rewards.welcome.actions.0.parameters")
                        .orElseThrow().currentDisplayValue());
        assertEquals("count=5, id=minecraft:diamond",
                core.entry("rewards", "rewards.starter_diamonds.actions.0.parameters")
                        .orElseThrow().currentDisplayValue());
    }

    @Test
    void descriptorCollectionsAreImmutable() {
        ElarionConfigDomain domain = testDomain("core");

        assertThrows(UnsupportedOperationException.class, () -> domain.files().add("other.yml"));
        assertThrows(UnsupportedOperationException.class, () -> domain.categories().clear());
        assertThrows(UnsupportedOperationException.class, () -> domain.categories().getFirst().entries().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> domain.categories().getFirst().entries().getFirst().choices().add("x"));
    }

    private static ElarionConfigDomain testDomain(String id) {
        return new ElarionConfigDomain(
                id,
                "test:" + id,
                id,
                "Test domain",
                List.of("config/elarion/" + id + ".yml"),
                "/e test reload",
                List.of(new ElarionConfigCategory(
                        "general",
                        "General",
                        "General settings",
                        List.of(new ElarionConfigEntry<>(
                                "enabled",
                                "Enabled",
                                "Whether the test domain is enabled.",
                                id + ".enabled",
                                ElarionConfigCodec.BOOLEAN,
                                true,
                                () -> true,
                                ElarionConfigValidator.pass(),
                                List.of("true", "false"),
                                "",
                                "",
                                true,
                                false,
                                ElarionConfigPermission.OPERATOR,
                                ElarionConfigPermission.OPERATOR)))));
    }
}
