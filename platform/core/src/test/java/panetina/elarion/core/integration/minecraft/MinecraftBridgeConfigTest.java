package panetina.elarion.core.integration.minecraft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBridgeConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void createsDisabledConfigurationWithoutASecret() throws Exception {
        Path file = tempDir.resolve("minecraft-bridge.yml");

        MinecraftBridgeConfig config = MinecraftBridgeConfig.load(
                file, LoggerFactory.getLogger("test"), Map.of());

        assertFalse(config.enabled());
        assertEquals(30, config.pollSeconds());
        assertTrue(Files.readString(file).contains("enabled: false"));
    }

    @Test
    void environmentOverridesEnableAValidatedConfiguration() {
        MinecraftBridgeConfig config = MinecraftBridgeConfig.load(
                tempDir.resolve("minecraft-bridge.yml"), LoggerFactory.getLogger("test"), Map.of(
                        "ELARION_MINECRAFT_BRIDGE_ENABLED", "true",
                        "ELARION_MINECRAFT_BRIDGE_URL", "https://ashesofelarion.com",
                        "ELARION_MINECRAFT_SERVER_ID", "staging",
                        "ELARION_MINECRAFT_BRIDGE_SECRET", "0123456789abcdef0123456789abcdef"
                ));

        assertTrue(config.enabled());
        assertEquals("staging", config.serverId());
    }

    @Test
    void rejectsInsecureRemoteTransport() throws Exception {
        Path file = tempDir.resolve("minecraft-bridge.yml");
        Files.writeString(file, "enabled: false\nbase-url: http://example.com\n");

        assertThrows(IllegalStateException.class,
                () -> MinecraftBridgeConfig.load(file, LoggerFactory.getLogger("test"), Map.of()));
    }

    @Test
    void rejectsUnsupportedSchemaAndUnknownKeys() throws Exception {
        Path file = tempDir.resolve("minecraft-bridge.yml");
        Files.writeString(file, "version: 2\nenabled: false\n");
        assertThrows(IllegalStateException.class,
                () -> MinecraftBridgeConfig.load(file, LoggerFactory.getLogger("test"), Map.of()));

        Files.writeString(file, "version: 1\nenabled: false\nunbounded-option: true\n");
        assertThrows(IllegalStateException.class,
                () -> MinecraftBridgeConfig.load(file, LoggerFactory.getLogger("test"), Map.of()));
    }
}
