package panetina.elarion.addons.portals.model;

import java.time.Instant;

public record PortalRouteSnapshot(
        String routeId,
        String displayName,
        PortalRouteMode mode,
        boolean unlocked,
        boolean complete,
        boolean active,
        Instant windowStart,
        Instant windowEnd,
        PortalEndpoint source,
        PortalEndpoint returnEndpoint,
        PortalVisualDefinition visual
) {
}
