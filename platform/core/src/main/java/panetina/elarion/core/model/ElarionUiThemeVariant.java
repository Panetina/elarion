package panetina.elarion.core.model;

public record ElarionUiThemeVariant(
        String id,
        int panelColor,
        int headerColor,
        int insetColor,
        int borderColor,
        int bevelHighlightColor,
        int bevelShadowColor,
        int backgroundOverlayColor,
        int titleColor,
        int textColor,
        int mutedColor,
        int successColor,
        int warningColor,
        int errorColor,
        int disabledColor,
        int buttonColor,
        int buttonHoverColor,
        int cardColor,
        int progressBackgroundColor,
        int progressFillColor,
        int progressCompleteColor,
        int scrollbarTrackColor,
        int scrollbarThumbColor,
        String panelTexture,
        String cardTexture,
        String textureMode,
        int textureTint
) {
    public ElarionUiThemeVariant {
        id = id == null || id.isBlank() ? "default" : id;
        panelTexture = panelTexture == null ? "" : panelTexture;
        cardTexture = cardTexture == null ? "" : cardTexture;
        textureMode = textureMode == null || textureMode.isBlank() ? "tiled" : textureMode;
    }
}
