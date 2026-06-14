package panetina.elarion.addons.portals.storage;

import panetina.elarion.addons.portals.model.PortalReturnEntitlement;
import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalRouteState;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PortalState {
    public final Map<String, PortalRouteState> routes = new LinkedHashMap<>();
    public final Map<String, PortalReturnEntitlement> entitlements = new LinkedHashMap<>();
    public final Map<String, PortalFreePassageState> freePassages = new LinkedHashMap<>();

    public PortalState copy() {
        PortalState copy = new PortalState();
        routes.forEach((id, route) -> copy.routes.put(id, route.copy()));
        copy.entitlements.putAll(entitlements);
        copy.freePassages.putAll(freePassages);
        return copy;
    }
}
