package panetina.elarion.addons.quests.model;

public record QuestActorDefinition(
        String id,
        String npc,
        String displayName,
        String role,
        java.util.List<String> allowedNpcDefinitions,
        boolean required
) {
    public QuestActorDefinition(
            String id,
            String npc,
            String displayName,
            String role
    ) {
        this(id, npc, displayName, role, npc == null || npc.isBlank() ? java.util.List.of() : java.util.List.of(npc),
                true);
    }

    public QuestActorDefinition {
        id = id == null ? "" : id;
        npc = npc == null ? "" : npc;
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        role = role == null ? "" : role;
        allowedNpcDefinitions = allowedNpcDefinitions == null
                ? java.util.List.of()
                : allowedNpcDefinitions.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
    }
}
