package panetina.elarion.addons.npcs.service;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.NpcRelationshipRecord;
import panetina.elarion.addons.npcs.model.NpcReputationSummary;
import panetina.elarion.addons.npcs.api.NpcFactionReputation;
import panetina.elarion.addons.npcs.api.NpcReputationApi;
import panetina.elarion.addons.npcs.storage.NpcRelationshipStorage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class NpcRelationshipService implements NpcReputationApi {
    public static final int MIN_SCORE = -10_000;
    public static final int MAX_SCORE = 10_000;

    private final Logger logger;
    private final NpcRelationshipStorage storage;
    private final Map<String, NpcRelationshipRecord> relationships = new LinkedHashMap<>();
    private final Map<UUID, Map<String, MutableSummary>> factionSummaries = new LinkedHashMap<>();
    private final Function<UUID, String> factionResolver;
    private MinecraftServer server;
    private boolean bound;

    public NpcRelationshipService(Logger logger, NpcRelationshipStorage storage) {
        this(logger, storage, ignored -> "unaffiliated");
    }

    public NpcRelationshipService(Logger logger, NpcRelationshipStorage storage, Function<UUID, String> factionResolver) {
        this.logger = logger;
        this.storage = storage;
        this.factionResolver = factionResolver;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        this.bound = true;
        relationships.clear();
        relationships.putAll(storage.load(server));
        rebuildFactionSummaries();
    }

    public synchronized void shutdown() {
        if (bound) storage.save(server, relationships);
    }

    public synchronized int resetAllPlayerState() {
        int changed = relationships.size();
        relationships.clear();
        factionSummaries.clear();
        if (bound) storage.save(server, relationships);
        return changed;
    }

    public synchronized int score(UUID playerId, UUID npcId) {
        return record(playerId, npcId).map(NpcRelationshipRecord::score).orElse(0);
    }

    public synchronized NpcRelationshipRecord set(UUID playerId, UUID npcId, int score) {
        requireBound();
        NpcRelationshipRecord record = new NpcRelationshipRecord(
                require(playerId, "playerId"),
                require(npcId, "npcId"),
                clamp(score),
                System.currentTimeMillis());
        NpcRelationshipRecord previous = relationships.put(NpcRelationshipStorage.key(playerId, npcId), record);
        String faction = faction(npcId);
        MutableSummary summary = mutableSummary(playerId, faction);
        if (previous != null) summary.remove(previous.score());
        summary.add(record.score());
        persist();
        return record;
    }

    public synchronized NpcRelationshipRecord add(UUID playerId, UUID npcId, int amount) {
        int current = score(playerId, npcId);
        return set(playerId, npcId, clamp(current + amount));
    }

    public synchronized Optional<NpcRelationshipRecord> record(UUID playerId, UUID npcId) {
        if (playerId == null || npcId == null) return Optional.empty();
        return Optional.ofNullable(relationships.get(NpcRelationshipStorage.key(playerId, npcId)));
    }

    public synchronized Map<String, NpcReputationSummary> factionSummaries(UUID playerId) {
        Map<String, MutableSummary> values = playerId == null ? null : factionSummaries.get(playerId);
        if (values == null) return Map.of();
        Map<String, NpcReputationSummary> result = new LinkedHashMap<>();
        values.forEach((faction, summary) -> result.put(faction, summary.snapshot()));
        return Map.copyOf(result);
    }

    @Override
    public synchronized NpcFactionReputation faction(UUID playerId, String factionId) {
        String normalized = normalizeFaction(factionId);
        Map<String, MutableSummary> summaries = playerId == null ? null : factionSummaries.get(playerId);
        long score = summaries == null || summaries.get(normalized) == null
                ? 0L : summaries.get(normalized).total;
        NpcReputationTier tier = NpcReputationTier.forScore(score);
        return new NpcFactionReputation(normalized, score, tier.id(), tier.label(),
                tier.progress(), tier.progressMaximum());
    }

    @Override
    public synchronized boolean meets(UUID playerId, String factionId, long minimumScore) {
        return faction(playerId, factionId).score() >= minimumScore;
    }

    @Override
    public synchronized boolean meetsStanding(UUID playerId, String factionId, String standingId) {
        long minimum = NpcReputationTier.minimumScore(standingId);
        return minimum != Long.MAX_VALUE && meets(playerId, factionId, minimum);
    }

    public synchronized void rebuildFactionSummaries() {
        factionSummaries.clear();
        relationships.values().forEach(record ->
                mutableSummary(record.playerId(), faction(record.npcId())).add(record.score()));
    }

    private MutableSummary mutableSummary(UUID playerId, String faction) {
        return factionSummaries.computeIfAbsent(playerId, ignored -> new LinkedHashMap<>())
                .computeIfAbsent(faction, ignored -> new MutableSummary());
    }

    private String faction(UUID npcId) {
        String value = factionResolver.apply(npcId);
        return value == null || value.isBlank() ? "unaffiliated" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static String normalizeFaction(String factionId) {
        return factionId == null || factionId.isBlank()
                ? "unaffiliated" : factionId.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private void persist() {
        try {
            storage.saveChecked(server, relationships);
        } catch (IllegalStateException exception) {
            logger.error("Failed to persist NPC relationship state", exception);
            throw exception;
        }
    }

    private void requireBound() {
        if (!bound) throw new IllegalStateException("NPC relationship service is not bound to a server");
    }

    private static UUID require(UUID value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " cannot be null");
        return value;
    }

    static int clamp(int score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }

    private static final class MutableSummary {
        private int count;
        private long total;

        private void add(int score) {
            count++;
            total += score;
        }

        private void remove(int score) {
            count = Math.max(0, count - 1);
            total -= score;
            if (count == 0) total = 0L;
        }

        private NpcReputationSummary snapshot() {
            return new NpcReputationSummary(count, total);
        }
    }
}
