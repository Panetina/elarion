package panetina.elarion.addons.quests.service;

import panetina.elarion.addons.quests.storage.QuestScheduledConsequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

final class QuestConsequenceDeadlineIndex {
    private final PriorityQueue<IndexedConsequence> deadlines = new PriorityQueue<>(
            Comparator.comparingLong((IndexedConsequence entry) -> entry.consequence().dueAt)
                    .thenComparingLong(IndexedConsequence::order));
    private long nextOrder;
    private int lastPollInspections;

    void rebuild(Collection<QuestScheduledConsequence> consequences) {
        clear();
        if (consequences != null) consequences.forEach(this::add);
    }

    void add(QuestScheduledConsequence consequence) {
        if (consequence != null) deadlines.add(new IndexedConsequence(consequence, nextOrder++));
    }

    List<QuestScheduledConsequence> pollDue(long now, int limit) {
        int safeLimit = Math.max(0, limit);
        List<QuestScheduledConsequence> due = new ArrayList<>(safeLimit);
        int inspections = 0;
        while (due.size() < safeLimit) {
            inspections++;
            IndexedConsequence next = deadlines.peek();
            if (next == null || next.consequence().dueAt > now) break;
            due.add(deadlines.remove().consequence());
        }
        lastPollInspections = inspections;
        return List.copyOf(due);
    }

    void clear() {
        deadlines.clear();
        nextOrder = 0L;
        lastPollInspections = 0;
    }

    int size() {
        return deadlines.size();
    }

    int lastPollInspections() {
        return lastPollInspections;
    }

    private record IndexedConsequence(QuestScheduledConsequence consequence, long order) {
    }
}
