package panetina.elarion.addons.npcs.model;

import java.util.UUID;

public record NpcRelationshipRecord(
        UUID playerId,
        UUID npcId,
        int score,
        long updatedAtMillis
) {
    public NpcRelationshipRecord withScore(int score, long updatedAtMillis) {
        return new NpcRelationshipRecord(playerId, npcId, score, updatedAtMillis);
    }
}
