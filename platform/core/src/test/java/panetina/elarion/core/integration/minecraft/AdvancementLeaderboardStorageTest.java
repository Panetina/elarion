package panetina.elarion.core.integration.minecraft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AdvancementLeaderboardStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void recoverableNullAndInvalidRowsDoNotDiscardValidEntries() throws Exception {
        Path file = tempDir.resolve("core").resolve("advancement-leaderboard.json");
        Files.createDirectories(file.getParent());
        UUID citizenId = UUID.randomUUID();
        Files.writeString(file, """
                {
                  "entries": {
                    "%s": {"name": "Citizen", "realmId": "oak", "completed": 7},
                    "broken": null,
                    "not-a-uuid": {"name": "Invalid", "completed": 99}
                  }
                }
                """.formatted(citizenId));
        AdvancementLeaderboardStorage storage =
                new AdvancementLeaderboardStorage(LoggerFactory.getLogger("test"));

        var loaded = storage.load(tempDir);

        assertEquals(1, loaded.size());
        assertEquals("Citizen", loaded.get(citizenId).name());
        assertEquals(7, loaded.get(citizenId).completed());
        assertTrue(Files.exists(file));
        try (var files = Files.list(file.getParent())) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString()
                    .startsWith("advancement-leaderboard.json.corrupt-")));
        }
    }

    @Test
    void explicitNullEntryMapLoadsAsEmpty() throws Exception {
        Path file = tempDir.resolve("core").resolve("advancement-leaderboard.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, """
                {"entries": null}
                """);

        assertTrue(new AdvancementLeaderboardStorage(LoggerFactory.getLogger("test"))
                .load(tempDir).isEmpty());
        assertTrue(Files.exists(file));
    }
}
