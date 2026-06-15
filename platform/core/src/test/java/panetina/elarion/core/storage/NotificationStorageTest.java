package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionStoredNotification;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
