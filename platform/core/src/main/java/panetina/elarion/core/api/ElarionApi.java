package panetina.elarion.core.api;

import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.service.AbilityService;
import panetina.elarion.core.service.ChatService;
import panetina.elarion.core.service.CitizenService;
import panetina.elarion.core.service.CommunityService;
import panetina.elarion.core.service.IdentityService;
import panetina.elarion.core.service.IdentitySyncService;
import panetina.elarion.core.service.RewardActionService;
import panetina.elarion.core.service.TitleService;

public final class ElarionApi {
    private static ElarionApi instance;

    private final CitizenService citizens;
    private final CommunityService communities;
    private final TitleService titles;
    private final AbilityService abilities;
    private final IdentityService identities;
    private final IdentitySyncService identitySync;
    private final ChatService chat;
    private final RewardActionService rewards;
    private final ElarionEventBus events;
    private final ElarionCommandRegistry commands;

    public ElarionApi(
            CitizenService citizens,
            CommunityService communities,
            TitleService titles,
            AbilityService abilities,
            IdentityService identities,
            IdentitySyncService identitySync,
            ChatService chat,
            RewardActionService rewards,
            ElarionEventBus events,
            ElarionCommandRegistry commands
    ) {
        if (instance != null) throw new IllegalStateException("ElarionApi is already initialized");
        this.citizens = citizens;
        this.communities = communities;
        this.titles = titles;
        this.abilities = abilities;
        this.identities = identities;
        this.identitySync = identitySync;
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
    public CommunityService communities() { return communities; }
    public TitleService titles() { return titles; }
    public AbilityService abilities() { return abilities; }
    public IdentityService identities() { return identities; }
    public IdentitySyncService identitySync() { return identitySync; }
    public ChatService chat() { return chat; }
    public RewardActionService rewards() { return rewards; }
    public ElarionEventBus events() { return events; }
    public ElarionCommandRegistry commands() { return commands; }
}
