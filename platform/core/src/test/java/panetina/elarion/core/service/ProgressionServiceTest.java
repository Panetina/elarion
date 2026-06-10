package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ProgressionRegion;
import panetina.elarion.core.model.TitleUnlockRule;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProgressionServiceTest {
    @Test
    void continuousRulesAreGroupedBySampleInterval() {
        TitleUnlockRule fast = continuousRule("fast", 20);
        TitleUnlockRule slow = continuousRule("slow", 100);
        TitleUnlockRule secondFast = continuousRule("second_fast", 20);

        Map<Long, List<TitleUnlockRule>> indexed =
                ProgressionService.indexContinuousRules(List.of(fast, slow, secondFast));

        assertEquals(2, indexed.get(20L).size());
        assertEquals(1, indexed.get(100L).size());
    }

    @Test
    void regionsAreGroupedByWorld() {
        ProgressionRegion oak = new ProgressionRegion("oak_ruins", "elarion:oak", 0, 0, 0, 10, 10, 10);
        ProgressionRegion ash = new ProgressionRegion("ash_ruins", "elarion:ash", 0, 0, 0, 10, 10, 10);

        Map<String, List<ProgressionRegion>> indexed = ProgressionService.indexRegionsByWorld(List.of(oak, ash));

        assertEquals(List.of(oak), indexed.get("elarion:oak"));
        assertEquals(List.of(ash), indexed.get("elarion:ash"));
        assertFalse(indexed.containsKey("minecraft:overworld"));
    }

    @Test
    void nonContinuousRulesAreNotIndexedAsContinuous() {
        TitleUnlockRule eventRule = new TitleUnlockRule(
                "maze_runner",
                "maze_runner",
                "region-enter",
                "",
                1,
                1,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of("maze_end"),
                Map.of(),
                null
        );

        assertTrue(ProgressionService.indexContinuousRules(List.of(eventRule)).isEmpty());
    }

    private static TitleUnlockRule continuousRule(String id, long interval) {
        return new TitleUnlockRule(
                id,
                id,
                "continuous",
                "",
                1,
                1,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Map.of(),
                new TitleUnlockRule.Continuous(1, "ticks", interval, true, Set.of(), Set.of(), Set.of())
        );
    }
}
