package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HistoryServiceTest {
    @Test
    void publicHistoryWeeksUseTheConfiguredBoundForDefaultAndCallerValues() {
        assertEquals(8, HistoryService.boundedPublicHistoryWeeks(0, 8, 52));
        assertEquals(52, HistoryService.boundedPublicHistoryWeeks(999, 8, 52));
        assertEquals(1, HistoryService.boundedPublicHistoryWeeks(-1, -2, 0));
    }

    @Test
    void recentIndexReadsNeverExceedTheConfiguredMonthWindow() {
        assertEquals(3, HistoryService.boundedRecentIndexMonths(999, 3));
        assertEquals(2, HistoryService.boundedRecentIndexMonths(2, 3));
        assertEquals(1, HistoryService.boundedRecentIndexMonths(0, 0));
    }
}
