package panetina.elarion.core.model;

public record ElarionNotificationAction(
        String id,
        String label,
        boolean enabled
) {
    public ElarionNotificationAction {
        id = id == null ? "" : id;
        label = label == null ? "" : label;
    }
}
