package panetina.elarion.addons.government.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.government.model.GovernmentCivicScreen;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentGateStatus;
import panetina.elarion.addons.government.model.GovernmentVoteOption;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.groups.api.ElarionGroupsApi;
import panetina.elarion.addons.offerings.api.ElarionOfferingsApi;
import panetina.elarion.addons.government.storage.GovernmentState;
import panetina.elarion.addons.government.storage.GovernmentStorage;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;

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
import java.util.regex.Pattern;

public final class GovernmentStateService {
    private static final Duration FOUNDING_VOTE_DURATION = Duration.ofHours(24);
    private static final Pattern REALM_TAG = Pattern.compile("[A-Z0-9]{2,6}");
    private final ElarionApi api;
    private final GovernmentDefinitionService definitions;
    private final GovernmentStorage storage;
    private GovernmentState state = new GovernmentState();
    private MinecraftServer server;
    private int authorityCleanupTicks;

    public GovernmentStateService(ElarionApi api, GovernmentDefinitionService definitions, GovernmentStorage storage) {
        this.api = api;
        this.definitions = definitions;
        this.storage = storage;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        state = storage.load(server);
        api.realms().all().forEach(realm ->
                state.realms.computeIfAbsent(realm.id(), RealmGovernmentState::empty));
        save();
    }

