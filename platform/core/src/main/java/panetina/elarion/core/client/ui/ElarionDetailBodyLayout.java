package panetina.elarion.core.client.ui;

public final class ElarionDetailBodyLayout {
    private ElarionDetailBodyLayout() {
    }

    public static SectionTitle sectionTitle(int x, int y, int iconSize, int iconTextGap, int iconYOffset) {
        int safeIconSize = Math.max(1, iconSize);
        int safeGap = Math.max(0, iconTextGap);
        return new SectionTitle(
                new ElarionSemanticRowLayout.Rect(x, y + iconYOffset, safeIconSize, safeIconSize),
                x + safeIconSize + safeGap,
                y
        );
    }

    public static BodyText bodyText(int x, int y, int width, int height) {
        return new BodyText(new ElarionSemanticRowLayout.Rect(
                x,
                y,
                Math.max(1, width),
                Math.max(1, height)
        ));
    }

    public static KeyValueRow keyValueRow(int x, int y, int width, int labelWidth, int gap) {
        int safeWidth = Math.max(1, width);
        int safeLabelWidth = Math.max(1, Math.min(labelWidth, safeWidth));
        int safeGap = Math.max(0, gap);
        int valueX = x + safeLabelWidth + safeGap;
        int valueWidth = Math.max(1, x + safeWidth - valueX);
        return new KeyValueRow(
                x,
                valueX,
                y,
                safeLabelWidth,
                valueWidth
        );
    }

    public record SectionTitle(ElarionSemanticRowLayout.Rect icon, int textX, int textY) {
    }

    public record BodyText(ElarionSemanticRowLayout.Rect body) {
    }

    public record KeyValueRow(int labelX, int valueX, int textY, int labelWidth, int valueWidth) {
    }
}
