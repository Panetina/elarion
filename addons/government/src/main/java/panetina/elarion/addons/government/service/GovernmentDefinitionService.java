package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.config.GovernmentConfigLoader;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentSettings;
import panetina.elarion.core.api.ElarionApi;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class GovernmentDefinitionService {
    private final ElarionApi api;
    private Map<String, GovernmentFormDefinition> forms = Map.of();
    private GovernmentSettings settings = GovernmentSettings.defaults();

    public GovernmentDefinitionService(ElarionApi api) {
        this.api = api;
    }

    public void load() {
        settings = GovernmentConfigLoader.loadSettings();
        forms = GovernmentConfigLoader.load(api);
    }

    public Collection<GovernmentFormDefinition> forms() {
        return forms.values();
    }

    public Optional<GovernmentFormDefinition> form(String id) {
        return Optional.ofNullable(forms.get(id));
    }

    public GovernmentFormDefinition require(String id) {
        GovernmentFormDefinition form = forms.get(id);
        if (form == null) throw new IllegalArgumentException("Unknown government form " + id);
        return form;
    }

    public GovernmentSettings settings() {
        return settings;
    }
}
