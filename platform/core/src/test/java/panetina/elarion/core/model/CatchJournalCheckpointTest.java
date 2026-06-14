package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CatchJournalCheckpointTest {
    @Test
    void validatesMonthAndLinePosition() {
        assertTrue(CatchJournalCheckpoint.START.isStart());
        assertThrows(IllegalArgumentException.class, () -> new CatchJournalCheckpoint("2026-6", 0));
        assertThrows(IllegalArgumentException.class, () -> new CatchJournalCheckpoint("2026-06", -1));
        assertThrows(IllegalArgumentException.class, () -> new CatchJournalCheckpoint("", 1));
    }
}
