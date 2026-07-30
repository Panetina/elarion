package panetina.elarion.addons.government.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.government.model.GovernmentCivicScreen;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentFoundingPhase;
import panetina.elarion.addons.government.model.GovernmentGateStatus;
import panetina.elarion.addons.government.model.GovernmentLawRecord;
import panetina.elarion.addons.government.model.GovernmentOfficeTermRecord;
import panetina.elarion.addons.government.model.GovernmentVoteOption;
import panetina.elarion.addons.government.model.GovernmentProposalRecord;
import panetina.elarion.addons.government.model.GovernmentProposalStatus;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.government.model.RealmIdentityRules;
import panetina.elarion.addons.offerings.api.ElarionOfferingsApi;
import panetina.elarion.addons.government.storage.GovernmentState;
import panetina.elarion.addons.government.storage.GovernmentStorage;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RealmPresentation;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.PublicHistoryConsumer;
import panetina.elarion.core.model.PublicHistoryEntry;
import panetina.elarion.core.model.PublicHistoryQuery;
import panetina.elarion.core.service.ElarionNotificationService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class GovernmentStateService {
    private static final Duration FOUNDING_VOTE_DURATION = Duration.ofHours(24);
    private static final Duration RUNOFF_VOTE_DURATION = Duration.ofHours(12);
    private static final int VOTE_RESOLUTION_INTERVAL_TICKS = 20;
    private static final String SOURCE_SYSTEM = "elarion_government";
    private static final String OPEN_CIVIC_FORUM_ACTION = "elarion_government:open_civic_forum";
    private static final String NOTIFICATION_ICON = "realm";
    private static final Map<String, String> AUTHORITY_TITLE_IDS = Map.of(
            "monarch", "government_monarch",
            "heir", "government_heir",
            "president", "government_president",
            "officer", "government_officer");
    private static final List<String> AUTHORITY_TITLE_PRIORITY = List.of(
            "monarch", "president", "heir", "officer");
    private static final Set<String> AUTHORITY_TITLE_VALUES = Set.copyOf(AUTHORITY_TITLE_IDS.values());
    private final ElarionApi api;
    private final GovernmentDefinitionService definitions;
    private final GovernmentStorage storage;
    private GovernmentState state = new GovernmentState();
    private final Map<UUID, List<GovernmentOfficeTermRecord>> officeTermsByHolder = new LinkedHashMap<>();
    private MinecraftServer server;
    private int voteResolutionTicks;
    private int authorityCleanupTicks;

    public GovernmentStateService(ElarionApi api, GovernmentDefinitionService definitions, GovernmentStorage storage) {
        this.api = api;
        this.definitions = definitions;
        this.storage = storage;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        state = storage.load(server);
        if (state.realms == null) state.realms = new LinkedHashMap<>();
        if (state.votes == null) state.votes = new LinkedHashMap<>();
        if (state.proposals == null) state.proposals = new LinkedHashMap<>();
        if (state.laws == null) state.laws = new LinkedHashMap<>();
        if (state.officeTerms == null) state.officeTerms = new LinkedHashMap<>();
        if (state.authorityTitleRestores == null) state.authorityTitleRestores = new LinkedHashMap<>();
        api.realms().all().forEach(realm ->
                state.realms.computeIfAbsent(realm.id(), RealmGovernmentState::empty));
        boolean migratedForms = migrateRemovedForms();
        boolean migratedOffices = migrateRemovedOffices();
        boolean repairedFounding = reconcileFoundingCompletion();
        officeTermsByHolder.clear();
        reconcileOfficeTerms();
        rebuildOfficeTermIndex();
        sanitizeStoredVotes();
        boolean repairedColor = reconcileResolvedColorVotes();
        reconcileAuthorityTitles();
        save();
        if (migratedForms || migratedOffices || repairedColor || repairedFounding) refreshIdentities();
    }

    public void tick() {
        if (server == null) return;
        voteResolutionTicks++;
        if (voteResolutionTicks >= VOTE_RESOLUTION_INTERVAL_TICKS) {
            voteResolutionTicks = 0;
            resolveExpiredVotes(System.currentTimeMillis());
        }
        authorityCleanupTicks++;
        if (authorityCleanupTicks < definitions.settings().authorityInactivityCheckIntervalTicks()) return;
        authorityCleanupTicks = 0;
        removeInactiveAuthority(System.currentTimeMillis());
    }

    public RealmGovernmentState realm(String realmId) {
        if (api.realms().find(realmId).isEmpty()) throw new IllegalArgumentException("Unknown Realm " + realmId);
        return state.realms.computeIfAbsent(realmId, RealmGovernmentState::empty);
    }

    public Collection<RealmGovernmentState> realms() {
        return state.realms.values();
    }

    public String realmDisplayName(String realmId) {
        return api.realms().find(realmId).map(api.realms()::displayName).orElse(realmId == null ? "" : realmId);
    }

    /** Bounded read model for external presentation; Government remains the office authority. */
    public String officeDisplayName(UUID citizenId) {
        if (citizenId == null) return "";
        CitizenRecord citizen = api.citizens().find(citizenId).orElse(null);
        if (citizen == null || citizen.realmId().isBlank()) return "";
        RealmGovernmentState government = realm(citizen.realmId());
        String formId = government.activeGovernmentFormId();
        if (formId.isBlank()) return "";
        GovernmentFormDefinition form = definitions.require(formId);
        return government.officeHolders().entrySet().stream()
                .filter(entry -> entry.getValue().contains(citizenId))
                .map(Map.Entry::getKey)
                .sorted()
                .map(officeId -> GovernmentFoundingPhasePolicy.officeLabel(form, officeId))
                .filter(label -> label != null && !label.isBlank())
                .findFirst()
                .orElse("");
    }

    public int resetRealm(String realmId) {
        String normalizedRealm = normalize(realmId);
        if (api.realms().find(normalizedRealm).isEmpty()) {
            throw new IllegalArgumentException("Unknown Realm " + realmId);
        }
        restoreAuthorityTitlesForRealm(normalizedRealm);
        state.realms.put(normalizedRealm, RealmGovernmentState.empty(normalizedRealm));
        state.votes.entrySet().removeIf(entry -> normalizedRealm.equals(normalize(entry.getValue().realmId)));
        state.proposals.entrySet().removeIf(entry -> normalizedRealm.equals(normalize(entry.getValue().realmId())));
        state.laws.entrySet().removeIf(entry -> normalizedRealm.equals(normalize(entry.getValue().realmId())));
        state.officeTerms.entrySet().removeIf(entry -> normalizedRealm.equals(normalize(entry.getValue().realmId())));
        rebuildOfficeTermIndex();
        state.authorityTitleRestores.keySet().removeIf(key -> key.startsWith(normalizedRealm + "|"));
        save();
        refreshIdentities();
        return 1;
    }

    public int resetAllRealms() {
        restoreAllAuthorityTitles();
        state.realms.clear();
        api.realms().all().forEach(realm -> state.realms.put(realm.id(), RealmGovernmentState.empty(realm.id())));
        state.votes.clear();
        state.proposals.clear();
        state.laws.clear();
        state.officeTerms.clear();
        officeTermsByHolder.clear();
        state.authorityTitleRestores.clear();
        save();
        refreshIdentities();
        return state.realms.size();
    }

    private boolean migrateRemovedOffices() {
        boolean changed = false;
        for (RealmGovernmentState current : List.copyOf(state.realms.values())) {
            if (!"republic".equals(current.activeGovernmentFormId())) continue;
            if (!current.officeHolders().containsKey("council_member")) continue;
            Map<String, Set<UUID>> offices = mutableOffices(current);
            Set<UUID> removed = offices.remove("council_member");
            state.realms.put(current.realmId(), new RealmGovernmentState(
                    current.realmId(),
                    current.activeGovernmentFormId(),
                    current.votedDisplayName(),
                    current.votedTag(),
                    current.votedColor(),
                    offices,
                    current.activeLawIds(),
                    current.pendingProposalIds(),
                    current.nameVoteCompletedAt(),
                    current.colorVoteCompletedAt(),
                    current.foundingElectionCompletedAt(),
                    current.lastReformAt()));
            if (removed != null) {
                removed.forEach(citizenId -> restoreOrPromoteAuthorityTitle(current.realmId(), offices, citizenId));
            }
            changed = true;
        }
        if (changed) {
            state.officeTerms.entrySet().removeIf(entry -> "council_member".equals(entry.getValue().officeId()));
            rebuildOfficeTermIndex();
        }
        return changed;
    }

    public RealmGovernmentState setForm(String realmId, String formId) {
        String normalizedRealm = normalize(realmId);
        GovernmentFormDefinition form = definitions.require(formId);
        RealmGovernmentState updated = realm(normalizedRealm).withForm(formId);
        state.realms.put(normalizedRealm, updated);
        save();
        refreshIdentities();
        api.history().recordChronicle("government", "form-set", null, "realm", normalizedRealm, normalizedRealm,
                java.util.Map.of("governmentForm", formId),
                "The Realm " + officialName(normalizedRealm) + " was assigned the government form " + formId + ".");
        api.realmDeliveries().notifyRealm(
                normalizedRealm,
                "Government Form Selected",
                officialName(normalizedRealm) + " has chosen " + form.displayName() + ".",
                "government",
                null);
        emit("government-form-selected", null, normalizedRealm, "government-form", formId,
                Map.of("form", formId, "realmOfficial", officialName(normalizedRealm)));
        return updated;
    }

    public RealmGovernmentState setVotedIdentity(String realmId, String displayName, String tag) {
        String normalizedRealm = normalize(realmId);
        String validatedName = RealmIdentityRules.validateName(displayName);
        String validatedTag = RealmIdentityRules.validateTag(tag);
        RealmGovernmentState updated = realm(normalizedRealm).withVotedIdentity(
                validatedName,
                validatedTag);
        state.realms.put(normalizedRealm, updated);
        save();
        refreshIdentities();
        api.history().recordChronicle("government", "realm-identity-set", null, "realm", normalizedRealm,
                normalizedRealm, java.util.Map.of("displayName", updated.votedDisplayName(), "tag", updated.votedTag()),
                "The Realm " + updated.votedDisplayName() + " recorded its founding name.");
        api.realmDeliveries().notifyRealm(
                normalizedRealm,
                "Realm Name Selected",
                "Embers selected " + updated.votedDisplayName() + " as the Realm name and ["
                        + updated.votedTag() + "] as its public tag.",
                "government",
                null);
        emit("realm-name-selected", null, normalizedRealm, "realm", normalizedRealm,
                Map.of("displayName", updated.votedDisplayName(), "tag", updated.votedTag()));
        return updated;
    }

    public RealmGovernmentState setVotedColor(String realmId, String color) {
        String normalizedRealm = normalize(realmId);
        String normalizedColor = GovernmentTextRules.normalizeColor(color);
        RealmGovernmentState updated = realm(normalizedRealm).withVotedColor(normalizedColor);
        state.realms.put(normalizedRealm, updated);
        save();
        refreshIdentities();
        api.history().recordChronicle("government", "realm-color-set", null, "realm", normalizedRealm,
                normalizedRealm, Map.of("color", normalizedColor),
                "The Realm " + officialName(normalizedRealm) + " selected its public color.");
        api.realmDeliveries().notifyRealm(
                normalizedRealm,
                "Realm Color Selected",
                officialName(normalizedRealm) + " selected " + GovernmentTextRules.colorLabel(normalizedColor) + " as its public color.",
                "government",
                null);
        emit("realm-color-selected", null, normalizedRealm, "realm", normalizedRealm,
                Map.of("color", normalizedColor));
        return updated;
    }

    public RealmGovernmentState markFoundingElectionComplete(String realmId) {
        String normalizedRealm = normalize(realmId);
        RealmGovernmentState current = realm(normalizedRealm);
        if (current.activeGovernmentFormId().isBlank()) {
            throw new IllegalArgumentException("A government form must be chosen first.");
        }
        RealmGovernmentState updated = current.withFoundingElectionComplete();
        state.realms.put(normalizedRealm, updated);
        save();
        refreshIdentities();
        api.history().recordChronicle("government", "founding-election-complete", null, "realm", normalizedRealm,
                normalizedRealm, java.util.Map.of("governmentForm", updated.activeGovernmentFormId()),
                "The Realm " + officialName(normalizedRealm) + " completed its first founding election.");
        api.realmDeliveries().notifyRealm(
                normalizedRealm,
                "Founding Election Complete",
                officialName(normalizedRealm) + " elected its first authority holders.",
                "government",
                null);
        emit("founding-election-completed", null, normalizedRealm, "realm", normalizedRealm,
                Map.of("form", updated.activeGovernmentFormId(), "realmOfficial", officialName(normalizedRealm)));
        return updated;
    }

    public GovernmentGateStatus gates(String realmId) {
        String normalizedRealm = normalize(realmId);
        RealmGovernmentState realm = realm(normalizedRealm);
        return new GovernmentGateStatus(
                normalizedRealm,
                ElarionOfferingsApi.get().hasRealmFlag(normalizedRealm, "foundation_i"),
                ElarionOfferingsApi.get().hasRealmFlag(normalizedRealm, "foundation_ii"),
                ElarionOfferingsApi.get().hasRealmFlag(normalizedRealm, "foundation_iii"),
                realm.nameVoteCompletedAt() > 0L,
                realm.colorVoteCompletedAt() > 0L,
                !realm.activeGovernmentFormId().isBlank(),
                realm.foundingElectionCompletedAt() > 0L);
    }

    public GovernmentCivicScreen currentCivicScreen(String realmId) {
        GovernmentGateStatus gates = gates(realmId);
        if (!gates.nameChosen()) return GovernmentCivicScreen.REALM_NAME;
        if (!gates.colorChosen()) return GovernmentCivicScreen.REALM_COLOR;
        if (!gates.governmentChosen()) return GovernmentCivicScreen.GOVERNMENT_FORM;
        if (!gates.foundingElectionComplete()) return GovernmentCivicScreen.FOUNDING_ELECTION;
        return GovernmentCivicScreen.CITIZEN_FEATURES;
    }

    public GovernmentVoteState vote(String realmId, GovernmentVoteType type) {
        String key = voteKey(normalize(realmId), type);
        return state.votes.computeIfAbsent(key, ignored -> new GovernmentVoteState(normalize(realmId), type));
    }

    public Optional<GovernmentVoteState> existingVote(String realmId, GovernmentVoteType type) {
        return Optional.ofNullable(state.votes.get(voteKey(normalize(realmId), type)));
    }

    public boolean eligibleCitizen(ServerPlayerEntity player, String realmId) {
        if (player == null) return false;
        CitizenRecord citizen = api.citizens().getOrCreate(player);
        return normalize(realmId).equals(citizen.realmId()) && api.citizens().isActiveCitizen(citizen);
    }

    public GovernmentVoteState proposeRealmName(
            ServerPlayerEntity player,
            String realmId,
            String displayName,
            String tag
    ) {
        String realm = normalize(realmId);
        GovernmentGateStatus gates = gates(realm);
        if (!gates.nameVoteUnlocked()) throw new IllegalArgumentException(gates.nameVoteLockMessage());
        requireEligible(player, realm);
        String cleanName = RealmIdentityRules.validateName(displayName);
        String cleanTag = RealmIdentityRules.validateTag(tag);
        GovernmentVoteState vote = vote(realm, GovernmentVoteType.REALM_NAME);
        long now = System.currentTimeMillis();
        boolean openingProposalWindow = vote.proposalStartedAt <= 0L;
        if (vote.proposalEnded(now)) {
            throw new IllegalArgumentException("Realm name proposals are closed.");
        }
        UUID proposerId = player.getUuid();
        boolean alreadyProposed = vote.options.values().stream()
                .anyMatch(option -> proposerId.equals(option.proposedBy));
        if (alreadyProposed) {
            throw new IllegalArgumentException("You already proposed a Realm name.");
        }
        String id = "name_" + Integer.toUnsignedString((cleanName + "|" + cleanTag)
                .toLowerCase(java.util.Locale.ROOT).hashCode(), 36);
        if (vote.options.containsKey(id)) {
            throw new IllegalArgumentException("That Realm name and tag were already proposed.");
        }
        vote.options.put(id, GovernmentVoteOption.realmName(id, cleanName, cleanTag, player.getUuid()));
        vote.startProposalIfNeeded(now, FOUNDING_VOTE_DURATION);
        save();
        if (openingProposalWindow) {
            notifyVoteStage(realm, "Realm Name Proposals Open",
                    "Embers may propose a founding name and public tag for the next 24 hours.",
                    "name-proposals:" + vote.round, vote.proposalEndsAt);
        }
        api.history().recordChronicle("government", "name-proposed", player.getUuid(), "realm", realm, realm,
                Map.of("displayName", cleanName, "tag", cleanTag), "A Realm name was proposed.");
        notifyPersonal(player.getUuid(), "name-proposal-accepted",
                realm + ":name-proposal:" + player.getUuid(),
                "Name Proposal Accepted",
                "Your proposal " + cleanName + " [" + cleanTag + "] was added to the Civic Forum.",
                Map.of("realmId", realm, "displayName", cleanName, "tag", cleanTag));
        emit("realm-name-proposed", player.getUuid(), realm, "realm", realm,
                Map.of("displayName", cleanName, "tag", cleanTag));
        return vote;
    }

    public GovernmentVoteState nominateForFoundingElection(ServerPlayerEntity player, String realmId) {
        String realm = normalize(realmId);
        GovernmentFoundingPhase phase = foundingPhase(player, realm);
        if (!phase.canNominate()) throw new IllegalArgumentException(phase.nominationReason());
        RealmGovernmentState current = realm(realm);
        GovernmentFormDefinition form = definitions.require(current.activeGovernmentFormId());
        GovernmentVoteState vote = vote(realm, GovernmentVoteType.FOUNDING_ELECTION);
        long now = System.currentTimeMillis();
        boolean openingProposalWindow = vote.proposalStartedAt <= 0L;
        boolean added = false;
        for (String officeId : List.of(phase.officeId())) {
            String id = officeId + ":" + player.getUuid();
            if (vote.options.containsKey(id)) {
                continue;
            }
            String title = citizenName(api.citizens().getOrCreate(player)) + " - "
                    + GovernmentFoundingPhasePolicy.officeLabel(form, officeId);
            String body = "Candidate for " + GovernmentFoundingPhasePolicy.officeLabel(form, officeId);
            vote.options.put(id, GovernmentVoteOption.candidate(
                    id, title, body, officeId, player.getUuid(), ""));
            added = true;
        }
        if (!added && vote.options.isEmpty()) {
            throw new IllegalArgumentException("You are not eligible for any founding office.");
        }
        if (!added) {
            throw new IllegalArgumentException("You already nominated yourself.");
        }
        vote.startProposalIfNeeded(now, FOUNDING_VOTE_DURATION);
        save();
        if (openingProposalWindow) {
            notifyVoteStage(realm, "Founding Nominations Open",
                    "Embers may nominate themselves for the first authority offices.",
                    "founding-nominations:" + vote.round, vote.proposalEndsAt);
        }
        api.history().recordChronicle("government", "founding-nominated", player.getUuid(), "realm", realm, realm,
                Map.of("form", form.id()), "An Ember entered the founding election.");
        notifyPersonal(player.getUuid(), "founding-nomination-accepted",
                realm + ":nomination:" + player.getUuid(),
                "Nomination Accepted",
                "You entered the founding election for " + officialName(realm) + ".",
                Map.of("realmId", realm, "form", form.id()));
        emit("founding-nomination-accepted", player.getUuid(), realm, "realm", realm,
                Map.of("form", form.id()));
        return vote;
    }

    public GovernmentFoundingPhase foundingPhase(ServerPlayerEntity player, String realmId) {
        String realm = normalize(realmId);
        RealmGovernmentState government = realm(realm);
        if (government.activeGovernmentFormId().isBlank()) {
            return new GovernmentFoundingPhase("", "Leadership Election", "Government form required",
                    false, false, false, "Choose a Government form before founding elections.");
        }
        GovernmentFormDefinition form = definitions.require(government.activeGovernmentFormId());
        Optional<GovernmentVoteState> vote = existingVote(realm, GovernmentVoteType.FOUNDING_ELECTION);
        UUID playerId = player == null ? null : player.getUuid();
        boolean activeCitizen = player != null && eligibleCitizen(player, realm);
        return GovernmentFoundingPhasePolicy.phase(form, vote, playerId, activeCitizen,
                gates(realm).foundingElectionUnlocked(), gates(realm).foundingElectionLockMessage(),
                System.currentTimeMillis());
    }

    public GovernmentVoteState castVote(
            ServerPlayerEntity player,
            String realmId,
            GovernmentVoteType type,
            String optionId
    ) {
        String realm = normalize(realmId);
        requireEligible(player, realm);
        validateVoteUnlocked(realm, type);
        GovernmentVoteState vote = vote(realm, type);
        long now = System.currentTimeMillis();
        if (type == GovernmentVoteType.REALM_NAME && !vote.proposalEnded(now)) {
            throw new IllegalArgumentException("Name voting opens after the proposal window ends.");
        }
        if (type == GovernmentVoteType.FOUNDING_ELECTION && !vote.runoff && !vote.proposalEnded(now)) {
            throw new IllegalArgumentException("Founding election voting opens after the nomination window ends.");
        }
        if (type == GovernmentVoteType.GOVERNMENT_FORM && vote.options.isEmpty()) {
            definitions.forms().stream().filter(GovernmentFormDefinition::enabled)
                    .forEach(form -> vote.options.putIfAbsent(form.id(),
                            GovernmentVoteOption.governmentForm(form.id(), form.displayName(), form.description())));
        }
        if (type == GovernmentVoteType.REALM_COLOR && vote.options.isEmpty()) {
            seedColorOptions(vote);
        }
        GovernmentVoteOption option = vote.options.get(optionId);
        if (option == null) throw new IllegalArgumentException("Unknown vote option.");
        boolean openingVote = vote.startedAt <= 0L;
        vote.startIfNeeded(now, FOUNDING_VOTE_DURATION);
        if (openingVote) {
            notifyVoteStage(realm, GovernmentTextRules.voteTitle(type), GovernmentTextRules.voteBody(type),
                    type.name().toLowerCase(java.util.Locale.ROOT) + ":" + vote.round, vote.endsAt);
        }
        String voter = player.getUuid().toString();
        if (type == GovernmentVoteType.FOUNDING_ELECTION) {
            List<String> selected = new ArrayList<>(vote.ballots.getOrDefault(voter, List.of()));
            if (selected.contains(optionId)) selected.remove(optionId);
            else {
                int limit = maxApprovalCount(realm(realm), option.officeId());
                long selectedForOffice = selected.stream()
                        .map(vote.options::get)
                        .filter(candidate -> candidate != null && option.officeId().equals(candidate.officeId))
                        .count();
                if (selectedForOffice >= limit) {
                    throw new IllegalArgumentException("You have reached the approval limit for that office.");
                }
                selected.add(optionId);
            }
            vote.ballots.put(voter, selected);
        } else {
            vote.ballots.put(voter, List.of(optionId));
        }
        save();
        api.history().recordChronicle("government", "vote-cast", player.getUuid(), "realm", realm, realm,
                Map.of("type", type.name().toLowerCase(), "round", Integer.toString(vote.round)),
                "A private Government ballot was cast.");
        notifyPersonal(player.getUuid(), "vote-accepted",
                realm + ":" + type.name().toLowerCase(java.util.Locale.ROOT) + ":round:" + vote.round + ":" + player.getUuid(),
                "Vote Accepted",
                "Your private ballot for " + GovernmentTextRules.voteTitle(type).replace(" Open", "").toLowerCase(java.util.Locale.ROOT)
                        + " was recorded.",
                Map.of("realmId", realm, "voteType", type.name().toLowerCase(java.util.Locale.ROOT),
                        "round", Integer.toString(vote.round)));
        emit("government-vote-cast", player.getUuid(), realm, "realm", realm,
                Map.of("type", type.name().toLowerCase(java.util.Locale.ROOT),
                        "round", Integer.toString(vote.round)));
        return vote;
    }

    public int resolveExpiredVotes(long now) {
        int resolved = 0;
        for (GovernmentVoteState vote : List.copyOf(state.votes.values())) {
            if (!vote.ended(now)) continue;
            resolveVote(vote, now);
            resolved++;
        }
        if (resolved > 0) save();
        return resolved;
    }

    public String advanceCurrentWindow(String realmId) {
        String realm = normalize(realmId);
        GovernmentVoteType type = switch (currentCivicScreen(realm)) {
            case REALM_NAME -> GovernmentVoteType.REALM_NAME;
            case REALM_COLOR -> GovernmentVoteType.REALM_COLOR;
            case GOVERNMENT_FORM -> GovernmentVoteType.GOVERNMENT_FORM;
            case FOUNDING_ELECTION -> GovernmentVoteType.FOUNDING_ELECTION;
            case CITIZEN_FEATURES ->
                    throw new IllegalArgumentException("Founding is already complete for this Realm.");
        };
        GovernmentVoteState vote = existingVote(realm, type)
                .orElseThrow(() -> new IllegalArgumentException("No active civic window exists yet."));
        long now = System.currentTimeMillis();
        if (type == GovernmentVoteType.REALM_NAME && !vote.proposalEnded(now)) {
            if (vote.proposalStartedAt <= 0L) {
                throw new IllegalArgumentException("Submit at least one name proposal first.");
            }
            vote.proposalEndsAt = now - 1L;
            vote.startIfNeeded(now, FOUNDING_VOTE_DURATION);
            save();
            notifyVoteStage(realm, GovernmentTextRules.voteTitle(type), GovernmentTextRules.voteBody(type),
                    type.name().toLowerCase(java.util.Locale.ROOT) + ":" + vote.round, vote.endsAt);
            return "Name proposal window ended. Name voting is now available.";
        }
        if (type == GovernmentVoteType.FOUNDING_ELECTION && !vote.runoff && !vote.proposalEnded(now)) {
            if (vote.proposalStartedAt <= 0L || vote.options.isEmpty()) {
                throw new IllegalArgumentException("Submit at least one founding nomination first.");
            }
            vote.proposalEndsAt = now - 1L;
            vote.startIfNeeded(now, FOUNDING_VOTE_DURATION);
            save();
            notifyVoteStage(realm, GovernmentTextRules.voteTitle(type), GovernmentTextRules.voteBody(type),
                    type.name().toLowerCase(java.util.Locale.ROOT) + ":" + vote.round, vote.endsAt);
            return "Founding nomination window ended. Founding election voting is now available.";
        }
        if (vote.startedAt <= 0L) {
            throw new IllegalArgumentException("Cast at least one ballot before advancing this vote.");
        }
        vote.endsAt = now - 1L;
        int resolved = resolveExpiredVotes(now);
        if (resolved == 0) {
            throw new IllegalStateException("The current civic vote could not be resolved.");
        }
        return vote.runoff
                ? "The current runoff was advanced."
                : "The current civic vote was advanced and resolved.";
    }

    public RealmGovernmentState assignOffice(String realmId, String officeId, UUID citizenId) {
        String normalizedRealm = normalize(realmId);
        var form = definitions.require(realm(normalizedRealm).activeGovernmentFormId());
        var office = form.offices().stream()
                .filter(candidate -> candidate.id().equals(officeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown office " + officeId + " for " + form.id()));
        CitizenRecord citizen = api.citizens().find(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Ember " + citizenId));
        if (!normalizedRealm.equals(citizen.realmId())) {
            throw new IllegalArgumentException("Office holder must belong to the Realm.");
        }
        String conflict = republicOfficeConflict(form.id(), officeId, realm(normalizedRealm), citizenId);
        if (!conflict.isBlank()) {
            throw new IllegalArgumentException(conflict);
        }
        Set<UUID> current = realm(normalizedRealm).officeHolders().getOrDefault(officeId, Set.of());
        if (!current.contains(citizenId) && current.size() >= office.maxHolders()) {
            throw new IllegalArgumentException("Office " + officeId + " is full.");
        }
        RealmGovernmentState updated = realm(normalizedRealm).withOfficeHolder(officeId, citizenId);
        state.realms.put(normalizedRealm, updated);
        ensureOfficeTerm(normalizedRealm, officeId, citizenId, System.currentTimeMillis());
        applyAuthorityTitle(normalizedRealm, updated, citizenId);
        save();
        refreshIdentities();
        api.history().recordChronicle("government", "office-assigned", citizenId, "office", officeId,
                normalizedRealm, java.util.Map.of("office", officeId, "form", form.id()),
                "An Ember was assigned to " + office.displayName() + " in " + officialName(normalizedRealm) + ".");
        api.realmDeliveries().notifyRealm(normalizedRealm, "Authority Appointed",
                citizenName(citizen) + " was appointed as " + office.displayName() + ".",
                "government", citizenId);
        notifyPersonal(citizenId, "office-assigned",
                normalizedRealm + ":office-assigned:" + officeId + ":" + citizenId,
                "Office Assigned",
                "You were assigned as " + office.displayName() + " in " + officialName(normalizedRealm) + ".",
                Map.of("realmId", normalizedRealm, "office", officeId, "form", form.id()));
        emit("office-assigned", citizenId, normalizedRealm, "office", officeId,
                Map.of("office", officeId, "form", form.id()));
        return updated;
    }

    public RealmGovernmentState removeOffice(String realmId, String officeId, UUID citizenId) {
        String normalizedRealm = normalize(realmId);
        RealmGovernmentState current = realm(normalizedRealm);
        boolean heldOffice = current.officeHolders().getOrDefault(officeId, Set.of()).contains(citizenId);
        RealmGovernmentState updated = current.withoutOfficeHolder(officeId, citizenId);
        boolean electionReopened = heldOffice
                && GovernmentFoundingPhasePolicy.shouldReopenLeadershipElection(current, updated, officeId);
        if (electionReopened) {
            updated = updated.withFoundingElectionReopened();
            state.votes.put(voteKey(normalizedRealm, GovernmentVoteType.FOUNDING_ELECTION),
                    new GovernmentVoteState(normalizedRealm, GovernmentVoteType.FOUNDING_ELECTION));
        }
        state.realms.put(normalizedRealm, updated);
        closeOfficeTerm(normalizedRealm, officeId, citizenId, System.currentTimeMillis());
        restoreOrPromoteAuthorityTitle(normalizedRealm, updated, citizenId);
        save();
        refreshIdentities();
        api.history().recordChronicle("government", "office-removed", citizenId, "office", officeId,
                normalizedRealm, java.util.Map.of("office", officeId),
                "An Ember was removed from " + officeId + " in " + officialName(normalizedRealm) + ".");
        api.realmDeliveries().notifyRealm(normalizedRealm, "Authority Changed",
                "The office of " + officeId.replace('_', ' ') + " changed.",
                "government", citizenId);
        notifyPersonal(citizenId, "office-removed",
                normalizedRealm + ":office-removed:" + officeId + ":" + citizenId,
                "Office Removed",
                "You were removed from " + officeId.replace('_', ' ') + " in " + officialName(normalizedRealm) + ".",
                Map.of("realmId", normalizedRealm, "office", officeId));
        emit("office-removed", citizenId, normalizedRealm, "office", officeId,
                Map.of("office", officeId));
        if (electionReopened) {
            GovernmentFormDefinition form = definitions.require(current.activeGovernmentFormId());
            String office = GovernmentFoundingPhasePolicy.officeLabel(form, officeId);
            api.history().recordChronicle("government", "leadership-election-reopened", citizenId,
                    "office", officeId, normalizedRealm,
                    Map.of("office", officeId, "form", form.id(), "reason", "vacancy"),
                    "The " + office + " election reopened after the office became vacant.");
            notifyVoteStage(normalizedRealm, office + " Election Reopened",
                    "The " + office + " office is vacant. Eligible Embers may nominate themselves in the Civic Forum.",
                    "founding-election:vacancy:" + officeId + ":" + System.currentTimeMillis(), 0L);
            emit("leadership-election-reopened", citizenId, normalizedRealm, "office", officeId,
                    Map.of("office", officeId, "form", form.id(), "reason", "vacancy"));
        }
        return updated;
    }

    public List<GovernmentOfficeTermRecord> activeOfficeTerms(String realmId, String officeId) {
        String normalizedRealm = normalize(realmId);
        String normalizedOffice = normalize(officeId);
        return state.officeTerms.values().stream()
                .filter(term -> normalizedRealm.equals(normalize(term.realmId())))
                .filter(term -> normalizedOffice.isBlank() || normalizedOffice.equals(normalize(term.officeId())))
                .filter(GovernmentOfficeTermRecord::active)
                .sorted(Comparator.comparingLong(GovernmentOfficeTermRecord::chosenAt))
                .toList();
    }

    public List<GovernmentOfficeTermRecord> officeTermsFor(UUID holderId, int limit) {
        return GovernmentOfficeTermIndex.query(officeTermsByHolder, holderId, limit);
    }

    public List<PublicHistoryEntry> governmentHistory(String realmId, int limit) {
        String normalizedRealm = normalize(realmId);
        realm(normalizedRealm);
        return api.publicHistory()
                .query(PublicHistoryQuery.forConsumer(PublicHistoryConsumer.GUI_SEARCH)
                        .withCategories(Set.of("government"))
                        .forRealm(normalizedRealm)
                        .limitedTo(Math.max(1, limit))
                        .withinWeeks(8))
                .entries();
    }

    public RealmGovernmentState appointOffice(
            ServerPlayerEntity actor,
            String realmId,
            String officeId,
            String citizenNameOrId
    ) {
        String realm = normalize(realmId);
        RealmGovernmentState government = realm(realm);
        requireOfficeManager(actor, government, officeId, true);
        CitizenRecord citizen = resolveCitizen(realm, citizenNameOrId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Realm Ember " + citizenNameOrId + "."));
        return assignOffice(realm, officeId, citizen.uuid());
    }

    public RealmGovernmentState removeOfficeByActor(
            ServerPlayerEntity actor,
            String realmId,
            String officeId,
            String citizenNameOrId
    ) {
        String realm = normalize(realmId);
        RealmGovernmentState government = realm(realm);
        requireOfficeManager(actor, government, officeId, false);
        CitizenRecord citizen = resolveCitizen(realm, citizenNameOrId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Realm Ember " + citizenNameOrId + "."));
        if (!government.officeHolders().getOrDefault(officeId, Set.of()).contains(citizen.uuid())) {
            throw new IllegalArgumentException(citizenName(citizen) + " does not hold " + officeId + ".");
        }
        return removeOffice(realm, officeId, citizen.uuid());
    }

    public RealmGovernmentState resignOffice(ServerPlayerEntity actor, String realmId, String officeId) {
        String realm = normalize(realmId);
        RealmGovernmentState government = realm(realm);
        if (actor == null || !government.officeHolders().getOrDefault(officeId, Set.of()).contains(actor.getUuid())) {
            throw new IllegalArgumentException("You do not hold that office.");
        }
        return removeOffice(realm, officeId, actor.getUuid());
    }

    public void sendNotice(ServerPlayerEntity actor, String realmId, String title, String body) {
        String realm = normalize(realmId);
        if (!isAuthority(realm, actor.getUuid())) {
            throw new IllegalArgumentException("Only Realm authority holders can send notices.");
        }
        String cleanTitle = GovernmentTextRules.validateShortText(title, "Notice title", 4, 64);
        String cleanBody = GovernmentTextRules.validateShortText(body, "Notice body", 4, 1500);
        String authority = authorityNoticeLabel(realm, actor.getUuid());
        String noticeTitle = truncate("Notice from " + authority, 64);
        String noticeBody = cleanTitle + "\n" + cleanBody;
        api.history().recordChronicle("government", "realm-notice-sent", actor.getUuid(), "realm",
                realm, realm, Map.of("title", cleanTitle), cleanBody);
        api.realmDeliveries().notifyRealm(realm, noticeTitle, noticeBody, "government", actor.getUuid());
        emit("government-realm-notice-sent", actor.getUuid(), realm, "realm", realm,
                Map.of("title", cleanTitle, "authority", authority));
    }

    public boolean isAuthority(String realmId, UUID citizenId) {
        String normalizedRealm = normalize(realmId);
        RealmGovernmentState realm = realm(normalizedRealm);
        if (realm.activeGovernmentFormId().isBlank()) return false;
        var form = definitions.require(realm.activeGovernmentFormId());
        for (String office : form.authorityOffices()) {
            if (realm.officeHolders().getOrDefault(office, Set.of()).contains(citizenId)) return true;
        }
        return false;
    }

    public Set<UUID> authorityHolders(String realmId) {
        String normalizedRealm = normalize(realmId);
        RealmGovernmentState realm = realm(normalizedRealm);
        if (realm.activeGovernmentFormId().isBlank()) return Set.of();
        var form = definitions.require(realm.activeGovernmentFormId());
        LinkedHashSet<UUID> holders = new LinkedHashSet<>();
        for (String office : form.authorityOffices()) {
            holders.addAll(realm.officeHolders().getOrDefault(office, Set.of()));
        }
        return Set.copyOf(holders);
    }

    public List<GovernmentProposalRecord> proposals(String realmId) {
        String normalizedRealm = normalize(realmId);
        realm(normalizedRealm);
        return state.proposals.values().stream()
                .filter(proposal -> normalizedRealm.equals(proposal.realmId()))
                .sorted(Comparator.comparingLong(GovernmentProposalRecord::createdAt).reversed())
                .toList();
    }

    public List<GovernmentLawRecord> laws(String realmId) {
        String normalizedRealm = normalize(realmId);
        realm(normalizedRealm);
        return state.laws.values().stream()
                .filter(law -> normalizedRealm.equals(law.realmId()))
                .sorted(Comparator.comparingLong(GovernmentLawRecord::enactedAt).reversed())
                .toList();
    }

    public GovernmentProposalRecord createProposal(
            ServerPlayerEntity player,
            String realmId,
            String category,
            String title,
            String body
    ) {
        String realm = normalize(realmId);
        GovernmentGateStatus gates = gates(realm);
        if (!gates.seatOfRuleUnlocked()) {
            RealmGovernmentState government = realm(realm);
            throw new IllegalArgumentException(gates.seatOfRuleLockMessage());
        }
        requireEligible(player, realm);
        RealmGovernmentState government = realm(realm);
        if (!"monarchy".equals(government.activeGovernmentFormId())) {
            throw new IllegalArgumentException("Audience requests are only available in Monarchy.");
        }
        String cleanCategory = "audience_request";
        String cleanTitle = GovernmentTextRules.validateShortText(title, "Audience request title", 4, 48);
        String cleanBody = GovernmentTextRules.validateShortText(body, "Audience request body", 8, 1500);
        String id = nextProposalId(realm, cleanTitle);
        GovernmentProposalRecord proposal = GovernmentProposalRecord.create(
                id, realm, player.getUuid(), cleanCategory, cleanTitle, cleanBody, System.currentTimeMillis());
        state.proposals.put(id, proposal);
        state.realms.put(realm, government.withPendingProposal(id));
        save();
        api.history().recordChronicle("government", "audience-request-created", player.getUuid(), "audience_request", id,
                realm, Map.of("category", cleanCategory, "title", cleanTitle),
                "An Ember requested an audience with the Monarch.");
        notifyPersonal(player.getUuid(), "audience-request-created", realm + ":audience-request-created:" + id,
                "Audience Requested",
                "Your audience request \"" + cleanTitle + "\" was sent to the Seat of Rule.",
                Map.of("realmId", realm, "proposalId", id, "category", cleanCategory));
        notifyInitialProposalReviewers(realm, cleanCategory, "Audience Request",
                cleanTitle + " was submitted by " + citizenName(api.citizens().getOrCreate(player)) + ".",
                "audience-request:" + id,
                Map.of("realmId", realm, "proposalId", id, "category", cleanCategory));
        emit("government-audience-request-created", player.getUuid(), realm, "audience_request", id,
                Map.of("category", cleanCategory, "title", cleanTitle));
        return proposal;
    }

    public GovernmentProposalRecord reviewProposal(
            ServerPlayerEntity player,
            String realmId,
            String proposalId,
            boolean approve
    ) {
        String realm = normalize(realmId);
        RealmGovernmentState government = realm(realm);
        if (!isAuthority(realm, player.getUuid())) {
            throw new IllegalArgumentException("Only Realm authority holders can review audience requests.");
        }
        if (government.activeGovernmentFormId().isBlank()) {
            throw new IllegalArgumentException("The Realm has no active Government form.");
        }
        GovernmentFormDefinition form = definitions.require(government.activeGovernmentFormId());
        GovernmentProposalRecord proposal = state.proposals.get(proposalId);
        if (proposal == null || !realm.equals(proposal.realmId())) {
            throw new IllegalArgumentException("Unknown proposal " + proposalId + ".");
        }
        if (proposal.status() == GovernmentProposalStatus.FINAL_TEXT_REVIEW) {
            return reviewFinalText(player, realm, government, proposal, approve);
        }
        if (proposal.status() != GovernmentProposalStatus.PENDING) {
            throw new IllegalArgumentException("That audience request is not open for authority review.");
        }
        if (!proposalDecisionMakers(government, form).contains(player.getUuid())) {
            throw new IllegalArgumentException("Your office cannot decide audience requests.");
        }
        recordOfficeDecision(realm, player.getUuid(), approve);
        GovernmentProposalRecord updated = proposal.withVote(player.getUuid(), approve);
        GovernmentProposalDecision decision = proposalDecision(government, updated);
        if (decision == GovernmentProposalDecision.WAITING) {
            state.proposals.put(proposal.id(), updated);
            save();
            notifyPersonal(player.getUuid(), "proposal-review-recorded", realm + ":proposal-review:" + proposal.id()
                            + ":" + player.getUuid(),
                    "Review Recorded", "Your proposal review vote was recorded.",
                    Map.of("realmId", realm, "proposalId", proposal.id(), "approve", Boolean.toString(approve)));
            emit("government-proposal-review-vote", player.getUuid(), realm, "proposal", proposal.id(),
                    Map.of("approve", Boolean.toString(approve)));
            return updated;
        }
        return decision == GovernmentProposalDecision.APPROVED
                ? approveProposal(player, realm, government, updated)
                : rejectProposal(player, realm, updated);
    }

    static GovernmentProposalStatus initialProposalStatus(String formId, String category) {
        return GovernmentProposalStatus.PENDING;
    }

    public GovernmentProposalRecord ratifyProposal(
            ServerPlayerEntity player,
            String realmId,
            String proposalId,
            boolean approve
    ) {
        String realm = normalize(realmId);
        requireEligible(player, realm);
        GovernmentProposalRecord proposal = state.proposals.get(proposalId);
        if (proposal == null || !realm.equals(proposal.realmId())) {
            throw new IllegalArgumentException("Unknown proposal " + proposalId + ".");
        }
        if (proposal.status() != GovernmentProposalStatus.CITIZEN_RATIFICATION) {
            throw new IllegalArgumentException("That proposal is not open for Ember voting.");
        }
        GovernmentProposalRecord updated = proposal.withCitizenVote(player.getUuid(), approve);
        int threshold = activeCitizenThreshold(realm);
        long approvals = updated.citizenVotes().values().stream().filter(Boolean::booleanValue).count();
        long rejections = updated.citizenVotes().values().stream().filter(value -> !value).count();
        if (approvals >= threshold) {
            if (updated.finalTitle().isBlank()) {
                GovernmentProposalRecord pendingReview = updated.withStatus(
                        GovernmentProposalStatus.PENDING, player.getUuid(), System.currentTimeMillis());
                state.proposals.put(proposal.id(), pendingReview);
                save();
                notifyInitialProposalReviewers(realm, proposal.category(), "Ember Proposal Approved",
                        "\"" + proposal.title() + "\" reached Ember support and is ready for authority review.",
                        "proposal-review:" + proposal.id(),
                        Map.of("realmId", realm, "proposalId", proposal.id(), "category", proposal.category()));
                api.realmDeliveries().notifyRealm(realm, "Proposal Sent To Government",
                        "\"" + proposal.title() + "\" reached Ember support and moved to authority review.",
                        "government", player.getUuid());
                emit("government-proposal-citizen-approved", player.getUuid(), realm, "proposal", proposal.id(),
                        Map.of("status", "pending_authority_review", "category", proposal.category()));
                return pendingReview;
            }
            GovernmentLawRecord record = createRecord(realm, proposal.category(), finalTitle(proposal),
                    finalBody(proposal), player.getUuid(), updated);
            GovernmentProposalRecord enacted = updated.withStatus(GovernmentProposalStatus.ENACTED, player.getUuid(),
                    record.enactedAt());
            state.proposals.put(proposal.id(), enacted);
            state.realms.put(realm, realm(realm).withoutPendingProposal(proposal.id()));
            save();
            api.realmDeliveries().notifyRealm(realm, "Law Ratified",
                    "\"" + record.title() + "\" was approved by Embers and is now active.",
                    "government", player.getUuid());
            emit("government-proposal-citizen-ratified", player.getUuid(), realm, "proposal", proposal.id(),
                    Map.of("status", "enacted", "category", proposal.category(), "recordId", record.id()));
            return enacted;
        }
        if (rejections >= threshold) {
            api.realmDeliveries().notifyRealm(realm, "Law Rejected",
                    "\"" + proposal.title() + "\" failed Ember ratification.",
                    "government", player.getUuid());
            emit("government-proposal-citizen-ratified", player.getUuid(), realm, "proposal", proposal.id(),
                    Map.of("status", "rejected", "category", proposal.category()));
            return rejectProposal(player, realm, updated);
        }
        state.proposals.put(proposal.id(), updated);
        save();
        notifyPersonal(player.getUuid(), "proposal-ratification-recorded", realm + ":proposal-ratification:"
                        + proposal.id() + ":" + player.getUuid(),
                "Law Vote Recorded", "Your law ratification vote was recorded.",
                Map.of("realmId", realm, "proposalId", proposal.id(), "approve", Boolean.toString(approve)));
        emit("government-proposal-citizen-vote", player.getUuid(), realm, "proposal", proposal.id(),
                Map.of("approve", Boolean.toString(approve), "category", proposal.category()));
        return updated;
    }

    public GovernmentProposalRecord finalizeProposal(
            ServerPlayerEntity player,
            String realmId,
            String proposalId,
            String title,
            String body
    ) {
        String realm = normalize(realmId);
        if (!isAuthority(realm, player.getUuid())) {
            throw new IllegalArgumentException("Only Realm authority holders can finalize proposals.");
        }
        GovernmentProposalRecord proposal = state.proposals.get(proposalId);
        if (proposal == null || !realm.equals(proposal.realmId())) {
            throw new IllegalArgumentException("Unknown proposal " + proposalId + ".");
        }
        if (proposal.status() != GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION) {
            throw new IllegalArgumentException("That proposal is not awaiting official wording.");
        }
        RealmGovernmentState government = realm(realm);
        String formId = government.activeGovernmentFormId();
        if ("law".equals(proposal.category()) && "republic".equals(formId)) {
            requireOffice(player, government, "president", "Only the President can finalize Republic laws.");
        }
        GovernmentLawRecord record = createRecord(realm, proposal.category(), title, body, player.getUuid(), proposal);
        GovernmentProposalRecord enacted = proposal.withStatus(GovernmentProposalStatus.ENACTED, player.getUuid(),
                record.enactedAt());
        state.proposals.put(proposal.id(), enacted);
        state.realms.put(realm, realm(realm).withoutPendingProposal(proposal.id()));
        save();
        notifyPersonal(proposal.authorId(), "proposal-finalized", realm + ":proposal-finalized:" + proposal.id(),
                "Proposal Finalized",
                "Your proposal \"" + proposal.title() + "\" became an official "
                        + GovernmentTextRules.recordTypeLabel(record.category()) + " in " + officialName(realm) + ".",
                Map.of("realmId", realm, "proposalId", proposal.id(), "recordId", record.id(),
                        "category", record.category()));
        emit("government-proposal-finalized", player.getUuid(), realm, "proposal", proposal.id(),
                Map.of("recordId", record.id(), "category", record.category(), "title", record.title()));
        return enacted;
    }

    public GovernmentLawRecord createDirectRecord(
            ServerPlayerEntity player,
            String realmId,
            String category,
            String title,
            String body
    ) {
        String realm = normalize(realmId);
        String cleanCategory = GovernmentTextRules.validateRecordCategory(category);
        requireDirectRecordAuthority(player, realm);
        return createRecord(realm, cleanCategory, title, body, player.getUuid(), null);
    }

    public GovernmentProposalRecord createRepublicLawVote(
            ServerPlayerEntity player,
            String realmId,
            String title,
            String body
    ) {
        String realm = normalize(realmId);
        RealmGovernmentState government = realm(realm);
        requireOffice(player, government, "president", "Only the President can propose Republic laws.");
        String cleanTitle = GovernmentTextRules.validateShortText(title, "Law title", 4, 64);
        String cleanBody = GovernmentTextRules.validateShortText(body, "Law body", 8, 2000);
        long now = System.currentTimeMillis();
        String id = nextProposalId(realm, cleanTitle);
        GovernmentProposalRecord proposal = GovernmentProposalRecord.create(
                        id, realm, player.getUuid(), "law", cleanTitle, cleanBody, now)
                .withFinalText(cleanTitle, cleanBody, player.getUuid(), now)
                .withStatus(GovernmentProposalStatus.CITIZEN_RATIFICATION, player.getUuid(), now);
        state.proposals.put(id, proposal);
        state.realms.put(realm, government.withPendingProposal(id));
        save();
        api.history().recordChronicle("government", "republic-law-vote-opened",
                player.getUuid(), "proposal", id, realm,
                Map.of("category", "law", "title", cleanTitle),
                "A President opened a Republic law ratification vote.");
        api.realmDeliveries().notifyRealm(realm, "Law Vote Opened",
                "\"" + cleanTitle + "\" was proposed by the President and is open for Yes or No votes.",
                "government", player.getUuid());
        emit("government-republic-law-vote-opened", player.getUuid(), realm, "proposal", id,
                Map.of("category", "law", "title", cleanTitle));
        return proposal;
    }

    public boolean canDirectCreateRecords(ServerPlayerEntity player, String realmId) {
        if (player == null) return false;
        String realm = normalize(realmId);
        try {
            requireDirectRecordAuthority(player, realm);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean canCreateRepublicLawVotes(ServerPlayerEntity player, String realmId) {
        if (player == null) return false;
        RealmGovernmentState government = realm(normalize(realmId));
        return "republic".equals(government.activeGovernmentFormId())
                && government.officeHolders().getOrDefault("president", Set.of()).contains(player.getUuid());
    }

    public boolean canOpenSeatOfRule(ServerPlayerEntity player, String realmId) {
        if (player == null) return false;
        RealmGovernmentState government = realm(normalize(realmId));
        UUID playerId = player.getUuid();
        return switch (government.activeGovernmentFormId()) {
            case "monarchy" -> government.officeHolders().getOrDefault("monarch", Set.of()).contains(playerId);
            case "republic" -> government.officeHolders().getOrDefault("president", Set.of()).contains(playerId);
            default -> false;
        };
    }

    public boolean canReviewProposals(ServerPlayerEntity player, String realmId) {
        if (player == null) return false;
        String realm = normalize(realmId);
        try {
            RealmGovernmentState government = realm(realm);
            if (government.activeGovernmentFormId().isBlank()) return false;
            GovernmentFormDefinition form = definitions.require(government.activeGovernmentFormId());
            return "monarchy".equals(form.id()) && proposalDecisionMakers(government, form).contains(player.getUuid());
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean canActOnProposal(ServerPlayerEntity player, String realmId, GovernmentProposalRecord proposal) {
        if (player == null || proposal == null) return false;
        String realm = normalize(realmId);
        if (!realm.equals(proposal.realmId())) return false;
        try {
            RealmGovernmentState government = realm(realm);
            if (government.activeGovernmentFormId().isBlank()) return false;
            GovernmentFormDefinition form = definitions.require(government.activeGovernmentFormId());
            return switch (proposal.status()) {
                case PENDING -> {
                    yield "monarchy".equals(form.id()) && proposalDecisionMakers(government, form).contains(player.getUuid());
                }
                case FINAL_TEXT_REVIEW -> false;
                case APPROVED_PENDING_FINALIZATION -> {
                    yield false;
                }
                default -> false;
            };
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public GovernmentLawRecord archiveRecord(ServerPlayerEntity player, String realmId, String lawId) {
        String realm = normalize(realmId);
        if (!isAuthority(realm, player.getUuid())) {
            throw new IllegalArgumentException("Only Realm authority holders can archive records.");
        }
        GovernmentLawRecord law = state.laws.get(lawId);
        if (law == null || !realm.equals(law.realmId())) throw new IllegalArgumentException("Unknown record " + lawId + ".");
        if (!law.active()) throw new IllegalArgumentException("That record is already archived.");
        GovernmentLawRecord archived = law.archived(player.getUuid(), System.currentTimeMillis());
        state.laws.put(lawId, archived);
        state.realms.put(realm, realm(realm).withoutActiveLaw(lawId));
        save();
        api.realmDeliveries().notifyRealm(realm, GovernmentTextRules.recordTypeLabel(law.category()) + " Archived",
                "\"" + law.title() + "\" was archived by the Seat of Rule.", "government", player.getUuid());
        emit("government-civic-record-archived", player.getUuid(), realm, "civic_record", lawId,
                Map.of("title", law.title(), "category", law.category()));
        return archived;
    }

    public GovernmentLawRecord archiveLaw(ServerPlayerEntity player, String realmId, String lawId) {
        return archiveRecord(player, realmId, lawId);
    }

    public GovernmentLawRecord restoreRecord(ServerPlayerEntity player, String realmId, String lawId) {
        String realm = normalize(realmId);
        if (!isAuthority(realm, player.getUuid())) {
            throw new IllegalArgumentException("Only Realm authority holders can restore records.");
        }
        GovernmentLawRecord law = state.laws.get(lawId);
        if (law == null || !realm.equals(law.realmId())) throw new IllegalArgumentException("Unknown record " + lawId + ".");
        if (law.active()) throw new IllegalArgumentException("That record is already active.");
        GovernmentLawRecord restored = law.restored();
        state.laws.put(lawId, restored);
        state.realms.put(realm, realm(realm).withActiveLaw(lawId));
        save();
        api.realmDeliveries().notifyRealm(realm, GovernmentTextRules.recordTypeLabel(law.category()) + " Restored",
                "\"" + law.title() + "\" was restored by the Seat of Rule.", "government", player.getUuid());
        emit("government-civic-record-restored", player.getUuid(), realm, "civic_record", lawId,
                Map.of("title", law.title(), "category", law.category()));
        return restored;
    }

    public int removeInactiveAuthority(long now) {
        int removed = 0;
        for (RealmGovernmentState current : List.copyOf(state.realms.values())) {
            if (current.activeGovernmentFormId().isBlank()) continue;
            var form = definitions.require(current.activeGovernmentFormId());
            Map<String, Set<UUID>> offices = mutableOffices(current);
            Set<UUID> titleUpdates = new LinkedHashSet<>();
            boolean changed = false;
            int beforeRealm = removed;
            if ("monarchy".equals(form.id())) {
                removed += handleMonarchInactivity(current.realmId(), offices, now, titleUpdates);
                changed = removed > beforeRealm;
            }
            for (String office : List.copyOf(offices.keySet())) {
                Set<UUID> holders = offices.getOrDefault(office, Set.of());
                for (UUID holder : List.copyOf(holders)) {
                    if (!inactive(holder, now)) continue;
                    holders.remove(holder);
                    removed++;
                    changed = true;
                    titleUpdates.add(holder);
                    recordAuthorityRemoval(current.realmId(), form.id(), office, holder);
                }
                if (holders.isEmpty()) offices.remove(office);
            }
            if (changed) {
                state.realms.put(current.realmId(), new RealmGovernmentState(
                        current.realmId(),
                        current.activeGovernmentFormId(),
                        current.votedDisplayName(),
                        current.votedTag(),
                        current.votedColor(),
                        offices,
                        current.activeLawIds(),
                        current.pendingProposalIds(),
                        current.nameVoteCompletedAt(),
                        current.colorVoteCompletedAt(),
                        current.foundingElectionCompletedAt(),
                        current.lastReformAt()));
                for (UUID citizenId : titleUpdates) {
                    restoreOrPromoteAuthorityTitle(current.realmId(), offices, citizenId);
                }
            }
        }
        if (removed > 0) save();
        if (removed > 0) refreshIdentities();
        return removed;
    }

    public int handleCharacterTrueDeath(UUID citizenId) {
        int removed = 0;
        for (RealmGovernmentState current : List.copyOf(state.realms.values())) {
            Map<String, Set<UUID>> offices = mutableOffices(current);
            Set<UUID> titleUpdates = new LinkedHashSet<>();
            boolean changed = false;
            for (String officeId : List.copyOf(offices.keySet())) {
                Set<UUID> holders = offices.get(officeId);
                if (holders == null || !holders.remove(citizenId)) continue;
                removed++;
                changed = true;
                titleUpdates.add(citizenId);
                recordAuthorityRemoval(current.realmId(), current.activeGovernmentFormId(),
                        officeId, citizenId, "true-death");
                if (holders.isEmpty()) offices.remove(officeId);
                if ("monarchy".equals(current.activeGovernmentFormId()) && "monarch".equals(officeId)) {
                    UUID heir = offices.getOrDefault("heir", Set.of()).stream().findFirst().orElse(null);
                    if (heir != null) {
                        offices.get("heir").remove(heir);
                        if (offices.get("heir").isEmpty()) offices.remove("heir");
                        offices.computeIfAbsent("monarch", ignored -> new LinkedHashSet<>()).add(heir);
                        titleUpdates.add(heir);
                        api.history().recordChronicle("government", "monarchy-succession", heir,
                                "office", "monarch", current.realmId(),
                                Map.of("previous", citizenId.toString(), "successor", heir.toString(),
                                        "reason", "true-death"),
                                "The heir succeeded after the monarch's True Death.");
                    }
                }
            }
            if (!changed) continue;
            state.realms.put(current.realmId(), new RealmGovernmentState(
                    current.realmId(), current.activeGovernmentFormId(), current.votedDisplayName(),
                    current.votedTag(), current.votedColor(), offices, current.activeLawIds(), current.pendingProposalIds(),
                    current.nameVoteCompletedAt(), current.colorVoteCompletedAt(),
                    current.foundingElectionCompletedAt(), current.lastReformAt()));
            clearAuthorityTitleRestore(current.realmId(), citizenId);
            for (UUID titleUpdate : titleUpdates) {
                if (!titleUpdate.equals(citizenId)) {
                    restoreOrPromoteAuthorityTitle(current.realmId(), offices, titleUpdate);
                }
            }
        }
        if (removed > 0) {
            save();
            refreshIdentities();
        }
        return removed;
    }

    public Optional<RealmPresentation> presentation(RealmDefinition realm) {
        if (realm == null) return Optional.empty();
        RealmGovernmentState government = state.realms.get(realm.id());
        if (government == null) return Optional.empty();
        String displayName = government.votedDisplayName().isBlank()
                ? realm.displayName()
                : government.votedDisplayName();
        String shortName = government.votedTag().isBlank() ? realm.shortName() : government.votedTag();
        String prefix = shortName.isBlank() ? realm.prefix() : "[" + shortName + "]";
        String color = government.votedColor().isBlank() ? realm.color() : government.votedColor();
        String officialName = displayName;
        if (!government.activeGovernmentFormId().isBlank()) {
            try {
                officialName = definitions.require(government.activeGovernmentFormId())
                        .officialNameTemplate()
                        .replace("%realm%", displayName)
                        .replace("%REALM%", displayName.toUpperCase(java.util.Locale.ROOT))
                        .replace("%realm_lower%", displayName.toLowerCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                officialName = displayName;
            }
        }
        return Optional.of(new RealmPresentation(displayName, officialName, shortName, prefix, color));
    }

    private void reconcileAuthorityTitles() {
        for (RealmGovernmentState realm : state.realms.values()) {
            Set<UUID> holders = new LinkedHashSet<>();
            realm.officeHolders().values().forEach(holders::addAll);
            for (UUID citizenId : holders) {
                applyAuthorityTitle(realm.realmId(), realm, citizenId);
            }
        }
    }

    private void applyAuthorityTitle(String realmId, RealmGovernmentState realm, UUID citizenId) {
        applyAuthorityTitle(realmId, realm.officeHolders(), citizenId);
    }

    private void applyAuthorityTitle(String realmId, Map<String, Set<UUID>> offices, UUID citizenId) {
        String titleId = highestAuthorityTitleId(offices, citizenId);
        if (titleId.isBlank() || api.titles().find(titleId).isEmpty()) return;
        api.citizens().find(citizenId).ifPresent(citizen -> {
            String restoreKey = authorityTitleRestoreKey(realmId, citizenId);
            String activeTitle = citizen.activeTitleId();
            if (!state.authorityTitleRestores.containsKey(restoreKey)
                    && !AUTHORITY_TITLE_VALUES.contains(activeTitle)) {
                state.authorityTitleRestores.put(restoreKey, activeTitle);
            }
            boolean changed = false;
            if (!citizen.hasUnlockedTitle(titleId)) {
                citizen.unlockTitle(titleId, System.currentTimeMillis());
                changed = true;
            }
            if (!titleId.equals(activeTitle)) {
                citizen.setActiveTitleId(titleId);
                changed = true;
            }
            if (removeAuthorityTitleUnlocks(citizen, titleId)) {
                changed = true;
            }
            if (changed) api.citizens().save(citizen, "government-authority-title-set");
        });
    }

    private void restoreOrPromoteAuthorityTitle(String realmId, RealmGovernmentState realm, UUID citizenId) {
        restoreOrPromoteAuthorityTitle(realmId, realm.officeHolders(), citizenId);
    }

    private void restoreOrPromoteAuthorityTitle(String realmId, Map<String, Set<UUID>> offices, UUID citizenId) {
        String remainingTitleId = highestAuthorityTitleId(offices, citizenId);
        if (!remainingTitleId.isBlank()) {
            applyAuthorityTitle(realmId, offices, citizenId);
            return;
        }
        String restoreKey = authorityTitleRestoreKey(realmId, citizenId);
        String previousTitleId = state.authorityTitleRestores.remove(restoreKey);
        api.citizens().find(citizenId).ifPresent(citizen -> {
            boolean changed = removeAuthorityTitleUnlocks(citizen, "");
            if (previousTitleId == null) {
                if (AUTHORITY_TITLE_VALUES.contains(citizen.activeTitleId())) {
                    citizen.clearActiveTitle();
                    changed = true;
                }
                if (changed) api.citizens().save(citizen, "government-authority-title-restored");
                return;
            }
            if (previousTitleId.isBlank()) {
                if (!citizen.activeTitleId().isBlank()) {
                    citizen.clearActiveTitle();
                    changed = true;
                }
                if (changed) api.citizens().save(citizen, "government-authority-title-restored");
                return;
            }
            if (api.titles().find(previousTitleId).isEmpty()) {
                citizen.clearActiveTitle();
            } else {
                citizen.setActiveTitleId(previousTitleId);
            }
            api.citizens().save(citizen, "government-authority-title-restored");
        });
    }

    private void restoreAuthorityTitlesForRealm(String realmId) {
        Set<UUID> citizens = new LinkedHashSet<>();
        String prefix = realmId + "|";
        for (String key : List.copyOf(state.authorityTitleRestores.keySet())) {
            if (!key.startsWith(prefix)) continue;
            UUID citizenId = parseAuthorityTitleRestoreCitizen(key);
            if (citizenId != null) citizens.add(citizenId);
        }
        RealmGovernmentState realm = state.realms.get(normalize(realmId));
        if (realm != null) realm.officeHolders().values().forEach(citizens::addAll);
        for (UUID citizenId : citizens) {
            restoreOrPromoteAuthorityTitle(realmId, Map.of(), citizenId);
        }
    }

    private void restoreAllAuthorityTitles() {
        Map<String, Set<UUID>> byRealm = new LinkedHashMap<>();
        state.realms.values().forEach(realm -> {
            Set<UUID> citizens = byRealm.computeIfAbsent(realm.realmId(), ignored -> new LinkedHashSet<>());
            realm.officeHolders().values().forEach(citizens::addAll);
        });
        for (String key : List.copyOf(state.authorityTitleRestores.keySet())) {
            int separator = key.indexOf('|');
            if (separator <= 0 || separator >= key.length() - 1) continue;
            UUID citizenId = parseAuthorityTitleRestoreCitizen(key);
            if (citizenId == null) continue;
            byRealm.computeIfAbsent(key.substring(0, separator), ignored -> new LinkedHashSet<>()).add(citizenId);
        }
        byRealm.forEach((realmId, citizens) ->
                citizens.forEach(citizenId -> restoreOrPromoteAuthorityTitle(realmId, Map.of(), citizenId)));
    }

    private void clearAuthorityTitleRestore(String realmId, UUID citizenId) {
        state.authorityTitleRestores.remove(authorityTitleRestoreKey(realmId, citizenId));
    }

    private String highestAuthorityTitleId(Map<String, Set<UUID>> offices, UUID citizenId) {
        for (String officeId : AUTHORITY_TITLE_PRIORITY) {
            if (offices.getOrDefault(officeId, Set.of()).contains(citizenId)) {
                return AUTHORITY_TITLE_IDS.getOrDefault(officeId, "");
            }
        }
        return offices.entrySet().stream()
                .filter(entry -> entry.getValue().contains(citizenId))
                .map(entry -> AUTHORITY_TITLE_IDS.getOrDefault(entry.getKey(), ""))
                .filter(titleId -> !titleId.isBlank())
                .findFirst()
                .orElse("");
    }

    static boolean removeAuthorityTitleUnlocks(CitizenRecord citizen, String keepTitleId) {
        String keep = keepTitleId == null ? "" : keepTitleId.trim().toLowerCase(java.util.Locale.ROOT);
        boolean changed = false;
        for (String titleId : List.copyOf(citizen.unlockedTitleIds())) {
            if (!AUTHORITY_TITLE_VALUES.contains(titleId) || titleId.equals(keep)) continue;
            citizen.revokeTitle(titleId);
            changed = true;
        }
        return changed;
    }

    private static String authorityTitleRestoreKey(String realmId, UUID citizenId) {
        return normalize(realmId) + "|" + citizenId;
    }

    private static UUID parseAuthorityTitleRestoreCitizen(String key) {
        int separator = key.indexOf('|');
        if (separator <= 0 || separator >= key.length() - 1) return null;
        try {
            return UUID.fromString(key.substring(separator + 1));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private int handleMonarchInactivity(
            String realmId,
            Map<String, Set<UUID>> offices,
            long now,
            Set<UUID> titleUpdates
    ) {
        Set<UUID> monarchs = offices.getOrDefault("monarch", Set.of());
        UUID monarch = monarchs.stream().findFirst().orElse(null);
        if (monarch == null || !inactive(monarch, now)) return 0;
        offices.computeIfAbsent("monarch", ignored -> new LinkedHashSet<>()).remove(monarch);
        titleUpdates.add(monarch);
        recordAuthorityRemoval(realmId, "monarchy", "monarch", monarch);
        UUID heir = offices.getOrDefault("heir", Set.of()).stream()
                .filter(candidate -> !inactive(candidate, now))
                .findFirst()
                .orElse(null);
        if (heir != null) {
            offices.computeIfAbsent("heir", ignored -> new LinkedHashSet<>()).remove(heir);
            offices.computeIfAbsent("monarch", ignored -> new LinkedHashSet<>()).add(heir);
            titleUpdates.add(heir);
            api.history().recordChronicle("government", "monarchy-succession", heir, "office", "monarch",
                    realmId, java.util.Map.of("previous", monarch.toString(), "successor", heir.toString()),
                    "A Realm heir succeeded an inactive monarch.");
        } else {
            api.history().recordChronicle("government", "monarchy-vacancy", monarch, "office", "monarch",
                    realmId, java.util.Map.of("reason", "inactive-no-heir"),
                    "A Realm monarchy became vacant because no active heir was available.");
        }
        return 1;
    }

    private boolean inactive(UUID citizenId, long now) {
        if (server != null && server.getPlayerManager().getPlayer(citizenId) != null) return false;
        return api.citizens().find(citizenId)
                .map(citizen -> citizen.lastSeenAt() <= 0L
                        || now - citizen.lastSeenAt() > definitions.settings().authorityInactivityMillis())
                .orElse(true);
    }

    private void validateVoteUnlocked(String realmId, GovernmentVoteType type) {
        GovernmentGateStatus gates = gates(realmId);
        switch (type) {
            case REALM_NAME -> {
                if (!gates.nameVoteUnlocked()) throw new IllegalArgumentException(gates.nameVoteLockMessage());
            }
            case REALM_COLOR -> {
                if (!gates.colorVoteUnlocked()) throw new IllegalArgumentException(gates.colorVoteLockMessage());
            }
            case GOVERNMENT_FORM -> {
                if (!gates.governmentVoteUnlocked()) throw new IllegalArgumentException(gates.governmentVoteLockMessage());
            }
            case FOUNDING_ELECTION -> {
                if (!gates.foundingElectionUnlocked()) throw new IllegalArgumentException(gates.foundingElectionLockMessage());
            }
        }
    }

    private void requireEligible(ServerPlayerEntity player, String realmId) {
        if (!eligibleCitizen(player, realmId)) {
            throw new IllegalArgumentException("Only active Embers of this Realm can do that.");
        }
    }

    private void resolveVote(GovernmentVoteState vote, long now) {
        if (vote.type == GovernmentVoteType.FOUNDING_ELECTION) {
            resolveFoundingElection(vote, now);
            return;
        }
        Map<String, Long> totals = GovernmentVoteResolutionPolicy.totals(vote);
        if (totals.isEmpty()) {
            reopenEmptyVote(vote, now);
            return;
        }
        List<String> winners = GovernmentVoteResolutionPolicy.topOptions(totals);
        if (winners.size() > 1) {
            openRunoff(vote, winners, totals, now, "Government Runoff Open",
                    "There was a tie between the leading options. A 12-hour runoff is now open.",
                    vote.type.name().toLowerCase(java.util.Locale.ROOT) + ":runoff:");
            return;
        }
        String winner = winners.getFirst();
        vote.resolved = true;
        vote.winnerIds = List.of(winner);
        vote.resultTotals = Map.copyOf(totals);
        GovernmentVoteOption option = vote.options.get(winner);
        if (vote.type == GovernmentVoteType.REALM_NAME) {
            setVotedIdentity(vote.realmId, option.title, option.tag);
        } else if (vote.type == GovernmentVoteType.REALM_COLOR) {
            setVotedColor(vote.realmId, option.id);
        } else if (vote.type == GovernmentVoteType.GOVERNMENT_FORM) {
            setForm(vote.realmId, option.formId.isBlank() ? option.id : option.formId);
        }
        String resultEvent = switch (vote.type) {
            case REALM_NAME -> "realm-name-chosen";
            case REALM_COLOR -> "realm-color-chosen";
            case GOVERNMENT_FORM -> "government-form-chosen";
            default -> "vote-resolved";
        };
        String resultText = switch (vote.type) {
            case REALM_NAME -> "The Realm chose " + option.title + " as its official name.";
            case REALM_COLOR -> "The Realm chose " + option.title + " as its official color.";
            case GOVERNMENT_FORM -> "The Realm adopted " + option.title + " as its Government form.";
            default -> "A Government vote resolved.";
        };
        api.history().recordChronicle("government", resultEvent, null, "realm", vote.realmId, vote.realmId,
                Map.of("type", vote.type.name().toLowerCase(), "winner", winner),
                resultText);
    }

    private void resolveFoundingElection(GovernmentVoteState vote, long now) {
        RealmGovernmentState current = realm(vote.realmId);
        GovernmentFormDefinition form = definitions.require(current.activeGovernmentFormId());
        Map<String, Long> totals = GovernmentVoteResolutionPolicy.totals(vote);
        if (totals.isEmpty()) {
            reopenEmptyVote(vote, now);
            return;
        }
        Map<String, List<String>> winnersByOffice = new LinkedHashMap<>();
        List<String> tiedBoundary = new ArrayList<>();
        for (String office : GovernmentFoundingPhasePolicy.electionOffices(form)) {
            int seats = maxApprovalCount(current, office);
            List<Map.Entry<String, Long>> officeTotals = totals.entrySet().stream()
                    .filter(entry -> {
                        GovernmentVoteOption option = vote.options.get(entry.getKey());
                        return option != null && office.equals(option.officeId);
                    })
                    .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                            .thenComparing(Map.Entry::getKey))
                    .toList();
            if (officeTotals.isEmpty()) continue;
            if (officeTotals.size() > seats) {
                long boundary = officeTotals.get(seats - 1).getValue();
                List<String> boundaryTies = officeTotals.stream()
                        .filter(entry -> entry.getValue() == boundary)
                        .map(Map.Entry::getKey)
                        .toList();
                long winnersAbove = officeTotals.stream().filter(entry -> entry.getValue() > boundary).count();
                if (winnersAbove < seats && boundaryTies.size() > seats - winnersAbove) {
                    tiedBoundary.addAll(boundaryTies);
                    continue;
                }
            }
            winnersByOffice.put(office, officeTotals.stream().limit(seats).map(Map.Entry::getKey).toList());
        }
        if (!tiedBoundary.isEmpty()) {
            openRunoff(vote, tiedBoundary.stream().sorted().toList(), totals, now,
                    "Founding Election Runoff",
                    "There was a seat-boundary tie. A 12-hour runoff is now open.",
                    "founding-election:runoff:");
            return;
        }
        RealmGovernmentState updated = current;
        List<String> winnerIds = new ArrayList<>();
        Set<UUID> winnerCitizenIds = new LinkedHashSet<>();
        for (Map.Entry<String, List<String>> entry : winnersByOffice.entrySet()) {
            for (String optionId : entry.getValue()) {
                GovernmentVoteOption option = vote.options.get(optionId);
                if (option == null || option.candidateId == null) continue;
                updated = updated.withOfficeHolder(entry.getKey(), option.candidateId);
                ensureOfficeTerm(vote.realmId, entry.getKey(), option.candidateId, now);
                winnerIds.add(optionId);
                winnerCitizenIds.add(option.candidateId);
                notifyPersonal(option.candidateId, "elected-to-office",
                        vote.realmId + ":elected:" + entry.getKey() + ":" + option.candidateId,
                        "Elected to Office",
                        "You were elected as " + GovernmentFoundingPhasePolicy.officeLabel(form, entry.getKey()) + " in "
                                + officialName(vote.realmId) + ".",
                        Map.of("realmId", vote.realmId, "office", entry.getKey(), "form", form.id()));
            }
        }
        boolean complete = GovernmentFoundingPhasePolicy.electionComplete(form, updated);
        if (complete) {
            updated = updated.withFoundingElectionComplete();
        }
        state.realms.put(vote.realmId, updated);
        if (complete) {
            vote.resolved = true;
            vote.winnerIds = List.copyOf(winnerIds);
            vote.resultTotals = Map.copyOf(totals);
        } else {
            state.votes.remove(voteKey(vote.realmId, vote.type));
        }
        for (UUID citizenId : winnerCitizenIds) {
            applyAuthorityTitle(vote.realmId, updated, citizenId);
        }
        refreshIdentities();
        if (complete) {
            api.history().recordChronicle("government", "founding-election-resolved", null, "realm",
                    vote.realmId, vote.realmId, Map.of("form", form.id(), "winners", Integer.toString(winnerIds.size())),
                    "A Realm founding election resolved.");
            api.realmDeliveries().notifyRealm(
                    vote.realmId,
                    "Founding Election Complete",
                    "Embers elected " + winnerIds.size() + " founding authority holder"
                            + (winnerIds.size() == 1 ? "." : "s."),
                    "government",
                    null);
            emit("founding-election-completed", null, vote.realmId, "realm", vote.realmId,
                    Map.of("form", form.id(), "winners", Integer.toString(winnerIds.size()),
                            "realmOfficial", officialName(vote.realmId)));
        } else {
            api.history().recordChronicle("government", "founding-election-phase-resolved", null, "realm",
                    vote.realmId, vote.realmId, Map.of("form", form.id(), "winners", Integer.toString(winnerIds.size())),
                    "A Realm founding election phase resolved.");
            notifyVoteStage(vote.realmId, "Founding Election Continues",
                    nextFoundingPhaseMessage(form, updated),
                    "founding-election:next-phase:" + System.currentTimeMillis(), 0L);
            emit("founding-election-phase-completed", null, vote.realmId, "realm", vote.realmId,
                    Map.of("form", form.id(), "winners", Integer.toString(winnerIds.size()),
                            "realmOfficial", officialName(vote.realmId)));
        }
    }

    private void reopenEmptyVote(GovernmentVoteState vote, long now) {
        vote.startedAt = 0L;
        vote.endsAt = 0L;
        vote.runoff = false;
        vote.ballots.clear();
        vote.winnerIds = List.of();
        vote.resultTotals = Map.of();
        vote.startIfNeeded(now, FOUNDING_VOTE_DURATION);
        api.history().recordChronicle("government", "vote-expired-empty", null, "realm",
                vote.realmId, vote.realmId, Map.of("type", vote.type.name().toLowerCase()),
                "A Government vote window expired without valid ballots and reopened.");
        notifyVoteStage(vote.realmId, GovernmentTextRules.voteTitle(vote.type),
                GovernmentTextRules.voteBody(vote.type) + " The previous window had no valid ballots, so this stage reopened.",
                vote.type.name().toLowerCase(java.util.Locale.ROOT) + ":empty-reopen:" + vote.round,
                vote.endsAt);
        emit("government-vote-reopened-empty", null, vote.realmId, "realm", vote.realmId,
                Map.of("type", vote.type.name().toLowerCase(java.util.Locale.ROOT),
                        "round", Integer.toString(vote.round)));
    }


    private void openRunoff(
            GovernmentVoteState vote,
            List<String> optionIds,
            Map<String, Long> totals,
            long now,
            String title,
            String body,
            String dedupePrefix
    ) {
        GovernmentVoteState runoff = vote.runoff(optionIds, now, RUNOFF_VOTE_DURATION);
        runoff.resultTotals = Map.copyOf(totals);
        state.votes.put(voteKey(vote.realmId, vote.type), runoff);
        notifyVoteStage(vote.realmId, title, body,
                dedupePrefix + runoff.round, runoff.endsAt);
        emit("government-vote-runoff-opened", null, vote.realmId, "vote",
                vote.type.name().toLowerCase(java.util.Locale.ROOT),
                Map.of("type", vote.type.name().toLowerCase(java.util.Locale.ROOT),
                        "round", Integer.toString(runoff.round),
                        "options", Integer.toString(runoff.options.size())));
    }

    private String nextFoundingPhaseMessage(GovernmentFormDefinition form, RealmGovernmentState current) {
        return switch (form.id()) {
            case "republic" -> "Embers may now nominate the President.";
            default -> "The next founding election phase is ready.";
        };
    }

    private int maxApprovalCount(RealmGovernmentState state, String officeId) {
        if (state.activeGovernmentFormId().isBlank()) return 1;
        return definitions.require(state.activeGovernmentFormId()).offices().stream()
                .filter(office -> office.id().equals(officeId))
                .findFirst()
                .map(office -> Math.max(1, office.maxHolders()))
                .orElse(1);
    }

    private static String citizenName(CitizenRecord citizen) {
        if (citizen.nickname() != null && !citizen.nickname().isBlank()) return citizen.nickname();
        if (citizen.lastKnownUsername() != null && !citizen.lastKnownUsername().isBlank()) {
            return citizen.lastKnownUsername();
        }
        return citizen.uuid().toString();
    }

    private String authorityNoticeLabel(String realmId, UUID citizenId) {
        RealmGovernmentState government = realm(realmId);
        String formId = government.activeGovernmentFormId();
        String officeId = GovernmentFoundingPhasePolicy.primaryOffice(formId);
        if (officeId.isBlank() || !government.officeHolders().getOrDefault(officeId, Set.of()).contains(citizenId)) {
            officeId = definitions.require(formId).authorityOffices().stream()
                    .filter(candidate -> government.officeHolders().getOrDefault(candidate, Set.of()).contains(citizenId))
                    .findFirst()
                    .orElse(officeId);
        }
        CitizenRecord citizen = api.citizens().find(citizenId).orElse(null);
        String name = citizen == null ? "Unknown Ember" : citizenName(citizen);
        String office = GovernmentFoundingPhasePolicy.officeLabel(definitions.require(formId), officeId);
        return office.isBlank() ? name : office + " " + name;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value == null ? "" : value;
        if (maxLength <= 3) return value.substring(0, Math.max(0, maxLength));
        return value.substring(0, maxLength - 3) + "...";
    }

    private static String voteKey(String realmId, GovernmentVoteType type) {
        return normalize(realmId) + ":" + type.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static Map<String, Set<UUID>> mutableOffices(RealmGovernmentState state) {
        Map<String, Set<UUID>> result = new LinkedHashMap<>();
        state.officeHolders().forEach((office, holders) -> result.put(office, new LinkedHashSet<>(holders)));
        return result;
    }

    static String republicOfficeConflict(
            String formId,
            String targetOfficeId,
            RealmGovernmentState state,
            UUID citizenId
    ) {
        if (!"republic".equals(formId) || state == null || citizenId == null) return "";
        return "";
    }

    private void recordAuthorityRemoval(String realmId, String formId, String officeId, UUID citizenId) {
        recordAuthorityRemoval(realmId, formId, officeId, citizenId, "inactive");
    }

    private void recordAuthorityRemoval(String realmId, String formId, String officeId, UUID citizenId, String reason) {
        String eventType = switch (officeId) {
            case "president" -> "republic-vacancy";
            default -> "authority-removed-inactive";
        };
        String readableReason = switch (reason) {
            case "true-death" -> "its holder suffered True Death";
            default -> "its holder became inactive";
        };
        api.history().recordChronicle("government", eventType, citizenId, "office", officeId,
                realmId, java.util.Map.of("form", formId, "office", officeId, "reason", reason),
                "A Government office was vacated because " + readableReason + ".");
        api.realmDeliveries().notifyRealm(realmId, "Authority Seat Vacated",
                "The " + officeId.replace('_', ' ') + " office was vacated because " + readableReason + ".",
                "government", citizenId);
        notifyPersonal(citizenId, "office-vacated",
                realmId + ":office-vacated:" + officeId + ":" + citizenId,
                "Office Vacated",
                "Your " + officeId.replace('_', ' ') + " office in " + officialName(realmId)
                        + " was vacated because " + readableReason + ".",
                Map.of("realmId", realmId, "office", officeId, "form", formId, "reason", reason));
        emit("office-vacated", citizenId, realmId, "office", officeId,
                Map.of("office", officeId, "form", formId, "reason", reason));
    }

    private void notifyVoteStage(
            String realmId, String title, String body, String dedupe, long expiresAt
    ) {
        api.notifications().publishRealm(
                realmId,
                ElarionNotificationCategory.GOVERNMENT,
                SOURCE_SYSTEM,
                "vote-stage",
                realmId + ":" + dedupe,
                title,
                body,
                "Civic Forum",
                NOTIFICATION_ICON,
                governmentNotificationActions(Map.of("realmId", realmId)),
                Map.of("realmId", realmId),
                expiresAt);
    }

    private void notifyPersonal(
            UUID recipientId,
            String eventType,
            String dedupe,
            String title,
            String body,
            Map<String, String> metadata
    ) {
        if (recipientId == null) return;
        api.notifications().publishPersonal(
                recipientId,
                ElarionNotificationCategory.PERSONAL,
                SOURCE_SYSTEM,
                eventType,
                dedupe,
                title,
                body,
                "Government",
                NOTIFICATION_ICON,
                governmentNotificationActions(metadata),
                metadata,
                api.notifications().defaultExpiry());
    }

    private static List<ElarionNotificationAction> governmentNotificationActions(Map<String, String> metadata) {
        if (metadata != null && !metadata.getOrDefault("realmId", "").isBlank()) {
            return List.of(
                    new ElarionNotificationAction(OPEN_CIVIC_FORUM_ACTION, "Open Forum", true),
                    new ElarionNotificationAction(ElarionNotificationService.DISMISS, "Dismiss", true));
        }
        return List.of(new ElarionNotificationAction(ElarionNotificationService.DISMISS, "Dismiss", true));
    }

    private void notifyAuthority(String realmId, String title, String body, String dedupe, Map<String, String> metadata) {
        Set<UUID> holders = authorityHolders(realmId);
        for (UUID holder : holders) {
            notifyPersonal(holder, "authority-review", realmId + ":" + dedupe + ":" + holder,
                    title, body, metadata);
        }
    }

    private void notifyInitialProposalReviewers(
            String realmId,
            String category,
            String title,
            String body,
            String dedupe,
            Map<String, String> metadata
    ) {
        RealmGovernmentState government = realm(realmId);
        if (government.activeGovernmentFormId().isBlank()) return;
        GovernmentFormDefinition form = definitions.require(government.activeGovernmentFormId());
        Set<UUID> recipients;
        if ("republic".equals(form.id()) && "law".equals(category)) {
            recipients = government.officeHolders().getOrDefault("president", Set.of());
        } else {
            recipients = proposalDecisionMakers(government, form);
        }
        for (UUID holder : recipients) {
            notifyPersonal(holder, "authority-review", realmId + ":" + dedupe + ":" + holder,
                    title, body, metadata);
        }
    }

    private GovernmentProposalRecord approveProposal(
            ServerPlayerEntity actor,
            String realm,
            RealmGovernmentState government,
            GovernmentProposalRecord proposal
    ) {
        if ("audience_request".equals(proposal.category())) {
            return acceptAudienceRequest(actor, realm, proposal);
        }
        if ("law".equals(proposal.category()) && "republic".equals(government.activeGovernmentFormId())) {
            return openCitizenRatification(actor, realm, proposal);
        }
        return approveProposalForFinalization(actor, realm, proposal);
    }

    private GovernmentProposalRecord acceptAudienceRequest(
            ServerPlayerEntity actor,
            String realm,
            GovernmentProposalRecord proposal
    ) {
        long now = System.currentTimeMillis();
        GovernmentProposalRecord accepted = proposal.withStatus(GovernmentProposalStatus.ENACTED, actor.getUuid(), now);
        state.proposals.put(proposal.id(), accepted);
        state.realms.put(realm, realm(realm).withoutPendingProposal(proposal.id()));
        save();
        api.history().recordChronicle("government", "audience-request-accepted", actor.getUuid(),
                "audience_request", proposal.id(), realm,
                Map.of("title", proposal.title()),
                "A Monarch accepted an audience request.");
        notifyPersonal(proposal.authorId(), "audience-request-accepted",
                realm + ":audience-request-accepted:" + proposal.id(),
                "Audience Accepted",
                "Your request \"" + proposal.title() + "\" was accepted by the Seat of Rule.",
                Map.of("realmId", realm, "proposalId", proposal.id()));
        emit("government-audience-request-accepted", actor.getUuid(), realm, "audience_request", proposal.id(),
                Map.of("title", proposal.title()));
        return accepted;
    }

    private GovernmentProposalRecord openCitizenRatification(
            ServerPlayerEntity actor,
            String realm,
            GovernmentProposalRecord proposal
    ) {
        long now = System.currentTimeMillis();
        GovernmentProposalRecord ratification = proposal.withStatus(
                GovernmentProposalStatus.CITIZEN_RATIFICATION, actor.getUuid(), now);
        state.proposals.put(proposal.id(), ratification);
        save();
        api.history().recordChronicle("government", "proposal-citizen-ratification-opened",
                actor.getUuid(), "proposal", proposal.id(), realm,
                Map.of("category", proposal.category(), "title", proposal.title()),
                "A Republic law proposal was sent to Embers for ratification.");
        notifyPersonal(proposal.authorId(), "proposal-ratification-opened",
                realm + ":proposal-ratification-opened:" + proposal.id(),
                "Proposal Sent To Embers",
                "The official wording for \"" + finalTitle(proposal) + "\" was approved and now needs Ember approval.",
                Map.of("realmId", realm, "proposalId", proposal.id(), "category", proposal.category()));
        api.realmDeliveries().notifyRealm(realm, "Law Vote Opened",
                "\"" + finalTitle(proposal) + "\" now needs Ember ratification.",
                "government", actor.getUuid());
        emit("government-proposal-citizen-ratification-opened", actor.getUuid(), realm, "proposal", proposal.id(),
                Map.of("category", proposal.category(), "title", finalTitle(proposal)));
        return ratification;
    }

    private GovernmentProposalRecord openFinalTextReview(
            ServerPlayerEntity actor,
            String realm,
            GovernmentProposalRecord proposal,
            String title,
            String body,
            String notificationTitle,
            String notificationBody,
            String eventType
    ) {
        String cleanTitle = GovernmentTextRules.validateShortText(title, GovernmentTextRules.recordTypeLabel(proposal.category()) + " title", 4, 64);
        String cleanBody = GovernmentTextRules.validateShortText(body, GovernmentTextRules.recordTypeLabel(proposal.category()) + " body", 8, 2000);
        long now = System.currentTimeMillis();
        GovernmentProposalRecord review = proposal.withFinalText(cleanTitle, cleanBody, actor.getUuid(), now)
                .withStatusAndClearedReview(GovernmentProposalStatus.FINAL_TEXT_REVIEW, actor.getUuid(), now);
        state.proposals.put(proposal.id(), review);
        save();
        api.history().recordChronicle("government", eventType, actor.getUuid(), "proposal", proposal.id(),
                realm, Map.of("category", proposal.category(), "title", cleanTitle),
                "Official wording was submitted for authority review.");
        notifyFinalTextDecisionMakers(realm, notificationTitle, notificationBody,
                "final-text-review:" + proposal.id(),
                Map.of("realmId", realm, "proposalId", proposal.id(), "category", proposal.category()));
        emit(eventType, actor.getUuid(), realm, "proposal", proposal.id(),
                Map.of("category", proposal.category(), "title", cleanTitle));
        return review;
    }

    private GovernmentProposalRecord reviewFinalText(
            ServerPlayerEntity actor,
            String realm,
            RealmGovernmentState government,
            GovernmentProposalRecord proposal,
            boolean approve
    ) {
        Set<UUID> reviewers = finalTextDecisionMakers(government);
        if (!reviewers.contains(actor.getUuid())) {
            throw new IllegalArgumentException("Your office cannot review this official wording.");
        }
        recordOfficeDecision(realm, actor.getUuid(), approve);
        GovernmentProposalRecord updated = proposal.withVote(actor.getUuid(), approve);
        GovernmentProposalDecision decision = finalTextDecision(government, updated);
        if (decision == GovernmentProposalDecision.WAITING) {
            state.proposals.put(proposal.id(), updated);
            save();
            notifyPersonal(actor.getUuid(), "final-text-review-recorded", realm + ":final-text-review:"
                            + proposal.id() + ":" + actor.getUuid(),
                    "Review Recorded", "Your official wording review was recorded.",
                    Map.of("realmId", realm, "proposalId", proposal.id(), "approve", Boolean.toString(approve)));
            emit("government-proposal-final-text-review-vote", actor.getUuid(), realm, "proposal", proposal.id(),
                    Map.of("approve", Boolean.toString(approve), "category", proposal.category()));
            return updated;
        }
        if (decision == GovernmentProposalDecision.REJECTED) {
            long now = System.currentTimeMillis();
            GovernmentProposalRecord rewrite = updated.withStatusAndClearedReview(
                    GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION, actor.getUuid(), now);
            state.proposals.put(proposal.id(), rewrite);
            save();
            UUID wordingOwner = proposal.sponsorId() == null ? proposal.resolvedBy() : proposal.sponsorId();
            notifyPersonal(wordingOwner, "final-text-rejected", realm + ":final-text-rejected:"
                            + proposal.id() + ":" + now,
                    "Official Wording Rejected",
                    "The wording for \"" + finalTitle(proposal) + "\" was rejected and needs revision.",
                    Map.of("realmId", realm, "proposalId", proposal.id(), "category", proposal.category()));
            notifyPersonal(proposal.authorId(), "proposal-wording-rejected", realm + ":proposal-wording-rejected:"
                            + proposal.id(),
                    "Proposal Needs New Wording",
                    "Authority rejected the official wording for your approved proposal.",
                    Map.of("realmId", realm, "proposalId", proposal.id(), "category", proposal.category()));
            api.realmDeliveries().notifyRealm(realm, "Official Wording Rejected",
                    "\"" + finalTitle(proposal) + "\" was returned for new wording.",
                    "government", actor.getUuid());
            emit("government-proposal-final-text-rejected", actor.getUuid(), realm, "proposal", proposal.id(),
                    Map.of("category", proposal.category(), "title", finalTitle(proposal)));
            return rewrite;
        }
        GovernmentLawRecord record = createRecord(realm, proposal.category(), finalTitle(updated),
                finalBody(updated), actor.getUuid(), updated);
        GovernmentProposalRecord enacted = updated.withStatus(GovernmentProposalStatus.ENACTED, actor.getUuid(),
                record.enactedAt());
        state.proposals.put(proposal.id(), enacted);
        state.realms.put(realm, realm(realm).withoutPendingProposal(proposal.id()));
        save();
        notifyPersonal(proposal.authorId(), "proposal-finalized", realm + ":proposal-finalized:" + proposal.id(),
                "Proposal Finalized",
                "Your proposal \"" + proposal.title() + "\" became an official "
                        + GovernmentTextRules.recordTypeLabel(record.category()) + " in " + officialName(realm) + ".",
                Map.of("realmId", realm, "proposalId", proposal.id(), "recordId", record.id(),
                        "category", record.category()));
        emit("government-proposal-finalized", actor.getUuid(), realm, "proposal", proposal.id(),
                Map.of("recordId", record.id(), "category", record.category(), "title", record.title()));
        return enacted;
    }

    private GovernmentProposalRecord approveProposalForFinalization(
            ServerPlayerEntity actor,
            String realm,
            GovernmentProposalRecord proposal
    ) {
        long now = System.currentTimeMillis();
        GovernmentProposalRecord approved = proposal.withStatusAndSponsor(
                GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION, actor.getUuid(), actor.getUuid(), now);
        state.proposals.put(proposal.id(), approved);
        save();
        api.history().recordChronicle("government", "proposal-approved", actor.getUuid(), "proposal", proposal.id(),
                realm, Map.of("category", proposal.category(), "title", proposal.title()),
                "A civic proposal was approved and awaits official wording.");
        notifyPersonal(proposal.authorId(), "proposal-approved", realm + ":proposal-approved:" + proposal.id(),
                "Proposal Approved",
                "Your proposal \"" + proposal.title() + "\" was approved and awaits official wording.",
                Map.of("realmId", realm, "proposalId", proposal.id(), "category", proposal.category()));
        emit("government-proposal-resolved", actor.getUuid(), realm, "proposal", proposal.id(),
                Map.of("status", "approved_pending_finalization",
                        "category", proposal.category(),
                        "sponsorId", actor.getUuid().toString()));
        return approved;
    }

    private GovernmentLawRecord createRecord(
            String realm,
            String category,
            String title,
            String body,
            UUID actorId,
            GovernmentProposalRecord sourceProposal
    ) {
        String cleanCategory = GovernmentTextRules.validateRecordCategory(category);
        String cleanTitle = GovernmentTextRules.validateShortText(title, GovernmentTextRules.recordTypeLabel(cleanCategory) + " title", 4, 64);
        String cleanBody = GovernmentTextRules.validateShortText(body, GovernmentTextRules.recordTypeLabel(cleanCategory) + " body", 8, 2000);
        long now = System.currentTimeMillis();
        String id = nextLawId(realm, cleanTitle);
        GovernmentLawRecord record = sourceProposal == null
                ? GovernmentLawRecord.direct(id, realm, cleanCategory, cleanTitle, cleanBody, actorId, now)
                : GovernmentLawRecord.enact(id, sourceProposal, cleanTitle, cleanBody, actorId, now);
        state.laws.put(record.id(), record);
        state.realms.put(realm, realm(realm).withActiveLaw(record.id()));
        save();
        api.history().recordChronicle("government", "civic-record-created", actorId, "civic_record", record.id(),
                realm, Map.of("proposalId", record.sourceProposalId(), "category", record.category(),
                        "title", record.title()),
                "A Government civic record was created.");
        api.realmDeliveries().notifyRealm(realm, GovernmentTextRules.recordTypeLabel(record.category()) + " Created",
                "\"" + record.title() + "\" became active in " + officialName(realm) + ".",
                "government", actorId);
        emit("government-civic-record-created", actorId, realm, "civic_record", record.id(),
                Map.of("proposalId", record.sourceProposalId(), "category", record.category(),
                        "title", record.title()));
        return record;
    }

    private GovernmentProposalRecord rejectProposal(
            ServerPlayerEntity actor,
            String realm,
            GovernmentProposalRecord proposal
    ) {
        long now = System.currentTimeMillis();
        GovernmentProposalRecord rejected = proposal.withStatus(GovernmentProposalStatus.REJECTED, actor.getUuid(), now);
        state.proposals.put(proposal.id(), rejected);
        state.realms.put(realm, realm(realm).withoutPendingProposal(proposal.id()));
        save();
        api.history().recordChronicle("government", "proposal-rejected", actor.getUuid(), "proposal", proposal.id(),
                realm, Map.of("category", proposal.category(), "title", proposal.title()),
                "A civic proposal was rejected.");
        notifyPersonal(proposal.authorId(), "proposal-rejected", realm + ":proposal-rejected:" + proposal.id(),
                "Proposal Rejected",
                "Your proposal \"" + proposal.title() + "\" was rejected by the Seat of Rule.",
                Map.of("realmId", realm, "proposalId", proposal.id(), "category", proposal.category()));
        emit("government-proposal-resolved", actor.getUuid(), realm, "proposal", proposal.id(),
                Map.of("status", "rejected", "category", proposal.category()));
        return rejected;
    }

    private GovernmentProposalDecision proposalDecision(RealmGovernmentState government, GovernmentProposalRecord proposal) {
        GovernmentFormDefinition form = government.activeGovernmentFormId().isBlank()
                ? null : definitions.require(government.activeGovernmentFormId());
        return GovernmentProposalDecisionPolicy.proposalDecision(
                government, form, proposal, authorityHolders(government.realmId()));
    }

    private GovernmentProposalDecision finalTextDecision(RealmGovernmentState government, GovernmentProposalRecord proposal) {
        return GovernmentProposalDecisionPolicy.finalTextDecision(
                government, proposal, authorityHolders(government.realmId()));
    }

    private Set<UUID> proposalDecisionMakers(RealmGovernmentState government, GovernmentFormDefinition form) {
        return GovernmentProposalDecisionPolicy.proposalDecisionMakers(
                government, form, authorityHolders(government.realmId()));
    }

    private Set<UUID> finalTextDecisionMakers(RealmGovernmentState government) {
        return GovernmentProposalDecisionPolicy.finalTextDecisionMakers(
                government, authorityHolders(government.realmId()));
    }

    private void notifyFinalTextDecisionMakers(
            String realmId,
            String title,
            String body,
            String dedupe,
            Map<String, String> metadata
    ) {
        for (UUID holder : finalTextDecisionMakers(realm(realmId))) {
            notifyPersonal(holder, "official-text-review", realmId + ":" + dedupe + ":" + holder,
                    title, body, metadata);
        }
    }

    private void reconcileOfficeTerms() {
        long now = System.currentTimeMillis();
        for (RealmGovernmentState government : state.realms.values()) {
            long chosenAt = government.foundingElectionCompletedAt() > 0L
                    ? government.foundingElectionCompletedAt()
                    : now;
            government.officeHolders().forEach((officeId, holders) ->
                    holders.forEach(holder -> ensureOfficeTerm(government.realmId(), officeId, holder, chosenAt)));
        }
        state.officeTerms.entrySet().removeIf(entry -> {
            GovernmentOfficeTermRecord term = entry.getValue();
            RealmGovernmentState government = state.realms.get(term.realmId());
            return government == null || term.active()
                    && !government.officeHolders().getOrDefault(term.officeId(), Set.of()).contains(term.holderId());
        });
    }

    private void ensureOfficeTerm(String realmId, String officeId, UUID holderId, long chosenAt) {
        if (holderId == null) return;
        Optional<GovernmentOfficeTermRecord> active = state.officeTerms.values().stream()
                .filter(term -> term.active()
                        && normalize(term.realmId()).equals(normalize(realmId))
                        && normalize(term.officeId()).equals(normalize(officeId))
                        && holderId.equals(term.holderId()))
                .findFirst();
        if (active.isPresent()) return;
        GovernmentOfficeTermRecord term = GovernmentOfficeTermRecord.active(realmId, officeId, holderId, chosenAt);
        state.officeTerms.put(term.key(), term);
        indexOfficeTerm(term);
    }

    private void closeOfficeTerm(String realmId, String officeId, UUID holderId, long removedAt) {
        if (holderId == null) return;
        updateHolderTerms(holderId, term -> term.active()
                && normalize(term.realmId()).equals(normalize(realmId))
                && normalize(term.officeId()).equals(normalize(officeId))
                ? term.withRemovedAt(removedAt) : term);
    }

    private void recordOfficeDecision(String realmId, UUID holderId, boolean approved) {
        if (holderId == null) return;
        updateHolderTerms(holderId, term -> term.active()
                && normalize(term.realmId()).equals(normalize(realmId))
                ? term.withDecision(approved) : term);
    }

    private void rebuildOfficeTermIndex() {
        officeTermsByHolder.clear();
        state.officeTerms.values().forEach(this::indexOfficeTerm);
    }

    private void indexOfficeTerm(GovernmentOfficeTermRecord term) {
        if (term == null || term.holderId() == null) return;
        officeTermsByHolder.computeIfAbsent(term.holderId(), ignored -> new ArrayList<>()).add(term);
    }

    private void updateHolderTerms(UUID holderId,
                                   java.util.function.UnaryOperator<GovernmentOfficeTermRecord> update) {
        List<GovernmentOfficeTermRecord> current = officeTermsByHolder.getOrDefault(holderId, List.of());
        if (current.isEmpty()) return;
        List<GovernmentOfficeTermRecord> updated = new ArrayList<>(current.size());
        for (GovernmentOfficeTermRecord term : current) {
            GovernmentOfficeTermRecord next = update.apply(term);
            state.officeTerms.put(term.key(), next);
            updated.add(next);
        }
        officeTermsByHolder.put(holderId, updated);
    }

    private Optional<CitizenRecord> resolveCitizen(String realmId, String nameOrId) {
        String clean = nameOrId == null ? "" : nameOrId.trim();
        if (clean.isBlank()) return Optional.empty();
        try {
            UUID id = UUID.fromString(clean);
            return api.citizens().find(id).filter(citizen -> normalize(realmId).equals(normalize(citizen.realmId())));
        } catch (IllegalArgumentException ignored) {
            String lower = clean.toLowerCase(java.util.Locale.ROOT);
            return api.citizens().all().stream()
                    .filter(citizen -> normalize(realmId).equals(normalize(citizen.realmId())))
                    .filter(citizen -> citizenName(citizen).toLowerCase(java.util.Locale.ROOT).equals(lower)
                            || citizen.lastKnownUsername() != null
                            && citizen.lastKnownUsername().toLowerCase(java.util.Locale.ROOT).equals(lower))
                    .findFirst();
        }
    }

    private void requireOfficeManager(
            ServerPlayerEntity actor,
            RealmGovernmentState government,
            String officeId,
            boolean appoint
    ) {
        if (actor == null) throw new IllegalArgumentException("Only Realm authority holders can manage offices.");
        if (government.activeGovernmentFormId().isBlank()) {
            throw new IllegalArgumentException("The Realm has no active Government form.");
        }
        String formId = government.activeGovernmentFormId();
        UUID actorId = actor.getUuid();
        if (GovernmentFoundingPhasePolicy.primaryOffice(formId).equals(officeId)) {
            throw new IllegalArgumentException("Primary elected offices are changed through elections, not appointments.");
        }
        boolean allowed = switch (formId) {
            case "monarchy" -> government.officeHolders().getOrDefault("monarch", Set.of()).contains(actorId)
                    && Set.of("heir", "officer").contains(officeId);
            case "republic" -> government.officeHolders().getOrDefault("president", Set.of()).contains(actorId)
                    && "officer".equals(officeId);
            default -> false;
        };
        if (!allowed) {
            throw new IllegalArgumentException("Your office cannot " + (appoint ? "appoint" : "remove") + " that office.");
        }
    }

    private static void requireOffice(
            ServerPlayerEntity player,
            RealmGovernmentState government,
            String officeId,
            String message
    ) {
        if (player == null || !government.officeHolders().getOrDefault(officeId, Set.of()).contains(player.getUuid())) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String finalTitle(GovernmentProposalRecord proposal) {
        return proposal.finalTitle().isBlank() ? proposal.title() : proposal.finalTitle();
    }

    private static String finalBody(GovernmentProposalRecord proposal) {
        return proposal.finalBody().isBlank() ? proposal.body() : proposal.finalBody();
    }

    public int activeCitizenThreshold(String realm) {
        long active = api.citizens().all().stream()
                .filter(citizen -> realm.equals(normalize(citizen.realmId())))
                .filter(api.citizens()::isActiveCitizen)
                .count();
        return Math.max(1, (int) active / 2 + 1);
    }

    private void requireDirectRecordAuthority(ServerPlayerEntity player, String realm) {
        RealmGovernmentState government = realm(realm);
        if (government.activeGovernmentFormId().isBlank()) {
            throw new IllegalArgumentException("The Realm has no active Government form.");
        }
        GovernmentFormDefinition form = definitions.require(government.activeGovernmentFormId());
        if ("monarchy".equals(form.id())
                && government.officeHolders().getOrDefault("monarch", Set.of()).contains(player.getUuid())) {
            return;
        }
        throw new IllegalArgumentException("This Government form requires proposal approval before official records.");
    }

    private String nextProposalId(String realm, String title) {
        return GovernmentTextRules.nextId(state.proposals.keySet(), realm + "_proposal_", title);
    }

    private String nextLawId(String realm, String title) {
        return GovernmentTextRules.nextId(state.laws.keySet(), realm + "_law_", title);
    }

    public List<String> realmColorOptions() {
        return GovernmentTextRules.realmColors();
    }

    public void seedColorOptions(GovernmentVoteState vote) {
        for (String color : GovernmentTextRules.realmColors()) {
            vote.options.putIfAbsent(color, GovernmentVoteOption.realmColor(
                    color, GovernmentTextRules.colorLabel(color), "Use " + GovernmentTextRules.colorLabel(color) + " for public Realm presentation."));
        }
    }

    private void save() {
        if (server != null) storage.save(server, state);
    }

    private void refreshIdentities() {
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach(api.realms()::applyCurrentScoreboardTeam);
            api.identitySync().syncAll(server);
        }
    }

    private void emit(
            String eventType,
            UUID actorId,
            String realmId,
            String subjectType,
            String subjectId,
            Map<String, String> metadata
    ) {
        api.system().events().emitDomainEvent(ElarionDomainEvent.of(
                "elarion_government", eventType, actorId, realmId, subjectType, subjectId, metadata));
    }

    private String officialName(String realmId) {
        return api.realms().find(realmId).map(api.realms()::officialName).orElse(realmId);
    }

    private void sanitizeStoredVotes() {
        for (GovernmentVoteState vote : state.votes.values()) {
            if (vote.type == GovernmentVoteType.REALM_NAME && !vote.resolved) {
                vote.options.entrySet().removeIf(entry -> !validStoredRealmName(entry.getValue()));
            }
            Set<String> validOptions = Set.copyOf(vote.options.keySet());
            vote.ballots.replaceAll((voter, selections) -> selections.stream()
                    .filter(validOptions::contains)
                    .distinct()
                    .toList());
            vote.ballots.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
    }

    private boolean migrateRemovedForms() {
        Set<String> activeFormIds = definitions.forms().stream()
                .filter(GovernmentFormDefinition::enabled)
                .map(GovernmentFormDefinition::id)
                .collect(java.util.stream.Collectors.toSet());
        boolean changed = false;
        for (Map.Entry<String, RealmGovernmentState> entry : new ArrayList<>(state.realms.entrySet())) {
            RealmGovernmentState government = entry.getValue();
            if (government == null || government.activeGovernmentFormId().isBlank()) continue;
            if (activeFormIds.contains(government.activeGovernmentFormId())) continue;
            String realmId = normalize(entry.getKey());
            restoreAuthorityTitlesForRealm(realmId);
            state.realms.put(realmId, RealmGovernmentState.empty(realmId));
            state.votes.entrySet().removeIf(voteEntry -> realmId.equals(normalize(voteEntry.getValue().realmId)));
            state.proposals.entrySet().removeIf(proposalEntry -> realmId.equals(normalize(proposalEntry.getValue().realmId())));
            state.laws.entrySet().removeIf(lawEntry -> realmId.equals(normalize(lawEntry.getValue().realmId())));
            state.officeTerms.entrySet().removeIf(termEntry -> realmId.equals(normalize(termEntry.getValue().realmId())));
            state.authorityTitleRestores.keySet().removeIf(key -> key.startsWith(realmId + "|"));
            changed = true;
        }
        if (changed) rebuildOfficeTermIndex();
        return changed;
    }

    private boolean reconcileFoundingCompletion() {
        boolean changed = false;
        for (Map.Entry<String, RealmGovernmentState> entry : new ArrayList<>(state.realms.entrySet())) {
            RealmGovernmentState government = entry.getValue();
            if (government == null || government.activeGovernmentFormId().isBlank()) continue;
            GovernmentFormDefinition form;
            try {
                form = definitions.require(government.activeGovernmentFormId());
            } catch (IllegalArgumentException exception) {
                continue;
            }
            String voteKey = voteKey(entry.getKey(), GovernmentVoteType.FOUNDING_ELECTION);
            if (GovernmentFoundingPhasePolicy.hasCompletedLeadershipVacancy(government)) {
                government = government.withFoundingElectionReopened();
                state.realms.put(entry.getKey(), government);
                state.votes.put(voteKey,
                        new GovernmentVoteState(entry.getKey(), GovernmentVoteType.FOUNDING_ELECTION));
                changed = true;
            } else if (GovernmentFoundingPhasePolicy.electionComplete(form, government)) {
                if (government.foundingElectionCompletedAt() <= 0L) {
                    government = government.withFoundingElectionComplete();
                    state.realms.put(entry.getKey(), government);
                    changed = true;
                }
                if (state.votes.remove(voteKey) != null) changed = true;
            }
        }
        return changed;
    }

    private boolean reconcileLegacyRepublicPetitions() {
        boolean changed = false;
        long now = System.currentTimeMillis();
        for (GovernmentProposalRecord proposal : new ArrayList<>(state.proposals.values())) {
            RealmGovernmentState government = state.realms.get(proposal.realmId());
            if (!shouldMigrateLegacyRepublicPetition(government, proposal)) continue;
            GovernmentProposalRecord migrated = proposal.withStatus(
                    GovernmentProposalStatus.CITIZEN_RATIFICATION, null, now);
            state.proposals.put(proposal.id(), migrated);
            if (government != null && !government.pendingProposalIds().contains(proposal.id())) {
                state.realms.put(proposal.realmId(), government.withPendingProposal(proposal.id()));
            }
            changed = true;
        }
        return changed;
    }

    static boolean shouldMigrateLegacyRepublicPetition(
            RealmGovernmentState government,
            GovernmentProposalRecord proposal
    ) {
        return false;
    }

    private boolean reconcileResolvedColorVotes() {
        boolean changed = false;
        for (GovernmentVoteState vote : state.votes.values()) {
            if (vote.type != GovernmentVoteType.REALM_COLOR || !vote.resolved || vote.winnerIds.size() != 1) continue;
            String winner = vote.winnerIds.getFirst();
            if (!vote.options.containsKey(winner)) continue;
            String normalizedColor;
            try {
                normalizedColor = GovernmentTextRules.normalizeColor(winner);
            } catch (IllegalArgumentException exception) {
                continue;
            }
            RealmGovernmentState current = state.realms.get(vote.realmId);
            if (current == null || normalizedColor.equals(current.votedColor())) continue;
            state.realms.put(vote.realmId, current.withVotedColor(normalizedColor));
            changed = true;
        }
        return changed;
    }

    private static boolean validStoredRealmName(GovernmentVoteOption option) {
        try {
            RealmIdentityRules.validateName(option.title);
            RealmIdentityRules.validateTag(option.tag);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
