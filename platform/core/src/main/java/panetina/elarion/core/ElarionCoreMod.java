package panetina.elarion.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
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
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.api.ElarionCommandRegistry;
import panetina.elarion.core.api.reset.PlayerResetHandler;
import panetina.elarion.core.api.reset.PlayerResetRegistry;
import panetina.elarion.core.api.reset.PlayerResetResult;
import panetina.elarion.core.api.reset.WorldResetRegistry;
import panetina.elarion.core.command.ElarionCommands;
import panetina.elarion.core.command.PlayerResetCommandRegistrar;
import panetina.elarion.core.command.WorldResetCommandRegistrar;
import panetina.elarion.core.service.PlayerResetService;
import panetina.elarion.core.service.WorldResetService;
import panetina.elarion.core.command.CharacterCommands;
import panetina.elarion.core.config.CoreConfigDescriptors;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplier;
import panetina.elarion.core.config.ElarionConfigApplyRegistry;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSnapshot;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.model.PublicHistoryConsumer;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RealmPresentation;
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
import panetina.elarion.core.service.PlayerRestrictionService;
import panetina.elarion.core.service.PlayerInteractionRestrictionRegistrar;
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
import panetina.elarion.core.storage.NotificationStorage;
import panetina.elarion.core.network.IdentitySyncRequestPayload;
import panetina.elarion.core.network.IdentitySyncPayload;
import panetina.elarion.core.network.NotificationClaimPayload;
import panetina.elarion.core.network.NotificationDismissPayload;
import panetina.elarion.core.network.NotificationActionPayload;
import panetina.elarion.core.network.NotificationSnapshotPayload;
import panetina.elarion.core.network.UiThemeSyncPayload;
import panetina.elarion.core.service.ElarionUiThemeService;
import panetina.elarion.core.service.DeferredRewardGrantService;
import panetina.elarion.core.service.ElarionNotificationService;
import panetina.elarion.core.service.CatchTelemetryService;
import panetina.elarion.core.service.CatchTelemetryWorker;
import panetina.elarion.core.service.CoreTitleCollectionProvider;
import panetina.elarion.core.service.CoreProgressionProfileContributor;
import panetina.elarion.core.storage.CatchSummaryStorage;
import panetina.elarion.core.storage.CatchTelemetryJournalStorage;
import panetina.elarion.core.storage.JsonStateStorage;
import panetina.elarion.core.storage.CharacterLifecycleStorage;
import panetina.elarion.core.service.CharacterLifecycleService;
import panetina.elarion.core.network.CharacterCreationRequirementPayload;
import panetina.elarion.core.network.CharacterRealmAssignmentConfirmPayload;
import panetina.elarion.core.network.CharacterRealmAssignmentPayload;
import panetina.elarion.core.network.CharacterCreationSubmitPayload;
import panetina.elarion.core.network.CharacterCreationStatusRequestPayload;
import panetina.elarion.core.network.CollectionActionPayload;
import panetina.elarion.core.network.CollectionOpenPayload;
import panetina.elarion.core.network.CollectionOpenRequestPayload;
import panetina.elarion.core.network.CitizenProfileRequestPayload;
import panetina.elarion.core.network.CitizenProfileSnapshotPayload;
import panetina.elarion.core.network.CitizenProfileOpenPayload;
import panetina.elarion.core.network.ElarionRequestLimiter;
import panetina.elarion.core.service.CitizenProfileService;
import panetina.elarion.core.service.CoreChronicleText;
import panetina.elarion.core.service.ElarionCollectionService;
import panetina.elarion.core.network.AdminPanelActionPayload;
import panetina.elarion.core.network.AdminPanelOpenPayload;
import panetina.elarion.core.network.AdminPanelOpenRequestPayload;
import panetina.elarion.core.network.ElarionConfigEditOpenPayload;
import panetina.elarion.core.network.ElarionConfigEditRequestPayload;
import panetina.elarion.core.network.ElarionConfigEditResultPayload;
import panetina.elarion.core.network.LauncherPassageTicketPayload;
import panetina.elarion.core.network.ChatChannelSendPayload;
import panetina.elarion.core.network.ChatRecipientRequestPayload;
import panetina.elarion.core.network.ChatRecipientSnapshotPayload;
import panetina.elarion.core.network.ChatChannelAvailabilityPayload;
import panetina.elarion.core.network.PlayerContextActionExecutePayload;
import panetina.elarion.core.network.PlayerContextActionRequestPayload;
import panetina.elarion.core.network.PlayerContextActionSnapshotPayload;
import panetina.elarion.core.model.ElarionChatChannel;
import panetina.elarion.core.service.ElarionChatChannelRouter;
import panetina.elarion.core.service.ElarionAdminPanelService;
import panetina.elarion.core.service.ElarionConfigApplyService;
import panetina.elarion.core.service.WorldheartGovernanceService;
import panetina.elarion.core.storage.WorldheartAuthorityStorage;
import panetina.elarion.core.integration.minecraft.MinecraftBridgeConfig;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Visibility;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionPublisher;
import panetina.elarion.core.integration.minecraft.AdvancementLeaderboardProjection;
import panetina.elarion.core.integration.minecraft.MinecraftWhitelistBridgeService;
import panetina.elarion.core.integration.minecraft.LauncherPassageTicketService;
import panetina.elarion.core.metric.MetricProjectionWorker;
import panetina.elarion.core.metric.PersistentMetricProjectionService;
import panetina.elarion.core.storage.MetricJournalStorage;
import panetina.elarion.core.storage.MetricProjectionStorage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class ElarionCoreMod implements ModInitializer {
    public static final String MOD_ID = "elarion_core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String PROFILE_REQUEST_CHANNEL = "citizen-profile";
    private static final String PLAYER_CONTEXT_REQUEST_CHANNEL = "player-context-actions";
    private static final int PROFILE_REQUESTS_PER_WINDOW = 4;
    private static final int PLAYER_CONTEXT_REQUESTS_PER_WINDOW = 4;
    private static final long PROFILE_REQUEST_WINDOW_MILLIS = 1_000L;
    private static final long PLAYER_CONTEXT_REQUEST_WINDOW_MILLIS = 1_000L;

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(IdentitySyncPayload.ID, IdentitySyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(IdentitySyncRequestPayload.ID, IdentitySyncRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UiThemeSyncPayload.ID, UiThemeSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(NotificationSnapshotPayload.ID, NotificationSnapshotPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NotificationClaimPayload.ID, NotificationClaimPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NotificationDismissPayload.ID, NotificationDismissPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NotificationActionPayload.ID, NotificationActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChatChannelSendPayload.ID, ChatChannelSendPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChatChannelAvailabilityPayload.ID, ChatChannelAvailabilityPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChatRecipientRequestPayload.ID, ChatRecipientRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChatRecipientSnapshotPayload.ID, ChatRecipientSnapshotPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(
                PlayerContextActionRequestPayload.ID, PlayerContextActionRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(
                PlayerContextActionExecutePayload.ID, PlayerContextActionExecutePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(
                PlayerContextActionSnapshotPayload.ID, PlayerContextActionSnapshotPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(
                CharacterCreationRequirementPayload.ID, CharacterCreationRequirementPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(
                CharacterRealmAssignmentPayload.ID, CharacterRealmAssignmentPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(
                CharacterCreationSubmitPayload.ID, CharacterCreationSubmitPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(
                CharacterRealmAssignmentConfirmPayload.ID, CharacterRealmAssignmentConfirmPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(
                CharacterCreationStatusRequestPayload.ID, CharacterCreationStatusRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CollectionOpenPayload.ID, CollectionOpenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CollectionOpenRequestPayload.ID, CollectionOpenRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CollectionActionPayload.ID, CollectionActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CitizenProfileRequestPayload.ID, CitizenProfileRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CitizenProfileSnapshotPayload.ID, CitizenProfileSnapshotPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CitizenProfileOpenPayload.ID, CitizenProfileOpenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AdminPanelOpenPayload.ID, AdminPanelOpenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AdminPanelOpenRequestPayload.ID, AdminPanelOpenRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AdminPanelActionPayload.ID, AdminPanelActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ElarionConfigEditOpenPayload.ID, ElarionConfigEditOpenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ElarionConfigEditResultPayload.ID, ElarionConfigEditResultPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ElarionConfigEditRequestPayload.ID, ElarionConfigEditRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LauncherPassageTicketPayload.ID, LauncherPassageTicketPayload.CODEC);

        CoreConfigManager config = new CoreConfigManager(LOGGER);
        config.load();
        MinecraftBridgeConfig minecraftBridgeConfig = MinecraftBridgeConfig.load(LOGGER);
        MinecraftWhitelistBridgeService minecraftBridge = new MinecraftWhitelistBridgeService(LOGGER, minecraftBridgeConfig);
        LauncherPassageTicketService launcherPassageTickets = new LauncherPassageTicketService(minecraftBridgeConfig);
        MinecraftProjectionPublisher webProjections = minecraftBridge.projections();
        Map<UUID, String> citizenRealms = new ConcurrentHashMap<>();
        Map<String, Integer> realmMemberCounts = new ConcurrentHashMap<>();
        Map<UUID, String> onlineCitizenRealms = new ConcurrentHashMap<>();
        Map<String, Integer> realmOnlineCounts = new ConcurrentHashMap<>();
        AtomicReference<MinecraftServer> activeServer = new AtomicReference<>();

        ElarionEventBus events = new ElarionEventBus();
        CitizenService citizens = new CitizenService(new CitizenStorage(LOGGER), config, events);
        RealmService realms = new RealmService(config, citizens);
        WorldheartGovernanceService worldheart = new WorldheartGovernanceService(
                new WorldheartAuthorityStorage(LOGGER),
                rulerId -> citizens.find(rulerId).isPresent(),
                rulerId -> citizens.find(rulerId).map(citizen ->
                        citizen.nickname() == null || citizen.nickname().isBlank()
                                ? citizen.lastKnownUsername() : citizen.nickname()),
                events);
        HistoryService history = new HistoryService(
                config, new HistoryStorage(LOGGER), new HistoryIndexStorage(LOGGER),
                new ChronicleArchiveStorage(LOGGER), events);
        history.registerChronicleRenderer(CoreChronicleText.INSTANCE);
        history.onRecorded(event -> publishChronicleProjection(history, webProjections, event));
        TitleService titles = new TitleService(config, citizens, new TitleClaimStorage(LOGGER), history, events);
        AbilityService abilities = new AbilityService(titles);
        config.titles().values().forEach(title -> title.abilities().forEach(abilities::register));
        IdentityService identities = new IdentityService(citizens, realms, titles);
        IdentitySyncService identitySync = new IdentitySyncService(citizens, realms, titles, identities);
        NicknameService nicknames = new NicknameService(config, citizens);
        RealmGovernanceService governance =
                new RealmGovernanceService(new RealmRuntimeStorage(LOGGER), realms, citizens, history);
        identities.setGovernance(governance);
        PlayerRestrictionService restrictions = new PlayerRestrictionService();
        PlayerInteractionRestrictionRegistrar.register(restrictions);
        identities.setRestrictions(restrictions);
        CharacterLifecycleService characters = new CharacterLifecycleService(
                LOGGER, new CharacterLifecycleStorage(LOGGER), citizens, realms, nicknames, events, restrictions);
        ServerPlayNetworking.registerGlobalReceiver(IdentitySyncRequestPayload.ID, (payload, context) -> {
            if (payload.requested()) {
                context.server().execute(() -> identitySync.syncAllNow(context.server()));
            }
        });
        ChatService chat = new ChatService(config, citizens, realms, identities, history, governance, restrictions);
        PrivateMessageService privateMessages =
                new PrivateMessageService(realms, citizens, identities, governance, history, chat,
                        config.serverIdentity(), restrictions);
        RealmSpawnService realmSpawns = new RealmSpawnService(citizens, realms, history, config.serverIdentity());
        RewardActionService rewards = new RewardActionService(config, citizens, titles, abilities, events);
        DeferredRewardGrantService deferredRewards = new DeferredRewardGrantService(
                new DeferredRewardGrantStorage(LOGGER), rewards, history);
        ElarionNotificationService notifications =
                new ElarionNotificationService(new NotificationStorage(LOGGER), citizens, webProjections);
        notifications.registerProvider(deferredRewards::snapshotEntries);
        deferredRewards.setNotificationSync(notifications::sync);
        titles.setNotifications(notifications);
        governance.setNotifications(notifications);
        PlayerStatsService playerStats = new PlayerStatsService(new PlayerStatsStorage(LOGGER), titles);
        ProgressionService progression =
                new ProgressionService(config, citizens, titles, playerStats, new TitleProgressStorage(LOGGER));
        AdvancementLeaderboardProjection advancementLeaderboard =
                new AdvancementLeaderboardProjection(LOGGER, webProjections);
        progression.setAdvancementCountListener(citizenId -> citizens.find(citizenId).ifPresent(citizen ->
                advancementLeaderboard.update(citizen,
                        playerStats.value(citizenId, ProgressionService.ADVANCEMENTS_COMPLETED))));
        characters.registerResetHandler("elarion_core_titles",
                context -> titles.retireUniqueClaims(context.accountId(), context.reason()));
        characters.registerResetHandler("elarion_core_progression", context -> {
            playerStats.reset(context.accountId());
            progression.resetProgress(context.accountId(), "");
        });
        RealmDeliveryService realmDeliveries =
                new RealmDeliveryService(new RealmDeliveryStorage(LOGGER), citizens, realms, rewards,
                        deferredRewards, notifications, history, config.serverIdentity());
        ElarionCommandRegistry commands = new ElarionCommandRegistry();
        PlayerResetRegistry playerResets = new PlayerResetRegistry();
        PlayerResetService playerResetService = new PlayerResetService(LOGGER, playerResets);
        WorldResetRegistry worldResets = new WorldResetRegistry();
        WorldResetService worldResetService = new WorldResetService(LOGGER, worldResets);
        playerResets.register(new PlayerResetHandler() {
            @Override public String id() { return "elarion_core"; }

            @Override public java.util.Map<String, Long> preview(MinecraftServer server) {
                return java.util.Map.of("citizens", (long) citizens.all().size());
            }

            @Override public java.util.List<java.nio.file.Path> backupTargets(MinecraftServer server) {
                java.nio.file.Path root = JsonStateStorage.elarionRoot(server);
                return java.util.List.of(
                        root.resolve("citizens"), root.resolve("core/characters/state.json"),
                        root.resolve("notifications/notifications.json"), root.resolve("reward-grants.json"),
                        root.resolve("player-stats"), root.resolve("progression/title-progress"),
                        root.resolve("title-claims.json"));
            }

            @Override public PlayerResetResult reset(panetina.elarion.core.api.reset.PlayerResetContext context)
                    throws Exception {
                java.util.Map<String, Long> changed = new java.util.LinkedHashMap<>();
                changed.put("notifications", (long) notifications.resetAllPlayerState());
                changed.put("rewardGrants", (long) deferredRewards.resetAllPlayerState());
                changed.put("titleProgress", (long) progression.resetAllPlayerState());
                changed.put("playerStats", (long) playerStats.resetAll());
                changed.put("titleClaims", (long) titles.resetAllClaims());
                changed.put("characters", (long) characters.resetAllPlayerState());
                changed.put("citizens", (long) citizens.resetAll());
                return new PlayerResetResult(changed);
            }
        });
        commands.registerAdminSubcommand(() -> PlayerResetCommandRegistrar.register(playerResetService));
        commands.registerAdminSubcommand(() -> WorldResetCommandRegistrar.register(worldResetService, worldResets::worldIds));
        commands.registerHelpDescription("/e reset players", "Preview and confirm a complete player progression reset.");
        commands.registerHelpDescription("/e reset world <world>", "Regenerate a managed world and remove its world-scoped content.");
        commands.registerAdminSubcommand(() -> CharacterCommands.admin(characters));
        commands.registerTestSubcommand(() -> CharacterCommands.test(characters));
        commands.registerHelpDescription("/e character ...", "Inspect character lifecycle and archives.");
        commands.registerHelpDescription("/e test character ...", "Development character lifecycle controls.");
        ElarionRegistries registries = new ElarionRegistries();
        ElarionTaskService tasks = new ElarionTaskService(LOGGER, ElarionTaskConfig.loadSettings(LOGGER));
        ElarionCollectionService collections = new ElarionCollectionService();
        CitizenProfileService profiles = new CitizenProfileService(citizens, realms, titles, LOGGER);
        ElarionRequestLimiter requestLimiter = new ElarionRequestLimiter();
        profiles.registerContributor(new CoreProgressionProfileContributor(playerStats));
        ElarionAdminPanelService adminPanel = new ElarionAdminPanelService();
        ElarionConfigRegistry configRegistry = new ElarionConfigRegistry();
        ElarionConfigApplyRegistry configApplyRegistry = new ElarionConfigApplyRegistry();
        ElarionConfigApplyService configApplyService =
                new ElarionConfigApplyService(configRegistry, configApplyRegistry);
        CoreConfigDescriptors.register(configRegistry, config);
        adminPanel.bindConfigApplyExecutor(configApplyService);
        collections.registerTab(new CoreTitleCollectionProvider(citizens, titles));
        ElarionUiThemeService uiThemes = new ElarionUiThemeService(config);
        CoreUiThemeFontScaleConfigApplier.register(configApplyRegistry::register, config, () -> {
            MinecraftServer server = activeServer.get();
            if (server != null) uiThemes.syncAll(server);
        });
        CatchTelemetryService catchTelemetry = new CatchTelemetryService(
                new CatchTelemetryJournalStorage(),
                new CatchSummaryStorage(),
                LOGGER);
        CatchTelemetryWorker catchTelemetryWorker = new CatchTelemetryWorker(catchTelemetry);
        MetricProjectionWorker metrics = new MetricProjectionWorker(new PersistentMetricProjectionService(
                new MetricJournalStorage(), new MetricProjectionStorage()), events::emitMetricUpdated);
        progression.setMetricProjection(metrics);
        events.onMetricUpdated(progression::recordMetric);
        history.setTaskService(tasks);
        ElarionApi api = new ElarionApi(
                citizens, realms, titles, abilities, identities, identitySync, nicknames, history, privateMessages,
                chat, governance, realmSpawns, realmDeliveries, rewards, playerStats, progression, events, commands,
                registries, tasks, collections, profiles, adminPanel, configRegistry, configApplyRegistry::register,
                config.serverIdentity(), uiThemes, deferredRewards, notifications, restrictions, catchTelemetry,
                catchTelemetryWorker,
                characters, worldheart, webProjections, metrics, playerResets, worldResets, worldResetService);
        adminPanel.bindApi(api);

        ServerPlayNetworking.registerGlobalReceiver(CharacterCreationSubmitPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    CharacterLifecycleService.SubmissionResult result = characters.submit(
                            context.player(), payload.nonce(), payload.displayName(), payload.biography());
                    if (!result.success()) characters.sync(context.player(), result.message());
                }));
        ServerPlayNetworking.registerGlobalReceiver(CharacterCreationStatusRequestPayload.ID, (payload, context) ->
                context.server().execute(() -> characters.sync(context.player(), "")));
        ServerPlayNetworking.registerGlobalReceiver(CharacterRealmAssignmentConfirmPayload.ID, (payload, context) ->
                context.server().execute(() -> realmSpawns.teleportAfterRealmAssignment(context.player())));

        notifications.registerAction("elarion_core:realm_decision_approve", action ->
                voteOnRealmDecision(governance, action, true));
        notifications.registerAction("elarion_core:realm_decision_reject", action ->
                voteOnRealmDecision(governance, action, false));

        ServerPlayNetworking.registerGlobalReceiver(NotificationClaimPayload.ID, (payload, context) ->
                context.server().execute(() -> deferredRewards.claim(context.player(), payload.grantId())));
        ServerPlayNetworking.registerGlobalReceiver(NotificationDismissPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    notifications.act(context.player(), payload.notificationId(), ElarionNotificationService.DISMISS);
                }));
        ServerPlayNetworking.registerGlobalReceiver(NotificationActionPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    if ("elarion_core:claim_reward".equals(payload.actionId())) {
                        deferredRewards.claim(context.player(), payload.notificationId());
                    } else {
                        notifications.act(context.player(), payload.notificationId(), payload.actionId());
                    }
                }));
        ServerPlayNetworking.registerGlobalReceiver(ChatChannelSendPayload.ID, (payload, context) ->
                context.server().execute(() -> routeChatChannel(api, context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(ChatRecipientRequestPayload.ID, (payload, context) ->
                context.server().execute(() -> ServerPlayNetworking.send(context.player(), chatRecipients(api, context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(
                PlayerContextActionRequestPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    if (requestLimiter.allow(context.player().getUuid(), PLAYER_CONTEXT_REQUEST_CHANNEL,
                            System.currentTimeMillis(), PLAYER_CONTEXT_REQUESTS_PER_WINDOW,
                            PLAYER_CONTEXT_REQUEST_WINDOW_MILLIS)) {
                        sendPlayerContextActions(api, context.player(), payload.targetId());
                    }
                }));
        ServerPlayNetworking.registerGlobalReceiver(
                PlayerContextActionExecutePayload.ID, (payload, context) ->
                context.server().execute(() -> executePlayerContextAction(api, context.player(), payload.targetId(), payload.actionId())));
        ServerPlayNetworking.registerGlobalReceiver(CollectionOpenRequestPayload.ID, (payload, context) ->
                context.server().execute(() -> collections.open(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(CollectionActionPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ElarionCollectionService.ActionResult result = collections.act(
                            context.player(), payload.tabId(), payload.entryId(), payload.actionId());
                    collections.open(context.player(), payload.tabId(), result.message());
                }));
        ServerPlayNetworking.registerGlobalReceiver(CitizenProfileRequestPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    if (!requestLimiter.allow(context.player().getUuid(), PROFILE_REQUEST_CHANNEL,
                            System.currentTimeMillis(), PROFILE_REQUESTS_PER_WINDOW,
                            PROFILE_REQUEST_WINDOW_MILLIS)) {
                        return;
                    }
                    CitizenProfileSnapshotPayload profile = profileSnapshotPayload(profiles, context.player(), payload);
                    if (payload.targetId().equals(new UUID(0L, 0L))) {
                        ServerPlayNetworking.send(context.player(), profile);
                    } else {
                        ServerPlayNetworking.send(context.player(), new CitizenProfileOpenPayload(
                                collections.snapshot(context.player(), ElarionCollectionService.PROFILE_TAB_ID,
                                        "Viewing " + profile.snapshot().title()),
                                profile.snapshot()));
                    }
                }));
        ServerPlayNetworking.registerGlobalReceiver(AdminPanelOpenRequestPayload.ID, (payload, context) ->
                context.server().execute(() -> adminPanel.open(
                        context.player(), payload.selectedTabId(), payload.selectedRowId(), "")));
        ServerPlayNetworking.registerGlobalReceiver(AdminPanelActionPayload.ID, (payload, context) ->
                context.server().execute(() -> adminPanel.act(
                        context.player(), payload.providerId(), payload.actionId(), payload.targetId(),
                        payload.parameters(), payload.confirmed())));
        ServerPlayNetworking.registerGlobalReceiver(ElarionConfigEditRequestPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ElarionConfigEditResultPayload result = adminPanel.validateConfigEdit(context.player(), payload);
                    ServerPlayNetworking.send(context.player(), result);
                    adminPanel.open(context.player(), "configs", "", result.message());
                }));

        progression.registerEvents();
        events.onCitizenChanged(event -> {
            MinecraftServer server = citizens.server();
            updateCitizenProjection(event.citizen(), config, realms, webProjections, citizenRealms,
                    realmMemberCounts, onlineCitizenRealms, realmOnlineCounts);
            advancementLeaderboard.update(event.citizen(),
                    playerStats.value(event.citizenId(), ProgressionService.ADVANCEMENTS_COMPLETED));
            ServerPlayerEntity player = server == null ? null : server.getPlayerManager().getPlayer(event.citizenId());
            if (player != null) {
                identitySync.syncSubject(server, player);
                notifications.sync(player);
            } else if (server != null) {
                identitySync.syncAll(server);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            activeServer.set(server);
            citizens.bind(server);
            worldheart.bind(server);
            characters.bind(server);
            history.bind(server);
            titles.bind(server);
            playerStats.bind(server);
            progression.bind(server);
            governance.bind(server);
            deferredRewards.bind(server);
            notifications.bind(server);
            realmDeliveries.bind(server);
            catchTelemetryWorker.bind(JsonStateStorage.elarionRoot(server));
            long recoveredMetrics = metrics.bind(JsonStateStorage.elarionRoot(server));
            configApplyService.bind(JsonStateStorage.elarionRoot(server));
            minecraftBridge.start(server);
            advancementLeaderboard.bind(JsonStateStorage.elarionRoot(server));
            initializeWorldProjections(citizens, config, realms, webProjections, citizenRealms,
                    realmMemberCounts, realmOnlineCounts);
            realms.initializeScoreboardTeams(server);
            LOGGER.info("Elarion Core bound to server {}", server.getServerMotd());
            if (recoveredMetrics > 0) {
                LOGGER.info("Recovered {} Core metric batches after the last checkpoint", recoveredMetrics);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            playerResetService.clearResetDisconnect(handler.getPlayer().getUuid());
            CitizenRecord joinedCitizen = citizens.markLocation(handler.getPlayer(), "join-location");
            updateOnlineRealm(joinedCitizen.uuid(), joinedCitizen.realmId(), realms, webProjections,
                    onlineCitizenRealms, realmMemberCounts, realmOnlineCounts);
            characters.onJoin(handler.getPlayer());
            titles.repair(citizens.getOrCreate(handler.getPlayer()), null);
            progression.synchronizeAdvancementCount(handler.getPlayer());
            progression.reconcileMetricRules(handler.getPlayer());
            realms.applyCurrentScoreboardTeam(handler.getPlayer());
            realmDeliveries.deliverPending(handler.getPlayer());
            notifications.sync(handler.getPlayer());
            catchTelemetryWorker.activate(handler.getPlayer().getUuid());
            identitySync.syncAllNow(server);
            uiThemes.sync(handler.getPlayer());
            ServerPlayNetworking.send(handler.getPlayer(), chatChannels(api, handler.getPlayer()));
            String launcherPassageTicket = launcherPassageTickets.issue(handler.getPlayer().getUuid());
            if (!launcherPassageTicket.isBlank()) {
                ServerPlayNetworking.send(handler.getPlayer(), new LauncherPassageTicketPayload(
                        handler.getPlayer().getUuid().toString(), launcherPassageTicket));
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (playerResetService.isResetDisconnect(handler.getPlayer().getUuid())) return;
            citizens.markLocation(handler.getPlayer(), "disconnect-location");
            updateOnlineRealm(handler.getPlayer().getUuid(), "", realms, webProjections,
                    onlineCitizenRealms, realmMemberCounts, realmOnlineCounts);
            playerStats.save(handler.getPlayer().getUuid());
            progression.save(handler.getPlayer().getUuid());
            catchTelemetryWorker.save(handler.getPlayer().getUuid());
            requestLimiter.clear(handler.getPlayer().getUuid());
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                citizens.markLocation(player, "world-transition"));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tasks.tickServerQueue();
            playerStats.saveIfDue();
            progression.tick(server);
            history.tick();
            identitySync.tick(server);
            catchTelemetryWorker.tick();
            characters.tick();
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

        initializeAddons(api);
        // Core is the dependency root, so addon shutdown hooks registered
        // during initializeAddons must drain before these shared workers stop.
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            playerStats.saveDirty();
            progression.saveDirty();
            deferredRewards.save();
            notifications.save();
            history.flush();
            catchTelemetryWorker.shutdown();
            metrics.shutdown();
            characters.save();
            configApplyService.unbind();
            minecraftBridge.stop();
            activeServer.set(null);
            tasks.shutdown();
        });
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

    private static CitizenProfileSnapshotPayload profileSnapshotPayload(
            CitizenProfileService profiles,
            ServerPlayerEntity viewer,
            CitizenProfileRequestPayload payload
    ) {
        UUID targetId = payload.targetId().equals(new UUID(0L, 0L)) ? viewer.getUuid() : payload.targetId();
        CitizenProfileRequestContext profileContext = viewer.hasPermissionLevel(4)
                ? CitizenProfileRequestContext.admin(viewer.getUuid(), targetId)
                : CitizenProfileRequestContext.publicView(viewer.getUuid(), targetId);
        CitizenProfileSnapshot snapshot = profiles.snapshot(profileContext)
                .map(profile -> requestedSection(profile, payload.sectionId()))
                .orElseGet(() -> new CitizenProfileSnapshot(targetId, "", List.of()));
        return new CitizenProfileSnapshotPayload(snapshot);
    }

    private static CitizenProfileSnapshot requestedSection(CitizenProfileSnapshot snapshot, String sectionId) {
        if (sectionId == null || sectionId.isBlank()) return snapshot;
        return new CitizenProfileSnapshot(
                snapshot.targetId(),
                snapshot.title(),
                snapshot.section(sectionId).map(List::of).orElseGet(List::of));
    }

    private static void initializeWorldProjections(
            CitizenService citizens,
            CoreConfigManager config,
            RealmService realms,
            MinecraftProjectionPublisher projections,
            Map<UUID, String> citizenRealms,
            Map<String, Integer> memberCounts,
            Map<String, Integer> onlineCounts
    ) {
        citizenRealms.clear();
        memberCounts.clear();
        for (CitizenRecord citizen : citizens.all()) {
            citizenRealms.put(citizen.uuid(), citizen.realmId());
            increment(memberCounts, citizen.realmId(), 1);
            publishCitizenProjection(citizen, config, realms, projections);
        }
        for (RealmDefinition realm : realms.all()) {
            publishRealmProjection(realm, realms, projections, memberCounts, onlineCounts);
            publishRealmWorldPresentation(realm, realms, projections);
        }
        publishWorldPresentation(projections, "elarion:worldheart", "Worldheart", "");
        publishWorldPresentation(projections, "minecraft:the_nether", "Nether", "");
        publishWorldPresentation(projections, "minecraft:the_end", "End", "");
    }

    private static void updateCitizenProjection(
            CitizenRecord citizen,
            CoreConfigManager config,
            RealmService realms,
            MinecraftProjectionPublisher projections,
            Map<UUID, String> citizenRealms,
            Map<String, Integer> memberCounts,
            Map<UUID, String> onlineCitizenRealms,
            Map<String, Integer> onlineCounts
    ) {
        String newRealm = citizen.realmId();
        String oldRealm = citizenRealms.put(citizen.uuid(), newRealm);
        if (oldRealm != null && !oldRealm.equals(newRealm)) {
            increment(memberCounts, oldRealm, -1);
            increment(memberCounts, newRealm, 1);
            realms.find(oldRealm).ifPresent(realm ->
                    publishRealmProjection(realm, realms, projections, memberCounts, onlineCounts));
        }
        if (oldRealm == null) increment(memberCounts, newRealm, 1);
        if (onlineCitizenRealms.containsKey(citizen.uuid())) {
            updateOnlineRealm(citizen.uuid(), newRealm, realms, projections,
                    onlineCitizenRealms, memberCounts, onlineCounts);
        }
        publishCitizenProjection(citizen, config, realms, projections);
        realms.find(newRealm).ifPresent(realm ->
                publishRealmProjection(realm, realms, projections, memberCounts, onlineCounts));
    }

    private static void updateOnlineRealm(
            UUID citizenId,
            String newRealm,
            RealmService realms,
            MinecraftProjectionPublisher projections,
            Map<UUID, String> onlineCitizenRealms,
            Map<String, Integer> memberCounts,
            Map<String, Integer> onlineCounts
    ) {
        String normalized = newRealm == null ? "" : newRealm;
        String oldRealm = normalized.isBlank()
                ? onlineCitizenRealms.remove(citizenId)
                : onlineCitizenRealms.put(citizenId, normalized);
        if (oldRealm != null && !oldRealm.equals(normalized)) {
            increment(onlineCounts, oldRealm, -1);
            realms.find(oldRealm).ifPresent(realm ->
                    publishRealmProjection(realm, realms, projections, memberCounts, onlineCounts));
        }
        if (!normalized.isBlank() && !normalized.equals(oldRealm)) {
            increment(onlineCounts, normalized, 1);
            realms.find(normalized).ifPresent(realm ->
                    publishRealmProjection(realm, realms, projections, memberCounts, onlineCounts));
        }
    }

    private static void publishCitizenProjection(
            CitizenRecord citizen,
            CoreConfigManager config,
            RealmService realms,
            MinecraftProjectionPublisher projections
    ) {
        Map<String, String> payload = new LinkedHashMap<>();
        putProjection(payload, "username", citizen.lastKnownUsername());
        putProjection(payload, "nickname", citizen.nickname());
        putProjection(payload, "realmId", citizen.realmId());
        putProjection(payload, "titleId", citizen.titleId());
        var title = config.titles().get(citizen.titleId());
        if (title != null) putProjection(payload, "titleDisplayName", title.displayName());
        putProjection(payload, "status", citizen.status().name());
        payload.put("joinedAt", Long.toString(citizen.joinedAt()));
        payload.put("lastSeenAt", Long.toString(citizen.lastSeenAt()));
        putProjection(payload, "lastWorldId", citizen.lastWorldId());
        realms.find(citizen.realmId()).ifPresent(realm -> {
            RealmPresentation presentation = realms.presentation(realm);
            putProjection(payload, "realmName", presentation.officialName());
            putProjection(payload, "realmTag", presentation.prefix());
            putProjection(payload, "realmColor", colorHex(presentation.color()));
        });
        projections.publishState("citizen", citizen.uuid().toString(), citizen.realmId(),
                Visibility.WHITELISTED, payload);
    }

    private static void publishRealmProjection(
            RealmDefinition realm,
            RealmService realms,
            MinecraftProjectionPublisher projections,
            Map<String, Integer> memberCounts,
            Map<String, Integer> onlineCounts
    ) {
        RealmPresentation presentation = realms.presentation(realm);
        Map<String, String> payload = new LinkedHashMap<>();
        putProjection(payload, "displayName", presentation.displayName());
        putProjection(payload, "officialName", presentation.officialName());
        putProjection(payload, "shortName", presentation.shortName());
        putProjection(payload, "tag", presentation.prefix());
        putProjection(payload, "colorName", presentation.color());
        payload.put("color", colorHex(presentation.color()));
        payload.put("memberCount", Integer.toString(memberCounts.getOrDefault(realm.id(), 0)));
        payload.put("activeCount", Integer.toString(onlineCounts.getOrDefault(realm.id(), 0)));
        projections.publishState("realm", realm.id(), realm.id(), Visibility.PUBLIC, payload);
    }

    private static void publishRealmWorldPresentation(
            RealmDefinition realm,
            RealmService realms,
            MinecraftProjectionPublisher projections
    ) {
        if (realm == null || realm.spawn() == null) return;
        RealmPresentation presentation = realms.presentation(realm);
        publishWorldPresentation(projections, realm.spawn().worldId(), presentation.officialName(), realm.id());
    }

    private static void publishWorldPresentation(
            MinecraftProjectionPublisher projections,
            String worldId,
            String displayName,
            String realmId
    ) {
        if (worldId == null || worldId.isBlank() || displayName == null || displayName.isBlank()) return;
        projections.publishState("world.presentation", worldId, realmId, Visibility.PUBLIC,
                Map.of("displayName", displayName));
    }

    private static void publishChronicleProjection(
            HistoryService history,
            MinecraftProjectionPublisher projections,
            HistoryEvent event
    ) {
        history.publicProjection(event, PublicHistoryConsumer.CHRONICLE, ChronicleRenderContext.EMPTY)
                .ifPresent(chronicle -> {
                    Map<String, String> payload = new LinkedHashMap<>();
                    putProjection(payload, "title", chronicle.title());
                    putProjection(payload, "body", chronicle.body());
                    putProjection(payload, "category", chronicle.category());
                    putProjection(payload, "detailLabel", chronicle.detailLabel());
                    putProjection(payload, "variantId", chronicle.variantId());
                    putProjection(payload, "type", event.type());
                    putProjection(payload, "actorId", event.actorId() == null ? "" : event.actorId().toString());
                    projections.publishEvent("chronicle", event.id().toString(), event.realmId(),
                            Visibility.PUBLIC, payload);
                });
    }

    private static void increment(Map<String, Integer> counts, String key, int delta) {
        if (key == null || key.isBlank() || delta == 0) return;
        counts.compute(key, (ignored, current) -> Math.max(0, (current == null ? 0 : current) + delta));
    }

    private static void putProjection(Map<String, String> payload, String key, String value) {
        if (value != null && !value.isBlank()) payload.put(key, value);
    }

    private static String colorHex(String color) {
        return switch (color == null ? "" : color.toLowerCase(java.util.Locale.ROOT)) {
            case "black" -> "#111111";
            case "dark_blue" -> "#0000aa";
            case "dark_green" -> "#00aa00";
            case "dark_aqua" -> "#00aaaa";
            case "dark_red" -> "#aa0000";
            case "dark_purple" -> "#aa00aa";
            case "gold" -> "#ffaa00";
            case "gray" -> "#aaaaaa";
            case "dark_gray" -> "#555555";
            case "blue" -> "#5555ff";
            case "green" -> "#55ff55";
            case "aqua" -> "#55ffff";
            case "red" -> "#ff5555";
            case "light_purple" -> "#ff55ff";
            case "yellow" -> "#ffff55";
            case "white" -> "#f2f0e7";
            default -> color != null && color.matches("#[0-9a-fA-F]{6}") ? color : "#b89552";
        };
    }

    private static ElarionNotificationService.ActionResult voteOnRealmDecision(
            RealmGovernanceService governance,
            ElarionNotificationService.ActionContext action,
            boolean approve
    ) {
        String rawId = action.notification().metadata().getOrDefault("decisionId", "");
        try {
            boolean accepted = governance.vote(
                    java.util.UUID.fromString(rawId), action.player().getUuid(), approve);
            return accepted
                    ? ElarionNotificationService.ActionResult.success(
                            approve ? "Approval recorded." : "Rejection recorded.", true)
                    : ElarionNotificationService.ActionResult.failure(
                            "That Realm decision is no longer available to you.");
        } catch (IllegalArgumentException exception) {
            return ElarionNotificationService.ActionResult.failure("Invalid Realm decision.");
        }
    }

    private static void routeChatChannel(ElarionApi api, ServerPlayerEntity sender, ChatChannelSendPayload payload) {
        if (payload.message().isBlank()) return;
        if (!ElarionChatChannelRouter.available(api, sender).contains(payload.channel())) {
            sender.sendMessage(Text.literal("That chat channel is unavailable."), false);
            return;
        }
        switch (payload.channel()) {
            case LOCAL -> api.chat().sendLocalMessage(sender, payload.message());
            case REALM -> api.chat().sendRealmMessage(sender, payload.message());
            case ALLIANCE -> api.chat().sendAllianceMessage(sender, payload.message());
            case GUILD -> {
                if (!ElarionChatChannelRouter.route(ElarionChatChannel.GUILD, sender, payload.message())) {
                    sender.sendMessage(Text.literal("Guild chat is unavailable."), false);
                }
            }
            case PRIVATE -> {
                ServerPlayerEntity recipient = payload.recipientId() == null ? null
                        : sender.getServer().getPlayerManager().getPlayer(payload.recipientId());
                if (recipient == null) {
                    sender.sendMessage(Text.literal("That private-message recipient is no longer online."), false);
                } else {
                    api.privateMessages().privateMessage(sender, recipient, payload.message());
                }
            }
        }
    }

    private static void sendPlayerContextActions(ElarionApi api, ServerPlayerEntity actor, UUID targetId) {
        ServerPlayerEntity target = actor.getServer().getPlayerManager().getPlayer(targetId);
        if (target == null || target.getUuid().equals(actor.getUuid())) return;
        var actions = api.registries().playerContextActions().available(actor, target).stream()
                .map(action -> new PlayerContextActionSnapshotPayload.Entry(
                        action.id(), action.label()))
                .toList();
        if (actions.isEmpty()) return;
        ServerPlayNetworking.send(actor, new PlayerContextActionSnapshotPayload(
                target.getUuid(), target.getGameProfile().getName(), actions));
    }

    private static void executePlayerContextAction(ElarionApi api, ServerPlayerEntity actor, UUID targetId, String actionId) {
        ServerPlayerEntity target = actor.getServer().getPlayerManager().getPlayer(targetId);
        if (target == null) return;
        var result = api.registries().playerContextActions().execute(actionId, actor, target);
        if (!result.success()) actor.sendMessage(Text.literal(result.message()), false);
    }

    private static ChatChannelAvailabilityPayload chatChannels(ElarionApi api, ServerPlayerEntity player) {
        return new ChatChannelAvailabilityPayload(ElarionChatChannelRouter.available(api, player));
    }

    private static ChatRecipientSnapshotPayload chatRecipients(ElarionApi api, ServerPlayerEntity sender) {
        return new ChatRecipientSnapshotPayload(sender.getServer().getPlayerManager().getPlayerList().stream()
                .filter(candidate -> api.privateMessages().canMessage(sender, candidate))
                .map(candidate -> new ChatRecipientSnapshotPayload.Entry(candidate.getUuid(),
                        api.identities().resolve(candidate).displayName().getString()))
                .sorted(java.util.Comparator.comparing(ChatRecipientSnapshotPayload.Entry::nickname,
                        String.CASE_INSENSITIVE_ORDER))
                .toList());
    }
}
