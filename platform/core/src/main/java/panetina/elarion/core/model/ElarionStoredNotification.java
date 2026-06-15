package panetina.elarion.core.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ElarionStoredNotification(
        String id,
        UUID recipientId,
        ElarionNotificationCategory category,
        String sourceSystem,
        String eventType,
        String deduplicationKey,
        String title,
        String body,
        String status,
        String icon,
        boolean unread,
        boolean resolved,
        long createdAt,
        long expiresAt,
        List<ElarionNotificationAction> actions,
        Map<String, String> metadata
) {
    public ElarionStoredNotification {
        id = clean(id);
        category = category == null ? ElarionNotificationCategory.PERSONAL : category;
        sourceSystem = clean(sourceSystem);
        eventType = clean(eventType);
        deduplicationKey = clean(deduplicationKey);
        title = clean(title);
        body = clean(body);
        status = clean(status);
        icon = clean(icon);
        createdAt = createdAt <= 0L ? System.currentTimeMillis() : createdAt;
        actions = actions == null ? List.of() : List.copyOf(actions);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public ElarionStoredNotification read() {
        return new ElarionStoredNotification(id, recipientId, category, sourceSystem, eventType,
                deduplicationKey, title, body, status, icon, false, resolved, createdAt, expiresAt,
                actions, metadata);
    }

    public ElarionStoredNotification withStatus(String nextStatus) {
        return new ElarionStoredNotification(id, recipientId, category, sourceSystem, eventType,
                deduplicationKey, title, body, nextStatus, icon, unread, resolved, createdAt, expiresAt,
                actions, metadata);
    }

    public ElarionStoredNotification resolve() {
        return new ElarionStoredNotification(id, recipientId, category, sourceSystem, eventType,
                deduplicationKey, title, body, status, icon, false, true, createdAt, expiresAt,
                List.of(), metadata);
    }

    public boolean expired(long now) {
        return expiresAt > 0L && now >= expiresAt;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
