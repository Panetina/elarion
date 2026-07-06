package panetina.elarion.core.model;

public record RealmPresentation(
        String displayName,
        String officialName,
        String shortName,
        String prefix,
        String color
) {
    public RealmPresentation {
        displayName = clean(displayName);
        officialName = clean(officialName).isBlank() ? displayName : clean(officialName);
        shortName = clean(shortName);
        prefix = clean(prefix);
        color = clean(color).isBlank() ? "white" : clean(color);
    }

    public static RealmPresentation from(RealmDefinition realm) {
        if (realm == null) return new RealmPresentation("", "", "", "", "white");
        return new RealmPresentation(realm.displayName(), realm.displayName(), realm.shortName(), realm.prefix(),
                realm.color());
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
