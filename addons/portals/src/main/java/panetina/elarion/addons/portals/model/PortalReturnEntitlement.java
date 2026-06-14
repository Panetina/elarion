package panetina.elarion.addons.portals.model;

import java.util.UUID;

public record PortalReturnEntitlement(UUID playerId, String routeId, long createdAt, long sourceWindowStart) {
}
