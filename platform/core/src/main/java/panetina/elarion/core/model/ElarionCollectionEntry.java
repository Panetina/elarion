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
        List<ElarionCollectionAction> actions
) {
    public ElarionCollectionEntry {
        id = clean(id);
        title = clean(title);
        subtitle = clean(subtitle);
        body = clean(body);
        state = clean(state);
        icon = clean(icon);
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
