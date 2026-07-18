package panetina.elarion.addons.npcs.api;

public record NpcFactionReputation(
        String factionId,
        long score,
        String standingId,
        String standingLabel,
        int progress,
        int progressMaximum
) {
}
