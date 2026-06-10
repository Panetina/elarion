package panetina.elarion.core.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class RealmDecision {
    private final UUID id;
    private final RealmDecisionType type;
    private final String declaringRealmId;
    private final String receivingRealmId;
    private final UUID leaderId;
    private final long createdAt;
    private final long expiresAt;
    private RealmDecisionStatus status;
    private final Map<UUID, Boolean> votes;

    public RealmDecision(
            UUID id,
            RealmDecisionType type,
            String declaringRealmId,
            String receivingRealmId,
            UUID leaderId,
            long createdAt,
            long expiresAt,
            RealmDecisionStatus status,
            Map<UUID, Boolean> votes
    ) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.type = type;
        this.declaringRealmId = clean(declaringRealmId);
        this.receivingRealmId = clean(receivingRealmId);
        this.leaderId = leaderId;
        this.createdAt = createdAt <= 0 ? Instant.now().toEpochMilli() : createdAt;
        this.expiresAt = expiresAt;
        this.status = status == null ? RealmDecisionStatus.PENDING : status;
        this.votes = votes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(votes);
    }

    public static RealmDecision create(
            RealmDecisionType type,
            String declaringRealmId,
            String receivingRealmId,
            UUID leaderId,
            long durationMillis
    ) {
        long now = Instant.now().toEpochMilli();
        RealmDecision decision = new RealmDecision(null, type, declaringRealmId, receivingRealmId,
                leaderId, now, now + durationMillis, RealmDecisionStatus.PENDING, Map.of());
        if (leaderId != null) decision.votes().put(leaderId, true);
        return decision;
    }

    public UUID id() { return id; }
    public RealmDecisionType type() { return type; }
    public String declaringRealmId() { return declaringRealmId; }
    public String receivingRealmId() { return receivingRealmId; }
    public UUID leaderId() { return leaderId; }
    public long createdAt() { return createdAt; }
    public long expiresAt() { return expiresAt; }
    public RealmDecisionStatus status() { return status; }
    public Map<UUID, Boolean> votes() { return votes; }

    public void setStatus(RealmDecisionStatus status) {
        this.status = status == null ? RealmDecisionStatus.PENDING : status;
    }

    public boolean isPending() {
        return status == RealmDecisionStatus.PENDING;
    }

    public boolean isExpired(long now) {
        return isPending() && expiresAt > 0 && now >= expiresAt;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
