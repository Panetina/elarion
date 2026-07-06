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
}
