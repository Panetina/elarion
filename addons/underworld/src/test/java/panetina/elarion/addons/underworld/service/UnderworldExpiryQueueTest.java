package panetina.elarion.addons.underworld.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UnderworldExpiryQueueTest {
    @Test
    void returnsOnlyDueEntriesWithinTheRequestedBound() {
        UnderworldExpiryQueue queue = new UnderworldExpiryQueue();
        queue.schedule("first", 100L);
        queue.schedule("second", 200L);
        queue.schedule("third", 300L);

        assertEquals(List.of("first"), queue.pollDue(250L, 1));
        assertEquals(List.of("second"), queue.pollDue(250L, 8));
        assertEquals(1, queue.scheduledCount());
    }

    @Test
    void rescheduleAndCancelInvalidateOlderQueueEntries() {
        UnderworldExpiryQueue queue = new UnderworldExpiryQueue();
        queue.schedule("corpse", 100L);
        queue.schedule("corpse", 500L);

        assertEquals(List.of(), queue.pollDue(100L, 8));
        assertEquals(List.of("corpse"), queue.pollDue(500L, 8));

        queue.schedule("cancelled", 100L);
        queue.cancel("cancelled");
        assertEquals(List.of(), queue.pollDue(1_000L, 8));
        assertEquals(0, queue.scheduledCount());
    }
}
