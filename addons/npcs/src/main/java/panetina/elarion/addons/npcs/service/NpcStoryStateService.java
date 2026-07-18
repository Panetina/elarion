package panetina.elarion.addons.npcs.service;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.NpcStoryStateRecord;
import panetina.elarion.addons.npcs.storage.NpcStoryStateStorage;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

public final class NpcStoryStateService {
    private final Logger logger;
    private final NpcStoryStateStorage storage;
    private final Map<String, NpcStoryStateRecord> states = new LinkedHashMap<>();
    private MinecraftServer server;
    private boolean bound;

    public NpcStoryStateService(Logger logger, NpcStoryStateStorage storage) {
        this.logger = logger;
        this.storage = storage;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        this.bound = true;
        states.clear();
        states.putAll(storage.load(server));
    }

    public synchronized void shutdown() {
        if (bound) storage.save(server, states);
    }

    public synchronized NpcStoryStateRecord state(UUID playerId, UUID npcId) {
        if (playerId == null || npcId == null) return NpcStoryStateRecord.empty(playerId, npcId);
        return states.getOrDefault(NpcStoryStateStorage.key(playerId, npcId),
                NpcStoryStateRecord.empty(playerId, npcId));
    }

    public synchronized boolean hasFlag(UUID playerId, UUID npcId, String flag) {
        return state(playerId, npcId).flags().contains(requireId(flag, "flag"));
    }

    public synchronized NpcStoryStateRecord setFlag(UUID playerId, UUID npcId, String flag, boolean enabled) {
        NpcStoryStateRecord current = state(playerId, npcId);
        LinkedHashSet<String> flags = new LinkedHashSet<>(current.flags());
        if (enabled) flags.add(requireId(flag, "flag"));
        else flags.remove(requireId(flag, "flag"));
        return store(current, flags, current.usedChoices(), current.endingId(), current.reentryNodeId());
    }

    public synchronized boolean choiceUsed(UUID playerId, UUID npcId, String choiceKey) {
        return state(playerId, npcId).usedChoices().contains(requireId(choiceKey, "choiceKey"));
    }

    public synchronized NpcStoryStateRecord markChoiceUsed(UUID playerId, UUID npcId, String choiceKey) {
        NpcStoryStateRecord current = state(playerId, npcId);
        LinkedHashSet<String> choices = new LinkedHashSet<>(current.usedChoices());
        choices.add(requireId(choiceKey, "choiceKey"));
        return store(current, current.flags(), choices, current.endingId(), current.reentryNodeId());
    }

    public synchronized NpcStoryStateRecord setEnding(UUID playerId, UUID npcId, String endingId) {
        NpcStoryStateRecord current = state(playerId, npcId);
        return store(current, current.flags(), current.usedChoices(), requireId(endingId, "endingId"),
                current.reentryNodeId());
    }

    public synchronized NpcStoryStateRecord setReentryNode(UUID playerId, UUID npcId, String nodeId) {
        NpcStoryStateRecord current = state(playerId, npcId);
        return store(current, current.flags(), current.usedChoices(), current.endingId(), clean(nodeId));
    }

    public static String choiceKey(String dialogueId, String nodeId, String optionId) {
        return requireId(dialogueId, "dialogueId") + "/" + requireId(nodeId, "nodeId")
                + "/" + requireId(optionId, "optionId");
    }

    private NpcStoryStateRecord store(
            NpcStoryStateRecord current,
            java.util.Set<String> flags,
            java.util.Set<String> choices,
            String endingId,
            String reentryNodeId
    ) {
        requireBound();
        NpcStoryStateRecord updated = new NpcStoryStateRecord(
                require(current.playerId(), "playerId"), require(current.npcId(), "npcId"),
                flags, choices, endingId, reentryNodeId, System.currentTimeMillis());
        states.put(NpcStoryStateStorage.key(updated.playerId(), updated.npcId()), updated);
        persist();
        return updated;
    }

    private void persist() {
        try {
            storage.saveChecked(server, states);
        } catch (IllegalStateException exception) {
            logger.error("Failed to persist NPC story state", exception);
            throw exception;
        }
    }

    private void requireBound() {
        if (!bound) throw new IllegalStateException("NPC story-state service is not bound to a server");
    }

    private static UUID require(UUID value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " cannot be null");
        return value;
    }

    private static String requireId(String value, String name) {
        String clean = clean(value);
        if (clean.isBlank()) throw new IllegalArgumentException(name + " cannot be blank");
        return clean;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
