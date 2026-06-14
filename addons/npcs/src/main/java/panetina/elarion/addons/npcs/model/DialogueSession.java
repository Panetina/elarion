package panetina.elarion.addons.npcs.model;

import java.util.UUID;

public record DialogueSession(UUID playerId, UUID npcId, String dialogueId, String nodeId, long updatedAt) {
}