    public void tick() {
        if (server == null) return;
        resolveExpiredVotes(System.currentTimeMillis());
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

    public RealmGovernmentState setForm(String realmId, String formId) {
        definitions.require(formId);
        RealmGovernmentState updated = realm(realmId).withForm(formId);
        state.realms.put(realmId, updated);
        save();
        api.history().recordChronicle("government", "form-set", null, "realm", realmId, realmId,
                java.util.Map.of("governmentForm", formId),
                "The Realm " + realmId + " was assigned the government form " + formId + ".");
        return updated;
    }

    public RealmGovernmentState setVotedIdentity(String realmId, String displayName, String tag) {
        String normalizedRealm = normalize(realmId);
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name is required.");
        }
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Tag is required.");
        }
        RealmGovernmentState updated = realm(normalizedRealm).withVotedIdentity(
                displayName.trim().replaceAll("\\s+", " "),
                tag.trim().toUpperCase(java.util.Locale.ROOT));
        state.realms.put(normalizedRealm, updated);
        save();
        api.history().recordChronicle("government", "realm-identity-set", null, "realm", normalizedRealm,
                normalizedRealm, java.util.Map.of("displayName", updated.votedDisplayName(), "tag", updated.votedTag()),
                "The Realm " + normalizedRealm + " recorded its founding name.");
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
        api.history().recordChronicle("government", "founding-election-complete", null, "realm", normalizedRealm,
                normalizedRealm, java.util.Map.of("governmentForm", updated.activeGovernmentFormId()),
                "The Realm " + normalizedRealm + " completed its first founding election.");
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
                !realm.activeGovernmentFormId().isBlank(),
                realm.foundingElectionCompletedAt() > 0L);
    }

    public GovernmentCivicScreen currentCivicScreen(String realmId) {
        GovernmentGateStatus gates = gates(realmId);
        if (!gates.nameChosen()) return GovernmentCivicScreen.REALM_NAME;
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
        if (!gates.nameVoteUnlocked()) throw new IllegalArgumentException("Realm naming is locked.");
        requireEligible(player, realm);
        String cleanName = cleanName(displayName);
        String cleanTag = tag == null ? "" : tag.trim().toUpperCase(java.util.Locale.ROOT);
        if (cleanName.length() < 3 || cleanName.length() > 32) {
            throw new IllegalArgumentException("Realm name must be 3-32 characters.");
        }
        if (!REALM_TAG.matcher(cleanTag).matches()) {
            throw new IllegalArgumentException("Realm tag must be 2-6 uppercase letters or numbers.");
        }
        GovernmentVoteState vote = vote(realm, GovernmentVoteType.REALM_NAME);
        String id = "name_" + Integer.toUnsignedString((cleanName + "|" + cleanTag)
                .toLowerCase(java.util.Locale.ROOT).hashCode(), 36);
        vote.options.putIfAbsent(id, GovernmentVoteOption.realmName(id, cleanName, cleanTag, player.getUuid()));
        save();
        api.history().recordChronicle("government", "name-proposed", player.getUuid(), "realm", realm, realm,
                Map.of("displayName", cleanName, "tag", cleanTag), "A Realm name was proposed.");
        return vote;
    }

    public GovernmentVoteState nominateForFoundingElection(ServerPlayerEntity player, String realmId) {
        String realm = normalize(realmId);
        GovernmentGateStatus gates = gates(realm);
        if (!gates.foundingElectionUnlocked()) throw new IllegalArgumentException("Founding elections are locked.");
        requireEligible(player, realm);
        RealmGovernmentState current = realm(realm);
        GovernmentFormDefinition form = definitions.require(current.activeGovernmentFormId());
        GovernmentVoteState vote = vote(realm, GovernmentVoteType.FOUNDING_ELECTION);
        for (String officeId : foundingElectionOffices(form)) {
            if ("delegate".equals(officeId) && !eligibleDelegateCandidate(player, realm)) {
                continue;
            }
            String id = officeId + ":" + player.getUuid();
            String groupId = "delegate".equals(officeId)
                    ? ElarionGroupsApi.get().groupFor(player.getUuid()).map(group -> group.id()).orElse("")
                    : "";
            String title = citizenName(api.citizens().getOrCreate(player)) + " - " + officeLabel(form, officeId);
            String body = groupId.isBlank() ? "Candidate for " + officeLabel(form, officeId)
                    : "Represents group " + groupId;
            vote.options.putIfAbsent(id, GovernmentVoteOption.candidate(
                    id, title, body, officeId, player.getUuid(), groupId));
        }
        if (vote.options.isEmpty()) {
            throw new IllegalArgumentException("You are not eligible for any founding office.");
        }
        save();
        api.history().recordChronicle("government", "founding-nominated", player.getUuid(), "realm", realm, realm,
                Map.of("form", form.id()), "A citizen entered the founding election.");
        return vote;
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
        if (type == GovernmentVoteType.GOVERNMENT_FORM && vote.options.isEmpty()) {
            definitions.forms().stream().filter(GovernmentFormDefinition::enabled)
                    .forEach(form -> vote.options.putIfAbsent(form.id(),
                            GovernmentVoteOption.governmentForm(form.id(), form.displayName(), form.description())));
        }
        GovernmentVoteOption option = vote.options.get(optionId);
        if (option == null) throw new IllegalArgumentException("Unknown vote option.");
        long now = System.currentTimeMillis();
        vote.startIfNeeded(now, FOUNDING_VOTE_DURATION);
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

    public RealmGovernmentState assignOffice(String realmId, String officeId, UUID citizenId) {
        String normalizedRealm = normalize(realmId);
        var form = definitions.require(realm(normalizedRealm).activeGovernmentFormId());
        var office = form.offices().stream()
                .filter(candidate -> candidate.id().equals(officeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown office " + officeId + " for " + form.id()));
        CitizenRecord citizen = api.citizens().find(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown citizen " + citizenId));
        if (!normalizedRealm.equals(citizen.realmId())) {
            throw new IllegalArgumentException("Office holder must belong to the Realm.");
        }
        Set<UUID> current = realm(normalizedRealm).officeHolders().getOrDefault(officeId, Set.of());
        if (!current.contains(citizenId) && current.size() >= office.maxHolders()) {
            throw new IllegalArgumentException("Office " + officeId + " is full.");
        }
        RealmGovernmentState updated = realm(normalizedRealm).withOfficeHolder(officeId, citizenId);
        state.realms.put(normalizedRealm, updated);
        save();
        api.history().recordChronicle("government", "office-assigned", citizenId, "office", officeId,
                normalizedRealm, java.util.Map.of("office", officeId, "form", form.id()),
                "A citizen was assigned to " + office.displayName() + " in " + normalizedRealm + ".");
        return updated;
    }

    public RealmGovernmentState removeOffice(String realmId, String officeId, UUID citizenId) {
        String normalizedRealm = normalize(realmId);
        RealmGovernmentState updated = realm(normalizedRealm).withoutOfficeHolder(officeId, citizenId);
        state.realms.put(normalizedRealm, updated);
        save();
        api.history().recordChronicle("government", "office-removed", citizenId, "office", officeId,
                normalizedRealm, java.util.Map.of("office", officeId),
                "A citizen was removed from " + officeId + " in " + normalizedRealm + ".");
        return updated;
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

    public int removeInactiveAuthority(long now) {
        int removed = 0;
        for (RealmGovernmentState current : List.copyOf(state.realms.values())) {
            if (current.activeGovernmentFormId().isBlank()) continue;
            var form = definitions.require(current.activeGovernmentFormId());
            Map<String, Set<UUID>> offices = mutableOffices(current);
            boolean changed = false;
            int beforeRealm = removed;
            if ("monarchy".equals(form.id())) {
                removed += handleMonarchInactivity(current.realmId(), offices, now);
                changed = removed > beforeRealm;
            }
            for (String office : List.copyOf(offices.keySet())) {
                Set<UUID> holders = offices.getOrDefault(office, Set.of());
                for (UUID holder : List.copyOf(holders)) {
                    if (!inactive(holder, now)) continue;
                    holders.remove(holder);
                    removed++;
                    changed = true;
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
                        offices,
                        current.activeLawIds(),
                        current.pendingProposalIds(),
                        current.nameVoteCompletedAt(),
                        current.foundingElectionCompletedAt(),
                        current.lastReformAt()));
            }
        }
        if (removed > 0) save();
        return removed;
    }

    private int handleMonarchInactivity(String realmId, Map<String, Set<UUID>> offices, long now) {
        Set<UUID> monarchs = offices.getOrDefault("monarch", Set.of());
        UUID monarch = monarchs.stream().findFirst().orElse(null);
        if (monarch == null || !inactive(monarch, now)) return 0;
        offices.computeIfAbsent("monarch", ignored -> new LinkedHashSet<>()).remove(monarch);
        recordAuthorityRemoval(realmId, "monarchy", "monarch", monarch);
        UUID heir = offices.getOrDefault("heir", Set.of()).stream()
                .filter(candidate -> !inactive(candidate, now))
                .findFirst()
                .orElse(null);
        if (heir != null) {
            offices.computeIfAbsent("heir", ignored -> new LinkedHashSet<>()).remove(heir);
            offices.computeIfAbsent("monarch", ignored -> new LinkedHashSet<>()).add(heir);
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
                if (!gates.nameVoteUnlocked()) throw new IllegalArgumentException("Realm naming is locked.");
            }
            case GOVERNMENT_FORM -> {
                if (!gates.governmentVoteUnlocked()) throw new IllegalArgumentException("Government voting is locked.");
            }
            case FOUNDING_ELECTION -> {
                if (!gates.foundingElectionUnlocked()) throw new IllegalArgumentException("Founding elections are locked.");
            }
        }
    }

    private void requireEligible(ServerPlayerEntity player, String realmId) {
        if (!eligibleCitizen(player, realmId)) {
            throw new IllegalArgumentException("Only active citizens of this Realm can do that.");
        }
    }

    private void resolveVote(GovernmentVoteState vote, long now) {
        if (vote.type == GovernmentVoteType.FOUNDING_ELECTION) {
            resolveFoundingElection(vote, now);
            return;
        }
        Map<String, Long> totals = totals(vote);
        if (totals.isEmpty()) return;
        long best = totals.values().stream().mapToLong(Long::longValue).max().orElse(0L);
        List<String> winners = totals.entrySet().stream()
                .filter(entry -> entry.getValue() == best)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
        if (winners.size() > 1) {
            state.votes.put(voteKey(vote.realmId, vote.type),
                    vote.runoff(winners, now, FOUNDING_VOTE_DURATION));
            return;
        }
        String winner = winners.getFirst();
        vote.resolved = true;
        vote.winnerIds = List.of(winner);
        GovernmentVoteOption option = vote.options.get(winner);
        if (vote.type == GovernmentVoteType.REALM_NAME) {
            setVotedIdentity(vote.realmId, option.title, option.tag);
        } else if (vote.type == GovernmentVoteType.GOVERNMENT_FORM) {
            setForm(vote.realmId, option.formId.isBlank() ? option.id : option.formId);
        }
        api.history().recordChronicle("government", "vote-resolved", null, "realm", vote.realmId, vote.realmId,
                Map.of("type", vote.type.name().toLowerCase(), "winner", winner),
                "A Government vote resolved.");
    }

    private void resolveFoundingElection(GovernmentVoteState vote, long now) {
        RealmGovernmentState current = realm(vote.realmId);
        GovernmentFormDefinition form = definitions.require(current.activeGovernmentFormId());
        Map<String, Long> totals = totals(vote);
        Map<String, List<String>> winnersByOffice = new LinkedHashMap<>();
        List<String> tiedBoundary = new ArrayList<>();
        for (String office : foundingElectionOffices(form)) {
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
            state.votes.put(voteKey(vote.realmId, vote.type),
                    vote.runoff(tiedBoundary, now, FOUNDING_VOTE_DURATION));
            return;
        }
        RealmGovernmentState updated = current;
        List<String> winnerIds = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : winnersByOffice.entrySet()) {
            for (String optionId : entry.getValue()) {
                GovernmentVoteOption option = vote.options.get(optionId);
                if (option == null || option.candidateId == null) continue;
                updated = updated.withOfficeHolder(entry.getKey(), option.candidateId);
                winnerIds.add(optionId);
                if ("delegate".equals(entry.getKey()) && option.groupId != null && !option.groupId.isBlank()) {
                    ElarionGroupsApi.get().setConfederationLocked(option.groupId, true);
                }
            }
        }
        updated = updated.withFoundingElectionComplete();
        state.realms.put(vote.realmId, updated);
        vote.resolved = true;
        vote.winnerIds = List.copyOf(winnerIds);
        api.history().recordChronicle("government", "founding-election-resolved", null, "realm",
                vote.realmId, vote.realmId, Map.of("form", form.id(), "winners", Integer.toString(winnerIds.size())),
                "A Realm founding election resolved.");
    }

    private Map<String, Long> totals(GovernmentVoteState vote) {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (List<String> selections : vote.ballots.values()) {
            for (String selected : selections) {
                if (vote.options.containsKey(selected)) totals.merge(selected, 1L, Long::sum);
            }
        }
        return totals;
    }

    private boolean eligibleDelegateCandidate(ServerPlayerEntity player, String realm) {
        try {
            return ElarionGroupsApi.get().groupFor(player.getUuid())
                    .filter(group -> group.leaderId().equals(player.getUuid()))
                    .filter(group -> ElarionGroupsApi.get().eligibleForConfederationDelegate(group.id(), realm))
                    .isPresent();
        } catch (IllegalStateException exception) {
            return false;
        }
    }

    private List<String> foundingElectionOffices(GovernmentFormDefinition form) {
        return switch (form.id()) {
            case "monarchy" -> List.of("monarch");
            case "republic" -> List.of("president", "council_member");
            case "theocracy" -> List.of("high_priest", "synod_member");
            case "confederation" -> List.of("delegate");
            default -> form.authorityOffices().stream().filter(office -> !"officer".equals(office)).toList();
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

    private String officeLabel(GovernmentFormDefinition form, String officeId) {
        return form.offices().stream()
                .filter(office -> office.id().equals(officeId))
                .findFirst()
                .map(office -> office.displayName().isBlank() ? office.id() : office.displayName())
                .orElse(officeId);
    }

    private static String citizenName(CitizenRecord citizen) {
        if (citizen.nickname() != null && !citizen.nickname().isBlank()) return citizen.nickname();
        if (citizen.lastKnownUsername() != null && !citizen.lastKnownUsername().isBlank()) {
            return citizen.lastKnownUsername();
        }
        return citizen.uuid().toString();
    }

    private static String cleanName(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private static String voteKey(String realmId, GovernmentVoteType type) {
        return normalize(realmId) + ":" + type.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static Map<String, Set<UUID>> mutableOffices(RealmGovernmentState state) {
        Map<String, Set<UUID>> result = new LinkedHashMap<>();
        state.officeHolders().forEach((office, holders) -> result.put(office, new LinkedHashSet<>(holders)));
        return result;
    }

    private void recordAuthorityRemoval(String realmId, String formId, String officeId, UUID citizenId) {
        String eventType = switch (officeId) {
            case "high_priest" -> "succession-crisis";
            case "delegate" -> "delegate-vacancy";
            case "president", "council_member" -> "republic-vacancy";
            default -> "authority-removed-inactive";
        };
        api.history().recordChronicle("government", eventType, citizenId, "office", officeId,
                realmId, java.util.Map.of("form", formId, "office", officeId, "reason", "inactive"),
                "A Government office was vacated because its holder became inactive.");
    }

    private void save() {
        if (server != null) storage.save(server, state);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
