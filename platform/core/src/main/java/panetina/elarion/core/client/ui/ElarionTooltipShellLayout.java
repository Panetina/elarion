package panetina.elarion.core.client.ui;

public final class ElarionTooltipShellLayout {
    private ElarionTooltipShellLayout() {
    }

    public static Tooltip tooltip(
            int mouseX,
            int mouseY,
            int screenWidth,
            int screenHeight,
            int contentWidth,
            int contentHeight,
            int padding,
            int offset
    ) {
        int safePadding = Math.max(0, padding);
        int safeOffset = Math.max(0, offset);
        int width = Math.max(1, contentWidth) + safePadding * 2;
        int height = Math.max(1, contentHeight) + safePadding * 2;
        int x = mouseX + safeOffset;
        int y = mouseY + safeOffset;
        if (x + width > screenWidth) x = mouseX - safeOffset - width;
        if (y + height > screenHeight) y = mouseY - safeOffset - height;
        x = clamp(x, 0, Math.max(0, screenWidth - width));
        y = clamp(y, 0, Math.max(0, screenHeight - height));
        return new Tooltip(
                new ElarionSemanticRowLayout.Rect(x, y, width, height),
                new ElarionSemanticRowLayout.Rect(x + safePadding, y + safePadding,
                        Math.max(1, width - safePadding * 2), Math.max(1, height - safePadding * 2))
        );
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public record Tooltip(
            ElarionSemanticRowLayout.Rect shell,
            ElarionSemanticRowLayout.Rect content
    ) {
    }
}
