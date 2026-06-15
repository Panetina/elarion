package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElarionNotificationSnapshotTest {
    @Test
    void questFilterReturnsOnlyQuestEntries() {
        ElarionNotificationSnapshot snapshot = new ElarionNotificationSnapshot(List.of(
                entry("mail", ElarionNotificationCategory.MAIL),
                entry("realm", ElarionNotificationCategory.REALM),
                entry("quest", ElarionNotificationCategory.QUEST)
        ));

        List<ElarionNotificationEntry> quests = snapshot.filtered("quest", 10);

        assertEquals(1, quests.size());
        assertEquals("quest", quests.getFirst().id());
    }

    @Test
    void emptyQuestFilterIsEmptyAndNotUnread() {
        assertTrue(ElarionNotificationSnapshot.EMPTY.filtered("quest", 10).isEmpty());
        assertFalse(ElarionNotificationSnapshot.EMPTY.hasUnread("quest"));
    }

    @Test
    void unreadStateRespectsFilter() {
        ElarionNotificationSnapshot snapshot = new ElarionNotificationSnapshot(List.of(
                entry("quest", ElarionNotificationCategory.QUEST)
        ));

        assertTrue(snapshot.hasUnread("quest"));
        assertFalse(snapshot.hasUnread("realm"));
    }

    @Test
    void personalAndRealmFiltersAreDistinct() {
        ElarionNotificationSnapshot snapshot = new ElarionNotificationSnapshot(List.of(
                entry("personal", ElarionNotificationCategory.PERSONAL),
                entry("reward", ElarionNotificationCategory.REWARD),
                entry("realm", ElarionNotificationCategory.REALM),
                entry("government", ElarionNotificationCategory.GOVERNMENT)
        ));

        assertEquals(java.util.Set.of("personal", "reward"),
                snapshot.filtered("personal", 10).stream()
                        .map(ElarionNotificationEntry::id).collect(java.util.stream.Collectors.toSet()));
        assertEquals(java.util.Set.of("government", "realm"),
                snapshot.filtered("realm", 10).stream()
                        .map(ElarionNotificationEntry::id).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void unreadEntriesSortBeforeReadThenNewestFirst() {
        ElarionNotificationEntry oldUnread = entry("old-unread", ElarionNotificationCategory.PERSONAL, true, 10L);
        ElarionNotificationEntry newRead = entry("new-read", ElarionNotificationCategory.PERSONAL, false, 30L);
        ElarionNotificationEntry newUnread = entry("new-unread", ElarionNotificationCategory.PERSONAL, true, 20L);

        ElarionNotificationSnapshot snapshot =
                new ElarionNotificationSnapshot(List.of(oldUnread, newRead, newUnread));

        assertEquals(List.of("new-unread", "old-unread", "new-read"),
                snapshot.filtered("personal", 10).stream().map(ElarionNotificationEntry::id).toList());
    }

    private static ElarionNotificationEntry entry(String id, ElarionNotificationCategory category) {
        return new ElarionNotificationEntry(id, category, id, "body", "", "", true, List.of());
    }

    private static ElarionNotificationEntry entry(
            String id, ElarionNotificationCategory category, boolean unread, long createdAt
    ) {
        return new ElarionNotificationEntry(
                id, category, id, "body", "", "", unread, List.of(), List.of(), createdAt);
    }
}
