package panetina.elarion.core.model;

import java.util.Comparator;
import java.util.List;

public record ElarionNotificationSnapshot(List<ElarionNotificationEntry> entries, boolean worldVisible) {
    public static final ElarionNotificationSnapshot EMPTY = new ElarionNotificationSnapshot(List.of(), false);

    public ElarionNotificationSnapshot(List<ElarionNotificationEntry> entries) {
        this(entries, false);
    }

    public ElarionNotificationSnapshot {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public List<ElarionNotificationEntry> filtered(String filter, int limit) {
        int safeLimit = Math.max(0, limit);
        return entries.stream()
                .filter(entry -> entry.category().matchesFilter(filter))
                .sorted(Comparator.comparing(ElarionNotificationEntry::createdAt, Comparator.reverseOrder())
                        .thenComparing(ElarionNotificationEntry::id))
                .limit(safeLimit)
                .toList();
    }

    public boolean hasUnread(String filter) {
        return entries.stream()
                .anyMatch(entry -> entry.unread() && entry.category().matchesFilter(filter));
    }
}
