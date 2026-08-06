package panetina.elarion.addons.quests.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.npcs.api.ElarionNpcApi;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.quests.model.QuestActorDefinition;
import panetina.elarion.addons.quests.model.QuestDefinition;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Sends a small viewer-specific projection. Definitions opt in through
 * {@code metadata.start-actor}; no NPC or quest state is duplicated here.
 */
public final class QuestNpcMarkerService {
    private final QuestStateService states;
    private final Map<UUID, Set<UUID>> lastSent = new LinkedHashMap<>();
    private final Map<UUID, Set<UUID>> trackedNpcIds = new LinkedHashMap<>();
    private volatile Map<String, List<QuestDefinition>> byNpcDefinition = Map.of();

    public QuestNpcMarkerService(QuestDefinitionService definitions, QuestStateService states) {
        this.states = states;
        rebuildIndex(definitions);
        definitions.onReload(() -> rebuildIndex(definitions));
    }

    public void sync(ServerPlayerEntity player) {
        Set<UUID> marked = new LinkedHashSet<>();
        for (UUID npcId : trackedNpcIds.getOrDefault(player.getUuid(), Set.of())) {
            PlacedNpcRecord npc = ElarionNpcApi.get().findPlacement(npcId).orElse(null);
            if (npc == null) continue;
            List<QuestDefinition> candidates = byNpcDefinition.get(npc.definitionId());
            if (candidates == null) continue;
            for (QuestDefinition quest : candidates) {
                if (!states.markerAvailable(quest, player, npc.worldId())) continue;
                marked.add(npc.id());
                break;
            }
        }
        Set<UUID> immutable = Set.copyOf(marked);
        if (immutable.equals(lastSent.get(player.getUuid()))) return;
        lastSent.put(player.getUuid(), immutable);
        ElarionNpcApi.get().syncQuestMarkers(player, immutable);
    }

    public void track(ServerPlayerEntity player, UUID npcId) {
        if (player == null || npcId == null) return;
        trackedNpcIds.computeIfAbsent(player.getUuid(), ignored -> new LinkedHashSet<>()).add(npcId);
        sync(player);
    }

    public void untrack(ServerPlayerEntity player, UUID npcId) {
        if (player == null || npcId == null) return;
        Set<UUID> tracked = trackedNpcIds.get(player.getUuid());
        if (tracked != null) tracked.remove(npcId);
        sync(player);
    }

    public void syncAll() {
        ElarionNpcApi.get().server().ifPresent(server ->
                server.getPlayerManager().getPlayerList().forEach(this::sync));
    }

    private void rebuildIndex(QuestDefinitionService definitions) {
        Map<String, List<QuestDefinition>> rebuilt = new LinkedHashMap<>();
        for (QuestDefinition quest : definitions.all()) {
            String actorId = quest.metadata().getOrDefault("start-actor", "");
            QuestActorDefinition actor = quest.actors().get(actorId);
            if (actor == null || actor.allowedNpcDefinitions().isEmpty()) continue;
            for (String npcDefinition : actor.allowedNpcDefinitions()) {
                rebuilt.computeIfAbsent(npcDefinition, ignored -> new java.util.ArrayList<>()).add(quest);
            }
        }
        Map<String, List<QuestDefinition>> immutable = new LinkedHashMap<>();
        rebuilt.forEach((key, value) -> immutable.put(key, List.copyOf(value)));
        byNpcDefinition = Map.copyOf(immutable);
        lastSent.clear();
    }

    public void clear(UUID playerId) {
        if (playerId == null) return;
        lastSent.remove(playerId);
        trackedNpcIds.remove(playerId);
    }
}
