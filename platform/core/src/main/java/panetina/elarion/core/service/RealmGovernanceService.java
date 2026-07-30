package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.model.RealmDecision;
import panetina.elarion.core.model.RealmDecisionStatus;
import panetina.elarion.core.model.RealmDecisionType;
import panetina.elarion.core.model.RealmRelationship;
import panetina.elarion.core.storage.RealmRuntimeStorage;
import panetina.elarion.core.storage.RealmRuntimeStorage.RealmRuntimeState;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class RealmGovernanceService {
    public static final String DIPLOMACY_EXCLUDED_FLAG = "diplomacy-excluded";
    private static final long DEFAULT_VOTE_DURATION_MILLIS = Duration.ofDays(3).toMillis();
    private final RealmRuntimeStorage storage;
    private final RealmService realms;
    private final CitizenService citizens;
    private final HistoryService history;
    private MinecraftServer server;
    private RealmRuntimeState state = new RealmRuntimeState();
    private ElarionNotificationService notifications;

    public RealmGovernanceService(
            RealmRuntimeStorage storage,
            RealmService realms,
            CitizenService citizens,
            HistoryService history
    ) {
        this.storage = storage;
        this.realms = realms;
        this.citizens = citizens;
        this.history = history;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        this.state = storage.load(server);
        expirePendingDecisions();
    }

    public void setNotifications(ElarionNotificationService notifications) {
        this.notifications = notifications;
    }

    public Optional<UUID> leader(String realmId) {
        String normalized = normalize(realmId);
        return citizens.all().stream()
                .filter(citizen -> normalized.equals(citizen.leaderRealmId()))
                .map(CitizenRecord::uuid)
                .findFirst();
    }

    public boolean isDiplomacyEligible(String realmId) {
        return realms.find(normalize(realmId))
                .filter(realm -> !realm.hasFlag(DIPLOMACY_EXCLUDED_FLAG))
                .isPresent();
    }

    public boolean setLeader(String realmId, UUID leaderId, UUID actorId) {
        String normalizedRealm = normalize(realmId);
        if (realms.find(normalizedRealm).isEmpty()) return false;
        CitizenRecord leader = citizens.find(leaderId).orElse(null);
        if (leader == null || !normalizedRealm.equals(leader.realmId())) return false;
        for (CitizenRecord citizen : citizens.all()) {
            if (normalizedRealm.equals(citizen.leaderRealmId()) || leaderId.equals(citizen.uuid())) {
                citizen.setLeaderRealmId(leaderId.equals(citizen.uuid()) ? normalizedRealm : "");
                citizens.save(citizen, "realm-leader-set");
            }
        }
        history.record("realm", "leader-set", actorId, "player",
                leaderId.toString(), normalizedRealm, Map.of("realm", normalizedRealm));
        return true;
    }

    public RealmRelationship relationship(String firstRealm, String secondRealm) {
        String first = normalize(firstRealm);
        String second = normalize(secondRealm);
        if (first.isBlank() || second.isBlank()) return RealmRelationship.NEUTRAL;
        if (first.equals(second)) {
            return RealmRelationship.ALLY;
        }
        return state.relationships().getOrDefault(pair(first, second), RealmRelationship.NEUTRAL);
    }

    public boolean isHidden(String realmId) {
        return state.hiddenRealms().contains(normalize(realmId));
    }

    public boolean setHidden(String realmId, boolean hidden, UUID actorId, String reason) {
        String normalizedRealm = normalize(realmId);
        if (!isDiplomacyEligible(normalizedRealm)) return false;
        boolean changed = hidden
                ? state.hiddenRealms().add(normalizedRealm)
                : state.hiddenRealms().remove(normalizedRealm);
        if (changed) {
            save();
            history.record("realm", hidden ? "hidden-enabled" : "hidden-disabled", actorId,
                    "realm", normalizedRealm, normalizedRealm, Map.of(
                            "hidden", Boolean.toString(hidden),
                            "reason", reason == null ? "manual" : reason
                    ));
        }
        return true;
    }

    public boolean setRelationship(
            String firstRealm,
            String secondRealm,
            RealmRelationship relationship,
            UUID actorId,
            String reason
    ) {
        String first = normalize(firstRealm);
        String second = normalize(secondRealm);
        if (!isDiplomacyEligible(first) || !isDiplomacyEligible(second)) return false;
        if (relationship == RealmRelationship.HIDDEN) return false;
        if (first.equals(second) && relationship != RealmRelationship.ALLY) {
            return false;
        }
        RealmRelationship normalizedRelationship = relationship == null ? RealmRelationship.NEUTRAL : relationship;
        state.relationships().put(pair(first, second), normalizedRelationship);
        save();
        history.record("realm", "relationship-set", actorId, "realm", second, first,
                Map.of(
                        "relationship", normalizedRelationship.name(),
                        "otherRealm", second,
                        "reason", reason == null ? "manual" : reason
                ));
        return true;
    }

    public RealmDecision propose(
            RealmDecisionType type,
            String declaringRealmId,
            String receivingRealmId,
            UUID leaderId
    ) {
        String declaring = normalize(declaringRealmId);
        String receiving = normalize(receivingRealmId);
        if (!isDiplomacyEligible(declaring) || !isDiplomacyEligible(receiving)) {
            throw new IllegalArgumentException("Realm decisions require diplomacy-eligible Realms");
        }
        if (type == RealmDecisionType.GO_HIDDEN && !declaring.equals(receiving)) {
            throw new IllegalArgumentException("Hiding is a self-Realm decision");
        }
        RealmDecision decision = RealmDecision.create(type, declaring,
                receiving, leaderId, DEFAULT_VOTE_DURATION_MILLIS);
        state.decisions().put(decision.id(), decision);
        save();
        history.record("realm-decision", "declared", leaderId, "decision",
                decision.id().toString(), decision.declaringRealmId(), decisionMetadata(decision));
        publishDecisionOpened(decision);
        return decision;
    }

    public boolean vote(UUID decisionId, UUID citizenId, boolean approve) {
        RealmDecision decision = state.decisions().get(decisionId);
        if (decision == null || !decision.isPending() || !citizenAffected(decision, citizenId)) return false;
        decision.votes().put(citizenId, approve);
        evaluateDecision(decision);
        save();
        history.record("realm-decision", approve ? "vote-approve" : "vote-reject",
                citizenId, "decision", decision.id().toString(), decision.declaringRealmId(),
                Map.of("status", decision.status().name()));
        return true;
    }

    public List<RealmDecision> pendingFor(CitizenRecord citizen) {
        expirePendingDecisions();
        String realmId = citizen.realmId();
        if (realmId.isBlank()) return List.of();
        return state.decisions().values().stream()
                .filter(RealmDecision::isPending)
                .filter(decision -> realmId.equals(decision.declaringRealmId())
                        || realmId.equals(decision.receivingRealmId()))
                .sorted(Comparator.comparingLong(RealmDecision::createdAt))
                .toList();
    }

    public List<RealmDecision> pending() {
        expirePendingDecisions();
        return state.decisions().values().stream()
                .filter(RealmDecision::isPending)
                .sorted(Comparator.comparingLong(RealmDecision::createdAt))
                .toList();
    }

    public void expirePendingDecisions() {
        long now = Instant.now().toEpochMilli();
        boolean changed = false;
        for (RealmDecision decision : state.decisions().values()) {
            if (decision.isExpired(now)) {
                decision.setStatus(RealmDecisionStatus.EXPIRED);
                changed = true;
                if (server != null) {
                    history.record("realm-decision", "expired", null, "decision",
                            decision.id().toString(), decision.declaringRealmId(), decisionMetadata(decision));
                }
                resolveDecisionNotifications(decision);
                publishDecisionResult(decision, "Realm Decision Expired",
                        "The " + decision.type().name().toLowerCase(Locale.ROOT).replace('_', ' ')
                                + " decision expired without passing.");
            }
        }
        if (changed) save();
    }

    private void evaluateDecision(RealmDecision decision) {
        if (!decision.isPending()) return;
        int totalCitizens = affectedCitizenCount(decision);
        if (totalCitizens <= 0) return;
        long approvals = decision.votes().entrySet().stream()
                .filter(Map.Entry::getValue)
                .filter(entry -> citizenAffected(decision, entry.getKey()))
                .count();
        boolean leaderApproved = decision.leaderId() != null
                && Boolean.TRUE.equals(decision.votes().get(decision.leaderId()));
        int required = (int) Math.floor(totalCitizens * 0.51d) + 1;
        if (leaderApproved && approvals >= required) {
            decision.setStatus(RealmDecisionStatus.SUCCEEDED);
            applyDecision(decision);
            history.record("realm-decision", "succeeded", decision.leaderId(), "decision",
                    decision.id().toString(), decision.declaringRealmId(), decisionMetadata(decision));
            resolveDecisionNotifications(decision);
            publishDecisionResult(decision, "Realm Decision Passed",
                    "The " + decision.type().name().toLowerCase(Locale.ROOT).replace('_', ' ')
                            + " decision succeeded.");
        }
    }

    private void applyDecision(RealmDecision decision) {
        if (decision.type() == RealmDecisionType.GO_HIDDEN) {
            setHidden(decision.declaringRealmId(), true, decision.leaderId(), "decision");
            return;
        }
        RealmRelationship relationship = switch (decision.type()) {
            case DECLARE_WAR -> RealmRelationship.HOSTILE;
            case END_WAR, RETURN_NEUTRAL -> RealmRelationship.NEUTRAL;
            case PROPOSE_ALLIANCE -> RealmRelationship.ALLY;
            case DECLARE_EMBARGO -> RealmRelationship.EMBARGOED;
            case GO_HIDDEN -> throw new IllegalStateException("GO_HIDDEN is handled before relationship application");
        };
        String target = decision.receivingRealmId().isBlank()
                ? decision.declaringRealmId()
                : decision.receivingRealmId();
        setRelationship(decision.declaringRealmId(), target, relationship, decision.leaderId(), "decision");
    }

    private int affectedCitizenCount(RealmDecision decision) {
        return citizens.citizenIdsInRealms(List.of(
                decision.declaringRealmId(), decision.receivingRealmId())).size();
    }

    private boolean citizenAffected(RealmDecision decision, UUID citizenId) {
        return citizens.find(citizenId)
                .map(citizen -> citizen.realmId().equals(decision.declaringRealmId())
                        || citizen.realmId().equals(decision.receivingRealmId()))
                .orElse(false);
    }

    private Map<String, String> decisionMetadata(RealmDecision decision) {
        return Map.of(
                "type", decision.type().name(),
                "declaringRealm", decision.declaringRealmId(),
                "receivingRealm", decision.receivingRealmId(),
                "status", decision.status().name()
        );
    }

    private void save() {
        if (server != null) storage.save(server, state);
    }

    private void publishDecisionOpened(RealmDecision decision) {
        if (notifications == null) return;
        String title = "Realm Decision: " + decision.type().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String body = decision.receivingRealmId().isBlank()
                ? "Your Realm opened a civic decision."
                : "A decision involving " + decision.declaringRealmId() + " and "
                        + decision.receivingRealmId() + " is open.";
        List<ElarionNotificationAction> actions = List.of(
                new ElarionNotificationAction("elarion_core:realm_decision_approve", "Approve", true),
                new ElarionNotificationAction("elarion_core:realm_decision_reject", "Reject", true));
        Map<String, String> metadata = Map.of("decisionId", decision.id().toString());
        notifications.publishRealm(decision.declaringRealmId(), ElarionNotificationCategory.REALM,
                "elarion_core", "realm-decision-open", "decision:" + decision.id() + ":declaring",
                title, body, "Decision open", "item:minecraft:writable_book", actions, metadata,
                decision.expiresAt());
        if (!decision.receivingRealmId().isBlank()
                && !decision.receivingRealmId().equals(decision.declaringRealmId())) {
            notifications.publishRealm(decision.receivingRealmId(), ElarionNotificationCategory.REALM,
                    "elarion_core", "realm-decision-open", "decision:" + decision.id() + ":receiving",
                    title, body, "Decision open", "item:minecraft:writable_book", actions, metadata,
                    decision.expiresAt());
        }
    }

    private void publishDecisionResult(RealmDecision decision, String title, String body) {
        if (notifications == null) return;
        Map<String, String> metadata = Map.of("decisionId", decision.id().toString());
        List<ElarionNotificationAction> actions = List.of(
                new ElarionNotificationAction(ElarionNotificationService.DISMISS, "Dismiss", true));
        notifications.publishRealm(decision.declaringRealmId(), ElarionNotificationCategory.REALM,
                "elarion_core", "realm-decision-result", "decision-result:" + decision.id() + ":declaring",
                title, body, decision.status().name(), "item:minecraft:paper", actions, metadata,
                notifications.defaultExpiry());
        if (!decision.receivingRealmId().isBlank()
                && !decision.receivingRealmId().equals(decision.declaringRealmId())) {
            notifications.publishRealm(decision.receivingRealmId(), ElarionNotificationCategory.REALM,
                    "elarion_core", "realm-decision-result", "decision-result:" + decision.id() + ":receiving",
                    title, body, decision.status().name(), "item:minecraft:paper", actions, metadata,
                    notifications.defaultExpiry());
        }
    }

    private void resolveDecisionNotifications(RealmDecision decision) {
        if (notifications != null) {
            notifications.resolveByMetadata("elarion_core", "decisionId", decision.id().toString());
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String pair(String first, String second) {
        return first.compareTo(second) <= 0 ? first + "|" + second : second + "|" + first;
    }
}
