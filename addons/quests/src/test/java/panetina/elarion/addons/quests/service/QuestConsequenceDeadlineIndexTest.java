package panetina.elarion.addons.quests.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.quests.storage.QuestScheduledConsequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class QuestConsequenceDeadlineIndexTest {
    @Test
    void pollsOnlyTheBoundedDuePrefix() {
        QuestConsequenceDeadlineIndex index = new QuestConsequenceDeadlineIndex();
        List<QuestScheduledConsequence> consequences = new ArrayList<>();
        for (int entry = 0; entry < 1_000; entry++) {
            consequences.add(consequence("future-" + entry, 10_000L + entry));
        }
        for (int entry = 0; entry < 20; entry++) {
            consequences.add(consequence("due-" + entry, 100L + entry));
        }
        index.rebuild(consequences);

        List<QuestScheduledConsequence> due = index.pollDue(1_000L, 16);

        assertEquals(16, due.size());
        assertEquals("due-0", due.getFirst().id);
        assertEquals("due-15", due.getLast().id);
        assertEquals(1_004, index.size());
        assertTrue(index.lastPollInspections() <= 16);
    }

    @Test
    void rebuildPreservesPersistedOrderForEqualDeadlines() {
        QuestConsequenceDeadlineIndex index = new QuestConsequenceDeadlineIndex();
        index.rebuild(List.of(
                consequence("first", 100L),
                consequence("second", 100L),
                consequence("third", 100L)));

        assertEquals(List.of("first", "second", "third"), index.pollDue(100L, 10).stream()
                .map(entry -> entry.id)
                .toList());
        assertTrue(index.lastPollInspections() <= 4);
    }

    private static QuestScheduledConsequence consequence(String id, long dueAt) {
        return new QuestScheduledConsequence(id, "quest", "global", null, dueAt, "action", Map.of());
    }
}
