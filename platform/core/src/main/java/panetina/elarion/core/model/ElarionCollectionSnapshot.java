package panetina.elarion.core.model;

import java.util.List;

public record ElarionCollectionSnapshot(
        String title,
        String subtitle,
        String selectedTabId,
        String message,
        List<ElarionCollectionTab> tabs
) {
    public ElarionCollectionSnapshot {
        title = clean(title);
        subtitle = clean(subtitle);
        selectedTabId = clean(selectedTabId);
        message = clean(message);
        tabs = tabs == null ? List.of() : List.copyOf(tabs);
    }

    public static ElarionCollectionSnapshot empty(String message) {
        return new ElarionCollectionSnapshot("Collection", "", "", message, List.of());
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
