package panetina.elarion.core.model;

public record ElarionNotificationRewardPreview(
        String label,
        String icon,
        int count
) {
    public ElarionNotificationRewardPreview {
        label = label == null ? "" : label;
        icon = icon == null ? "" : icon;
        count = Math.max(0, count);
    }
}
