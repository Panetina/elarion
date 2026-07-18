package panetina.elarion.core.integration.minecraft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.CitizenRecord;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AdvancementLeaderboardProjectionTest {
    @TempDir
    Path tempDir;

    @Test
    void persistsAndRanksBoundedEntries() {
        MinecraftProjectionPublisher publisher = new MinecraftProjectionPublisher(LoggerFactory.getLogger("test"), false);
        AdvancementLeaderboardProjection first = new AdvancementLeaderboardProjection(LoggerFactory.getLogger("test"), publisher);
        first.bind(tempDir);
        first.update(citizen("Lower"), 4);
        first.update(citizen("Higher"), 8);

        AdvancementLeaderboardProjection restored = new AdvancementLeaderboardProjection(LoggerFactory.getLogger("test"), publisher);
        restored.bind(tempDir);

        assertEquals(2, restored.leaders().size());
        assertEquals("Higher", restored.leaders().getFirst().name());
        assertEquals(8, restored.leaders().getFirst().completed());
    }

    private static CitizenRecord citizen(String name) {
        return new CitizenRecord(UUID.randomUUID(), name);
    }
}
