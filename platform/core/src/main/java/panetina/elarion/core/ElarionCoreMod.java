package panetina.elarion.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
import panetina.elarion.core.service.RealmService;
import panetina.elarion.core.service.IdentityService;
import panetina.elarion.core.service.IdentitySyncService;
import panetina.elarion.core.service.NicknameService;
import panetina.elarion.core.service.HistoryService;
import panetina.elarion.core.service.RealmDeliveryService;
import panetina.elarion.core.service.RealmGovernanceService;
import panetina.elarion.core.service.RealmSpawnService;
import panetina.elarion.core.service.RewardActionService;
import panetina.elarion.core.service.TitleService;
import panetina.elarion.core.service.PrivateMessageService;
import panetina.elarion.core.service.PlayerStatsService;
import panetina.elarion.core.service.ProgressionService;
import panetina.elarion.core.service.ElarionTaskService;
import panetina.elarion.core.service.ElarionTaskConfig;
import panetina.elarion.core.registry.ElarionRegistries;
import panetina.elarion.core.storage.CitizenStorage;
import panetina.elarion.core.storage.ChronicleArchiveStorage;
import panetina.elarion.core.storage.HistoryIndexStorage;
import panetina.elarion.core.storage.HistoryStorage;
import panetina.elarion.core.storage.PlayerStatsStorage;
import panetina.elarion.core.storage.RealmDeliveryStorage;
import panetina.elarion.core.storage.RealmRuntimeStorage;
import panetina.elarion.core.storage.TitleClaimStorage;
import panetina.elarion.core.storage.TitleProgressStorage;
import panetina.elarion.core.storage.DeferredRewardGrantStorage;
import panetina.elarion.core.network.IdentitySyncRequestPayload;
import panetina.elarion.core.network.IdentitySyncPayload;
import panetina.elarion.core.network.UiThemeSyncPayload;
import panetina.elarion.core.service.ElarionUiThemeService;
import panetina.elarion.core.service.DeferredRewardGrantService;
import panetina.elarion.core.service.CatchTelemetryService;
import panetina.elarion.core.storage.CatchSummaryStorage;
import panetina.elarion.core.storage.CatchTelemetryJournalStorage;
import panetina.elarion.core.storage.JsonStateStorage;

public final class ElarionCoreMod implements ModInitializer {
    public static final String MOD_ID = "elarion_core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(IdentitySyncPayload.ID, IdentitySyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(IdentitySyncRequestPayload.ID, IdentitySyncRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UiThemeSyncPayload.ID, UiThemeSyncPayload.CODEC);

        CoreConfigManager config = new CoreConfigManager(LOGGER);
        config.load();

        ElarionEventBus events = new ElarionEventBus();
        CitizenService citizens = new CitizenService(new CitizenStorage(LOGGER), config, events);
        RealmService realms = new RealmService(config, citizens);
        HistoryService history = new HistoryService(
                config, new HistoryStorage(LOGGER), new HistoryIndexStorage(LOGGER),
                new ChronicleArchiveStorage(LOGGER), events);
        TitleService titles = new TitleService(config, citizens, new TitleClaimStorage(LOGGER), history);
        AbilityService abilities = new AbilityService(titles);
        config.titles().values().forEach(title -> title.abilities().forEach(abilities::register));
        IdentityService identities = new IdentityService(citizens, realms, titles);
        IdentitySyncService identitySync = new IdentitySyncService(citizens, realms, titles, identities);
        NicknameService nicknames = new NicknameService(config, citizens);
        RealmGovernanceService governance =
                new RealmGovernanceService(new RealmRuntimeStorage(LOGGER), realms, citizens, history);
        identities.setGovernance(governance);
        ServerPlayNetworking.registerGlobalReceiver(IdentitySyncRequestPayload.ID, (payload, context) -> {
            if (payload.requested()) {
                context.server().execute(() -> identitySync.syncAllNow(context.server()));
            }
        });
        ChatService chat = new ChatService(config, citizens, realms, identities, history, governance);
        PrivateMessageService privateMessages =
                new PrivateMessageService(realms, citizens, identities, governance, history, chat,
                        config.serverIdentity());
        RealmSpawnService realmSpawns = new RealmSpawnService(citizens, realms, history, config.serverIdentity());
        RewardActionService rewards = new RewardActionService(config, citizens, titles, abilities, events);
        DeferredRewardGrantService deferredRewards = new DeferredRewardGrantService(
                new DeferredRewardGrantStorage(LOGGER), rewards, history);
        PlayerStatsService playerStats = new PlayerStatsService(new PlayerStatsStorage(LOGGER), titles);
        ProgressionService progression =
                new ProgressionService(config, citizens, titles, playerStats, new TitleProgressStorage(LOGGER));
        RealmDeliveryService realmDeliveries =
                new RealmDeliveryService(new RealmDeliveryStorage(LOGGER), citizens, realms, rewards, history,
                        config.serverIdentity());
        ElarionCommandRegistry commands = new ElarionCommandRegistry();
        ElarionRegistries registries = new ElarionRegistries();
        ElarionTaskService tasks = new ElarionTaskService(LOGGER, ElarionTaskConfig.loadSettings(LOGGER));
        ElarionUiThemeService uiThemes = new ElarionUiThemeService(config);
        CatchTelemetryService catchTelemetry = new CatchTelemetryService(
                new CatchTelemetryJournalStorage(),
                new CatchSummaryStorage(),
                LOGGER);
        catchTelemetry.registerEvents(events);
        history.setTaskService(tasks);
        ElarionApi api = new ElarionApi(
                citizens, realms, titles, abilities, identities, identitySync, nicknames, history, privateMessages,
                chat, governance, realmSpawns, realmDeliveries, rewards, playerStats, progression, events, commands,
                registries, tasks, config.serverIdentity(), uiThemes, deferredRewards, catchTelemetry);

