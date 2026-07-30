package panetina.elarion.addons.portals.storage;

import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalReturnEntitlement;
import panetina.elarion.addons.portals.model.PortalRouteState;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PortalState {
    public final Map<String, PortalRouteState> routes = new LinkedHashMap<>();
    public final Map<String, PortalReturnEntitlement> entitlements = new LinkedHashMap<>();
    public final Map<String, PortalFreePassageState> freePassages = new LinkedHashMap<>();

    public PortalState copy() {
        PortalState copy = new PortalState();
        if (routes != null) {
            routes.forEach((id, route) -> {
                if (validKey(id) && route != null) {
                    PortalRouteState routeCopy = route.copy();
                    if (!validEndpoint(routeCopy.source)) routeCopy.source = null;
                    if (!validEndpoint(routeCopy.returnEndpoint)) routeCopy.returnEndpoint = null;
                    copy.routes.put(id, routeCopy);
                }
            });
        }
        if (entitlements != null) {
            entitlements.forEach((id, entitlement) -> {
                if (validKey(id) && entitlement != null && entitlement.playerId() != null
                        && validKey(entitlement.routeId())) {
                    copy.entitlements.put(id, entitlement);
                }
            });
        }
        if (freePassages != null) {
            freePassages.forEach((id, passage) -> {
                if (validKey(id) && passage != null) {
                    copy.freePassages.put(id, passage);
                }
            });
        }
        return copy;
    }

    private static boolean validKey(String key) {
        return key != null && !key.isBlank();
    }

    private static boolean validEndpoint(PortalEndpoint endpoint) {
        return endpoint == null || (validKey(endpoint.worldId())
                && endpoint.bounds() != null && endpoint.bounds().axis() != null);
    }
}
