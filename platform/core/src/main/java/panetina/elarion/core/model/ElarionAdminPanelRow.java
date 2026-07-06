package panetina.elarion.core.model;

import java.util.List;

public record ElarionAdminPanelRow(
        String id,
        String title,
        String subtitle,
        String body,
        String state,
        String icon,
        String kind,
        boolean active,
        boolean danger,
        List<ElarionAdminPanelAction> actions
) {
    public ElarionAdminPanelRow {
        id = clean(id);
        title = clean(title);
        subtitle = clean(subtitle);
        body = clean(body);
        state = clean(state);
        icon = clean(icon);
        kind = clean(kind).isBlank() ? "card" : clean(kind);
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    public static ElarionAdminPanelRow card(
            String id, String title, String subtitle, String body, String state,
            String icon, List<ElarionAdminPanelAction> actions
    ) {
        return new ElarionAdminPanelRow(id, title, subtitle, body, state, icon,
                "card", false, false, actions);
    }

    public static ElarionAdminPanelRow danger(
            String id, String title, String subtitle, String body, String state,
            String icon, List<ElarionAdminPanelAction> actions
    ) {
        return new ElarionAdminPanelRow(id, title, subtitle, body, state, icon,
                "card", false, true, actions);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
