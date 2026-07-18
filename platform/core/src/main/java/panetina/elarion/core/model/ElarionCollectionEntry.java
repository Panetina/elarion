package panetina.elarion.core.model;

import java.util.List;

public record ElarionCollectionEntry(
        String id,
        String title,
        String subtitle,
        String body,
        String state,
        String icon,
        boolean unlocked,
        boolean active,
        List<ElarionCollectionAction> actions,
        int accentColor,
        String rankLabel,
        int rankColor
) {
    public ElarionCollectionEntry {
        id = clean(id);
        title = clean(title);
        subtitle = clean(subtitle);
        body = clean(body);
        state = clean(state);
        icon = clean(icon);
        actions = actions == null ? List.of() : List.copyOf(actions);
        rankLabel = clean(rankLabel);
        accentColor = withAlpha(accentColor);
        rankColor = withAlpha(rankColor);
    }

    public ElarionCollectionEntry(
            String id, String title, String subtitle, String body, String state, String icon,
            boolean unlocked, boolean active, List<ElarionCollectionAction> actions
    ) {
        this(id, title, subtitle, body, state, icon, unlocked, active, actions, 0, "", 0);
    }

    public ElarionCollectionEntry(
            String id, String title, String subtitle, String body, String state, String icon,
            boolean unlocked, boolean active, List<ElarionCollectionAction> actions, int accentColor
    ) {
        this(id, title, subtitle, body, state, icon, unlocked, active, actions, accentColor, "", 0);
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    private static int withAlpha(int color) {
        return color == 0 ? 0 : 0xFF000000 | (color & 0x00FFFFFF);
    }
}
