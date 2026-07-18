package panetina.elarion.core.client.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.ElarionUiThemeVariant;

import java.util.List;

public final class ElarionUiRenderer {
    private static final Identifier CURRENCY = Identifier.of("elarion", "textures/item/currency.png");
    public static final int CURRENCY_BADGE_WIDTH = 150;

    private ElarionUiRenderer() {
    }

    public static void panel(DrawContext context, int x, int y, int width, int height, ElarionUiStyle style) {
        context.fill(x, y, x + width, y + height, style.panelColor());
        texture(context, x, y, width, height, style.panelTexture(), style.panelTextureMode(),
                style.panelTextureTint());
        beveledFrame(context, x, y, width, height, style);
    }

    public static void panel(
            DrawContext context, int x, int y, int width, int height, ElarionUiThemeVariant theme
    ) {
        panel(context, x, y, width, height, ElarionUiStyle.from(theme));
    }

    private static void texture(
            DrawContext context, int x, int y, int width, int height, String raw,
            String mode, int tint
    ) {
        if (raw == null || raw.isBlank()) return;
        Identifier texture = Identifier.tryParse(raw);
        if (texture == null) return;
        if ("stretch".equalsIgnoreCase(mode)) {
            context.drawTexture(texture, x, y, 0, 0, width, height, width, height);
        } else {
            for (int tileY = y; tileY < y + height; tileY += 16) {
                for (int tileX = x; tileX < x + width; tileX += 16) {
                    int tileWidth = Math.min(16, x + width - tileX);
                    int tileHeight = Math.min(16, y + height - tileY);
                    context.drawTexture(texture, tileX, tileY, 0, 0, tileWidth, tileHeight, 16, 16);
                }
            }
        }
        if ((tint >>> 24) != 0 && tint != 0xFFFFFFFF) context.fill(x, y, x + width, y + height, tint);
    }

    public static void borderedBox(
            DrawContext context, int x, int y, int width, int height, ElarionUiStyle style
    ) {
        beveledBox(context, x, y, width, height, style.cardColor(), style);
        texture(context, x + 2, y + 2, Math.max(0, width - 4), Math.max(0, height - 4),
                style.cardTexture(), style.panelTextureMode(),
                style.panelTextureTint());
        beveledFrame(context, x, y, width, height, style);
    }