        initializeAddons(api);
        progression.registerEvents();
        events.onCitizenChanged(event -> {
            MinecraftServer server = citizens.server();
            ServerPlayerEntity player = server == null ? null : server.getPlayerManager().getPlayer(event.citizenId());
            if (player != null) {
                identitySync.syncSubject(server, player);
            } else if (server != null) {
                identitySync.syncAll(server);
            }
            if ("realm-assigned".equals(event.reason())) {
                if (player != null) realmSpawns.teleportAfterRealmAssignment(player);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            citizens.bind(server);
            history.bind(server);
            titles.bind(server);
            playerStats.bind(server);
            progression.bind(server);
            governance.bind(server);
            realmDeliveries.bind(server);
            deferredRewards.bind(server);
            catchTelemetry.bind(JsonStateStorage.elarionRoot(server));
            realms.initializeScoreboardTeams(server);
            LOGGER.info("Elarion Core bound to server {}", server.getServerMotd());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            citizens.markSeen(handler.getPlayer());
            titles.repair(citizens.getOrCreate(handler.getPlayer()), null);
            realms.applyCurrentScoreboardTeam(handler.getPlayer());
            realmDeliveries.deliverPending(handler.getPlayer());
            deferredRewards.deliverPending(handler.getPlayer());
            catchTelemetry.activate(handler.getPlayer().getUuid());
            identitySync.syncAllNow(server);
            uiThemes.sync(handler.getPlayer());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            citizens.markSeen(handler.getPlayer());
            playerStats.save(handler.getPlayer().getUuid());
            progression.save(handler.getPlayer().getUuid());
            catchTelemetry.save(handler.getPlayer().getUuid());
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tasks.tickServerQueue();
            playerStats.saveIfDue();
            progression.tick(server);
            history.tick();
            identitySync.tick(server);
            catchTelemetry.tick();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            playerStats.saveDirty();
            progression.saveDirty();
            deferredRewards.save();
            history.flush();
            catchTelemetry.shutdown();
            tasks.shutdown();
        });

        ServerPlayerEvents.JOIN.register(chat::sendJoinNotice);
        ServerPlayerEvents.LEAVE.register(chat::sendLeaveNotice);
        ServerPlayerEvents.AFTER_RESPAWN.register(realmSpawns::routeRespawn);
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            if (!config.localChatEnabled()) return true;
            chat.sendLocalMessage(sender, message.getContent().getString());
            return false;
        });
        ServerMessageEvents.ALLOW_GAME_MESSAGE.register((server, message, overlay) ->
                !chat.shouldBlockVanillaGameNotice(message));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ElarionCommands.register(dispatcher, registryAccess, api, config, commands));

        LOGGER.info("Elarion Core initialized");
    }

    private static void initializeAddons(ElarionApi api) {
        var containers = FabricLoader.getInstance()
                .getEntrypointContainers("elarion:addon", ElarionAddon.class);
        java.util.Map<String, java.util.List<EntrypointContainer<ElarionAddon>>> byProvider =
                new java.util.LinkedHashMap<>();
        containers.forEach(container -> byProvider
                .computeIfAbsent(container.getProvider().getMetadata().getId(), ignored -> new java.util.ArrayList<>())
                .add(container));
        java.util.Set<String> providers = java.util.Set.copyOf(byProvider.keySet());
        java.util.Map<String, java.util.Set<String>> dependencies = new java.util.LinkedHashMap<>();
        byProvider.forEach((provider, entries) ->
                dependencies.put(provider, AddonInitializationOrder.dependenciesOf(entries.getFirst(), providers)));

        for (String provider : AddonInitializationOrder.sort(dependencies)) {
            for (EntrypointContainer<ElarionAddon> container : byProvider.get(provider)) {
                try {
                    container.getEntrypoint().initialize(api);
                    LOGGER.info("Initialized Elarion addon {}", provider);
                } catch (RuntimeException exception) {
                    throw new IllegalStateException("Failed to initialize Elarion addon " + provider, exception);
                }
            }
        }
    }
}
