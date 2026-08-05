package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionStoredNotification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionNotificationRuntimeIndexTest {
    @Test
    void indexesRecipientsCategoriesAndOnlyDueExpiryRows() {
        ElarionNotificationRuntimeIndex index = new ElarionNotificationRuntimeIndex();
        UUID firstRecipient = UUID.randomUUID();
        UUID secondRecipient = UUID.randomUUID();
        ElarionStoredNotification noExpiry = notification("no-expiry", firstRecipient,
                ElarionNotificationCategory.PERSONAL, 10L, 0L);
        ElarionStoredNotification due = notification("due", firstRecipient,
                ElarionNotificationCategory.REALM, 20L, 100L);
        ElarionStoredNotification future = notification("future", secondRecipient,
                ElarionNotificationCategory.REALM, 30L, 200L);

        index.add(noExpiry);
        index.add(due);
        index.add(future);

        assertEquals(List.of("no-expiry", "due"), index.recipientIds(firstRecipient));
        assertEquals(List.of("due"), index.recipientCategoryIds(firstRecipient, ElarionNotificationCategory.REALM));
        assertEquals(List.of("due"), index.expired(100L));
        assertEquals(2, index.scheduledExpiryCount());

        index.remove(due);

        assertEquals(List.of("no-expiry"), index.recipientIds(firstRecipient));
        assertTrue(index.recipientCategoryIds(firstRecipient, ElarionNotificationCategory.REALM).isEmpty());
        assertEquals(List.of("future"), index.expired(200L));
        assertEquals(1, index.scheduledExpiryCount());
    }

    private static ElarionStoredNotification notification(
            String id,
            UUID recipient,
            ElarionNotificationCategory category,
            long createdAt,
            long expiresAt
    ) {
        return new ElarionStoredNotification(id, recipient, category, "test", "event", "", "title", "body", "",
                "", true, false, createdAt, expiresAt, List.of(), Map.of());
    }
}
