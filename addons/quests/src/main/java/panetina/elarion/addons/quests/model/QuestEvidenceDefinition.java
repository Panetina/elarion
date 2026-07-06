package panetina.elarion.addons.quests.model;

public record QuestEvidenceDefinition(
        String id,
        String displayName,
        String description,
        String icon
) {
    public QuestEvidenceDefinition {
        id = id == null ? "" : id;
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        description = description == null ? "" : description;
        icon = icon == null ? "" : icon;
    }
}
