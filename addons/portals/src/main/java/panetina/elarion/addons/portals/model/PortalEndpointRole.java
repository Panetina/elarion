package panetina.elarion.addons.portals.model;

public enum PortalEndpointRole {
    SOURCE,
    RETURN;

    public static PortalEndpointRole parse(String value) {
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "source" -> SOURCE;
            case "destination", "return" -> RETURN;
            default -> throw new IllegalArgumentException("Endpoint role must be source or destination.");
        };
    }
}
