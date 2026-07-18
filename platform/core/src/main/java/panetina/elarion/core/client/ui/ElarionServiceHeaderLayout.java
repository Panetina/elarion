package panetina.elarion.core.client.ui;

public final class ElarionServiceHeaderLayout {
    private ElarionServiceHeaderLayout() {
    }

    public static PortraitTitle portraitTitle(
            int x,
            int y,
            int width,
            int height,
            int padding,
            int portraitYOffset,
            int portraitSize,
            int titleOffsetX,
            int titleYOffset,
            int subtitleYOffset,
            int closeSize,
            int badgeWidth,
            int badgeRightGap,
            int badgeYOffset
    ) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safePadding = Math.max(0, padding);
        int safeCloseSize = Math.max(1, closeSize);
        int safePortraitSize = Math.max(1, Math.min(portraitSize, safeHeight));
        int closeX = x + Math.max(0, safeWidth - safePadding - safeCloseSize);
        int closeY = y + safePadding;
        int titleX = x + Math.max(0, titleOffsetX);
        int badgeX = closeX - Math.max(0, badgeRightGap) - Math.max(1, badgeWidth);
        int titleRight = Math.max(titleX + 1, Math.min(badgeX - 8, closeX - 8));
        return new PortraitTitle(
                new ElarionSemanticRowLayout.Rect(x, y, safeWidth, safeHeight),
                new ElarionSemanticRowLayout.Rect(x + safePadding, y + Math.max(0, portraitYOffset),
                        safePortraitSize, safePortraitSize),
                titleX,
                y + Math.max(0, titleYOffset),
                Math.max(1, titleRight - titleX),
                y + Math.max(0, subtitleYOffset),
                new ElarionSemanticRowLayout.Rect(closeX, closeY, safeCloseSize, safeCloseSize),
                new ElarionSemanticRowLayout.Rect(badgeX, y + Math.max(0, badgeYOffset),
                        Math.max(1, badgeWidth), 20)
        );
    }

    public record PortraitTitle(
            ElarionSemanticRowLayout.Rect bounds,
            ElarionSemanticRowLayout.Rect portrait,
            int titleX,
            int titleY,
            int titleMaxWidth,
            int subtitleY,
            ElarionSemanticRowLayout.Rect close,
            ElarionSemanticRowLayout.Rect badge
    ) {
    }
}
