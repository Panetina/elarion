package panetina.elarion.core.client.ui;

import panetina.elarion.core.model.ElarionUiTheme;
import panetina.elarion.core.model.ElarionUiThemeVariant;

public final class ElarionUiThemes {
    private static volatile ElarionUiTheme current = ElarionUiTheme.defaults();

    private ElarionUiThemes() {
    }

    public static void update(ElarionUiTheme theme) {
        current = theme == null ? ElarionUiTheme.defaults() : theme;
    }

    public static void clear() {
        current = ElarionUiTheme.defaults();
    }

    public static ElarionUiTheme current() {
        return current;
    }

    public static ElarionUiThemeVariant variant(String id) {
        return current.variant(id);
    }
}
