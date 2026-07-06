package panetina.elarion.core.model;

import java.util.List;

public record ElarionAdminPanelSnapshot(
        String title,
        String subtitle,
        String selectedTabId,
        String selectedRowId,
        String message,
        List<ElarionAdminPanelTab> tabs
) {
    public ElarionAdminPanelSnapshot {
        title = clean(title);
        subtitle = clean(subtitle);
        selectedTabId = clean(selectedTabId);
        selectedRowId = clean(selectedRowId);
        message = clean(message);
        tabs = tabs == null ? List.of() : List.copyOf(tabs);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
