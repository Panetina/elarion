package panetina.elarion.addons.government.client;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.addons.government.client.seat.SeatOfRuleScreen;
import panetina.elarion.core.client.ui.ElarionScaledLayout;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentUiScreenLayoutTest {
    @Test
    void civicForumPanelsAndTabsFitInsideLogicalBounds() {
        CivicForumScreen.Layout layout = CivicForumScreen.layoutMetrics();

        assertEquals(760, layout.logicalWidth());
        assertEquals(500, layout.logicalHeight());
        assertEquals(6, layout.tabCount());
        assertTrue(layout.leftX() + layout.leftWidth() < layout.rightX());
        assertTrue(layout.rightX() + layout.rightWidth() < layout.logicalWidth());
        assertTrue(layout.headerHeight() <= 50);
        assertEquals(layout.headerHeight() - 1, layout.bodyY());
        assertTrue(layout.bodyY() < layout.tabY());
        assertTrue(layout.tabY() + layout.tabHeight() < layout.contentTop());
        assertEquals(layout.rightX() + layout.rightWidth(), layout.tabRightEdge());
        assertEquals(layout.leftX(), layout.tabAreaX());
        assertEquals(layout.rightX() + layout.rightWidth() - layout.leftX(), layout.tabAreaWidth());
        assertTrue(layout.contentTop() < layout.mainBottom());
        assertTrue(layout.mainBottom() < layout.bottomY());
        assertEquals(layout.headerIdentityX() + layout.headerIdentityWidth(), layout.headerAuthorityX());
        assertEquals(layout.headerAuthorityX() + layout.headerAuthorityWidth(), layout.headerRoleX());
        assertEquals(layout.headerRoleX() + layout.headerRoleWidth(), layout.headerColorX());
        assertTrue(layout.headerColorX() + layout.headerColorWidth() < layout.closeX());
        assertEquals(layout.headerIdentityWidth(), layout.headerAuthorityWidth());
        assertEquals(layout.headerIdentityWidth(), layout.headerRoleWidth());
        assertTrue(layout.headerColorWidth() >= layout.headerIdentityWidth());
        assertTrue(layout.foundingDecisionBottom() < layout.currentRowsY());
        assertTrue(layout.currentRowMetricIconX() > layout.leftX() + layout.leftWidth() / 2);
        assertTrue(layout.currentRowMetricIconX() + 112 < layout.leftX() + layout.leftWidth());
        assertTrue(layout.currentRowHeight() >= 36);
        assertTrue(layout.moduleVisibleRows() >= 5);
        assertTrue(layout.currentVoteVisibleRows() >= 4);
        assertEquals(75, layout.readableMinimumScalePercent());
    }

    @Test
    void seatOfRulePanelsAndTabsFitInsideLogicalBounds() {
        SeatOfRuleScreen.Layout layout = SeatOfRuleScreen.layoutMetrics();

        assertEquals(760, layout.logicalWidth());
        assertEquals(500, layout.logicalHeight());
        assertEquals(5, layout.tabCount());
        assertTrue(layout.headerHeight() <= 50);
        assertEquals(layout.headerHeight() - 1, layout.bodyY());
        assertTrue(layout.bodyY() < layout.tabY());
        assertTrue(layout.tabY() + layout.tabHeight() < layout.contentTop());
        assertTrue(layout.leftX() + layout.leftWidth() < layout.rightX());
        assertTrue(layout.rightX() + layout.rightWidth() < layout.logicalWidth());
        assertTrue(layout.contentTop() < layout.mainBottom());
        assertTrue(layout.contentTop() < layout.rowsY());
        assertTrue(layout.mainBottom() < layout.bottomY());
        assertEquals(layout.rightX() + layout.rightWidth(), layout.tabRightEdge());
        assertEquals(layout.leftX(), layout.tabAreaX());
        assertEquals(layout.rightX() + layout.rightWidth() - layout.leftX(), layout.tabAreaWidth());
        assertTrue(layout.headerIdentityX() + layout.headerIdentityWidth() <= layout.headerRoleX());
        assertEquals(layout.headerRoleX() + layout.headerRoleWidth(), layout.headerColorX());
        assertTrue(layout.headerColorX() + layout.headerColorWidth() < layout.closeX());
        assertEquals(layout.headerIdentityWidth(), layout.headerRoleWidth());
        assertTrue(layout.headerColorWidth() >= layout.headerIdentityWidth());
        assertTrue(layout.visibleRows() >= 5);
        assertEquals(75, layout.readableMinimumScalePercent());
    }

    @Test
    void seatReviewUsesReviewRowsInsteadOfNavigationRows() {
        List<GovernmentUiOpenPayload.Row> reviewRows = List.of(new GovernmentUiOpenPayload.Row(
                "proposal-1", "Harbor Law", "Law - Build docks.", "Review", true, false, "action_detail"));
        List<GovernmentUiOpenPayload.Row> officeRows = List.of(new GovernmentUiOpenPayload.Row(
                "president", "President", "Holders", "Filled", true, true, "office"));

        assertEquals(reviewRows, SeatOfRuleScreen.rowsForTab("review", reviewRows, officeRows));
        assertEquals(reviewRows, SeatOfRuleScreen.rowsForTab("", reviewRows, officeRows));
        assertEquals(officeRows, SeatOfRuleScreen.rowsForTab("offices", reviewRows, officeRows));
    }

    @Test
    void seatPrimaryOfficesUseSelfResignOnly() {
        assertTrue(SeatOfRuleScreen.primaryOffice("monarch"));
        assertTrue(SeatOfRuleScreen.primaryOffice("president"));
        assertTrue(!SeatOfRuleScreen.primaryOffice("heir"));
        assertTrue(!SeatOfRuleScreen.primaryOffice("officer"));
    }

    @Test
    void civicFoundingScreensChooseCorrectCurrentVoteRows() {
        assertEquals("stageRows", CivicForumScreen.currentVoteRowSource("civic_name"));
        assertEquals("stageRows", CivicForumScreen.currentVoteRowSource("civic_color"));
        assertEquals("stageRows", CivicForumScreen.currentVoteRowSource("civic_form"));
        assertEquals("stageRows", CivicForumScreen.currentVoteRowSource("civic_election"));
        assertEquals("stageRows", CivicForumScreen.currentVoteRowSource("civic_features"));
    }

    @Test
    void governmentScreensUseReadableFallbackBelowMinimumScale() {
        ElarionScaledLayout civicTooSmall = ElarionScaledLayout.fit(
                420, 260, CivicForumScreen.LOGICAL_WIDTH, CivicForumScreen.LOGICAL_HEIGHT, 8,
                CivicForumScreen.READABLE_MINIMUM_SCALE_PERCENT);
        ElarionScaledLayout civicReadable = ElarionScaledLayout.fit(
                640, 420, CivicForumScreen.LOGICAL_WIDTH, CivicForumScreen.LOGICAL_HEIGHT, 8,
                CivicForumScreen.READABLE_MINIMUM_SCALE_PERCENT);
        ElarionScaledLayout seatTooSmall = ElarionScaledLayout.fit(
                420, 260, SeatOfRuleScreen.LOGICAL_WIDTH, SeatOfRuleScreen.LOGICAL_HEIGHT, 8,
                SeatOfRuleScreen.READABLE_MINIMUM_SCALE_PERCENT);

        assertTrue(civicTooSmall.belowPreferredScale());
        assertTrue(seatTooSmall.belowPreferredScale());
        assertTrue(!civicReadable.belowPreferredScale());
    }

    @Test
    void emptyChoicePanelStillReservesPrimaryAction() {
        List<GovernmentUiOpenPayload.Row> emptyChoices = List.of(new GovernmentUiOpenPayload.Row(
                "empty", "Nothing Submitted", "No candidates have nominated yet.", "Waiting", false, false, "static"));

        assertTrue(CivicForumScreen.choicePanelShowsPrimaryAction("nominate_self", emptyChoices));
        assertTrue(!CivicForumScreen.choicePanelShowsPrimaryAction("", emptyChoices));
    }

    @Test
    void civicPostFoundingTabsAreDisabledDuringFoundingScreens() {
        assertTrue(!CivicForumScreen.tabEnabledForScreen("civic_election", "audience", "current_votes"));
        assertTrue(!CivicForumScreen.tabEnabledForScreen("civic_election", "laws", "current_votes"));
        assertTrue(!CivicForumScreen.tabEnabledForScreen("civic_election", "projects", "current_votes"));
        assertTrue(!CivicForumScreen.tabEnabledForScreen("civic_election", "offices", "current_votes"));
        assertTrue(!CivicForumScreen.tabEnabledForScreen("civic_election", "history", "current_votes"));
        assertTrue(CivicForumScreen.tabEnabledForScreen("civic_features", "audience", "current_votes"));
        assertTrue(CivicForumScreen.tabEnabledForScreen("civic_module_audience", "history", "audience"));
    }

    @Test
    void currentVoteMetricsUseCountsForActiveRowsAndOutcomeForRecentRows() {
        GovernmentUiOpenPayload.Row active = new GovernmentUiOpenPayload.Row(
                "proposal-1", "Harbor Law", "", "Active", true, false,
                false, 3L, "active_vote");
        GovernmentUiOpenPayload.Row recent = new GovernmentUiOpenPayload.Row(
                "proposal-2", "Road Law", "", "Rejected", false, false,
                false, 7L, "recent_vote");

        assertEquals("3 votes", CivicForumScreen.currentVoteMetric(active));
        assertEquals("Rejected", CivicForumScreen.currentVoteMetric(recent));
    }

    @Test
    void sharedGovernmentComponentsReserveVoteAndMetricSpace() {
        CivicForumScreen.Layout civic = CivicForumScreen.layoutMetrics();
        SeatOfRuleScreen.Layout seat = SeatOfRuleScreen.layoutMetrics();

        assertTrue(GovernmentUiComponents.voteOptionLayoutFits(civic.rightWidth() - 28));
        assertTrue(GovernmentUiComponents.voteOptionLayoutFits(seat.rightWidth() - 28));
        assertTrue(GovernmentUiComponents.voteTrackWidth(civic.rightWidth() - 28, true) >= 72);
        assertTrue(GovernmentUiComponents.metricColumnX(civic.leftX(), civic.rowWidth())
                > civic.leftX() + civic.rowWidth() / 2);
        assertTrue(GovernmentUiComponents.DETAIL_ICON_SIZE <= 48);
        assertTrue(GovernmentUiComponents.VOTE_OPTION_HEIGHT <= 30);
    }

    @Test
    void civicIdentityComposesGovernmentFormAndRealmNameOnce() {
        assertEquals("Republic of Oak", GovernmentUiGlyphs.civicIdentityLabel("Republic", "Realm of Oak"));
        assertEquals("Republic of Oak", GovernmentUiGlyphs.civicIdentityLabel("Republic", "Republic of Oak"));
        assertEquals("Oak", GovernmentUiGlyphs.civicIdentityLabel("Unchosen", "Realm of Oak"));
    }

    @Test
    void categoryTagsUseDistinctStableColorRoles() {
        assertTrue(GovernmentUiGlyphs.tagColor("Ember Proposal") != GovernmentUiGlyphs.tagColor("Security"));
        assertTrue(GovernmentUiGlyphs.tagColor("Economy") != GovernmentUiGlyphs.tagColor("Culture"));
        assertEquals(GovernmentUiGlyphs.tagColor("Infrastructure"), GovernmentUiGlyphs.tagColor("realm_project"));
    }
}
