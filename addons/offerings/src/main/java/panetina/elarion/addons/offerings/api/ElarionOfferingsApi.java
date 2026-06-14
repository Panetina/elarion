package panetina.elarion.addons.offerings.api;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.offerings.model.OfferingAnchor;
import panetina.elarion.addons.offerings.model.OfferingContributionResult;
import panetina.elarion.addons.offerings.model.OfferingInstance;
import panetina.elarion.addons.offerings.model.OfferingProgress;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.service.OfferingDefinitionService;
import panetina.elarion.addons.offerings.service.OfferingService;

import java.util.Collection;
import java.util.Optional;

public final class ElarionOfferingsApi {
    private static ElarionOfferingsApi instance;
    private final OfferingDefinitionService definitions;
    private final OfferingService service;

    public ElarionOfferingsApi(OfferingDefinitionService definitions, OfferingService service) {
        instance = this;
        this.definitions = definitions;
        this.service = service;
    }

    public static ElarionOfferingsApi get() {
        if (instance == null) throw new IllegalStateException("Elarion offerings has not initialized yet");
        return instance;
    }

    public Collection<OfferingProjectDefinition> definitions() {
        return definitions.all();
    }

    public Optional<OfferingProjectDefinition> definition(String id) {
        return definitions.find(id);
    }

    public Collection<OfferingInstance> instances() {
        return service.instances();
    }

    public Collection<OfferingAnchor> anchors() {
        return service.anchors();
    }

    public OfferingInstance startRealm(String realmId, String projectId, ServerPlayerEntity actor) {
        return service.startRealm(realmId, projectId, actor);
    }

    public OfferingInstance startGlobal(String projectId, ServerPlayerEntity actor) {
        return service.startGlobal(projectId, actor);
    }

    public OfferingInstance startLocation(String projectId, ServerPlayerEntity actor) {
        return service.startLocation(projectId, actor);
    }

    public OfferingContributionResult contributeItems(
            String instanceId,
            String itemOrTag,
            long count,
            ServerPlayerEntity actor
    ) {
        return service.contributePlayer(instanceId, "item:" + itemOrTag, count, actor);
    }

    public OfferingContributionResult contributeCurrency(
            String instanceId,
            long amount,
            ServerPlayerEntity actor
    ) {
        return service.contributePlayer(instanceId, "currency", amount, actor);
    }

    public OfferingInstance contributeEvent(String instanceId, String eventId, long amount, ServerPlayerEntity actor) {
        return service.contributeEvent(instanceId, eventId, amount, actor);
    }

    public OfferingProgress progress(String instanceId) {
        return service.progress(instanceId);
    }

    public boolean hasRealmFlag(String realmId, String flag) {
        return service.hasRealmFlag(realmId, flag);
    }
}
