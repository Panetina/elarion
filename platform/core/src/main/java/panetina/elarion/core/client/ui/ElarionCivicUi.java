package panetina.elarion.core.client.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class ElarionCivicUi {
    public enum Tone {
        NORMAL,
        PRIMARY,
        DESTRUCTIVE,
        MUTED,
        INFO
    }

    private ElarionCivicUi() {
    }

    public static void thinBox(DrawContext context, int x, int y, int width, int height, int fill, int border) {
        if (width <= 0 || height <= 0) return;
        int shadow = border == ElarionCivicColors.ACTIVE_GREEN
                ? ElarionCivicColors.ACTIVE_GREEN_SHADOW
                : border == ElarionCivicColors.REJECT_RED
                ? ElarionCivicColors.REJECT_RED_SHADOW
                : ElarionCivicColors.GOLD_SHADOW;
        context.fill(x, y, x + width, y + height, fill);
        if (width <= 1 || height <= 1) return;
        context.fill(x + 1, y, x + width - 1, y + 1, border);
        context.fill(x, y + 1, x + 1, y + height - 1, border);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, shadow);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, shadow);
    }

    public static void attachedShell(
            DrawContext context, int x, int y, int width, int height, int headerHeight
    ) {
        if (width <= 0 || height <= 0) return;
        int safeHeader = Math.max(2, Math.min(height, headerHeight));
        int bodyY = y + safeHeader - 1;
        thinBox(context, x, y, width, safeHeader, ElarionCivicColors.HEADER_SURFACE,
                ElarionCivicColors.GOLD_BORDER);
        thinBox(context, x, bodyY, width, height - safeHeader + 1, ElarionCivicColors.ROOT_SURFACE,
                ElarionCivicColors.GOLD_BORDER);
        context.fill(x + 2, bodyY, x + width - 2, bodyY + 1, ElarionCivicColors.GOLD_SHADOW);
        context.fill(x + 6, y + 5, x + width - 6, y + 6, ElarionCivicColors.SOFT_GOLD_LINE);
        drawSubtleGrid(context, x, bodyY, width, height - safeHeader + 1);
    }

    public static void headerShell(
            DrawContext context, int x, int y, int width, int height, int headerHeight
    ) {
        if (width <= 0 || height <= 0) return;
        int safeHeader = Math.max(2, Math.min(height, headerHeight));
        thinBox(context, x, y, width, height, ElarionCivicColors.ROOT_SURFACE,
                ElarionCivicColors.GOLD_BORDER);
        context.fill(x + 2, y + safeHeader - 1, x + width - 2, y + safeHeader,
                ElarionCivicColors.GOLD_SHADOW);
        context.fill(x + 6, y + 5, x + width - 6, y + 6, ElarionCivicColors.SOFT_GOLD_LINE);
        context.fill(x + 1, y + 1, x + width - 1, y + safeHeader - 1,
                ElarionCivicColors.HEADER_SURFACE);
    }

    public static void railShell(DrawContext context, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        thinBox(context, x, y, width, height, ElarionCivicColors.ROOT_SURFACE,
                ElarionCivicColors.GOLD_BORDER);
        if (width <= 4 || height <= 4) return;
        context.fill(x + 2, y + 2, x + width - 2, y + height - 2, ElarionCivicColors.RAIL_INSET);
        for (int markY = y + 3; markY < y + height - 3; markY += 8) {
            context.fill(x + 2, markY, x + 3, Math.min(markY + 4, y + height - 2),
                    ElarionCivicColors.RAIL_SIDE_MARK);
            context.fill(x + width - 3, markY + 3, x + width - 2,
                    Math.min(markY + 7, y + height - 2), ElarionCivicColors.RAIL_SIDE_MARK);
        }
    }

    public static void rowSurface(
            DrawContext context, int x, int y, int width, int height,
            boolean selected, boolean hovered, boolean disabled
    ) {
        int fill = disabled ? ElarionCivicColors.BUTTON_DISABLED
                : selected ? ElarionCivicColors.CARD_SELECTED
                : hovered ? ElarionCivicColors.CARD_HOVER : ElarionCivicColors.CARD_SURFACE;
        int border = selected ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_BORDER;
        thinBox(context, x, y, width, height, fill, border);
        if (width <= 4 || height <= 4) return;
        context.fill(x + 2, y + 2, x + width - 2, y + 3,
                selected ? ElarionCivicColors.ROW_SELECTED_GLOSS : ElarionCivicColors.ROW_TOP_GLOSS);
        if (hovered && !selected && !disabled) {
            context.fill(x + 2, y + 3, x + width - 2, y + 4, ElarionCivicColors.ROW_HOVER_GLOSS);
        }
        if (selected) {
            context.fill(x + 2, y + 3, x + 4, y + height - 3, ElarionCivicColors.ACTIVE_GREEN);
        }
    }

    public static void messageBody(DrawContext context, int x, int y, int width, int height, int accent) {
        if (width <= 0 || height <= 0) return;
        context.fill(x, y, x + width, y + height, ElarionCivicColors.MESSAGE_BODY);
        context.fill(x, y, x + 2, y + height, dim(accent, 2, 3));
        context.fill(x + 2, y, x + width, y + 1, ElarionCivicColors.GOLD_SHADOW);
        context.fill(x + 2, y + height - 1, x + width, y + height, ElarionCivicColors.MESSAGE_BODY_BOTTOM);
    }

    public static void compactActionButton(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            int height,
            String label,
            boolean hovered,
            boolean pressed,
            boolean enabled,
            Tone tone,
            ElarionUiStyle style
    ) {
        Tone safeTone = tone == null ? Tone.NORMAL : tone;
        compactActionButtonFrame(context, x, y, width, height, hovered, enabled, safeTone, style);
        String visible = ElarionUiRenderer.ellipsize(renderer, label, Math.max(1, width - 10));
        int textColor = !enabled ? style.mutedColor()
                : safeTone == Tone.DESTRUCTIVE ? ElarionCivicColors.DESTRUCTIVE_TEXT : style.textColor();
        int textX = x + Math.max(4, (width - ElarionUiTypography.width(renderer, visible)) / 2);
        int textY = y + Math.max(2, (height - ElarionUiTypography.fontHeight(renderer)) / 2) + (pressed ? 1 : 0);
        ElarionUiTypography.draw(context, renderer, visible, textX, textY, textColor, false);
    }

    public static void compactActionButtonFrame(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            boolean hovered,
            boolean enabled,
            Tone tone,
            ElarionUiStyle style
    ) {
        Tone safeTone = tone == null ? Tone.NORMAL : tone;
        int fill = actionFill(safeTone, hovered, enabled);
        int border = actionBorder(safeTone, enabled);
        ElarionUiRenderer.beveledBox(context, x, y, width, height, fill, border, style);
        if (width > 6 && height > 6) {
            context.fill(x + 3, y + 2, x + width - 3, y + 3, actionGloss(safeTone, enabled));
            context.fill(x + 3, y + height - 4, x + width - 3, y + height - 3,
                    ElarionCivicColors.BUTTON_BOTTOM_SHADE);
        }
    }

    public static void divider(DrawContext context, int x, int y, int width) {
        if (width <= 0) return;
        context.fill(x, y, x + width, y + 1, ElarionCivicColors.DIVIDER);
    }

    public static void headerOrnament(DrawContext context, int x, int centerY, boolean mirrored) {
        int direction = mirrored ? -1 : 1;
        int lineEnd = x + direction * 7;
        context.fill(Math.min(x, lineEnd), centerY, Math.max(x, lineEnd) + 1, centerY + 1,
                ElarionCivicColors.GOLD_SHADOW);
        int diamondX = x + direction * 8;
        int left = Math.min(diamondX - 2, diamondX + 3);
        int right = Math.max(diamondX - 2, diamondX + 3);
        context.fill(left + 1, centerY - 2, right - 1, centerY + 3, ElarionCivicColors.GOLD_SHADOW);
        context.fill(left, centerY, right, centerY + 1, ElarionCivicColors.GOLD_BORDER);
        context.fill(left + 2, centerY - 1, right - 2, centerY + 2, ElarionCivicColors.GOLD_HIGHLIGHT);
    }

    public static void closeButton(DrawContext context, int x, int y, int size) {
        thinBox(context, x, y, size, size, ElarionCivicColors.CARD_SURFACE, ElarionCivicColors.GOLD_BORDER);
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int arm = Math.max(3, size / 3);
        for (int offset = -arm / 2; offset <= arm / 2; offset++) {
            context.fill(centerX + offset, centerY + offset, centerX + offset + 1, centerY + offset + 1,
                    ElarionCivicColors.GOLD_HIGHLIGHT);
            context.fill(centerX - offset, centerY + offset, centerX - offset + 1, centerY + offset + 1,
                    ElarionCivicColors.GOLD_HIGHLIGHT);
        }
    }

    public static void statusChip(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            String label,
            int maxWidth,
            Tone tone,
            ElarionUiStyle style
    ) {
        String visible = ElarionUiRenderer.ellipsize(renderer, label == null ? "" : label,
                Math.max(1, Math.max(18, maxWidth) - 10));
        int width = Math.min(Math.max(18, maxWidth), Math.max(24, ElarionUiTypography.width(renderer, visible) + 10));
        Tone safeTone = tone == null ? Tone.NORMAL : tone;
        int accent = chipAccent(safeTone);
        int fill = chipFill(safeTone);
        context.fill(x, y, x + width, y + 10, fill);
        context.fill(x, y, x + 2, y + 10, accent);
        context.fill(x + 2, y, x + width, y + 1,
                safeTone == Tone.MUTED ? ElarionCivicColors.GOLD_SHADOW : dim(accent));
        int textColor = safeTone == Tone.MUTED ? style.mutedColor() : style.textColor();
        ElarionUiTypography.draw(context, renderer, visible, x + 5, y + 1, textColor, false);
    }

    private static void drawSubtleGrid(DrawContext context, int x, int y, int width, int height) {
        if (width <= 16 || height <= 16) return;
        for (int gx = x + 16; gx < x + width - 8; gx += 14) {
            context.fill(gx, y + 10, gx + 1, y + height - 10, ElarionCivicColors.ROOT_GRID);
        }
        for (int gy = y + 16; gy < y + height - 10; gy += 14) {
            context.fill(x + 8, gy, x + width - 8, gy + 1, ElarionCivicColors.ROOT_GRID);
        }
    }

    private static int actionFill(Tone tone, boolean hovered, boolean enabled) {
        if (!enabled || tone == Tone.MUTED) return ElarionCivicColors.BUTTON_DISABLED;
        return switch (tone) {
            case PRIMARY -> hovered ? ElarionCivicColors.BUTTON_PRIMARY_HOVER : ElarionCivicColors.BUTTON_PRIMARY;
            case DESTRUCTIVE -> hovered
                    ? ElarionCivicColors.BUTTON_DESTRUCTIVE_HOVER : ElarionCivicColors.BUTTON_DESTRUCTIVE;
            case INFO -> hovered ? ElarionCivicColors.INFO_BUTTON_HOVER : ElarionCivicColors.INFO_BUTTON;
            case MUTED -> ElarionCivicColors.BUTTON_DISABLED;
            case NORMAL -> hovered ? ElarionCivicColors.BUTTON_HOVER : ElarionCivicColors.BUTTON_SURFACE;
        };
    }

    private static int actionBorder(Tone tone, boolean enabled) {
        if (!enabled || tone == Tone.MUTED) return ElarionCivicColors.GOLD_SHADOW;
        return switch (tone) {
            case PRIMARY -> ElarionCivicColors.ACTIVE_GREEN;
            case DESTRUCTIVE -> ElarionCivicColors.DESTRUCTIVE_BORDER;
            case INFO -> ElarionCivicColors.INFO_BLUE;
            case MUTED -> ElarionCivicColors.GOLD_SHADOW;
            case NORMAL -> ElarionCivicColors.GOLD_BORDER;
        };
    }

    private static int actionGloss(Tone tone, boolean enabled) {
        if (!enabled || tone == Tone.MUTED) return ElarionCivicColors.BUTTON_DISABLED_GLOSS;
        return switch (tone) {
            case PRIMARY -> ElarionCivicColors.BUTTON_GREEN_GLOSS;
            case DESTRUCTIVE -> ElarionCivicColors.BUTTON_RED_GLOSS;
            case INFO -> ElarionCivicColors.BUTTON_INFO_GLOSS;
            case MUTED -> ElarionCivicColors.BUTTON_DISABLED_GLOSS;
            case NORMAL -> ElarionCivicColors.BUTTON_GOLD_GLOSS;
        };
    }

    private static int chipAccent(Tone tone) {
        return switch (tone) {
            case PRIMARY -> ElarionCivicColors.ACTIVE_GREEN;
            case DESTRUCTIVE -> ElarionCivicColors.REJECT_RED;
            case INFO -> ElarionCivicColors.INFO_BLUE;
            case MUTED -> ElarionCivicColors.MUTED_BORDER;
            case NORMAL -> ElarionCivicColors.GOLD_BORDER;
        };
    }

    private static int chipFill(Tone tone) {
        return switch (tone) {
            case PRIMARY -> 0xCC142017;
            case DESTRUCTIVE -> 0xCC2A1412;
            case INFO -> 0xCC121D2C;
            case MUTED -> 0x8815100B;
            case NORMAL -> 0xCC24190D;
        };
    }

    private static int dim(int color) {
        return dim(color, 2, 3);
    }

    public static int dim(int color, int numerator, int denominator) {
        int safeDenominator = Math.max(1, denominator);
        return 0xFF000000 | (((color >> 16) & 0xFF) * numerator / safeDenominator) << 16
                | (((color >> 8) & 0xFF) * numerator / safeDenominator) << 8
                | ((color & 0xFF) * numerator / safeDenominator);
    }
}
