package panetina.elarion.core.model;

public record ElarionCollectionAction(
        String id,
        String label,
        boolean enabled
) {
    public ElarionCollectionAction {
        id = clean(id);
        label = clean(label);
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
