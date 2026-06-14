package panetina.elarion.addons.npcs.model;

public record NpcDefinition(
        String id,
        String displayName,
        String description,
        String skin,
        String portrait,
        String dialogue,
        java.util.List<String> tags,
        String requiredAbility,
        double interactionRangeBlocks,
        boolean enabled
) {
    public NpcDefinition(
            String id,
            String displayName,
            String description,
            String skin,
            String portrait,
            String dialogue,
            boolean enabled
    ) {
        this(id, displayName, description, skin, portrait, dialogue, java.util.List.of(), "", 0.0D, enabled);
    }

    public NpcDefinition {
        tags = tags == null ? java.util.List.of() : java.util.List.copyOf(tags);
        requiredAbility = requiredAbility == null ? "" : requiredAbility;
    }
}
