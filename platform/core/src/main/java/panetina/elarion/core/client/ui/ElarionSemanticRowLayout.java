package panetina.elarion.core.client.ui;

import net.minecraft.client.font.TextRenderer;

public final class ElarionSemanticRowLayout {
    private static final int PRICE_VALUE_GAP = 4;

    private ElarionSemanticRowLayout() {
    }

    public static CompactItemPriceRow compactItemPriceRow(
            int x,
            int y,
            int width,
            int height,
            int iconInsetX,
            int iconSize,
            int titleOffsetX,
            int metaOffsetX,
            int priceIconOffsetX,
            int priceIconSize
    ) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safeIconSize = Math.max(1, iconSize);
        int safePriceIconSize = Math.max(1, priceIconSize);
        int iconX = x + Math.max(0, iconInsetX);
        int iconY = centeredContentY(y, safeHeight, safeIconSize);
        int priceIconX = x + Math.max(0, priceIconOffsetX);
        int priceIconY = centeredContentY(y, safeHeight, safePriceIconSize);
        return new CompactItemPriceRow(
                new Rect(x, y, safeWidth, safeHeight),
                new Rect(iconX, iconY, safeIconSize, safeIconSize),
                x + Math.max(0, titleOffsetX),
                x + Math.max(0, metaOffsetX),
                new Rect(priceIconX, priceIconY, safePriceIconSize, safePriceIconSize),
                priceIconX + safePriceIconSize + PRICE_VALUE_GAP
        );
    }

    public static CompactRecordRow compactRecordRow(
            int x,
            int y,
            int width,
            int height,
            int iconInsetX,
            int iconSize,
            int titleOffsetX,
            int metricFromRight
    ) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safeIconSize = Math.max(1, iconSize);
        int iconX = x + Math.max(0, iconInsetX);
        int iconY = y + Math.max(4, (safeHeight - safeIconSize) / 2);
        int titleX = x + Math.max(0, titleOffsetX);
        int metricX = x + safeWidth - Math.max(1, metricFromRight);
        int titleMaxWidth = Math.max(72, metricX - titleX - 10);
        int right = x + safeWidth - 7;
        int textBlockTop = y + Math.max(3, (safeHeight - 22) / 2);
        int titleY = textBlockTop;
        int lowerY = Math.min(y + safeHeight - 10, textBlockTop + 15);
        return new CompactRecordRow(
                new Rect(x, y, safeWidth, safeHeight),
                new Rect(iconX, iconY, safeIconSize, safeIconSize),
                titleX,
                titleY,
                titleMaxWidth,
                lowerY,
                metricX,
                titleY,
                lowerY,
                right
        );
    }

    private static int centeredContentY(int y, int height, int contentHeight) {
        return y + Math.max(0, (height - contentHeight) / 2);
    }

    public record CompactItemPriceRow(
            Rect row,
            Rect icon,
            int titleX,
            int metaX,
            Rect priceIcon,
            int priceValueX
    ) {
        public int textY(TextRenderer renderer) {
            return ElarionCivicUi.centeredTextY(renderer, row.y(), row.height());
        }
    }

    public record CompactRecordRow(
            Rect row,
            Rect icon,
            int titleX,
            int titleY,
            int titleMaxWidth,
            int tagY,
            int metricX,
            int metricY,
            int secondaryMetricY,
            int metricRight
    ) {
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean contains(double px, double py) {
            return px >= x && py >= y && px < x + width && py < y + height;
        }
    }
}
