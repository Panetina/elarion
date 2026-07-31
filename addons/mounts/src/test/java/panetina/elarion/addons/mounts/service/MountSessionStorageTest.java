package panetina.elarion.addons.mounts.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MountSessionStorageTest {
    @TempDir
    Path root;

    @Test
    void recoverableNullRowsDoNotDiscardValidSessions() throws Exception {
        UUID player = UUID.randomUUID();
        UUID discarded = UUID.randomUUID();
        Path file = root.resolve("sessions.json");
        Files.writeString(file, """
                {
                  "sessions": {
                    "%s": {
                      "mountType": " WYVERN ",
                      "worldId": " minecraft:overworld ",
                      "x": 12.5,
                      "y": 80.0,
                      "z": -3.0,
                      "yaw": 45.0,
                      "updatedAt": -5
                    },
                    "%s": null
                  }
                }
                """.formatted(player, discarded));
        MountSessionService service = new MountSessionService(LoggerFactory.getLogger("test"));

        Map<UUID, MountSessionService.StoredSession> loaded = service.loadSessions(file);

        assertEquals(1, loaded.size());
        MountSessionService.StoredSession session = loaded.get(player);
        assertEquals(ElarionMountType.WYVERN.id(), session.mountType);
        assertEquals("minecraft:overworld", session.worldId);
        assertEquals(0L, session.updatedAt);
        assertTrue(Files.exists(file));
        assertNoQuarantinedSessions();
    }

    @Test
    void explicitNullSessionMapLoadsAsMutableEmptyState() throws Exception {
        Path file = root.resolve("sessions.json");
        Files.writeString(file, """
                {"sessions": null}
                """);
        MountSessionService service = new MountSessionService(LoggerFactory.getLogger("test"));

        Map<UUID, MountSessionService.StoredSession> loaded = service.loadSessions(file);

        loaded.put(UUID.randomUUID(), new MountSessionService.StoredSession());
        assertEquals(1, loaded.size());
        assertTrue(Files.exists(file));
        assertNoQuarantinedSessions();
    }

    private void assertNoQuarantinedSessions() throws Exception {
        try (var files = Files.list(root)) {
            assertTrue(files.noneMatch(path ->
                    path.getFileName().toString().startsWith("sessions.json.corrupt-")));
        }
    }
}
