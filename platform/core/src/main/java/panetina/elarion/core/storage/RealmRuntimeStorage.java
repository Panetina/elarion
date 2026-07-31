package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.model.RealmDecision;
import panetina.elarion.core.model.RealmDecisionStatus;
import panetina.elarion.core.model.RealmDecisionType;
import panetina.elarion.core.model.RealmRelationship;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RealmRuntimeStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type STRING_BOOLEAN_MAP = new TypeToken<Map<String, Boolean>>() {}.getType();
    private final Logger logger;

    public RealmRuntimeStorage(Logger logger) {
        this.logger = logger;
    }

    public RealmRuntimeState load(MinecraftServer server) {
        return load(file(server));
    }

    RealmRuntimeState load(Path file) {
        if (Files.notExists(file)) return new RealmRuntimeState();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StoredState stored = GSON.fromJson(reader, StoredState.class);
            return stored == null ? new RealmRuntimeState() : stored.toState();
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load realm runtime state", exception);
            return new RealmRuntimeState();
        }
    }

    public void save(MinecraftServer server, RealmRuntimeState state) {
        save(file(server), state);
    }

    void save(Path file, RealmRuntimeState state) {
        JsonStateStorage.writeAtomic(file, GSON, StoredState.from(state), logger, "realm runtime state");
    }

    private static Path file(MinecraftServer server) {
        return JsonStateStorage.addonStateRoot(server, "realms").resolve("realm-state.json");
    }

    public static final class RealmRuntimeState {
        private final Map<String, RealmRelationship> relationships = new LinkedHashMap<>();
        private final Set<String> hiddenRealms = new LinkedHashSet<>();
        private final Map<UUID, RealmDecision> decisions = new LinkedHashMap<>();

        public Map<String, RealmRelationship> relationships() { return relationships; }
        public Set<String> hiddenRealms() { return hiddenRealms; }
        public Map<UUID, RealmDecision> decisions() { return decisions; }
    }

    private static final class StoredState {
        Map<String, String> relationships = new LinkedHashMap<>();
        Set<String> hiddenRealms = new LinkedHashSet<>();
        List<StoredDecision> decisions = List.of();

        static StoredState from(RealmRuntimeState state) {
            StoredState stored = new StoredState();
            state.relationships().forEach((pair, relationship) -> stored.relationships.put(pair, relationship.name()));
            stored.hiddenRealms = new LinkedHashSet<>(state.hiddenRealms());
            stored.decisions = state.decisions().values().stream()
                    .map(StoredDecision::from)
                    .toList();
            return stored;
        }

        RealmRuntimeState toState() {
            RealmRuntimeState state = new RealmRuntimeState();
            if (relationships != null) {
                relationships.forEach((pair, relationship) -> {
                    if (pair == null || pair.isBlank() || relationship == null || relationship.isBlank()) return;
                    try {
                        RealmRelationship parsed = RealmRelationship.valueOf(relationship);
                        if (parsed != RealmRelationship.HIDDEN) {
                            state.relationships().put(pair, parsed);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                });
            }
            if (hiddenRealms != null) {
                hiddenRealms.stream()
                        .map(RealmRuntimeStorage::normalize)
                        .filter(realmId -> !realmId.isBlank())
                        .forEach(state.hiddenRealms()::add);
            }
            if (decisions != null) {
                for (StoredDecision stored : decisions) {
                    if (stored == null) continue;
                    RealmDecision decision = stored.toDecision();
                    if (decision != null) state.decisions().put(decision.id(), decision);
                }
            }
            return state;
        }
    }

    private static final class StoredDecision {
        String id;
        String type;
        String declaringRealmId;
        String receivingRealmId;
        String leaderId;
        long createdAt;
        long expiresAt;
        String status;
        Map<String, Boolean> votes = new LinkedHashMap<>();

        static StoredDecision from(RealmDecision decision) {
            StoredDecision stored = new StoredDecision();
            stored.id = decision.id().toString();
            stored.type = decision.type().name();
            stored.declaringRealmId = decision.declaringRealmId();
            stored.receivingRealmId = decision.receivingRealmId();
            stored.leaderId = decision.leaderId() == null ? "" : decision.leaderId().toString();
            stored.createdAt = decision.createdAt();
            stored.expiresAt = decision.expiresAt();
            stored.status = decision.status().name();
            decision.votes().forEach((uuid, vote) -> stored.votes.put(uuid.toString(), vote));
            return stored;
        }

        RealmDecision toDecision() {
            try {
                Map<String, Boolean> rawVotes = votes == null
                        ? Map.of()
                        : GSON.fromJson(GSON.toJson(votes), STRING_BOOLEAN_MAP);
                Map<UUID, Boolean> parsedVotes = new LinkedHashMap<>();
                rawVotes.forEach((uuid, vote) -> {
                    if (vote == null) return;
                    try {
                        parsedVotes.put(UUID.fromString(uuid), Boolean.TRUE.equals(vote));
                    } catch (IllegalArgumentException | NullPointerException ignored) {
                    }
                });
                RealmDecisionStatus parsedStatus = status == null || status.isBlank()
                        ? RealmDecisionStatus.PENDING
                        : RealmDecisionStatus.valueOf(status);
                return new RealmDecision(
                        UUID.fromString(id),
                        RealmDecisionType.valueOf(type),
                        declaringRealmId,
                        receivingRealmId,
                        leaderId == null || leaderId.isBlank() ? null : UUID.fromString(leaderId),
                        createdAt,
                        expiresAt,
                        parsedStatus,
                        parsedVotes
                );
            } catch (IllegalArgumentException | NullPointerException exception) {
                return null;
            }
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
