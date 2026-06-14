package panetina.elarion.addons.offerings.service;

import panetina.elarion.addons.offerings.config.OfferingConfigLoader;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingUiConfig;
import panetina.elarion.core.api.ElarionApi;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class OfferingDefinitionService {
    private final ElarionApi api;
    private Map<String, OfferingProjectDefinition> definitions = Map.of();
    private OfferingUiConfig ui = OfferingUiConfig.defaults();

    public OfferingDefinitionService(ElarionApi api) {
        this.api = api;
    }

    public void load() {
        definitions = OfferingConfigLoader.load(api);
        ui = OfferingConfigLoader.loadUi(api);
    }

    public Collection<OfferingProjectDefinition> all() {
        return definitions.values();
    }

    public Optional<OfferingProjectDefinition> find(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public OfferingUiConfig ui() {
        return ui;
    }
}
