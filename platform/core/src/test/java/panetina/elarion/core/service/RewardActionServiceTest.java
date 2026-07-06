package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RewardActionServiceTest {
    @Test
    void rewardIdsPreserveUnderscoresUsedByCoreConfig() {
        assertEquals("council_hall_blessing",
                RewardActionService.normalizeRewardId(" Council Hall Blessing "));
        assertEquals("council_hall_blessing",
                RewardActionService.normalizeRewardId("council_hall_blessing"));
    }

    @Test
    void itemRewardActionCanNormalizeCustomName() {
        assertEquals(
                "Pale Brookling",
                RewardActionService.customItemName(Map.of("name", "  Pale Brookling  "))
                        .orElseThrow()
                        .getString());
    }

    @Test
    void blankCustomNameIsIgnored() {
        assertTrue(RewardActionService.customItemName(Map.of("name", "   ")).isEmpty());
    }

    @Test
    void longCustomNameIsBounded() {
        String longName = "x".repeat(RewardActionService.MAX_CUSTOM_ITEM_NAME_LENGTH + 25);

        assertEquals(
                RewardActionService.MAX_CUSTOM_ITEM_NAME_LENGTH,
                RewardActionService.customItemName(Map.of("name", longName))
                        .orElseThrow()
                        .getString()
                        .length());
    }
}
