package panetina.elarion.core.api;

import panetina.elarion.core.service.CitizenService;
import panetina.elarion.core.service.RealmDeliveryService;
import panetina.elarion.core.service.RealmGovernanceService;
import panetina.elarion.core.service.RealmService;
import panetina.elarion.core.service.RealmSpawnService;

public final class ElarionRealmApi {
    private final CitizenService citizens;
    private final RealmService realms;
    private final RealmGovernanceService governance;
    private final RealmSpawnService spawns;
    private final RealmDeliveryService deliveries;

    ElarionRealmApi(
            CitizenService citizens,
            RealmService realms,
            RealmGovernanceService governance,
            RealmSpawnService spawns,
            RealmDeliveryService deliveries
    ) {
        this.citizens = citizens;
        this.realms = realms;
        this.governance = governance;
        this.spawns = spawns;
        this.deliveries = deliveries;
    }

    public CitizenService citizens() {
        return citizens;
    }

    public RealmService realms() {
        return realms;
    }

    public RealmGovernanceService governance() {
        return governance;
    }

    public RealmSpawnService spawns() {
        return spawns;
    }

    public RealmDeliveryService deliveries() {
        return deliveries;
    }
}
