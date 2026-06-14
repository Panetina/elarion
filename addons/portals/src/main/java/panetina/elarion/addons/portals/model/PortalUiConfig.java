package panetina.elarion.addons.portals.model;

public record PortalUiConfig(
        String themeVariant,
        int logicalWidth,
        int logicalHeight,
        int minimumScalePercent,
        int confirmButtonWidth,
        int closeButtonWidth
) {
    public static PortalUiConfig defaults() {
        return new PortalUiConfig("default", 340, 190, 50, 104, 104);
    }
}
