package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionUiTheme;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionUiTypographyTest {
    @AfterEach
    void resetTheme() {
        ElarionUiThemes.clear();
    }

    @Test
    void defaultsToUnscaledText() {
        ElarionUiThemes.clear();

        assertEquals(100, ElarionUiTypography.percent());
        assertEquals(1.0F, ElarionUiTypography.scale());
        assertEquals(10, ElarionUiTypography.lineHeight());
    }

    @Test
    void clampsServerScaleToSupportedBounds() {
        ElarionUiThemes.update(themeWithScale(125));

        assertEquals(125, ElarionUiTypography.percent());
        assertEquals(1.25F, ElarionUiTypography.scale());
        assertEquals(13, ElarionUiTypography.lineHeight());

        ElarionUiThemes.update(themeWithScale(99));
        assertEquals(100, ElarionUiTypography.percent());

        ElarionUiThemes.update(themeWithScale(151));
        assertEquals(150, ElarionUiTypography.percent());
        assertEquals(15, ElarionUiTypography.lineHeight());
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
