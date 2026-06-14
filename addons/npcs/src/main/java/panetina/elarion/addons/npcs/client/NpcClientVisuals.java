package panetina.elarion.addons.npcs.client;

import panetina.elarion.addons.npcs.network.NpcVisualSyncPayload;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcClientVisuals {
    private static final Map<UUID, NpcVisualSyncPayload.Entry> BY_ENTITY = new ConcurrentHashMap<>();
    private static final Map<UUID, NpcVisualSyncPayload.Entry> BY_NPC = new ConcurrentHashMap<>();

    private NpcClientVisuals() {
    }

    public static void replace(NpcVisualSyncPayload payload) {
        BY_ENTITY.clear();
        BY_NPC.clear();
        payload.entries().forEach(entry -> {
            if (entry.entityId() != null) BY_ENTITY.put(entry.entityId(), entry);
            if (entry.npcId() != null) BY_NPC.put(entry.npcId(), entry);
        });
    }

    public static Optional<NpcVisualSyncPayload.Entry> findByEntity(UUID entityId) {
        return Optional.ofNullable(BY_ENTITY.get(entityId));
    }

    public static Optional<NpcVisualSyncPayload.Entry> findByNpc(UUID npcId) {
        return Optional.ofNullable(BY_NPC.get(npcId));
    }
}
