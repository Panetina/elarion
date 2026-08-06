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
}
