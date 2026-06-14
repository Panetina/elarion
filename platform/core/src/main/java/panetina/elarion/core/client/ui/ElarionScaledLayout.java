package panetina.elarion.core.client.ui;

public record ElarionScaledLayout(
        int logicalWidth, int logicalHeight, float scale, int screenX, int screenY, boolean belowPreferredScale
) {
    public static ElarionScaledLayout fit(
            int screenWidth, int screenHeight, int logicalWidth, int logicalHeight, int margin, int minimumScalePercent
    ) {
        int safeWidth = Math.max(1, screenWidth - margin * 2);
        int safeHeight = Math.max(1, screenHeight - margin * 2);
        float scale = Math.min(1.0F, Math.min(
                safeWidth / (float) Math.max(1, logicalWidth),
                safeHeight / (float) Math.max(1, logicalHeight)));
        float preferredMinimum = Math.max(0.25F, Math.min(1.0F, minimumScalePercent / 100.0F));
        int renderedWidth = Math.round(logicalWidth * scale);
        int renderedHeight = Math.round(logicalHeight * scale);
        return new ElarionScaledLayout(logicalWidth, logicalHeight, scale,
                (screenWidth - renderedWidth) / 2, (screenHeight - renderedHeight) / 2,
                scale < preferredMinimum);
    }

    public double logicalX(double screenX) { return (screenX - this.screenX) / scale; }
    public double logicalY(double screenY) { return (screenY - this.screenY) / scale; }
}
