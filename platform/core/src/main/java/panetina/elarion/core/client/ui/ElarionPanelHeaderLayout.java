package panetina.elarion.core.client.ui;

public final class ElarionPanelHeaderLayout {
    private ElarionPanelHeaderLayout() {
    }

    public static LeftTitle leftTitle(
            int x,
            int y,
            int width,
            int height,
            int headerHeight,
            int titleInsetX,
            int titleYOffset,
            int dividerInsetX,
            int dividerYOffset
    ) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safeHeaderHeight = Math.max(1, Math.min(safeHeight, headerHeight));
        int safeTitleInset = Math.max(0, titleInsetX);
        int titleX = x + safeTitleInset;
        int titleMaxWidth = Math.max(1, safeWidth - safeTitleInset * 2);
        int safeDividerInset = Math.max(0, dividerInsetX);
        int dividerWidth = Math.max(1, safeWidth - safeDividerInset * 2);
        return new LeftTitle(
                new ElarionSemanticRowLayout.Rect(x, y, safeWidth, safeHeight),
                safeHeaderHeight,
                titleX,
                y + Math.max(0, titleYOffset),
                titleMaxWidth,
                new ElarionSemanticRowLayout.Rect(
                        x + safeDividerInset,
                        y + Math.max(0, dividerYOffset),
                        dividerWidth,
                        1
                ),
                y + safeHeaderHeight
        );
    }

    public record LeftTitle(
            ElarionSemanticRowLayout.Rect bounds,
            int headerHeight,
            int titleX,
            int titleY,
            int titleMaxWidth,
            ElarionSemanticRowLayout.Rect divider,
            int bodyY
    ) {
    }
}
