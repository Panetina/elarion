package panetina.elarion.addons.portals.api;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.portals.model.PortalReturnEntitlement;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalRouteSnapshot;
import panetina.elarion.addons.portals.model.PortalRouteStateEvent;
import panetina.elarion.addons.portals.model.PortalTravelDirection;
import panetina.elarion.addons.portals.service.PortalDefinitionService;
import panetina.elarion.addons.portals.service.PortalRouteService;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class ElarionPortalsApi {
    private static ElarionPortalsApi instance;
    private final PortalDefinitionService definitions;
    private final PortalRouteService routes;

    public ElarionPortalsApi(PortalDefinitionService definitions, PortalRouteService routes) {
        if (instance != null) throw new IllegalStateException("ElarionPortalsApi is already initialized");
        this.definitions = definitions;
        this.routes = routes;
        instance = this;
    }

    public static ElarionPortalsApi get() {
        if (instance == null) throw new IllegalStateException("Elarion Portals has not initialized yet");
        return instance;
    }

    public Collection<PortalRouteDefinition> definitions() {
        return definitions.all();
    }

    public Collection<PortalRouteSnapshot> routes() {
        return routes.snapshots();
    }

    public PortalRouteSnapshot route(String routeId) {
        return routes.snapshot(routeId);
    }

    public PortalRouteService.TravelResult travel(
            ServerPlayerEntity player, String routeId, PortalTravelDirection direction
    ) {
        return routes.travel(player, routeId, direction);
    }

    public Optional<PortalReturnEntitlement> entitlement(UUID playerId, String routeId) {
        return routes.entitlement(playerId, routeId);
    }

    public AutoCloseable onRouteStateChanged(Consumer<PortalRouteStateEvent> listener) {
        return routes.onRouteStateChanged(listener);
    }
}
