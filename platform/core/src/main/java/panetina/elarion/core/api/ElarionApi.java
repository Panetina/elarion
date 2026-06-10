package panetina.elarion.core.api;

import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.service.AbilityService;
import panetina.elarion.core.service.ChatService;
import panetina.elarion.core.service.CitizenService;
import panetina.elarion.core.service.RealmService;
import panetina.elarion.core.service.IdentityService;
import panetina.elarion.core.service.IdentitySyncService;
import panetina.elarion.core.service.NicknameService;
import panetina.elarion.core.service.HistoryService;
import panetina.elarion.core.service.PrivateMessageService;
import panetina.elarion.core.service.RealmDeliveryService;
import panetina.elarion.core.service.RealmGovernanceService;
import panetina.elarion.core.service.RealmSpawnService;
import panetina.elarion.core.service.RewardActionService;
import panetina.elarion.core.service.PlayerStatsService;
import panetina.elarion.core.service.ProgressionService;
import panetina.elarion.core.service.TitleService;
import panetina.elarion.core.service.ElarionTaskService;
import panetina.elarion.core.registry.ElarionRegistries;

public final class ElarionApi {
    private static ElarionApi instance;

    private final CitizenService citizens;
    private final RealmService realms;
    private final TitleService titles;
    private final AbilityService abilities;
    private final IdentityService identities;
    private final IdentitySyncService identitySync;
    private final NicknameService nicknames;
    private final HistoryService history;
    private final PrivateMessageService privateMessages;
    private final ChatService chat;
    private final RealmGovernanceService governance;
    private final RealmSpawnService realmSpawns;
    private final RealmDeliveryService realmDeliveries;
    private final RewardActionService rewards;
    private final PlayerStatsService playerStats;
    private final ProgressionService progression;
    private final ElarionEventBus events;
    private final ElarionCommandRegistry commands;
    private final ElarionRegistries registries;
    private final ElarionTaskService tasks;
    private final ElarionIdentityApi identityApi;
    private final ElarionRealmApi realmApi;
    private final ElarionMessagingApi messagingApi;
    private final ElarionProgressionApi progressionApi;
    private final ElarionSystemApi systemApi;
    private final ElarionPublicHistoryApi publicHistoryApi;

    public ElarionApi(
            CitizenService citizens,
            RealmService realms,
            TitleService titles,
            AbilityService abilities,
            IdentityService identities,
            IdentitySyncService identitySync,
            NicknameService nicknames,
            HistoryService history,
            PrivateMessageService privateMessages,
            ChatService chat,
            RealmGovernanceService governance,
            RealmSpawnService realmSpawns,
            RealmDeliveryService realmDeliveries,
            RewardActionService rewards,
            PlayerStatsService playerStats,
            ProgressionService progression,
            ElarionEventBus events,
            ElarionCommandRegistry commands,
            ElarionRegistries registries,
            ElarionTaskService tasks
    ) {
        if (instance != null) throw new IllegalStateException("ElarionApi is already initialized");
        this.citizens = citizens;
        this.realms = realms;
        this.titles = titles;
        this.abilities = abilities;
        this.identities = identities;
        this.identitySync = identitySync;
        this.nicknames = nicknames;
        this.history = history;
        this.privateMessages = privateMessages;
        this.chat = chat;
        this.governance = governance;
        this.realmSpawns = realmSpawns;
        this.realmDeliveries = realmDeliveries;
        this.rewards = rewards;
        this.playerStats = playerStats;
        this.progression = progression;
        this.events = events;
        this.commands = commands;
        this.registries = registries;
        this.tasks = tasks;
        this.identityApi = new ElarionIdentityApi(identities, identitySync, nicknames, titles);
        this.realmApi = new ElarionRealmApi(citizens, realms, governance, realmSpawns, realmDeliveries);
        this.messagingApi = new ElarionMessagingApi(chat, privateMessages);
        this.progressionApi = new ElarionProgressionApi(playerStats, progression, rewards, history);
        this.systemApi = new ElarionSystemApi(abilities, events, commands, registries, tasks);
        this.publicHistoryApi = new ElarionPublicHistoryApi(history);
        instance = this;
    }

    public static ElarionApi get() {
        if (instance == null) throw new IllegalStateException("Elarion Core has not initialized yet");
        return instance;
    }

    public CitizenService citizens() { return citizens; }
    public RealmService realms() { return realms; }
    public TitleService titles() { return titles; }
    public AbilityService abilities() { return abilities; }
    public IdentityService identities() { return identities; }
    public IdentitySyncService identitySync() { return identitySync; }
    public NicknameService nicknames() { return nicknames; }
    public HistoryService history() { return history; }
    public PrivateMessageService privateMessages() { return privateMessages; }
    public ChatService chat() { return chat; }
    public RealmGovernanceService governance() { return governance; }
    public RealmSpawnService realmSpawns() { return realmSpawns; }
    public RealmDeliveryService realmDeliveries() { return realmDeliveries; }
    public RewardActionService rewards() { return rewards; }
    public PlayerStatsService playerStats() { return playerStats; }
    public ProgressionService progression() { return progression; }
    public ElarionEventBus events() { return events; }
    public ElarionCommandRegistry commands() { return commands; }
    public ElarionRegistries registries() { return registries; }
    public ElarionTaskService tasks() { return tasks; }
    public ElarionIdentityApi identity() { return identityApi; }
    public ElarionRealmApi realm() { return realmApi; }
    public ElarionMessagingApi messaging() { return messagingApi; }
    public ElarionProgressionApi progressionApi() { return progressionApi; }
    public ElarionSystemApi system() { return systemApi; }
    public ElarionPublicHistoryApi publicHistory() { return publicHistoryApi; }
}
