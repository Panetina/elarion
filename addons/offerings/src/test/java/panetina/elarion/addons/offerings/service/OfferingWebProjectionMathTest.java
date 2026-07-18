package panetina.elarion.addons.offerings.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.offerings.model.OfferingProgress;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class OfferingWebProjectionMathTest {
    @Test
    void capsOverfundedRowsForPublicProgress() {
        OfferingProgress progress = new OfferingProgress("shrine", false, List.of(
                new OfferingProgress.Row("stone", 15, 10, true),
                new OfferingProgress.Row("currency", 5, 10, false)));

        long current = progress.rows().stream().mapToLong(row -> Math.min(row.current(), row.required())).sum();
        long required = progress.rows().stream().mapToLong(OfferingProgress.Row::required).sum();

        assertEquals(15, current);
        assertEquals(20, required);
        assertEquals(75, current * 100 / required);
    }
}
