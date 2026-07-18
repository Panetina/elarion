package panetina.elarion.core.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionAdminPanelScreenLayoutTest {
    @Test
    void defaultPanelFitsRowsAndDetailInsideContentBand() {
        ElarionAdminPanelScreen.Layout layout = ElarionAdminPanelScreen.layoutMetrics();

        assertEquals(392, layout.listWidth());
        assertEquals(200, layout.detailWidth());
        assertTrue(layout.contentTop() > 0);
        assertTrue(layout.contentBottom() > layout.contentTop());
        assertTrue(layout.visibleRows() >= 4);

        int used = layout.visibleRows() * layout.rowHeight()
                + (layout.visibleRows() - 1) * layout.rowGap();
        assertTrue(used <= layout.contentHeight());
        assertTrue(layout.actionTop() > layout.contentTop());
        assertTrue(layout.visibleActions() >= 5);
        assertTrue(layout.tabWidth() >= 76);
        assertTrue(layout.tabRight() <= 660 - 12);

        int actionsUsed = layout.visibleActions() * 20
                + (layout.visibleActions() - 1) * 5;
        assertTrue(layout.actionTop() + actionsUsed <= layout.contentBottom());
    }

    @Test
    void configIndexRowsRequestScopedReloadsOnlyOnConfigTab() {
        assertTrue(ElarionAdminPanelScreen.shouldRequestScopedRows("configs", "config:core"));
        assertTrue(ElarionAdminPanelScreen.shouldRequestScopedRows("configs", "config:core:category:realms"));
        assertFalse(ElarionAdminPanelScreen.shouldRequestScopedRows("configs", "config-entry|core|realms|realms.count"));
        assertFalse(ElarionAdminPanelScreen.shouldRequestScopedRows("players", "config:core"));
        assertFalse(ElarionAdminPanelScreen.shouldRequestScopedRows("systems", "provider-row"));
    }

    @Test
    void actionConfirmationModalKeepsBodyAndButtonsInsideFrame() {
        assertActionModalFits(ElarionAdminPanelScreen.actionModalLayout(false), false);
        assertActionModalFits(ElarionAdminPanelScreen.actionModalLayout(true), true);
    }

    @Test
    void configEditShellKeepsControlsInsideFrame() {
        ElarionAdminPanelScreen.ConfigEditLayout layout = ElarionAdminPanelScreen.configEditLayout();

        assertTrue(layout.x() > 0);
        assertTrue(layout.y() > 0);
        assertTrue(layout.x() + layout.width() < 660);
        assertTrue(layout.bottom() < 430);

        assertTrue(layout.topCloseX() > layout.x());
        assertTrue(layout.topCloseX() + layout.topCloseWidth() < layout.x() + layout.width());
        assertTrue(layout.topCloseY() > layout.y());

        assertTrue(layout.descriptionY() > layout.y());
        assertTrue(layout.metadataY() > layout.descriptionY() + layout.descriptionHeight());
        assertTrue(layout.proposedY() > layout.metadataY());
        assertTrue(layout.proposedInputX() > layout.bodyX());
        assertTrue(layout.proposedInputX() + layout.proposedInputWidth() < layout.x() + layout.width());

        assertTrue(layout.validateX() > layout.x());
        assertTrue(layout.applyX() > layout.validateX());
        assertTrue(layout.closeX() > layout.applyX());
        assertTrue(layout.closeX() + layout.buttonWidth() < layout.x() + layout.width());
        assertTrue(layout.buttonBottom() < layout.bottom());
    }

    private static void assertActionModalFits(ElarionAdminPanelScreen.ActionModalLayout layout, boolean withInput) {
        assertTrue(layout.x() > 0);
        assertTrue(layout.y() > 0);
        assertTrue(layout.x() + layout.width() < 660);
        assertTrue(layout.bottom() < 430);

        assertTrue(layout.bodyX() > layout.x());
        assertTrue(layout.bodyY() > layout.y());
        assertTrue(layout.bodyX() + layout.bodyWidth() < layout.x() + layout.width());
        assertTrue(layout.bodyY() + layout.bodyHeight() < layout.buttonY());

        assertTrue(layout.cancelX() > layout.x());
        assertTrue(layout.submitX() > layout.cancelX());
        assertTrue(layout.buttonBottom() < layout.bottom());

        if (withInput) {
            assertTrue(layout.inputY() > layout.bodyY() + layout.bodyHeight());
            assertTrue(layout.inputY() + 18 < layout.buttonY());
            assertEquals(layout.bodyWidth(), layout.inputWidth());
        }
    }
}
