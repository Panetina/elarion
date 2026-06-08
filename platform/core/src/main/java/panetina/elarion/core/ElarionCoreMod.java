package panetina.elarion.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.api.ElarionCommandRegistry;
import panetina.elarion.core.command.ElarionCommands;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.service.AbilityService;
import panetina.elarion.core.service.ChatService;
import panetina.elarion.core.service.CitizenService;
import panetina.elarion.core.service.CommunityService;
import panetina.elarion.core.service.IdentityService;
import panetina.elarion.core.service.RewardActionService;
import panetina.elarion.core.service.TitleService;
import panetina.elarion.core.storage.CitizenStorage;

public final class ElarionCoreMod implements ModInitializer {
    public static final String MOD_ID = "elarion_core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CoreConfigManager config = new CoreConfigManager(LOGGER);
        config.load();

        ElarionEventBus events = new ElarionEventBus();
        CitizenService citizens = new CitizenService(new CitizenStorage(LOGGER), config, events);
        CommunityService communities = new CommunityService(config, citizens);
        TitleService titles = new TitleService(config, citizens);
        AbilityService abilities = new AbilityService(titles);
        config.titles().values().forEach(title -> title.abilities().forEach(abilities::register));
        IdentityService identities = new IdentityService(citizens, communities, titles);
        ChatService chat = new ChatService(config, citizens, communities, identities);
        RewardActionService rewards = new RewardActionService(config, citizens, titles, abilities, events);
        ElarionCommandRegistry commands = new ElarionCommandRegistry();
        ElarionApi api = new ElarionApi(
                citizens, communities, titles, abilities, identities, chat, rewards, events, commands);

        initializeAddons(api);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            citizens.bind(server);
            communities.initializeScoreboardTeams(server);
            LOGGER.info("Elarion Core bound to server {}", server.getServerMotd());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            citizens.getOrCreate(handler.getPlayer());
            communities.applyCurrentScoreboardTeam(handler.getPlayer());
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ElarionCommands.register(dispatcher, api, config, commands));

        LOGGER.info("Elarion Core initialized");
    }

    private static void initializeAddons(ElarionApi api) {
        for (EntrypointContainer<ElarionAddon> container :
                FabricLoader.getInstance().getEntrypointContainers("elarion:addon", ElarionAddon.class)) {
            String provider = container.getProvider().getMetadata().getId();
            try {
                container.getEntrypoint().initialize(api);
                LOGGER.info("Initialized Elarion addon {}", provider);
            } catch (RuntimeException exception) {
                throw new IllegalStateException("Failed to initialize Elarion addon " + provider, exception);
            }
        }
    }
}
