package panetina.elarion.core.client.ui;

public final class ElarionProgressTrackLayout {
    private ElarionProgressTrackLayout() {
    }

    public static ProgressTrack track(int x, int y, int width, int height, float ratio) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        float clampedRatio = Math.min(1.0F, Math.max(0.0F, ratio));
        int fillWidth = Math.round(safeWidth * clampedRatio);
        int innerWidth = Math.max(0, Math.min(safeWidth - 2, fillWidth - 1));
        return new ProgressTrack(
                new ElarionSemanticRowLayout.Rect(x, y, safeWidth, safeHeight),
                new ElarionSemanticRowLayout.Rect(x, y, safeWidth, 1),
                new ElarionSemanticRowLayout.Rect(x + 1, y + 1, innerWidth, Math.max(0, safeHeight - 2))
        );
    }

    public record ProgressTrack(
            ElarionSemanticRowLayout.Rect bounds,
            ElarionSemanticRowLayout.Rect topLine,
            ElarionSemanticRowLayout.Rect fill
    ) {
        public boolean hasFill() {
            return fill.width() > 0 && fill.height() > 0;
        }
    }
}
