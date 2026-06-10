package panetina.elarion.addons.worlds.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WorldRuleServiceTest {
    @Test
    void blockRuleSlicesUseSixteenBlockSections() {
        List<WorldRuleService.ChunkSlice> slices = WorldRuleService.slices(-64, 320);

        assertEquals(24, slices.size());
        assertEquals(new WorldRuleService.ChunkSlice(-64, -48), slices.getFirst());
        assertEquals(new WorldRuleService.ChunkSlice(304, 320), slices.getLast());
    }

    @Test
    void finalSliceCanBeShorterThanSixteenBlocks() {
        List<WorldRuleService.ChunkSlice> slices = WorldRuleService.slices(0, 34);

        assertEquals(3, slices.size());
        assertEquals(new WorldRuleService.ChunkSlice(32, 34), slices.getLast());
    }
}
