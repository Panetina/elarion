package panetina.elarion.addons.portals.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalScheduleDefinitionTest {
    private final PortalScheduleDefinition schedule = new PortalScheduleDefinition(
            ZoneId.of("UTC"), Instant.parse("2026-01-01T00:00:00Z"),
            Duration.ofDays(7), Duration.ofHours(4), List.of(Duration.ofHours(1)));

    @Test
    void reportsActiveWindow() {
        var window = schedule.windowAt(Instant.parse("2026-01-01T02:00:00Z"));
        assertTrue(window.active());
        assertEquals(Instant.parse("2026-01-01T04:00:00Z"), window.end());
    }

    @Test
    void skipsToNextWindowAfterClosure() {
        var window = schedule.windowAt(Instant.parse("2026-01-02T00:00:00Z"));
        assertFalse(window.active());
        assertEquals(Instant.parse("2026-01-08T00:00:00Z"), window.start());
    }

    @Test
    void doesNotCreateRecurringWindowsBeforeAnchor() {
        var window = schedule.windowAt(Instant.parse("2025-12-01T00:00:00Z"));
        assertFalse(window.active());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), window.start());
    }

    @Test
    void continuousScheduleNeverCloses() {
        var window = PortalScheduleDefinition.alwaysOpenSchedule()
                .windowAt(Instant.parse("2026-06-13T00:00:00Z"));

        assertTrue(window.active());
        assertEquals(Instant.EPOCH, window.start());
        assertEquals(Instant.ofEpochMilli(Long.MAX_VALUE), window.end());
    }
}
