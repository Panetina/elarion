package panetina.elarion.addons.angling;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.angling.resource.FishDefinitionRepository;
import panetina.elarion.addons.angling.resource.FishDefinitionResourceReloadListener;
import panetina.elarion.addons.angling.condition.AnglingConditionRegistry;
import panetina.elarion.addons.angling.integration.VanillaFishingHooks;
import panetina.elarion.addons.angling.model.AnglingConditionId;
import panetina.elarion.addons.angling.service.AnglingCatchResolutionService;
import panetina.elarion.addons.angling.service.AnglingFeedbackService;
import panetina.elarion.addons.angling.service.AnglingFishingTriggerService;
import panetina.elarion.addons.angling.service.FishCandidateSelector;
import panetina.elarion.addons.angling.service.AnglingFishingSessionService;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionAnglingAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_angling");
    private static final FishDefinitionRepository FISH_DEFINITIONS = new FishDefinitionRepository();
    private static final AnglingConditionRegistry CONDITIONS = new AnglingConditionRegistry();
    private static AnglingCatchResolutionService catchResolution;
    private static FishCandidateSelector candidateSelector;
    private static AnglingFishingSessionService sessions;
    private static AnglingFeedbackService feedback;

    @Override
    public void initialize(ElarionApi api) {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new FishDefinitionResourceReloadListener(FISH_DEFINITIONS));
        CONDITIONS.register(
                AnglingConditionId.of("placeholder_condition_001"),
                (definition, context) -> true);
        candidateSelector = new FishCandidateSelector(FISH_DEFINITIONS, CONDITIONS);
        catchResolution = new AnglingCatchResolutionService(FISH_DEFINITIONS, api.system().events());
        sessions = new AnglingFishingSessionService(candidateSelector, catchResolution);
        feedback = new AnglingFeedbackService();
        VanillaFishingHooks.initialize(new AnglingFishingTriggerService(sessions), feedback);
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof FishingBobberEntity bobber) {
                VanillaFishingHooks.onBobberUnloaded(bobber);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> sessions.expireDue(
                System.currentTimeMillis(),
                AnglingFishingSessionService.DEFAULT_EXPIRATIONS_PER_TICK));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            sessions.cancel(handler.getPlayer().getUuid());
            feedback.clear(handler.getPlayer().getUuid());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            sessions.clear();
            feedback.clear();
        });
        LOGGER.info("Elarion Angling definitions and vanilla fishing telemetry trigger initialized");
    }
}
