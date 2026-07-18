package panetina.elarion.core.client.ui;

import net.minecraft.client.font.TextRenderer;

public final class ElarionInputFieldLayout {
    private ElarionInputFieldLayout() {
    }

    public static SingleLine singleLine(
            int x,
            int y,
            int width,
            int height,
            int insetX,
            int iconSize,
            int iconGap,
            boolean hasIcon
    ) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safeInset = Math.max(0, insetX);
        int safeIconSize = Math.max(1, iconSize);
        int safeIconGap = Math.max(0, iconGap);
        ElarionSemanticRowLayout.Rect icon = hasIcon
                ? new ElarionSemanticRowLayout.Rect(
                x + safeInset,
                y + Math.max(0, (safeHeight - safeIconSize) / 2),
                safeIconSize,
                safeIconSize
        )
                : new ElarionSemanticRowLayout.Rect(x + safeInset, y, 0, 0);
        int textX = hasIcon ? icon.x() + icon.width() + safeIconGap : x + safeInset;
        int rightInset = Math.max(1, safeInset + 2);
        int textMaxWidth = Math.max(1, x + safeWidth - rightInset - textX);
        return new SingleLine(
                new ElarionSemanticRowLayout.Rect(x, y, safeWidth, safeHeight),
                icon,
                textX,
                textMaxWidth,
                x + safeWidth - rightInset
        );
    }

    public record SingleLine(
            ElarionSemanticRowLayout.Rect bounds,
            ElarionSemanticRowLayout.Rect icon,
            int textX,
            int textMaxWidth,
            int caretMaxX
    ) {
        public int textY(TextRenderer renderer) {
            return ElarionCivicUi.centeredTextY(renderer, bounds.y(), bounds.height());
        }

        public int caretX(TextRenderer renderer, String visibleText) {
            return Math.min(caretMaxX, textX + ElarionUiTypography.width(renderer, visibleText) + 1);
        }
    }
}
