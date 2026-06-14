package panetina.elarion.core.client.ui;

import panetina.elarion.core.model.ElarionUiThemeVariant;

public record ElarionUiStyle(
        int panelColor, int headerColor, int insetColor, int borderColor,
        int bevelHighlightColor, int bevelShadowColor,
        int backgroundOverlayColor, int titleColor, int textColor,
        int feedbackColor, int errorColor, int mutedColor, int buttonColor, int buttonHoverColor,
        int buttonDisabledColor, int cardColor, int relationGoodColor, int relationBadColor,
        int scrollbarTrackColor, int scrollbarThumbColor,
        String panelTexture, String cardTexture, String panelTextureMode, int panelTextureTint
) {
    public static ElarionUiStyle from(ElarionUiThemeVariant theme) {
        return new ElarionUiStyle(
                theme.panelColor(), theme.headerColor(), theme.insetColor(), theme.borderColor(),
                theme.bevelHighlightColor(), theme.bevelShadowColor(), theme.backgroundOverlayColor(),
                theme.titleColor(), theme.textColor(), theme.successColor(), theme.errorColor(),
                theme.mutedColor(), theme.buttonColor(), theme.buttonHoverColor(), theme.disabledColor(),
                theme.cardColor(), theme.successColor(), theme.errorColor(),
                theme.scrollbarTrackColor(), theme.scrollbarThumbColor(),
                theme.panelTexture(), theme.cardTexture(), theme.textureMode(), theme.textureTint());
    }
}
