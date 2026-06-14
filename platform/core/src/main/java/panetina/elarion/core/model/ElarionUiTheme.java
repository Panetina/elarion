package panetina.elarion.core.model;

import java.util.Map;

public record ElarionUiTheme(
        int logicalWidth,
        int logicalHeight,
        int minimumScalePercent,
        int padding,
        int gap,
        int rowHeight,
        int buttonHeight,
        int scrollbarWidth,
        Map<String, ElarionUiThemeVariant> variants
) {
    public ElarionUiTheme {
        variants = variants == null ? Map.of() : Map.copyOf(variants);
    }

    public ElarionUiThemeVariant variant(String id) {
        return variants.getOrDefault(id, variants.getOrDefault("default", defaults().variants().get("default")));
    }

    public static ElarionUiTheme defaults() {
        ElarionUiThemeVariant base = new ElarionUiThemeVariant(
                "default", 0xFF202020, 0xFF292015, 0xFF181818, 0xFF8A8A8A,
                0xFFB78A46, 0xFF090705, 0x66000000,
                0xFFE0E0E0, 0xFFFFFFFF, 0xFFAAAAAA, 0xFF80FF80, 0xFFFFC766,
                0xFFFF7777, 0xFF262626, 0xFF4A4A4A, 0xFF666666, 0xFF303030,
                0xFF181818, 0xFFC58A2C, 0xFF70C060, 0xFF201C18, 0xFF9F7A2D,
                "", "", "tiled", 0x00000000);
        return new ElarionUiTheme(480, 340, 60, 16, 8, 18, 18, 6, Map.of("default", base));
    }
}