    public static void headerBand(
            DrawContext context, int x, int y, int width, int height, ElarionUiStyle style
    ) {
        if (width <= 0 || height <= 0) return;
        context.fill(x + 1, y, x + width - 1, y + height, style.headerColor());
        context.fill(x, y + 1, x + width, y + height - 1, style.headerColor());
        context.fill(x + 2, y, x + width - 2, y + 1, style.bevelHighlightColor());
        context.fill(x + 1, y + 1, x + 2, y + height - 1, style.bevelHighlightColor());
        context.fill(x + 2, y + height - 2, x + width - 2, y + height - 1, style.bevelShadowColor());
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, style.bevelShadowColor());
        context.fill(x + 2, y + height - 1, x + width - 2, y + height, style.borderColor());
    }

    public static void compactButton(
            DrawContext context, TextRenderer renderer, int x, int y, int width, int height,
            String label, boolean hovered, boolean active, ElarionUiStyle style
    ) {
        compactButton(context, renderer, x, y, width, height, label, hovered, false, active, style);
    }

    public static void compactButton(
            DrawContext context, TextRenderer renderer, int x, int y, int width, int height,
            String label, boolean hovered, boolean pressed, boolean active, ElarionUiStyle style
    ) {
        int fill = !active ? style.buttonDisabledColor()
                : pressed ? style.cardColor()
                : hovered ? style.buttonHoverColor() : style.buttonColor();
        beveledBox(context, x, y, width, height, fill,
                pressed ? ElarionCivicColors.ACTIVE_GREEN
                        : active && hovered ? style.titleColor() : style.borderColor(), style);
        if (active && hovered && !pressed && width > 4 && height > 4) {
            context.fill(x + 3, y + 2, x + width - 3, y + 3, 0x22FFFFFF);
        }
        String visible = ellipsize(renderer, label, Math.max(1, width - 10));
        int color = active ? style.textColor() : style.mutedColor();
        ElarionUiTypography.draw(context, renderer, visible,
                x + Math.max(4, (width - ElarionUiTypography.width(renderer, visible)) / 2),
                ElarionCivicUi.centeredTextY(renderer, y, height) + (pressed ? 1 : 0),
                color, false);
    }

    public static void tab(
            DrawContext context, TextRenderer renderer, int x, int y, int width, int height,
            String label, boolean selected, boolean hovered, ElarionUiThemeVariant theme
    ) {
        ElarionUiStyle style = ElarionUiStyle.from(theme);
        int fill = selected ? theme.cardColor() : hovered ? theme.buttonHoverColor() : theme.buttonColor();
        beveledBox(context, x, y, width, height, fill,
                selected ? ElarionCivicColors.ACTIVE_GREEN : theme.borderColor(), style);
        String visible = ellipsize(renderer, label, width - 8);
        ElarionUiTypography.draw(context, renderer, visible,
                x + (width - ElarionUiTypography.width(renderer, visible)) / 2,
                ElarionCivicUi.centeredTextY(renderer, y, height),
                selected ? style.titleColor() : style.textColor(), false);
    }

    public static void progressBar(
            DrawContext context, TextRenderer renderer, int x, int y, int width, int height,
            long current, long required, ElarionUiThemeVariant theme
    ) {
        long safeRequired = Math.max(1L, required);
        float ratio = Math.max(0.0F, Math.min(1.0F, current / (float) safeRequired));
        ElarionUiStyle style = ElarionUiStyle.from(theme);
        beveledBox(context, x, y, width, height, theme.progressBackgroundColor(), style);
        int innerWidth = Math.max(0, width - 4);
        int fillWidth = Math.round(innerWidth * ratio);
        context.fill(x + 2, y + 2, x + 2 + fillWidth, y + height - 2,
                ratio >= 1.0F ? theme.progressCompleteColor() : theme.progressFillColor());
        String label = current + " / " + required + "  " + Math.round(ratio * 100.0F) + "%";
        ElarionUiTypography.drawCentered(context, renderer, label, x + width / 2,
                y + Math.max(2, (height - ElarionUiTypography.fontHeight(renderer)) / 2),
                theme.textColor(), true);
    }

    public static void scrollbar(
            DrawContext context, int x, int y, int width, int height,
            int firstVisible, int visibleRows, int itemCount, ElarionUiThemeVariant theme
    ) {
        ElarionUiStyle style = ElarionUiStyle.from(theme);
        beveledBox(context, x, y, width, height, theme.scrollbarTrackColor(), style);
        if (itemCount <= visibleRows) return;
        int thumbHeight = Math.max(8, height * visibleRows / itemCount);
        int travel = Math.max(1, height - thumbHeight);
        int maxFirst = Math.max(1, itemCount - visibleRows);
        int thumbY = y + Math.round(travel * (firstVisible / (float) maxFirst));
        beveledBox(context, x, thumbY, width, thumbHeight, theme.scrollbarThumbColor(), style);
    }

    public static void dialogueBox(
            DrawContext context, TextRenderer renderer, int x, int y, int width, int height,
            String speaker, String message, ElarionUiStyle style
    ) {
        dialogueBox(context, renderer, x, y, width, height, speaker, message, style.textColor(), style);
    }

    public static void dialogueBox(
            DrawContext context, TextRenderer renderer, int x, int y, int width, int height,
            String speaker, String message, int messageColor, ElarionUiStyle style
    ) {
        borderedBox(context, x, y, width, height, style);
        boolean named = speaker != null && !speaker.isBlank();
        if (named) ElarionUiTypography.draw(context, renderer, speaker, x + 7, y + 6, style.titleColor(), false);
        int textY = named ? y + 9 + ElarionUiTypography.lineHeight() : y + 7;
        wrappedClipped(context, renderer, Text.literal(message), x + 7, textY, width - 14,
                named ? height - 26 : height - 14, messageColor, style.mutedColor());
    }

    public static void portraitFrame(
            DrawContext context, TextRenderer renderer, int x, int y, int size,
            String fallback, ElarionUiStyle style
    ) {
        borderedBox(context, x, y, size, size, style);
        int inner = size - 12;
        beveledBox(context, x + 6, y + 6, inner, inner, style.insetColor(), style);
        ElarionUiTypography.drawCentered(context, renderer, fallback, x + size / 2,
                y + (size - ElarionUiTypography.fontHeight(renderer)) / 2, style.mutedColor(), false);
    }

    public static void currencyBadge(
            DrawContext context, TextRenderer renderer, int x, int y, long amount,
            String currencyPlural, ElarionUiStyle style
    ) {
        int width = CURRENCY_BADGE_WIDTH;

        beveledBox(context, x, y, width, 30, style.cardColor(), style);
        context.drawTexture(CURRENCY, x + 7, y + 7, 0, 0, 16, 16, 16, 16);

        String currency = currencyPlural == null || currencyPlural.isBlank() ? "Currency" : currencyPlural;

        String prefix = "Balance: ";
        String value = amount + " " + currency;

        int textX = x + 29;
        int textY = y + 11;
        int maxTextWidth = width - 36;

        if (ElarionUiTypography.width(renderer, prefix) >= maxTextWidth) {
            String label = ellipsize(renderer, prefix + value, maxTextWidth);
            ElarionUiTypography.draw(context, renderer, label, textX, textY, style.textColor(), false);
            return;
        }

        String visibleValue = ellipsize(renderer, value, maxTextWidth - ElarionUiTypography.width(renderer, prefix));

        ElarionUiTypography.draw(context, renderer, prefix, textX, textY, style.textColor(), false);
        ElarionUiTypography.draw(context, renderer, visibleValue,
                textX + ElarionUiTypography.width(renderer, prefix), textY, 0xFF9696D1, false);
    }

    public static void currencyIcon(DrawContext context, int x, int y, int size) {
        context.drawTexture(CURRENCY, x, y, size, size, 0.0F, 0.0F, 16, 16, 16, 16);
    }

    public static void relationBar(
            DrawContext context, TextRenderer renderer, int x, int y, int width,
            String label, int value, ElarionUiStyle style
    ) {
        String text = label == null || label.isBlank() ? "Relation: Neutral" : label;
        int barWidth = Math.min(96, Math.max(52, width / 4));
        int barX = x + ElarionUiTypography.width(renderer, text) + 8;
        int clamped = Math.max(-100, Math.min(100, value));
        int fillWidth = (barWidth * (clamped + 100)) / 200;
        int color = clamped >= 0 ? style.relationGoodColor() : style.relationBadColor();
        ElarionUiTypography.draw(context, renderer, text, x, y, color, false);
        beveledBox(context, barX, y + 1, barWidth, 8, style.insetColor(), style);
        context.fill(barX + 2, y + 3, barX + Math.max(2, fillWidth - 1), y + 7, color);
    }

    public static void cards(
            DrawContext context, TextRenderer renderer, int x, int y, int width,
            List<ElarionUiCard> cards, ElarionUiStyle style
    ) {
        if (cards.isEmpty()) return;
        int cardWidth = Math.max(80, width / Math.min(3, cards.size()));
        for (int index = 0; index < cards.size(); index++) {
            int cardX = x + index * (cardWidth + 4);
            if (cardX + cardWidth > x + width) break;
            ElarionUiCard card = cards.get(index);
            beveledBox(context, cardX, y, cardWidth, 30,
                    card.disabled() ? style.buttonDisabledColor() : style.cardColor(), style);
            ElarionUiTypography.draw(context, renderer, ellipsize(renderer,
                    card.label() + (card.count() > 0 ? " x" + card.count() : ""), cardWidth - 10),
                    cardX + 5, y + 5, style.textColor(), false);
            if (card.currencyAmount() != 0) {
                context.drawTexture(CURRENCY, cardX + 5, y + 17, 0, 0, 8, 8, 16, 16);
                ElarionUiTypography.draw(context, renderer, String.valueOf(card.currencyAmount()), cardX + 16,
                        y + 7 + ElarionUiTypography.lineHeight(),
                        style.feedbackColor(), false);
            }
        }
    }

    public static void icon(
            DrawContext context, int x, int y, int size, String raw, ElarionUiThemeVariant theme
    ) {
        beveledBox(context, x, y, size, size, theme.insetColor(), ElarionUiStyle.from(theme));
        Identifier icon = Identifier.tryParse(raw);
        if (icon != null) {
            int drawSize = Math.max(1, size - 8);
            int drawX = x + (size - drawSize) / 2;
            int drawY = y + (size - drawSize) / 2;
            context.drawTexture(icon, drawX, drawY, 0, 0, drawSize, drawSize, 16, 16);
        }
    }

    public static void wrappedClipped(
            DrawContext context, TextRenderer renderer, Text text, int x, int y,
            int maxWidth, int maxHeight, int color, int mutedColor
    ) {
        ElarionUiTypography.wrappedClipped(
                context, renderer, text, x, y, maxWidth, maxHeight, color, mutedColor);
    }

    public static String ellipsize(TextRenderer renderer, String text, int maximumWidth) {
        return ElarionUiTypography.ellipsize(renderer, text, maximumWidth);
    }

    public static void beveledBox(
            DrawContext context, int x, int y, int width, int height, int fill, ElarionUiStyle style
    ) {
        beveledBox(context, x, y, width, height, fill, style.borderColor(), style);
    }

    public static void beveledBox(
            DrawContext context, int x, int y, int width, int height, int fill,
            int border, ElarionUiStyle style
    ) {
        if (width <= 2 || height <= 2) {
            context.fill(x, y, x + Math.max(0, width), y + Math.max(0, height), fill);
            return;
        }
        context.fill(x + 1, y, x + width - 1, y + height, fill);
        context.fill(x, y + 1, x + width, y + height - 1, fill);
        beveledFrame(context, x, y, width, height, border, style);
    }

    private static void beveledFrame(
            DrawContext context, int x, int y, int width, int height, ElarionUiStyle style
    ) {
        beveledFrame(context, x, y, width, height, style.borderColor(), style);
    }

    private static void beveledFrame(
            DrawContext context, int x, int y, int width, int height, int border, ElarionUiStyle style
    ) {
        if (width <= 2 || height <= 2) return;
        context.fill(x + 2, y, x + width - 2, y + 1, style.bevelHighlightColor());
        context.fill(x, y + 2, x + 1, y + height - 2, style.bevelHighlightColor());
        context.fill(x, y, x + 1, y + 1, style.bevelShadowColor());
        context.fill(x + width - 1, y, x + width, y + 1, style.bevelShadowColor());
        context.fill(x, y + height - 1, x + 1, y + height, style.bevelShadowColor());
        context.fill(x + width - 1, y + height - 1, x + width, y + height, style.bevelShadowColor());
        context.fill(x + 1, y + 1, x + width - 1, y + 2, border);
        context.fill(x + 1, y + 1, x + 2, y + height - 1, border);
        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, border);
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, border);
        context.fill(x + 2, y + height - 1, x + width - 2, y + height, style.bevelShadowColor());
        context.fill(x + width - 1, y + 2, x + width, y + height - 2, style.bevelShadowColor());
    }
}
