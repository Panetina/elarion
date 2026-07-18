package panetina.elarion.core.client.ui;

public final class ElarionDetailCardLayout {
    private ElarionDetailCardLayout() {
    }

    public static IdentityHeader identityHeader(
            int x,
            int y,
            int width,
            int iconSize,
            int iconTextGap,
            int titleYOffset,
            int tagYOffset,
            int subtitleYOffset
    ) {
        int safeWidth = Math.max(1, width);
        int safeIconSize = Math.max(1, iconSize);
        int safeGap = Math.max(0, iconTextGap);
        int titleX = x + safeIconSize + safeGap;
        int titleWidth = Math.max(1, x + safeWidth - titleX);
        return new IdentityHeader(
                new ElarionSemanticRowLayout.Rect(x, y, safeWidth, Math.max(safeIconSize, subtitleYOffset + 10)),
                new ElarionSemanticRowLayout.Rect(x, y, safeIconSize, safeIconSize),
                titleX,
                y + titleYOffset,
                titleWidth,
                y + tagYOffset,
                y + subtitleYOffset
        );
    }

    public record IdentityHeader(
            ElarionSemanticRowLayout.Rect bounds,
            ElarionSemanticRowLayout.Rect icon,
            int textX,
            int titleY,
            int textWidth,
            int tagY,
            int subtitleY
    ) {
    }
}
