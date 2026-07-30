package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.model.GovernmentVoteState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/** Runtime-only deadline projection for unresolved, started Government votes. */
final class GovernmentVoteDeadlineIndex {
    private final NavigableMap<Long, LinkedHashSet<String>> keysByDeadline = new TreeMap<>();
    private final Map<String, ScheduledVote> scheduledByKey = new LinkedHashMap<>();
    private final Map<String, Long> orderByKey = new LinkedHashMap<>();
    private final Map<String, String> realmByKey = new LinkedHashMap<>();
    private long nextOrder;

    void rebuild(Map<String, GovernmentVoteState> votes) {
        clear();
        votes.forEach(this::update);
    }

    void update(String key, GovernmentVoteState vote) {
        unschedule(key);
        if (vote == null) {
            orderByKey.remove(key);
            realmByKey.remove(key);
            return;
        }
        long order = orderByKey.computeIfAbsent(key, ignored -> nextOrder++);
        realmByKey.put(key, vote.realmId);
        if (vote.resolved || vote.startedAt <= 0L) return;
        ScheduledVote scheduled = new ScheduledVote(key, vote.endsAt, order, vote);
        scheduledByKey.put(key, scheduled);
        keysByDeadline.computeIfAbsent(vote.endsAt, ignored -> new LinkedHashSet<>()).add(key);
    }

    List<DueVote> expired(long now) {
        List<ScheduledVote> due = new ArrayList<>();
        keysByDeadline.headMap(now, true).values().forEach(keys -> keys.forEach(key -> {
            ScheduledVote scheduled = scheduledByKey.get(key);
            if (scheduled != null && scheduled.deadline() <= now) due.add(scheduled);
        }));
        due.sort((left, right) -> Long.compare(left.order(), right.order()));
        return due.stream().map(scheduled -> new DueVote(scheduled.key(), scheduled.vote())).toList();
    }

    void remove(String key) {
        unschedule(key);
        orderByKey.remove(key);
        realmByKey.remove(key);
    }

    void removeRealm(String realmId) {
        String normalizedRealm = normalize(realmId);
        realmByKey.entrySet().stream()
                .filter(entry -> normalizedRealm.equals(normalize(entry.getValue())))
                .map(Map.Entry::getKey)
                .toList()
                .forEach(this::remove);
    }

    void clear() {
        keysByDeadline.clear();
        scheduledByKey.clear();
        orderByKey.clear();
        realmByKey.clear();
        nextOrder = 0L;
    }

    int scheduledCount() {
        return scheduledByKey.size();
    }

    private void unschedule(String key) {
        ScheduledVote previous = scheduledByKey.remove(key);
        if (previous == null) return;
        LinkedHashSet<String> keys = keysByDeadline.get(previous.deadline());
        if (keys == null) return;
        keys.remove(key);
        if (keys.isEmpty()) keysByDeadline.remove(previous.deadline());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    record DueVote(String key, GovernmentVoteState vote) {
    }

    private record ScheduledVote(String key, long deadline, long order, GovernmentVoteState vote) {
    }
}
