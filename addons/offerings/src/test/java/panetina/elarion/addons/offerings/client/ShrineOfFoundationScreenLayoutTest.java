package panetina.elarion.addons.offerings.client;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.offerings.model.OfferingUiConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ShrineOfFoundationScreenLayoutTest {
    @Test
    void defaultLayoutKeepsHeaderContentAndFooterSeparated() {
        OfferingUiConfig config = OfferingUiConfig.defaults();
        int padding = 16;
        int gap = 8;

        ShrineOfFoundationScreen.LayoutMetrics layout = ShrineOfFoundationScreen.calculateLayout(
                config.logicalWidth(), config.logicalHeight(), padding, config.summaryWidth(), gap, config.tabHeight());

        assertEquals(padding + config.summaryWidth() + gap, layout.mainX());
        assertEquals(config.logicalWidth() - layout.mainX() - padding, layout.mainWidth());
        assertTrue(layout.progressY() < layout.tabsY());
        assertTrue(layout.tabsY() + config.tabHeight() <= layout.contentTop());
        assertTrue(layout.contentTop() < layout.contentBottom());
        assertEquals(layout.contentBottom() + gap, layout.closeY());
        assertEquals(config.logicalHeight() - padding,
                layout.closeY() + ShrineOfFoundationScreen.FOOTER_BUTTON_HEIGHT);
    }

    @Test
    void configuredMinimumDimensionsStillLeaveBoundedContent() {
        ShrineOfFoundationScreen.LayoutMetrics layout = ShrineOfFoundationScreen.calculateLayout(
                420, 300, 12, 128, 6, 18);

        assertTrue(layout.mainWidth() >= 250);
        assertTrue(layout.contentBottom() - layout.contentTop() >= 150);
        assertTrue(layout.closeY() > layout.contentBottom());
    }

    @Test
    void summaryRewardsReserveTwoRowsInsteadOfDroppingTheSecondRow() {
        int summaryContentWidth = 284;

        assertEquals(3, ShrineOfFoundationScreen.rewardGridColumns(summaryContentWidth));
        assertTrue(ShrineOfFoundationScreen.rewardPanelDesiredHeight(summaryContentWidth, 6) >= 100);
        assertTrue(ShrineOfFoundationScreen.rewardSlotSizeForRows(72, 2) >= 30);
    }

    @Test
    void crampedSummaryRewardsShrinkSlotsBeforeHidingRows() {
        assertTrue(ShrineOfFoundationScreen.rewardSlotSizeForRows(58, 2) >= 26);
        assertEquals(22, ShrineOfFoundationScreen.rewardSlotSizeForRows(40, 2));
    }
}
