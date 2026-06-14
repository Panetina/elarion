package panetina.elarion.addons.portals.service;

import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.portals.config.PortalConfigLoader;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;
import panetina.elarion.addons.portals.model.PortalUiConfig;
import panetina.elarion.core.api.ElarionApi;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class PortalDefinitionService {
    private final ElarionApi api;
    private volatile Map<String, PortalRouteDefinition> routes = Map.of();
    private volatile PortalUiConfig ui = PortalUiConfig.defaults();

    public PortalDefinitionService(ElarionApi api) {
        this.api = api;
    }

    public void load() {
        PortalConfigLoader.Loaded loaded = PortalConfigLoader.load(api);
        loaded.routes().values().forEach(route -> {
            if (route.mode().requiresTicket()) {
                ElarionEconomyApi.get().pricing().definition(route.ticketPriceKey());
            }
            if (route.mode().chargesPassage()) {
                ElarionEconomyApi.get().pricing().definition(route.passagePriceKey());
            }
        });
        routes = loaded.routes();
        ui = loaded.ui();
    }

    public Collection<PortalRouteDefinition> all() {
        return routes.values();
    }

    public Optional<PortalRouteDefinition> find(String id) {
        return Optional.ofNullable(routes.get(id));
    }

    public PortalRouteDefinition require(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown portal route " + id));
    }

    public PortalUiConfig ui() {
        return ui;
    }
}
