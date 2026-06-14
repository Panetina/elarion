package panetina.elarion.addons.portals.model;

public enum PortalArrivalRole {
    OUTBOUND,
    RETURN;

    public static PortalArrivalRole parse(String value) {
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "destination", "outbound" -> OUTBOUND;
            case "source", "return" -> RETURN;
            default -> throw new IllegalArgumentException("Arrival role must be destination or source.");
        };
    }
}
