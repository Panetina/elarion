package panetina.elarion.addons.government.model;

public record GovernmentOfficeDefinition(
        String id,
        String displayName,
        String description,
        int maxHolders
) {
    public GovernmentOfficeDefinition {
        id = id == null ? "" : id;
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        description = description == null ? "" : description;
        maxHolders = Math.max(1, maxHolders);
    }
}
