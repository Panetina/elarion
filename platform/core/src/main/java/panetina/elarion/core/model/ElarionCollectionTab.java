package panetina.elarion.core.model;

import java.util.List;

public record ElarionCollectionTab(
        String id,
        String title,
        String subtitle,
        List<ElarionCollectionEntry> entries
) {
    public ElarionCollectionTab {
        id = clean(id);
        title = clean(title);
        subtitle = clean(subtitle);
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
