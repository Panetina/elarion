package panetina.elarion.addons.underworld.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

final class UnderworldExpiryQueue {
    private static final int MINIMUM_COMPACTION_OVERHEAD = 64;

    private final Map<String, Long> scheduledDueTimes = new HashMap<>();
    private final PriorityQueue<ExpiryEntry> entries =
            new PriorityQueue<>(Comparator.comparingLong(ExpiryEntry::dueAtMillis));

    void schedule(String corpseId, long dueAtMillis) {
        if (corpseId == null || corpseId.isBlank()) return;
        scheduledDueTimes.put(corpseId, dueAtMillis);
        entries.add(new ExpiryEntry(corpseId, dueAtMillis));
        if (entries.size() > scheduledDueTimes.size() * 2 + MINIMUM_COMPACTION_OVERHEAD) compact();
    }

    void cancel(String corpseId) {
        if (corpseId != null) scheduledDueTimes.remove(corpseId);
    }

    List<String> pollDue(long nowMillis, int maximumResults) {
        if (maximumResults <= 0) return List.of();
        List<String> due = new ArrayList<>(maximumResults);
        int maximumInspections = maximumResults * 4 + MINIMUM_COMPACTION_OVERHEAD;
        int inspected = 0;
        while (!entries.isEmpty() && due.size() < maximumResults && inspected++ < maximumInspections) {
            ExpiryEntry entry = entries.peek();
            if (entry.dueAtMillis() > nowMillis) break;
            entries.poll();
            Long currentDueAt = scheduledDueTimes.get(entry.corpseId());
            if (currentDueAt == null || currentDueAt.longValue() != entry.dueAtMillis()) continue;
            scheduledDueTimes.remove(entry.corpseId());
            due.add(entry.corpseId());
        }
        return List.copyOf(due);
    }

    void clear() {
        scheduledDueTimes.clear();
        entries.clear();
    }

    int scheduledCount() {
        return scheduledDueTimes.size();
    }

    private void compact() {
        entries.clear();
        scheduledDueTimes.forEach((corpseId, dueAt) -> entries.add(new ExpiryEntry(corpseId, dueAt)));
    }

    private record ExpiryEntry(String corpseId, long dueAtMillis) {
    }
}
