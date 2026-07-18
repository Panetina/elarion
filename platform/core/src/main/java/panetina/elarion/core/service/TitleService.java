package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.model.ProgressionEvent;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.TitleOwnershipMode;
import panetina.elarion.core.model.TitleUnlockRule;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.storage.TitleClaimStorage;
import panetina.elarion.core.storage.TitleClaimStorage.TitleClaim;
import panetina.elarion.core.storage.TitleClaimStorage.TitleClaimState;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TitleService {
    private final CoreConfigManager config;
    private final CitizenService citizens;
    private final TitleClaimStorage claimStorage;
    private final HistoryService history;
    private final ElarionEventBus events;
    private net.minecraft.server.MinecraftServer server;
    private TitleClaimState claimState = new TitleClaimState();
    private ElarionNotificationService notifications;

    public record TitleOperation(boolean success, String message) {}

    public TitleService(
            CoreConfigManager config,
            CitizenService citizens,
            TitleClaimStorage claimStorage,
            HistoryService history,
            ElarionEventBus events
    ) {
        this.config = config;
        this.citizens = citizens;
        this.claimStorage = claimStorage;
        this.history = history;
        this.events = events;
    }

    public void bind(net.minecraft.server.MinecraftServer server) {
        this.server = server;
        this.claimState = claimStorage.load(server);
    }

    public void setNotifications(ElarionNotificationService notifications) {
        this.notifications = notifications;
    }

    public Collection<TitleDefinition> all() {
        return config.titles().values();
    }

    public Optional<TitleDefinition> find(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(config.titles().get(id.toLowerCase(Locale.ROOT)));
    }

    public Optional<TitleDefinition> forCitizen(CitizenRecord citizen) {
        return find(citizen.activeTitleId())
                .or(() -> activeDefaultTitle(citizen));
    }

    public boolean assign(ServerPlayerEntity player, String titleId) {
        return grant(player, titleId, player.getUuid(), "legacy-assign").success()
                && setActive(player, titleId, player.getUuid(), "legacy-assign").success();
    }

    public void clear(ServerPlayerEntity player) {
        clearActive(player, player.getUuid(), "legacy-clear");
    }

    public List<TitleDefinition> unlockedFor(CitizenRecord citizen) {
        ensureDefaultTitle(citizen);
        return citizen.unlockedTitleIds().stream()
                .map(this::find)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(TitleDefinition::priority).reversed()
                        .thenComparing(TitleDefinition::id))
                .toList();
    }

    public List<TitleDefinition> visibleForPlayer(CitizenRecord citizen) {
        return unlockedFor(citizen).stream()
                .filter(title -> !title.hiddenFromDiscovery() || citizen.hasUnlockedTitle(title.id()))
                .toList();
    }

    public TitleOperation grant(ServerPlayerEntity player, String titleId, UUID actorId, String reason) {
        return grant(citizens.getOrCreate(player), titleId, actorId, reason);
    }

    public TitleOperation grant(CitizenRecord citizen, String titleId, UUID actorId, String reason) {
        Optional<TitleDefinition> maybeTitle = find(titleId);
        if (maybeTitle.isEmpty()) return new TitleOperation(false, "Unknown title: " + titleId);
        TitleDefinition title = maybeTitle.get();
        if (citizen.hasUnlockedTitle(title.id())) {
            return new TitleOperation(true, "Ember already has title: " + title.id());
        }
        if (title.ownershipMode() == TitleOwnershipMode.GLOBALLY_UNIQUE) {
            if (claimState.retiredTitles().contains(title.id())) {
                return new TitleOperation(false, "Unique title was retired with its former character: " + title.id());
            }
            TitleOperation claim = claimUnique(citizen.uuid(), title, actorId, reason);
            if (!claim.success()) return claim;
        }
        citizen.unlockTitle(title.id(), System.currentTimeMillis());
        citizens.save(citizen, "title-granted");
        history.recordChronicle("title", "granted", actorId, "player",
                citizen.uuid().toString(), citizen.realmId(), Map.of(
                        "title", title.id(),
                        "reason", safe(reason)
                ), citizenName(citizen) + " received the title " + title.displayName() + ".");
        emitTitleEvent(citizen, title, actorId, reason, true);
        notifyTitle(citizen, title, true);
        return new TitleOperation(true, "Granted title " + title.id());
    }

    public TitleOperation revoke(ServerPlayerEntity player, String titleId, UUID actorId, String reason) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        Optional<TitleDefinition> maybeTitle = find(titleId);
        if (maybeTitle.isEmpty()) return new TitleOperation(false, "Unknown title: " + titleId);
        TitleDefinition title = maybeTitle.get();
        if (!citizen.hasUnlockedTitle(title.id())) {
            return new TitleOperation(false, "Ember does not have title: " + title.id());
        }
        citizen.revokeTitle(title.id());
        citizens.save(citizen, "title-revoked");
        history.recordChronicle("title", "revoked", actorId, "player",
                citizen.uuid().toString(), citizen.realmId(), Map.of(
                        "title", title.id(),
                        "reason", safe(reason)
                ), citizenName(citizen) + " lost the title " + title.displayName() + ".");
        emitTitleEvent(citizen, title, actorId, reason, false);
        notifyTitle(citizen, title, false);
        return new TitleOperation(true, "Revoked title " + title.id());
    }

    private void notifyTitle(CitizenRecord citizen, TitleDefinition title, boolean granted) {
        if (notifications == null) return;
        notifications.publishPersonal(
                citizen.uuid(),
                ElarionNotificationCategory.PERSONAL,
                "elarion_core",
                granted ? "title-unlocked" : "title-revoked",
                "title:" + title.id() + ":" + (granted ? "granted:" : "revoked:") + System.currentTimeMillis(),
                granted ? "Title Unlocked" : "Title Revoked",
                granted
                        ? "You received the title " + title.displayName() + "."
                        : "You no longer hold the title " + title.displayName() + ".",
                "Title",
                "item:minecraft:name_tag",
                List.of(new ElarionNotificationAction(ElarionNotificationService.DISMISS, "Dismiss", true)),
                Map.of("titleId", title.id()),
                notifications.defaultExpiry());
        if (granted && title.ownershipMode() != TitleOwnershipMode.UNLIMITED) {
            notifications.publishWorld(
                    "elarion_core",
                    "public-title-unlocked",
                    "title-public:" + title.id() + ":" + citizen.uuid(),
                    "A Unique Title Was Claimed",
                    citizenName(citizen) + " unlocked the title " + title.displayName() + ".",
                    "World Title",
                    "item:minecraft:name_tag",
                    List.of(new ElarionNotificationAction(
                            ElarionNotificationService.DISMISS, "Dismiss", true)),
                    Map.of("titleId", title.id(), "citizenId", citizen.uuid().toString()),
                    notifications.defaultExpiry());
        }
    }

    public TitleOperation setActive(ServerPlayerEntity player, String titleId, UUID actorId, String reason) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        Optional<TitleDefinition> maybeTitle = find(titleId);
        if (maybeTitle.isEmpty()) return new TitleOperation(false, "Unknown title: " + titleId);
        TitleDefinition title = maybeTitle.get();
        ensureDefaultTitle(citizen);
        if (!citizen.hasUnlockedTitle(title.id())) {
            return new TitleOperation(false, "Ember has not unlocked title: " + title.id());
        }
        citizen.setActiveTitleId(title.id());
        citizens.save(citizen, "title-active-set");
        history.recordChronicle("title", "active-set", actorId, "player",
                citizen.uuid().toString(), citizen.realmId(), Map.of(
                        "title", title.id(),
                        "reason", safe(reason)
                ), citizenName(citizen) + " chose the title " + title.displayName() + ".");
        return new TitleOperation(true, "Active title set to " + title.id());
    }

    public TitleOperation clearActive(ServerPlayerEntity player, UUID actorId, String reason) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        String previous = citizen.activeTitleId();
        citizen.clearActiveTitle();
        citizens.save(citizen, "title-active-cleared");
        history.recordChronicle("title", "active-cleared", actorId, "player",
                citizen.uuid().toString(), citizen.realmId(), Map.of(
                        "previousTitle", previous,
                        "reason", safe(reason)
                ), citizenName(citizen) + " set aside their active title.");
        return new TitleOperation(true, "Active title cleared.");
    }

    public Map<String, TitleClaim> claims() {
        return Map.copyOf(claimState.claims());
    }

    public synchronized int retireUniqueClaims(UUID owner, String reason) {
        int retired = 0;
        for (Map.Entry<String, TitleClaim> entry : claimState.claims().entrySet()) {
            if (!entry.getValue().owner().equals(owner)) continue;
            if (claimState.retiredTitles().add(entry.getKey())) retired++;
        }
        if (retired > 0) {
            saveClaims();
            history.recordChronicle("title", "unique-claims-retired", owner, "player", owner.toString(),
                    "", Map.of("count", Integer.toString(retired), "reason", safe(reason)),
                    "Globally unique titles were retired with a dead character.");
        }
        return retired;
    }

    public TitleOperation releaseClaim(String titleId, UUID actorId, String reason) {
        String normalized = normalize(titleId);
        TitleClaim removed = claimState.claims().remove(normalized);
        if (removed == null) return new TitleOperation(false, "No unique claim for title: " + normalized);
        saveClaims();
        history.recordChronicle("title", "unique-claim-released", actorId, "title",
                normalized, "", Map.of("title", normalized, "reason", safe(reason)),
                "The unique claim to " + normalized + " was released.");
        return new TitleOperation(true, "Released unique claim for " + normalized);
    }

    public TitleOperation repair(CitizenRecord citizen, UUID actorId) {
        boolean changed = false;
        for (String titleId : List.copyOf(citizen.unlockedTitleIds())) {
            if (find(titleId).isEmpty()) {
                citizen.revokeTitle(titleId);
                changed = true;
            }
        }
        ensureDefaultTitle(citizen);
        if (!citizen.activeTitleId().isBlank() && !citizen.hasUnlockedTitle(citizen.activeTitleId())) {
            citizen.clearActiveTitle();
            changed = true;
        }
        if (changed) {
            citizens.save(citizen, "title-repaired");
            history.record("title", "repaired", actorId, "player",
                    citizen.uuid().toString(), citizen.realmId(), Map.of());
        }
        return new TitleOperation(true, changed ? "Repaired title state." : "Title state was already valid.");
    }

    public void checkStatUnlocks(UUID uuid, String statKey, long value) {
        for (TitleUnlockRule rule : config.titleUnlockRules().values()) {
            if (!rule.isStatRule() || !rule.statKey().equals(normalize(statKey)) || value < rule.threshold()) {
                continue;
            }
            unlockFromRule(uuid, rule, uuid, "stat-threshold:" + rule.statKey(), value);
        }
    }

    public TitleOperation unlockFromProgression(UUID uuid, TitleUnlockRule rule, ProgressionEvent event, long progress) {
        UUID actorId = event == null ? uuid : event.actorId();
        return unlockFromRule(uuid, rule, actorId, "progression:" + rule.id(), progress);
    }

    private TitleOperation unlockFromRule(UUID uuid, TitleUnlockRule rule, UUID actorId, String reason, long progress) {
        Optional<TitleDefinition> maybeTitle = find(rule.titleId());
        if (maybeTitle.isEmpty()) return new TitleOperation(false, "Unknown title: " + rule.titleId());
        CitizenRecord citizen = citizens.find(uuid).orElse(null);
        if (citizen == null) return new TitleOperation(false, "Unknown Ember: " + uuid);
        TitleDefinition title = maybeTitle.get();
        if (citizen.hasUnlockedTitle(title.id())) return new TitleOperation(true, "Ember already has title: " + title.id());
        TitleOperation grant = grant(citizen, title.id(), actorId, reason);
        if (grant.success()) {
            history.recordChronicle("title", "progression-unlocked", actorId, "player",
                    citizen.uuid().toString(), citizen.realmId(), Map.of(
                            "title", title.id(),
                            "rule", rule.id(),
                            "progress", String.valueOf(progress)
                    ), citizenName(citizen) + " unlocked " + title.displayName()
                            + " through their deeds.");
        }
        return grant;
    }

    private Optional<TitleDefinition> activeDefaultTitle(CitizenRecord citizen) {
        ensureDefaultTitle(citizen);
        return find(citizen.activeTitleId());
    }

    private void ensureDefaultTitle(CitizenRecord citizen) {
        String defaultTitle = config.defaultTitleId();
        if (defaultTitle == null || defaultTitle.isBlank()) return;
        find(defaultTitle).ifPresent(title -> {
            if (!citizen.hasUnlockedTitle(title.id())) {
                citizen.unlockTitle(title.id(), citizen.joinedAt());
            }
        });
    }

    private TitleOperation claimUnique(CitizenRecord citizen, TitleDefinition title, UUID actorId, String reason) {
        return claimUnique(citizen.uuid(), title, actorId, reason);
    }

    private synchronized TitleOperation claimUnique(UUID citizenId, TitleDefinition title, UUID actorId, String reason) {
        TitleClaim existing = claimState.claims().get(title.id());
        if (existing != null) {
            if (existing.owner().equals(citizenId)) {
                return new TitleOperation(true, "Ember already owns unique claim: " + title.id());
            }
            history.recordChronicle("title", "unique-claim-failed", actorId, "title",
                    title.id(), "", Map.of(
                            "title", title.id(),
                            "currentOwner", existing.owner().toString(),
                            "challenger", citizenId.toString(),
                            "reason", safe(reason)
                    ), "A claim to the unique title " + title.displayName()
                            + " failed because it was already held.");
            return new TitleOperation(false, "Unique title is already claimed: " + title.id());
        }
        claimState.claims().put(title.id(), new TitleClaim(citizenId, System.currentTimeMillis(), safe(reason)));
        saveClaims();
        history.recordChronicle("title", "unique-claim-succeeded", actorId, "title",
                title.id(), "", Map.of(
                        "title", title.id(),
                        "owner", citizenId.toString(),
                        "reason", safe(reason)
                ), "The unique title " + title.displayName() + " was claimed for the first time.");
        return new TitleOperation(true, "Unique title claimed: " + title.id());
    }

    private void saveClaims() {
        if (server != null) claimStorage.save(server, claimState);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String citizenName(CitizenRecord citizen) {
        return citizen.nickname() == null || citizen.nickname().isBlank()
                ? citizen.lastKnownUsername()
                : citizen.nickname();
    }

    private void emitTitleEvent(
            CitizenRecord citizen,
            TitleDefinition title,
            UUID actorId,
            String reason,
            boolean granted
    ) {
        events.emitDomainEvent(ElarionDomainEvent.of(
                "elarion_core",
                granted ? "title-granted" : "title-revoked",
                actorId,
                citizen.realmId(),
                "title",
                title.id(),
                Map.of(
                        "citizenId", citizen.uuid().toString(),
                        "citizenName", citizenName(citizen),
                        "displayName", title.displayName(),
                        "ownershipMode", title.ownershipMode().name(),
                        "reason", safe(reason))));
    }
}
