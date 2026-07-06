package panetina.elarion.addons.portals.client;

import panetina.elarion.addons.portals.network.PortalRouteStatusSyncPayload;

import java.util.List;
import java.util.Optional;

public final class PortalClientRouteStatus {
    private static volatile List<PortalRouteStatusSyncPayload.Entry> routes = List.of();

    private PortalClientRouteStatus() {
    }

    public static void replace(PortalRouteStatusSyncPayload payload) {
        routes = payload.routes();
    }

    public static void clear() {
        routes = List.of();
    }

    public static List<PortalRouteStatusSyncPayload.Entry> all() {
        return routes;
    }

    public static Optional<PortalRouteStatusSyncPayload.Entry> find(String routeId) {
        return routes.stream().filter(route -> route.routeId().equals(routeId)).findFirst();
    }
}
