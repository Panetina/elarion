package panetina.elarion.addons.quests.model;

public record QuestStageDefinition(
        String id,
        String displayName,
        String description,
        String objective,
        java.util.List<String> next,
        java.util.Map<String, String> metadata
) {
    public QuestStageDefinition(
            String id,
            String displayName,
            String description
    ) {
        this(id, displayName, description, "", java.util.List.of(), java.util.Map.of());
    }

    public QuestStageDefinition {
        id = id == null ? "" : id;
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        description = description == null ? "" : description;
        objective = objective == null ? "" : objective;
        next = next == null ? java.util.List.of() : next.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
        metadata = metadata == null ? java.util.Map.of() : new java.util.LinkedHashMap<>(metadata);
    }
}
