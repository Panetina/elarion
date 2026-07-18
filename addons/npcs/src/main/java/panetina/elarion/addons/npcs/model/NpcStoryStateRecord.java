package panetina.elarion.addons.npcs.model;

import java.util.Set;
import java.util.UUID;

public record NpcStoryStateRecord(
        UUID playerId,
        UUID npcId,
        Set<String> flags,
        Set<String> usedChoices,
        String endingId,
        String reentryNodeId,
        long updatedAt
) {
    public NpcStoryStateRecord {
        flags = flags == null ? Set.of() : Set.copyOf(flags);
        usedChoices = usedChoices == null ? Set.of() : Set.copyOf(usedChoices);
        endingId = clean(endingId);
        reentryNodeId = clean(reentryNodeId);
    }

    public static NpcStoryStateRecord empty(UUID playerId, UUID npcId) {
        return new NpcStoryStateRecord(playerId, npcId, Set.of(), Set.of(), "", "", 0L);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
