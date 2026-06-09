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

        assertEquals(1, config.communities().size());
        try (var files = Files.list(tempDir)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".yml")).toList()) {
                assertTrue(Files.readString(file).contains("config-version: 1"), file.toString());
            }
        }
    }

    @Test
    void rejectsInvalidFieldWithPrecisePathAndKeepsPreviousSnapshot() throws Exception {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("config-test"), tempDir);
        config.load();
        int previousCommunityCount = config.communities().size();
        Path communities = tempDir.resolve("communities.yml");
        String content = Files.readString(communities, StandardCharsets.UTF_8)
                .replace("color: \"green\"", "color: \"gren\"");
        Files.writeString(communities, content, StandardCharsets.UTF_8);

        ConfigValidationException exception =
                assertThrows(ConfigValidationException.class, config::load);

        assertTrue(exception.errors().stream()
                .anyMatch(error -> error.contains("communities.yml.communities.oak.color")));
        assertEquals(previousCommunityCount, config.communities().size());
    }
}
