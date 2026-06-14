package panetina.elarion.addons.portals.model;

public record PortalRouteDefinition(
        String id,
        String displayName,
        String description,
        String sourceDimension,
        String destinationDimension,
        boolean enabled,
        PortalRouteMode mode,
        String ticketId,
        String ticketName,
        String ticketLore,
        String ticketPriceKey,
        String passagePriceKey,
        boolean firstRoundTripFree,
        PortalScheduleDefinition schedule,
        PortalVisualDefinition visual
) {
}
