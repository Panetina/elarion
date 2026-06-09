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
import panetina.elarion.core.service.RewardActionService;
import panetina.elarion.core.service.TitleService;

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
    private final RewardActionService rewards;
    private final ElarionEventBus events;
    private final ElarionCommandRegistry commands;

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
            RewardActionService rewards,
            ElarionEventBus events,
            ElarionCommandRegistry commands
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
        this.rewards = rewards;
        this.events = events;
        this.commands = commands;
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
    public RewardActionService rewards() { return rewards; }
    public ElarionEventBus events() { return events; }
    public ElarionCommandRegistry commands() { return commands; }
}
