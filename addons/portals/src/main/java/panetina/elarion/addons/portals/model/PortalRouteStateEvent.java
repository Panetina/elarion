package panetina.elarion.addons.portals.model;

import java.time.Instant;

public record PortalRouteStateEvent(
        String routeId,
        String displayName,
        Type type,
        Instant occurredAt,
        PortalRouteSnapshot snapshot
) {
    public enum Type {
        OPENED,
        CLOSED
    }
}
