package panetina.elarion.addons.realms.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RealmProtectionConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsProtectionConfigFromYaml() throws Exception {
        Path file = tempDir.resolve("protection.yml");
        Files.writeString(file, """
                shared-world-ids:
                  - "elarion:worldheart"
                operator-bypass: true
                protect-explosion-blocks: false
                feedback-cooldown-millis: 2500
                extra-ally-interactable-blocks:
                  - "minecraft:bell"
                extra-container-blocks:
                  - "minecraft:barrel"
                """);

        RealmProtectionConfig config = RealmProtectionConfig.load(file, null, RealmProtectionConfig.defaults());

        assertEquals(Set.of("elarion:worldheart"), config.sharedWorldIds());
        assertTrue(config.operatorBypass());
        assertEquals(false, config.protectExplosionBlocks());
        assertEquals(2500L, config.feedbackCooldownMillis());
        assertEquals(Set.of("minecraft:bell"), config.extraAllyInteractableBlocks());
        assertEquals(Set.of("minecraft:barrel"), config.extraContainerBlocks());
    }

    @Test
    void malformedProtectionConfigFallsBackToSafeDefaults() throws Exception {
        Path file = tempDir.resolve("protection.yml");
        Files.writeString(file, "shared-world-ids: [");

        RealmProtectionConfig fallback = new RealmProtectionConfig(
                Set.of("elarion:lobby", "elarion:worldheart"),
                false,
                true,
                1000L,
                Set.of(),
                Set.of());

        RealmProtectionConfig config = RealmProtectionConfig.load(file, null, fallback);

        assertEquals(fallback, config);
    }
}
