package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentOfficeTermRecord;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentOfficeTermIndexTest {
    @Test
    void holderQueryIsBoundedAndNewestFirst() {
        UUID holder = UUID.randomUUID();
        List<GovernmentOfficeTermRecord> terms = java.util.stream.LongStream.rangeClosed(1, 40)
                .mapToObj(time -> GovernmentOfficeTermRecord.active("realm1", "office", holder, time))
                .toList();

        List<GovernmentOfficeTermRecord> result = GovernmentOfficeTermIndex.query(
                Map.of(holder, terms), holder, 100);

        assertEquals(32, result.size());
        assertEquals(40L, result.getFirst().chosenAt());
        assertEquals(9L, result.getLast().chosenAt());
    }

    @Test
    void nullHolderIsEmptyAndNonPositiveLimitStillReturnsOneNewestTerm() {
        UUID holder = UUID.randomUUID();
        List<GovernmentOfficeTermRecord> terms = List.of(
                GovernmentOfficeTermRecord.active("realm1", "office", holder, 1L),
                GovernmentOfficeTermRecord.active("realm1", "office", holder, 2L));
        Map<UUID, List<GovernmentOfficeTermRecord>> index = Map.of(holder, terms);

        assertTrue(GovernmentOfficeTermIndex.query(index, null, 10).isEmpty());
        List<GovernmentOfficeTermRecord> result = GovernmentOfficeTermIndex.query(index, holder, 0);
        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().chosenAt());
    }
}
