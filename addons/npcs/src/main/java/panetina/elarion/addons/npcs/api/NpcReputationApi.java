package panetina.elarion.addons.npcs.api;

import java.util.UUID;

public interface NpcReputationApi {
    NpcFactionReputation faction(UUID playerId, String factionId);

    boolean meets(UUID playerId, String factionId, long minimumScore);

    boolean meetsStanding(UUID playerId, String factionId, String standingId);
}
