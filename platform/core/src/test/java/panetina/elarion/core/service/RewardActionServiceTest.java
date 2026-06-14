package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RewardActionServiceTest {
    @Test
    void rewardIdsPreserveUnderscoresUsedByCoreConfig() {
        assertEquals("council_hall_blessing",
                RewardActionService.normalizeRewardId(" Council Hall Blessing "));
        assertEquals("council_hall_blessing",
                RewardActionService.normalizeRewardId("council_hall_blessing"));
    }
}
