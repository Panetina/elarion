package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionStoredNotification;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NotificationStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsPersistentNotificationState() {
        Path file = tempDir.resolve("notifications.json");
        NotificationStorage storage =
                new NotificationStorage(LoggerFactory.getLogger("test"), file);
        UUID recipient = UUID.randomUUID();
        ElarionStoredNotification notification = new ElarionStoredNotification(
                "notice-1",
                recipient,
                ElarionNotificationCategory.GOVERNMENT,
                "elarion_government",
                "vote-open",
                "realm1:vote-open",
                "Voting Open",
                "Cast a private ballot.",
                "",
                "item:minecraft:writable_book",
                true,
                false,
                100L,
                200L,
                List.of(new ElarionNotificationAction("test:open", "Open", true)),
                Map.of("realmId", "realm1"));

        storage.save(file, List.of(notification));

        assertEquals(List.of(notification), storage.load(file));
    }

    @Test
    void recoverableNullRowsDoNotDiscardValidNotifications() throws Exception {
        Path file = tempDir.resolve("notifications.json");
        UUID recipient = UUID.randomUUID();
        Files.writeString(file, """
                {
                  "notifications": [
                    {
                      "id": "notice-1",
                      "recipientId": "%s",
                      "category": "PERSONAL",
                      "title": "Welcome",
                      "actions": [],
                      "metadata": {}
                    },
                    null
                  ]
                }
                """.formatted(recipient));
        NotificationStorage storage =
                new NotificationStorage(LoggerFactory.getLogger("test"), file);

        List<ElarionStoredNotification> loaded = storage.load(file);

        assertEquals(1, loaded.size());
        assertEquals("notice-1", loaded.getFirst().id());
        assertEquals(recipient, loaded.getFirst().recipientId());
        assertTrue(Files.exists(file));
        try (var files = Files.list(tempDir)) {
            assertTrue(files.noneMatch(path ->
                    path.getFileName().toString().startsWith("notifications.json.corrupt-")));
        }
    }

    @Test
    void explicitNullNotificationListLoadsAsEmpty() throws Exception {
        Path file = tempDir.resolve("notifications.json");
        Files.writeString(file, """
                {"notifications": null}
                """);
        NotificationStorage storage =
                new NotificationStorage(LoggerFactory.getLogger("test"), file);

        assertTrue(storage.load(file).isEmpty());
        assertTrue(Files.exists(file));
    }
}
