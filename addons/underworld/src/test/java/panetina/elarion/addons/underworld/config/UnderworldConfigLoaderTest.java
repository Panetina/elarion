package panetina.elarion.addons.underworld.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class UnderworldConfigLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsValidSnapshotFromPath() throws Exception {
        Path file = tempDir.resolve("underworld.yml");
        Files.writeString(file, """
                underworld:
                  world-id: "elarion:underworld_custom"
                  pve-timer-minutes: 42
                """, StandardCharsets.UTF_8);

        UnderworldConfig config = UnderworldConfigLoader.load(
                file, LoggerFactory.getLogger("underworld-config-test"));

        assertEquals("elarion:underworld_custom", config.worldId());
        assertEquals(42, config.pveTimerMinutes());
        assertEquals(UnderworldConfig.defaults().pvpTimerMinutes(), config.pvpTimerMinutes());
    }

    @Test
    void malformedInitialLoadFallsBackToDefaults() throws Exception {
        Path file = tempDir.resolve("underworld.yml");
        Files.writeString(file, "underworld: [", StandardCharsets.UTF_8);

        UnderworldConfig config = UnderworldConfigLoader.load(
                file, LoggerFactory.getLogger("underworld-config-test"));

        assertEquals(UnderworldConfig.defaults(), config);
    }

    @Test
    void malformedReloadPreservesPreviousValidSnapshot() throws Exception {
        Path file = tempDir.resolve("underworld.yml");
        UnderworldConfig previous = new UnderworldConfig(
                true,
                "elarion:kept_underworld",
                10.0D, 90.0D, -5.0D,
                44, 55, 66, 7,
                true, false, true, false,
                222,
                true, true, 1, 2, 0.5D, 30, false, true,
                java.util.List.of("minecraft:stick"),
                java.util.List.of("elarion:test_tag"),
                java.util.List.of("elarion:sigil"),
                java.util.List.of("elarion:currency"),
                true, 12,
                true, 4, true,
                java.util.List.of("minecraft:overworld"),
                java.util.List.of("elarion:underworld"));
        Files.writeString(file, "underworld: [", StandardCharsets.UTF_8);

        UnderworldConfig config = UnderworldConfigLoader.reload(
                file, LoggerFactory.getLogger("underworld-config-test"), previous);

        assertSame(previous, config);
        assertEquals("elarion:kept_underworld", config.worldId());
        assertEquals(44, config.pveTimerMinutes());
    }
}
