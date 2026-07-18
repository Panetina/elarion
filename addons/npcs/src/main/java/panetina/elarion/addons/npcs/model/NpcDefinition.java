package panetina.elarion.addons.npcs.model;

public record NpcDefinition(
        String id,
        String displayName,
        String description,
        String skin,
        String portrait,
        String dialogue,
        String faction,
        String tradeCatalog,
        String taxJurisdiction,
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
        this(id, displayName, description, skin, portrait, dialogue, "unaffiliated", "", "auto",
                java.util.List.of(), "", 0.0D, enabled);
    }

    public NpcDefinition(
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
        this(id, displayName, description, skin, portrait, dialogue, "unaffiliated", "", "auto", tags,
                requiredAbility, interactionRangeBlocks, enabled);
    }

    public NpcDefinition(
            String id,
            String displayName,
            String description,
            String skin,
            String portrait,
            String dialogue,
            String tradeCatalog,
            java.util.List<String> tags,
            String requiredAbility,
            double interactionRangeBlocks,
            boolean enabled
    ) {
        this(id, displayName, description, skin, portrait, dialogue, "unaffiliated", tradeCatalog, "auto", tags,
                requiredAbility, interactionRangeBlocks, enabled);
    }

    public NpcDefinition(
            String id, String displayName, String description, String skin, String portrait, String dialogue,
            String tradeCatalog, String taxJurisdiction, java.util.List<String> tags, String requiredAbility,
            double interactionRangeBlocks, boolean enabled
    ) {
        this(id, displayName, description, skin, portrait, dialogue, "unaffiliated", tradeCatalog,
                taxJurisdiction, tags, requiredAbility, interactionRangeBlocks, enabled);
    }

    public NpcDefinition {
        tags = tags == null ? java.util.List.of() : java.util.List.copyOf(tags);
        faction = faction == null || faction.isBlank() ? "unaffiliated"
                : faction.trim().toLowerCase(java.util.Locale.ROOT);
        tradeCatalog = tradeCatalog == null ? "" : tradeCatalog;
        taxJurisdiction = taxJurisdiction == null || taxJurisdiction.isBlank()
                ? "auto"
                : taxJurisdiction.trim().toLowerCase(java.util.Locale.ROOT);
        requiredAbility = requiredAbility == null ? "" : requiredAbility;
    }
}
