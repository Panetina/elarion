package panetina.elarion.core.client.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public final class ElarionUiTypography {
    private static final int BASE_LINE_HEIGHT = 10;

    private ElarionUiTypography() {
    }

    public static int percent() {
        return Math.max(100, Math.min(150, ElarionUiThemes.current().fontScalePercent()));
    }

    public static float scale() {
        return percent() / 100.0F;
    }

    public static int width(TextRenderer renderer, String text) {
        return Math.round(renderer.getWidth(text == null ? "" : text) * scale());
    }

    public static int width(TextRenderer renderer, Text text) {
        return Math.round(renderer.getWidth(text == null ? Text.empty() : text) * scale());
    }

    public static int fontHeight(TextRenderer renderer) {
        return Math.max(1, Math.round(renderer.fontHeight * scale()));
    }

    public static int lineHeight() {
        return Math.max(BASE_LINE_HEIGHT, Math.round(BASE_LINE_HEIGHT * scale()));
    }

    public static int controlHeight(int baseHeight, TextRenderer renderer, int verticalPadding) {
        return Math.max(baseHeight, fontHeight(renderer) + Math.max(2, verticalPadding));
    }

    public static void draw(
            DrawContext context, TextRenderer renderer, String text, int x, int y, int color, boolean shadow
    ) {
        draw(context, renderer, Text.literal(text == null ? "" : text), x, y, color, shadow);
    }

    public static void draw(
            DrawContext context, TextRenderer renderer, Text text, int x, int y, int color, boolean shadow
    ) {
        float scale = scale();
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0F);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(renderer, text == null ? Text.empty() : text, 0, 0, color, shadow);
        context.getMatrices().pop();
    }

    public static void draw(
            DrawContext context, TextRenderer renderer, OrderedText text, int x, int y, int color, boolean shadow
    ) {
        float scale = scale();
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0F);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(renderer, text, 0, 0, color, shadow);
        context.getMatrices().pop();
    }

    public static void drawCentered(
            DrawContext context, TextRenderer renderer, String text, int centerX, int y, int color, boolean shadow
    ) {
        draw(context, renderer, text, centerX - width(renderer, text) / 2, y, color, shadow);
    }

    public static void drawRight(
            DrawContext context, TextRenderer renderer, String text, int rightX, int y, int color, boolean shadow
    ) {
        draw(context, renderer, text, rightX - width(renderer, text), y, color, shadow);
    }

    public static String ellipsize(TextRenderer renderer, String text, int maximumWidth) {
        String value = text == null ? "" : text;
        int unscaledWidth = Math.max(0, (int) Math.floor(maximumWidth / scale()));
        if (renderer.getWidth(value) <= unscaledWidth) return value;
        int allowed = Math.max(0, unscaledWidth - renderer.getWidth("..."));
        return renderer.trimToWidth(value, allowed) + "...";
    }

    public static List<OrderedText> wrap(TextRenderer renderer, Text text, int maximumWidth) {
        int unscaledWidth = Math.max(1, (int) Math.floor(maximumWidth / scale()));
        return renderer.wrapLines(text == null ? Text.empty() : text, unscaledWidth);
    }

    public static void wrappedClipped(
            DrawContext context, TextRenderer renderer, Text text, int x, int y,
            int maxWidth, int maxHeight, int color, int mutedColor
    ) {
        if (maxHeight <= 0 || maxWidth <= 0) return;
        List<OrderedText> lines = wrap(renderer, text, maxWidth);
        int lineHeight = lineHeight();
        int maxLines = Math.max(1, maxHeight / lineHeight);
        int count = Math.min(lines.size(), maxLines);
        for (int index = 0; index < count; index++) {
            draw(context, renderer, lines.get(index), x, y + index * lineHeight, color, false);
        }
        if (lines.size() > count) {
            draw(context, renderer, "...", x, y + (count - 1) * lineHeight, mutedColor, false);
        }
    }
}
