package panetina.elarion.addons.npcs.client;

import panetina.elarion.addons.npcs.network.NpcQuestMarkerSyncPayload;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcClientQuestMarkers {
    private static final Set<UUID> MARKED = ConcurrentHashMap.newKeySet();
    private NpcClientQuestMarkers() { }
    public static void replace(NpcQuestMarkerSyncPayload payload) {
        MARKED.clear();
        MARKED.addAll(payload.npcIds());
    }
    public static void clear() { MARKED.clear(); }
    public static boolean marked(UUID npcId) { return npcId != null && MARKED.contains(npcId); }
}
