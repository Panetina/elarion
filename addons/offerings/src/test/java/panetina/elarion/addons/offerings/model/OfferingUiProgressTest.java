package panetina.elarion.addons.offerings.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class OfferingUiProgressTest {
    @Test
    void capsEachRequirementWhenCalculatingAggregateProgress() {
        OfferingUiProgress progress = OfferingUiProgress.from(List.of(
                new OfferingProgress.Row("item:stone", 80, 64, true),
                new OfferingProgress.Row("currency", 10, 25, false)));

        assertEquals(74, progress.current());
        assertEquals(89, progress.required());
    }
}
