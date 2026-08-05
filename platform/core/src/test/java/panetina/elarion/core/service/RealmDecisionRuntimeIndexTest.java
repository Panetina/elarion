package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.RealmDecision;
import panetina.elarion.core.model.RealmDecisionStatus;
import panetina.elarion.core.model.RealmDecisionType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RealmDecisionRuntimeIndexTest {
    @Test
    void projectsOnlyPendingDecisionsAndIndexesTheirAffectedRealms() {
        RealmDecisionRuntimeIndex index = new RealmDecisionRuntimeIndex();
        List<RealmDecision> decisions = new ArrayList<>();
        for (int entry = 0; entry < 1_000; entry++) {
            decisions.add(decision("resolved-" + entry, "realm1", "realm2", entry,
                    10_000L + entry, RealmDecisionStatus.SUCCEEDED));
        }
        RealmDecision due = decision("due", "realm1", "realm2", 200L, 1_000L, RealmDecisionStatus.PENDING);
        RealmDecision future = decision("future", "realm2", "realm3", 100L, 10_000L,
                RealmDecisionStatus.PENDING);
        RealmDecision indefinite = decision("indefinite", "realm3", "", 300L, 0L,
                RealmDecisionStatus.PENDING);
        decisions.add(due);
        decisions.add(future);
        decisions.add(indefinite);
        index.rebuild(decisions);

        assertEquals(3, index.pendingCount());
        assertEquals(2, index.scheduledDeadlineCount());
        assertEquals(List.of(future, due), index.pendingFor("realm2"));
        assertEquals(List.of(due), index.expired(1_000L));
    }

    @Test
    void preservesCanonicalOrderForEqualDeadlineExpiryAndRemovesResolvedEntries() {
        RealmDecisionRuntimeIndex index = new RealmDecisionRuntimeIndex();
        RealmDecision first = decision("first", "realm1", "realm2", 300L, 1_000L,
                RealmDecisionStatus.PENDING);
        RealmDecision second = decision("second", "realm1", "realm2", 100L, 1_000L,
                RealmDecisionStatus.PENDING);
        index.rebuild(List.of(first, second));

        assertEquals(List.of(second, first), index.pending());
        assertEquals(List.of(first, second), index.expired(1_000L));

        second.votes().put(UUID.randomUUID(), true);
        index.update(second);
        assertEquals(List.of(second, first), index.pending());

        first.setStatus(RealmDecisionStatus.EXPIRED);
        index.update(first);

        assertEquals(List.of(second), index.pending());
        assertEquals(1, index.scheduledDeadlineCount());
        assertTrue(index.pendingFor("realm3").isEmpty());
    }

    private static RealmDecision decision(
            String seed,
            String declaringRealmId,
            String receivingRealmId,
            long createdAt,
            long expiresAt,
            RealmDecisionStatus status
    ) {
        return new RealmDecision(UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)),
                RealmDecisionType.PROPOSE_ALLIANCE, declaringRealmId, receivingRealmId,
                null, createdAt, expiresAt, status, Map.of());
    }
}
