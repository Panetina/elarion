package panetina.elarion.addons.government.api;

import panetina.elarion.addons.government.model.GovernmentGateStatus;
import panetina.elarion.addons.government.service.GovernmentDefinitionService;
import panetina.elarion.addons.government.service.GovernmentStateService;

import java.util.Set;
import java.util.UUID;

public final class ElarionGovernmentApi {
    private static ElarionGovernmentApi instance;
    private final GovernmentDefinitionService definitions;
    private final GovernmentStateService states;

    public ElarionGovernmentApi(GovernmentDefinitionService definitions, GovernmentStateService states) {
        this.definitions = definitions;
        this.states = states;
        instance = this;
    }

    public static ElarionGovernmentApi get() {
        if (instance == null) throw new IllegalStateException("Elarion Government has not initialized yet");
        return instance;
    }

    public GovernmentDefinitionService definitions() {
        return definitions;
    }

    public GovernmentStateService states() {
        return states;
    }

    public boolean isAuthority(String realmId, UUID citizenId) {
        return states.isAuthority(realmId, citizenId);
    }

    public Set<UUID> authorityHolders(String realmId) {
        return states.authorityHolders(realmId);
    }

    public GovernmentGateStatus gates(String realmId) {
        return states.gates(realmId);
    }
}
