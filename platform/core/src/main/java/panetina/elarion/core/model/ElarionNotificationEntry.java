package panetina.elarion.core.model;

import java.util.List;

public record ElarionNotificationEntry(
        String id,
        ElarionNotificationCategory category,
        String title,
        String body,
        String status,
        String icon,
        boolean unread,
        List<ElarionNotificationAction> actions,
        List<ElarionNotificationRewardPreview> rewards,
        long createdAt
) {
    public ElarionNotificationEntry(
            String id,
            ElarionNotificationCategory category,
            String title,
            String body,
            String status,
            String icon,
            boolean unread,
            List<ElarionNotificationAction> actions
    ) {
        this(id, category, title, body, status, icon, unread, actions, List.of(), System.currentTimeMillis());
    }

    public ElarionNotificationEntry(
            String id,
            ElarionNotificationCategory category,
            String title,
            String body,
            String status,
            String icon,
            boolean unread,
            List<ElarionNotificationAction> actions,
            List<ElarionNotificationRewardPreview> rewards
    ) {
        this(id, category, title, body, status, icon, unread, actions, rewards, System.currentTimeMillis());
    }

    public ElarionNotificationEntry {
        id = id == null ? "" : id;
        category = category == null ? ElarionNotificationCategory.PERSONAL : category;
        title = title == null ? "" : title;
        body = body == null ? "" : body;
        status = status == null ? "" : status;
        icon = icon == null ? "" : icon;
        actions = actions == null ? List.of() : List.copyOf(actions);
        rewards = rewards == null ? List.of() : List.copyOf(rewards);
        createdAt = Math.max(0L, createdAt);
    }
}
