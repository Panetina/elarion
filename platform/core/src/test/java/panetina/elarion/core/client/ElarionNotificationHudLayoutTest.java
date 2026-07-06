package panetina.elarion.core.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.model.ElarionUiTheme;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionNotificationHudLayoutTest {
    @AfterEach
    void resetTheme() {
        ElarionUiThemes.clear();
    }

    @Test
    void closeButtonIsCenteredInsideHeaderAndPanelBounds() {
        ElarionNotificationHudLayout.Metrics layout = ElarionNotificationHudLayout.metrics();

        assertEquals(layout.headerCenterY(), layout.closeCenterY());
        assertTrue(layout.closeX() > layout.panelX());
        assertTrue(layout.closeX() + layout.closeSize() <= layout.panelRight() - 4);
        assertTrue(layout.closeCenterX() > layout.panelRight() - 16);
    }

    @Test
    void listBandStaysInsideDrawerPanel() {
        ElarionNotificationHudLayout.Metrics layout = ElarionNotificationHudLayout.metrics();

        assertEquals(layout.panelX() + 6, layout.listX());
        assertTrue(layout.listTop() > layout.drawerHeaderHeight());
        assertTrue(layout.listRight() <= layout.panelRight() - 6);
        assertTrue(layout.minimumEmptyDrawerHeight() <= layout.maxPanelHeight());
    }

    @Test
    void rowAndActionHeightsGrowWithFontScale() {
        ElarionNotificationHudLayout.Metrics normal = ElarionNotificationHudLayout.metrics();

        ElarionUiThemes.update(themeWithScale(150));
        ElarionNotificationHudLayout.Metrics large = ElarionNotificationHudLayout.metrics();

        assertTrue(large.rowHeight() > normal.rowHeight());
        assertTrue(large.actionHeaderHeight() > normal.actionHeaderHeight());
        assertTrue(large.actionButtonHeight() > normal.actionButtonHeight());
        assertTrue(large.rowHeight() <= large.maxPanelHeight() / 4);
    }

    @Test
    void shortDetailsUseContentHeightAndLongDetailsRemainBounded() {
        ElarionNotificationHudLayout.Metrics layout = ElarionNotificationHudLayout.metrics();

        int shortDetail = ElarionNotificationHudLayout.boundedDetailDrawerHeight(48, 72, 31, 68);
        int longDetail = ElarionNotificationHudLayout.boundedDetailDrawerHeight(48, 400, 31, 68);

        assertEquals(161, shortDetail);
        assertEquals(layout.maxPanelHeight(), longDetail);
    }

    @Test
    void selectedRailPointerOnlyShowsWhenItMeetsTheDrawer() {
        assertTrue(ElarionNotificationHudLayout.railPointerVisible(true, 40, 82));
        assertTrue(!ElarionNotificationHudLayout.railPointerVisible(true, 112, 82));
        assertTrue(!ElarionNotificationHudLayout.railPointerVisible(false, 40, 82));
    }

    private static ElarionUiTheme themeWithScale(int fontScalePercent) {
        ElarionUiTheme defaults = ElarionUiTheme.defaults();
        return new ElarionUiTheme(
                defaults.logicalWidth(),
                defaults.logicalHeight(),
                defaults.minimumScalePercent(),
                fontScalePercent,
                defaults.padding(),
                defaults.gap(),
                defaults.rowHeight(),
                defaults.buttonHeight(),
                defaults.scrollbarWidth(),
                Map.of());
    }
}
