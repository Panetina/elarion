package panetina.elarion.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.server.MinecraftServer;
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
import panetina.elarion.core.service.IdentitySyncService;
import panetina.elarion.core.service.NicknameService;
import panetina.elarion.core.service.HistoryService;
import panetina.elarion.core.service.RewardActionService;
import panetina.elarion.core.service.TitleService;
import panetina.elarion.core.storage.CitizenStorage;
import panetina.elarion.core.storage.HistoryStorage;
import panetina.elarion.core.network.IdentitySyncRequestPayload;
import panetina.elarion.core.network.IdentitySyncPayload;

public final class ElarionCoreMod implements ModInitializer {
    public static final String MOD_ID = "elarion_core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(IdentitySyncPayload.ID, IdentitySyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(IdentitySyncRequestPayload.ID, IdentitySyncRequestPayload.CODEC);

        CoreConfigManager config = new CoreConfigManager(LOGGER);
        config.load();

        ElarionEventBus events = new ElarionEventBus();
        CitizenService citizens = new CitizenService(new CitizenStorage(LOGGER), config, events);
        CommunityService communities = new CommunityService(config, citizens);
        TitleService titles = new TitleService(config, citizens);
        AbilityService abilities = new AbilityService(titles);
        config.titles().values().forEach(title -> title.abilities().forEach(abilities::register));
        IdentityService identities = new IdentityService(citizens, communities, titles);
        IdentitySyncService identitySync = new IdentitySyncService(citizens, communities, titles, identities);
        NicknameService nicknames = new NicknameService(config, citizens);
        HistoryService history = new HistoryService(new HistoryStorage(LOGGER), events);
        ServerPlayNetworking.registerGlobalReceiver(IdentitySyncRequestPayload.ID, (payload, context) -> {
            if (payload.requested()) {
                context.server().execute(() -> identitySync.syncAll(context.server()));
            }
        });
        ChatService chat = new ChatService(config, citizens, communities, identities);
        RewardActionService rewards = new RewardActionService(config, citizens, titles, abilities, events);
        ElarionCommandRegistry commands = new ElarionCommandRegistry();
        ElarionApi api = new ElarionApi(
                citizens, communities, titles, abilities, identities, identitySync, nicknames, history,
                chat, rewards, events, commands);

        initializeAddons(api);
        events.onCitizenChanged(event -> {
            MinecraftServer server = citizens.server();
            if (server != null) identitySync.syncAll(server);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            citizens.bind(server);
            history.bind(server);
            communities.initializeScoreboardTeams(server);
            LOGGER.info("Elarion Core bound to server {}", server.getServerMotd());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            citizens.getOrCreate(handler.getPlayer());
            communities.applyCurrentScoreboardTeam(handler.getPlayer());
            identitySync.syncAll(server);
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
