package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionUiTheme;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionUiMetricsTest {
    @AfterEach
    void resetTheme() {
        ElarionUiThemes.clear();
    }

    @Test
    void controlHeightsGrowWithFontScale() {
        ElarionUiThemes.update(themeWithScale(100));
        assertEquals(18, ElarionUiMetrics.rowHeight(18));
        assertEquals(18, ElarionUiMetrics.buttonHeight(18));

        ElarionUiThemes.update(themeWithScale(125));
        assertEquals(21, ElarionUiMetrics.rowHeight(18));
        assertEquals(19, ElarionUiMetrics.buttonHeight(18));

        ElarionUiThemes.update(themeWithScale(150));
        assertEquals(23, ElarionUiMetrics.rowHeight(18));
        assertEquals(21, ElarionUiMetrics.buttonHeight(18));
    }

    @Test
    void themedHeightsUseCurrentThemeBaseValues() {
        ElarionUiThemes.update(themeWithScaleAndHeights(150, 24, 22));

        assertEquals(24, ElarionUiMetrics.themedRowHeight());
        assertEquals(22, ElarionUiMetrics.themedButtonHeight());
    }

    @Test
    void visibleRowsAndRowsHeightUseSameBounds() {
        assertEquals(0, ElarionUiMetrics.visibleRows(17, 18, 4));
        assertEquals(1, ElarionUiMetrics.visibleRows(18, 18, 4));
        assertEquals(2, ElarionUiMetrics.visibleRows(40, 18, 4));
        assertEquals(62, ElarionUiMetrics.rowsHeight(3, 18, 4));
        assertEquals(0, ElarionUiMetrics.rowsHeight(0, 18, 4));
    }

    private static ElarionUiTheme themeWithScale(int fontScalePercent) {
        ElarionUiTheme defaults = ElarionUiTheme.defaults();
        return themeWithScaleAndHeights(fontScalePercent, defaults.rowHeight(), defaults.buttonHeight());
    }

    private static ElarionUiTheme themeWithScaleAndHeights(
            int fontScalePercent, int rowHeight, int buttonHeight
    ) {
        ElarionUiTheme defaults = ElarionUiTheme.defaults();
        return new ElarionUiTheme(
                defaults.logicalWidth(),
                defaults.logicalHeight(),
                defaults.minimumScalePercent(),
                fontScalePercent,
                defaults.padding(),
                defaults.gap(),
                rowHeight,
                buttonHeight,
                defaults.scrollbarWidth(),
                Map.of());
    }
}
