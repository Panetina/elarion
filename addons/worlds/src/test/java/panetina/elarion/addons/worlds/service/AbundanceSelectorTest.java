package panetina.elarion.addons.worlds.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AbundanceSelectorTest {
    @Test
    void boundaryChancesAreExact() {
        assertFalse(AbundanceSelector.keep(1, 2, 3, 0));
        assertTrue(AbundanceSelector.keep(1, 2, 3, 1));
    }

    @Test
    void selectionIsDeterministic() {
        boolean first = AbundanceSelector.keep(11001, 987654321L, 42, 0.25);
        assertEquals(first, AbundanceSelector.keep(11001, 987654321L, 42, 0.25));
    }

    @Test
    void approximateDistributionMatchesConfiguredChance() {
        int kept = 0;
        for (int i = 0; i < 10_000; i++) {
            if (AbundanceSelector.keep(11001, i, 42, 0.25)) kept++;
        }
        assertTrue(kept > 2300 && kept < 2700, "Expected about 25%, got " + kept);
    }
}
