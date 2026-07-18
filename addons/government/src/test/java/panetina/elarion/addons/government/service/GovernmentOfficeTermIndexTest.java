package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentOfficeTermRecord;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GovernmentOfficeTermIndexTest {
    @Test
    void holderQueryIsBoundedAndNewestFirst() {
        UUID holder = UUID.randomUUID();
        List<GovernmentOfficeTermRecord> terms = java.util.stream.LongStream.rangeClosed(1, 40)
                .mapToObj(time -> GovernmentOfficeTermRecord.active("realm1", "office", holder, time))
                .toList();

        List<GovernmentOfficeTermRecord> result = GovernmentStateService.boundedOfficeTerms(
                Map.of(holder, terms), holder, 100);

        assertEquals(32, result.size());
        assertEquals(40L, result.getFirst().chosenAt());
        assertEquals(9L, result.getLast().chosenAt());
    }
}
