package panetina.elarion.core.model;

import java.util.List;

public record ElarionAdminPanelTab(
        String id,
        String title,
        String subtitle,
        List<ElarionAdminPanelRow> rows
) {
    public ElarionAdminPanelTab {
        id = clean(id);
        title = clean(title);
        subtitle = clean(subtitle);
        rows = rows == null ? List.of() : List.copyOf(rows);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
