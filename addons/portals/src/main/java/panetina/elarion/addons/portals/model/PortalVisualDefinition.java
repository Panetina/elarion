package panetina.elarion.addons.portals.model;

public record PortalVisualDefinition(
        int rgb,
        float brightness,
        float opacity,
        int frameTime,
        String texture,
        String iconItem,
        int promptAccentColor
) {
    public PortalVisualDefinition {
        texture = texture == null ? "" : texture;
        iconItem = iconItem == null ? "" : iconItem;
    }

    public int argb() {
        int alpha = Math.max(0, Math.min(255, Math.round(opacity * 255.0F)));
        int red = Math.max(0, Math.min(255, Math.round(((rgb >> 16) & 0xFF) * brightness)));
        int green = Math.max(0, Math.min(255, Math.round(((rgb >> 8) & 0xFF) * brightness)));
        int blue = Math.max(0, Math.min(255, Math.round((rgb & 0xFF) * brightness)));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }
}
