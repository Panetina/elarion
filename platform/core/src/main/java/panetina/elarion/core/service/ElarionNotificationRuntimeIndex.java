package panetina.elarion.core.service;

import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionStoredNotification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/** Runtime-only notification lookup projection. Persistent notification rows remain canonical. */
final class ElarionNotificationRuntimeIndex {
    private final Map<UUID, LinkedHashSet<String>> byRecipient = new LinkedHashMap<>();
    private final Map<UUID, Map<ElarionNotificationCategory, LinkedHashSet<String>>> byRecipientCategory =
            new LinkedHashMap<>();
    private final TreeMap<Long, LinkedHashSet<String>> byExpiry = new TreeMap<>();

    void clear() {
        byRecipient.clear();
        byRecipientCategory.clear();
        byExpiry.clear();
    }

    void add(ElarionStoredNotification notification) {
        if (!eligible(notification)) return;
        byRecipient.computeIfAbsent(notification.recipientId(), ignored -> new LinkedHashSet<>())
                .add(notification.id());
        byRecipientCategory.computeIfAbsent(notification.recipientId(), ignored -> new LinkedHashMap<>())
                .computeIfAbsent(notification.category(), ignored -> new LinkedHashSet<>())
                .add(notification.id());
        if (notification.expiresAt() > 0L) {
            byExpiry.computeIfAbsent(notification.expiresAt(), ignored -> new LinkedHashSet<>())
                    .add(notification.id());
        }
    }

    void remove(ElarionStoredNotification notification) {
        if (!eligible(notification)) return;
        remove(byRecipient, notification.recipientId(), notification.id());
        Map<ElarionNotificationCategory, LinkedHashSet<String>> categories =
                byRecipientCategory.get(notification.recipientId());
        if (categories != null) {
            LinkedHashSet<String> ids = categories.get(notification.category());
            if (ids != null) {
                ids.remove(notification.id());
                if (ids.isEmpty()) categories.remove(notification.category());
            }
            if (categories.isEmpty()) byRecipientCategory.remove(notification.recipientId());
        }
        if (notification.expiresAt() > 0L) {
            LinkedHashSet<String> ids = byExpiry.get(notification.expiresAt());
            if (ids != null) {
                ids.remove(notification.id());
                if (ids.isEmpty()) byExpiry.remove(notification.expiresAt());
            }
        }
    }

    List<String> recipientIds(UUID recipientId) {
        return copy(byRecipient.get(recipientId));
    }

    List<UUID> recipients() {
        return List.copyOf(byRecipient.keySet());
    }

    List<String> recipientCategoryIds(UUID recipientId, ElarionNotificationCategory category) {
        Map<ElarionNotificationCategory, LinkedHashSet<String>> categories = byRecipientCategory.get(recipientId);
        return categories == null ? List.of() : copy(categories.get(category));
    }

    List<String> expired(long now) {
        List<String> ids = new ArrayList<>();
        byExpiry.headMap(now, true).values().forEach(ids::addAll);
        return ids;
    }

    int scheduledExpiryCount() {
        return byExpiry.values().stream().mapToInt(java.util.Collection::size).sum();
    }

    private static boolean eligible(ElarionStoredNotification notification) {
        return notification != null && notification.recipientId() != null && !notification.id().isBlank();
    }

    private static List<String> copy(LinkedHashSet<String> ids) {
        return ids == null || ids.isEmpty() ? List.of() : List.copyOf(ids);
    }

    private static void remove(Map<UUID, LinkedHashSet<String>> index, UUID recipientId, String id) {
        LinkedHashSet<String> ids = index.get(recipientId);
        if (ids == null) return;
        ids.remove(id);
        if (ids.isEmpty()) index.remove(recipientId);
    }
}
