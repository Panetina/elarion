package panetina.elarion.core.service;

import panetina.elarion.core.model.RealmDecision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

/** Runtime-only projection of pending Realm decisions. */
final class RealmDecisionRuntimeIndex {
    private final Map<UUID, IndexedDecision> pendingById = new LinkedHashMap<>();
    private final NavigableMap<Long, LinkedHashMap<UUID, IndexedDecision>> pendingByCreatedAt = new TreeMap<>();
    private final NavigableMap<Long, LinkedHashMap<UUID, IndexedDecision>> pendingByDeadline = new TreeMap<>();
    private final Map<String, NavigableMap<Long, LinkedHashMap<UUID, IndexedDecision>>> pendingByRealm =
            new LinkedHashMap<>();
    private long nextOrder;

    void rebuild(Collection<RealmDecision> decisions) {
        clear();
        if (decisions != null) decisions.forEach(this::update);
    }

    void update(RealmDecision decision) {
        if (decision == null || decision.id() == null) return;
        IndexedDecision previous = pendingById.remove(decision.id());
        long order = previous == null ? nextOrder : previous.order();
        if (previous != null) remove(previous);
        if (!decision.isPending()) return;

        IndexedDecision indexed = new IndexedDecision(decision, order);
        if (previous == null) nextOrder++;
        pendingById.put(decision.id(), indexed);
        add(pendingByCreatedAt, decision.createdAt(), indexed);
        if (decision.expiresAt() > 0L) add(pendingByDeadline, decision.expiresAt(), indexed);
        realms(decision).forEach(realmId -> pendingByRealm
                .computeIfAbsent(realmId, ignored -> new TreeMap<>())
                .computeIfAbsent(decision.createdAt(), ignored -> new LinkedHashMap<>())
                .put(decision.id(), indexed));
    }

    List<RealmDecision> pending() {
        return values(pendingByCreatedAt);
    }

    List<RealmDecision> pendingFor(String realmId) {
        NavigableMap<Long, LinkedHashMap<UUID, IndexedDecision>> values = pendingByRealm.get(realmId);
        return values == null ? List.of() : values(values);
    }

    List<RealmDecision> expired(long now) {
        List<IndexedDecision> due = new ArrayList<>();
        pendingByDeadline.headMap(now, true).values().forEach(bucket -> due.addAll(bucket.values()));
        due.sort(Comparator.comparingLong(IndexedDecision::order));
        return due.stream().map(IndexedDecision::decision).toList();
    }

    void clear() {
        pendingById.clear();
        pendingByCreatedAt.clear();
        pendingByDeadline.clear();
        pendingByRealm.clear();
        nextOrder = 0L;
    }

    int pendingCount() {
        return pendingById.size();
    }

    int scheduledDeadlineCount() {
        return pendingByDeadline.values().stream().mapToInt(Map::size).sum();
    }

    private void remove(IndexedDecision indexed) {
        RealmDecision decision = indexed.decision();
        remove(pendingByCreatedAt, decision.createdAt(), decision.id());
        if (decision.expiresAt() > 0L) remove(pendingByDeadline, decision.expiresAt(), decision.id());
        realms(decision).forEach(realmId -> {
            NavigableMap<Long, LinkedHashMap<UUID, IndexedDecision>> values = pendingByRealm.get(realmId);
            if (values == null) return;
            remove(values, decision.createdAt(), decision.id());
            if (values.isEmpty()) pendingByRealm.remove(realmId);
        });
    }

    private static void add(
            NavigableMap<Long, LinkedHashMap<UUID, IndexedDecision>> index,
            long key,
            IndexedDecision decision
    ) {
        index.computeIfAbsent(key, ignored -> new LinkedHashMap<>()).put(decision.decision().id(), decision);
    }

    private static void remove(
            NavigableMap<Long, LinkedHashMap<UUID, IndexedDecision>> index,
            long key,
            UUID decisionId
    ) {
        LinkedHashMap<UUID, IndexedDecision> bucket = index.get(key);
        if (bucket == null) return;
        bucket.remove(decisionId);
        if (bucket.isEmpty()) index.remove(key);
    }

    private static List<RealmDecision> values(
            NavigableMap<Long, LinkedHashMap<UUID, IndexedDecision>> index
    ) {
        return index.values().stream()
                .flatMap(bucket -> bucket.values().stream()
                        .sorted(Comparator.comparingLong(IndexedDecision::order)))
                .map(IndexedDecision::decision)
                .toList();
    }

    private static List<String> realms(RealmDecision decision) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (!decision.declaringRealmId().isBlank()) values.add(decision.declaringRealmId());
        if (!decision.receivingRealmId().isBlank()) values.add(decision.receivingRealmId());
        return List.copyOf(values);
    }

    private record IndexedDecision(RealmDecision decision, long order) {
    }
}
