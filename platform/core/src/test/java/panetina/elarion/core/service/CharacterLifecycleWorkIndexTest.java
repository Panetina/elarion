package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CharacterLifecycleRecord;
import panetina.elarion.core.model.CharacterLifecycleStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CharacterLifecycleWorkIndexTest {
    @Test
    void projectsOnlyRecordsThatRequirePeriodicWork() {
        CharacterLifecycleWorkIndex index = new CharacterLifecycleWorkIndex();
        List<CharacterLifecycleRecord> records = new ArrayList<>();
        for (int entry = 0; entry < 1_000; entry++) {
            records.add(record("active-" + entry, CharacterLifecycleStatus.ACTIVE, 0L));
        }
        CharacterLifecycleRecord resetting = record("resetting", CharacterLifecycleStatus.RESETTING, 0L);
        CharacterLifecycleRecord due = record("due", CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN, 1_000L);
        CharacterLifecycleRecord future = record("future", CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN, 10_000L);
        records.add(resetting);
        records.add(due);
        records.add(future);
        index.rebuild(records);

        assertEquals(List.of(resetting), index.pendingResets());
        assertEquals(List.of(due), index.pollDueCooldowns(2_000L));
        assertEquals(1, index.pendingResetCount());
        assertEquals(1, index.scheduledCooldownCount());
    }

    @Test
    void updatesRemoveStaleWorkAndPreserveEqualDeadlineOrder() {
        CharacterLifecycleWorkIndex index = new CharacterLifecycleWorkIndex();
        CharacterLifecycleRecord first = record("first", CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN, 1_000L);
        CharacterLifecycleRecord second = record("second", CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN, 1_000L);
        CharacterLifecycleRecord cancelled = record("cancelled", CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN, 50L);
        CharacterLifecycleRecord rescheduled = record(
                "rescheduled", CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN, 500L);
        index.rebuild(List.of(first, second, cancelled, rescheduled));

        cancelled.status = CharacterLifecycleStatus.ACTIVE;
        index.update(cancelled);
        rescheduled.eligibleAt = 2_000L;
        index.update(rescheduled);

        assertEquals(List.of(first, second), index.pollDueCooldowns(1_000L));
        assertEquals(1, index.scheduledCooldownCount());
        assertEquals(List.of(rescheduled), index.pollDueCooldowns(2_000L));
        assertEquals(0, index.scheduledCooldownCount());
        assertTrue(index.pollDueCooldowns(10_000L).isEmpty());
    }

    private static CharacterLifecycleRecord record(
            String seed, CharacterLifecycleStatus status, long eligibleAt
    ) {
        CharacterLifecycleRecord record = CharacterLifecycleRecord.migration(
                UUID.nameUUIDFromBytes(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        record.status = status;
        record.eligibleAt = eligibleAt;
        return record;
    }
}
