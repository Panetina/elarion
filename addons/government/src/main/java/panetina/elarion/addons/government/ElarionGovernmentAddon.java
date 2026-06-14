package panetina.elarion.addons.government;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.government.api.ElarionGovernmentApi;
import panetina.elarion.addons.government.command.GovernmentCommands;
import panetina.elarion.addons.government.network.GovernmentUiActionPayload;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.addons.government.service.GovernmentDefinitionService;
import panetina.elarion.addons.government.service.GovernmentStateService;
import panetina.elarion.addons.government.storage.GovernmentStorage;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionGovernmentAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_government");

    @Override
    public void initialize(ElarionApi api) {
        PayloadTypeRegistry.playS2C().register(GovernmentUiOpenPayload.ID, GovernmentUiOpenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GovernmentUiActionPayload.ID, GovernmentUiActionPayload.CODEC);
        GovernmentBlocks.register();
        api.system().abilities().register("elarion.government.manage");
        GovernmentDefinitionService definitions = new GovernmentDefinitionService(api);
        definitions.load();
        GovernmentStateService states = new GovernmentStateService(api, definitions, new GovernmentStorage(LOGGER));
        new ElarionGovernmentApi(definitions, states);
        ServerLifecycleEvents.SERVER_STARTED.register(states::bind);
        ServerTickEvents.END_SERVER_TICK.register(server -> states.tick());
        api.system().commands().registerAdminSubcommand(() -> GovernmentCommands.create(api, definitions, states));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                GovernmentCommands.registerLeaderChat(dispatcher, api, states));
        ServerPlayNetworking.registerGlobalReceiver(GovernmentUiActionPayload.ID, (payload, context) ->
                context.server().execute(() ->
                        GovernmentBlockInteractions.handleAction(api, definitions, states, context.player(), payload)));
        GovernmentBlockInteractions.register(api, definitions, states);
        LOGGER.info("Elarion Government addon initialized with {} form definitions", definitions.forms().size());
    }
}
