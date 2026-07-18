package panetina.elarion.core.client.ui;

public final class ElarionSplitSummaryLayout {
    private ElarionSplitSummaryLayout() {
    }

    public static Split split(
            int x,
            int dividerY,
            int dividerWidth,
            int summaryY,
            int rightX
    ) {
        int safeWidth = Math.max(1, dividerWidth);
        return new Split(
                new ElarionSemanticRowLayout.Rect(x, dividerY, safeWidth, 1),
                x,
                summaryY,
                Math.max(x, rightX),
                summaryY
        );
    }

    public record Split(
            ElarionSemanticRowLayout.Rect divider,
            int leftX,
            int leftY,
            int rightX,
            int rightY
    ) {
    }
}
