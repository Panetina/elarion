package panetina.elarion.core.client.ui;

public final class ElarionSectionHeaderLayout {
    private ElarionSectionHeaderLayout() {
    }

    public static CenteredIconHeader centeredIconHeader(
            int x,
            int y,
            int width,
            int height,
            int iconInsetX,
            int iconYOffset,
            int iconSize,
            int titleYOffset,
            int dividerInsetX,
            int dividerYOffset
    ) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safeIconSize = Math.max(1, iconSize);
        int safeDividerInset = Math.max(0, dividerInsetX);
        int dividerWidth = Math.max(1, safeWidth - safeDividerInset * 2);
        return new CenteredIconHeader(
                new ElarionSemanticRowLayout.Rect(x, y, safeWidth, safeHeight),
                new ElarionSemanticRowLayout.Rect(
                        x + Math.max(0, iconInsetX),
                        y + Math.max(0, iconYOffset),
                        safeIconSize,
                        safeIconSize
                ),
                x + safeWidth / 2,
                y + Math.max(0, titleYOffset),
                new ElarionSemanticRowLayout.Rect(
                        x + safeDividerInset,
                        y + Math.max(0, dividerYOffset),
                        dividerWidth,
                        1
                )
        );
    }

    public record CenteredIconHeader(
            ElarionSemanticRowLayout.Rect bounds,
            ElarionSemanticRowLayout.Rect icon,
            int titleCenterX,
            int titleY,
            ElarionSemanticRowLayout.Rect divider
    ) {
    }
}
