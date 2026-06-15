package panetina.elarion.addons.government;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import panetina.elarion.addons.government.model.GovernmentCivicScreen;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentGateStatus;
import panetina.elarion.addons.government.model.GovernmentVoteOption;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.government.network.GovernmentUiActionPayload;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.addons.government.service.GovernmentDefinitionService;
import panetina.elarion.addons.government.service.GovernmentStateService;
import panetina.elarion.addons.government.service.GovernmentUiSessionService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class GovernmentBlockInteractions {
    private static final int LOGICAL_WIDTH = 520;
    private static final int LOGICAL_HEIGHT = 360;
    private static final int MINIMUM_SCALE_PERCENT = 60;
    private static final double INTERACTION_RANGE_SQUARED = 64.0D;
    private static final long SESSION_TTL_MILLIS = 5 * 60 * 1000L;
    private static final GovernmentUiSessionService SESSIONS =
            new GovernmentUiSessionService(SESSION_TTL_MILLIS, INTERACTION_RANGE_SQUARED);

    private GovernmentBlockInteractions() {
    }

    public static void register(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states
    ) {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            var blockState = world.getBlockState(hit.getBlockPos());
            if (blockState.isOf(GovernmentBlocks.CIVIC_FORUM)) {
                openCivicForum(api, definitions, states, serverPlayer, world, hit.getBlockPos(), "");
                return ActionResult.SUCCESS;
            }
            if (blockState.isOf(GovernmentBlocks.SEAT_OF_RULE)) {
                openSeatOfRule(api, definitions, states, serverPlayer, world, hit.getBlockPos(), "");
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    public static void handleAction(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            GovernmentUiActionPayload payload
    ) {
        if (player == null || payload == null) return;
        String action = payload.action() == null ? "" : payload.action();
        String realmId = payload.realmId() == null ? "" : payload.realmId();
        String screenType = payload.screenType() == null ? "" : payload.screenType();
        GovernmentUiSessionService.Session session = validateSession(player, payload.sessionId(), realmId, screenType);
        if (session == null) {
            player.sendMessage(Text.literal("Open the civic block again before using that action."), false);
            return;
        }
        String message;
        try {
            switch (action) {
                case "propose_name" -> {
                    states.proposeRealmName(player, realmId, payload.value(), payload.secondaryValue());
                    message = "Name proposal submitted.";
                }
                case "vote" -> {
                    states.castVote(player, realmId, voteType(payload.screenType()), payload.targetId());
                    message = "Your ballot was recorded.";
                }
                case "nominate_self" -> {
                    states.nominateForFoundingElection(player, realmId);
                    message = "Your founding candidacy was added.";
                }
                case "open_module" -> {
                    sendSeatSnapshot(api, definitions, states, player, realmId, session,
                            "That module is visible now, but its gameplay logic is future work.");
                    return;
                }
                default -> message = "Unknown Government UI action.";
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            message = exception.getMessage() == null ? "Government action failed." : exception.getMessage();
        }

        if ("seat_of_rule".equals(screenType)) {
            sendSeatSnapshot(api, definitions, states, player, realmId, session, message);
        } else {
            sendCivicSnapshot(api, definitions, states, player, realmId, session, message);
        }
    }

    private static void openCivicForum(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            World world,
            BlockPos pos,
            String message
    ) {
        Optional<RealmDefinition> realm = realmForBlock(api, world);
        if (realm.isEmpty()) {
            player.sendMessage(Text.literal("This Civic Forum must be placed in a Realm-owned world."), false);
            return;
        }
        if (!belongsToRealm(api, player, realm.get().id())) {
            player.sendMessage(Text.literal("Only citizens of this Realm may use its Civic Forum."), false);
            return;
        }
        sendCivicSnapshot(api, definitions, states, player, realm.get().id(),
                createSession(player, "civic_forum", realm.get().id(), world, pos), message);
    }

    public static void openCivicForumFromNotification(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            String realmId
    ) {
        if (!belongsToRealm(api, player, realmId)) {
            throw new IllegalArgumentException("Only citizens of this Realm may open its Civic Forum.");
        }
        sendCivicSnapshot(api, definitions, states, player, realmId,
                createSession(player, "civic_forum", realmId, player.getWorld(), player.getBlockPos()), "");
    }

    private static void openSeatOfRule(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            World world,
            BlockPos pos,
            String message
    ) {
        Optional<RealmDefinition> realm = realmForBlock(api, world);
        if (realm.isEmpty()) {
            player.sendMessage(Text.literal("This Seat of Rule must be placed in a Realm-owned world."), false);
            return;
        }
        if (!belongsToRealm(api, player, realm.get().id())) {
            player.sendMessage(Text.literal("Only citizens of this Realm may use its Seat of Rule."), false);
            return;
        }
        sendSeatSnapshot(api, definitions, states, player, realm.get().id(),
                createSession(player, "seat_of_rule", realm.get().id(), world, pos), message);
    }

    private static void sendCivicSnapshot(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            String realmId,
            GovernmentUiSessionService.Session session,
            String message
    ) {
        RealmDefinition realm = api.realms().find(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Realm " + realmId));
        RealmGovernmentState state = states.realm(realm.id());
        GovernmentGateStatus gates = states.gates(realm.id());
        GovernmentCivicScreen screen = states.currentCivicScreen(realm.id());
        boolean eligible = states.eligibleCitizen(player, realm.id());
        boolean locked = false;
        boolean voted = false;
        long voteEndsAt = 0L;
        String title;
        String subtitle;
        String primaryAction = "";
        List<GovernmentUiOpenPayload.Row> rows = new ArrayList<>();
        List<GovernmentUiOpenPayload.Row> forms = List.of();
        List<GovernmentUiOpenPayload.Row> offices = List.of();
        List<GovernmentUiOpenPayload.Row> modules = List.of();

        switch (screen) {
            case REALM_NAME -> {
                title = "Realm Name";
                locked = !gates.nameVoteUnlocked();
                Optional<GovernmentVoteState> vote = states.existingVote(realm.id(), GovernmentVoteType.REALM_NAME);
                long now = System.currentTimeMillis();
                boolean proposalEnded = vote.map(candidate -> candidate.proposalEnded(now)).orElse(false);
                boolean proposalActive = vote.map(candidate -> candidate.proposalActive(now)).orElse(false);
                boolean alreadyProposed = vote.map(candidate -> candidate.options.values().stream()
                        .anyMatch(option -> player.getUuid().equals(option.proposedBy))).orElse(false);
                primaryAction = !locked && eligible && !proposalEnded && !alreadyProposed ? "propose_name" : "";
                voted = hasVoted(vote, player.getUuid());
                voteEndsAt = vote.map(candidate -> proposalEnded ? candidate.endsAt : candidate.proposalEndsAt)
                        .orElse(0L);
                String phase = locked ? "Locked until Foundation I"
                        : !proposalEnded ? proposalActive ? "Proposal phase" : "Waiting for the first proposal"
                        : vote.map(candidate -> candidate.active(now)).orElse(false) ? "Voting phase"
                        : "Voting is ready";
                subtitle = phase + " - " + api.realms().officialName(realm);
                rows.addAll(nameProposalRows(vote, player.getUuid(), !locked && eligible && proposalEnded,
                        "No Realm names proposed yet."));
            }
            case GOVERNMENT_FORM -> {
                title = "Government Form";
                subtitle = "Choose how authority is organized.";
                locked = !gates.governmentVoteUnlocked();
                Optional<GovernmentVoteState> vote = states.existingVote(realm.id(), GovernmentVoteType.GOVERNMENT_FORM);
                voted = hasVoted(vote, player.getUuid());
                voteEndsAt = vote.map(candidate -> candidate.endsAt).orElse(0L);
                rows.add(row("rules", "Rules",
                        "The first vote starts a 24h window. Plurality wins; ties trigger a runoff.",
                        locked ? "Locked until Foundation II" : eligible ? "Ready" : "Active citizens only",
                        !locked && eligible, false));
                boolean formRowsUnlocked = !locked && eligible;
                String activeFormId = state.activeGovernmentFormId();
                forms = definitions.forms().stream()
                        .filter(GovernmentFormDefinition::enabled)
                        .map(form -> row(form.id(), form.displayName(), form.description(),
                                selected(vote, player.getUuid(), form.id()) ? "Your vote" : "Vote",
                                formRowsUnlocked, form.id().equals(activeFormId)))
                        .toList();
            }
            case FOUNDING_ELECTION -> {
                title = "Founding Election";
                subtitle = "Choose the first authority holders for " + formName(definitions, state) + ".";
                locked = !gates.foundingElectionUnlocked();
                primaryAction = !locked && eligible ? "nominate_self" : "";
                Optional<GovernmentVoteState> vote = states.existingVote(realm.id(), GovernmentVoteType.FOUNDING_ELECTION);
                voted = hasVoted(vote, player.getUuid());
                voteEndsAt = vote.map(candidate -> candidate.endsAt).orElse(0L);
                rows.add(row("rules", "Rules",
                        "Nominate yourself, then approve candidates. Multi-seat offices accept multiple approvals.",
                        locked ? "Locked until Foundation III" : eligible ? "Ready" : "Active citizens only",
                        !locked && eligible, false));
                offices = voteRows(vote, player.getUuid(), !locked && eligible, "No candidates have nominated yet.");
            }
            case CITIZEN_FEATURES -> {
                title = "Citizen Civic Features";
                subtitle = "Founding is complete. Citizen modules can now be built out.";
                rows.add(row("complete", "Founding Complete",
                        "The Realm has a name, a government form, and founding authority holders.",
                        "Unlocked", true, true));
                modules = citizenModules();
            }
            default -> throw new IllegalStateException("Unhandled Civic screen " + screen);
        }

        send(player, new GovernmentUiOpenPayload(
                screenId(screen), title, subtitle, realm.id(), api.realms().officialName(realm), "default",
                LOGICAL_WIDTH, LOGICAL_HEIGHT, MINIMUM_SCALE_PERCENT,
                locked, eligible, voted, voteEndsAt, fallbackMessage(message, locked, eligible), primaryAction,
                session.id(),
                rows, forms, offices, modules));
    }

    private static void sendSeatSnapshot(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            String realmId,
            GovernmentUiSessionService.Session session,
            String message
    ) {
        RealmDefinition realm = api.realms().find(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Realm " + realmId));
        RealmGovernmentState state = states.realm(realm.id());
        GovernmentGateStatus gates = states.gates(realm.id());
        boolean locked = !gates.seatOfRuleUnlocked();
        List<GovernmentUiOpenPayload.Row> status = new ArrayList<>();
        List<GovernmentUiOpenPayload.Row> formRows = new ArrayList<>();
        List<GovernmentUiOpenPayload.Row> officeRows = new ArrayList<>();
        List<GovernmentUiOpenPayload.Row> modules = new ArrayList<>();

        if (locked) {
            status.add(row("seat_locked", "Seat Locked",
                    "Complete Foundation III and finish the founding election in the Civic Forum.",
                    "Locked", false, false));
        } else if (state.activeGovernmentFormId().isBlank()) {
            status.add(row("missing_form", "No Government Chosen",
                    "The Civic Forum must finish the government form vote first.", "Waiting", false, false));
        } else {
            GovernmentFormDefinition form = definitions.require(state.activeGovernmentFormId());
            status.add(row("authority", api.realms().officialName(realm),
                    "Authority-facing modules for the Realm.", "Active", true, true));
            formRows.add(row(form.id(), form.displayName(), form.description(), "Current", true, true));
            if (state.officeHolders().isEmpty()) {
                officeRows.add(row("vacant", "Offices", "No offices are currently filled.", "Vacant", true, false));
            } else {
                state.officeHolders().forEach((office, holders) -> officeRows.add(row(office,
                        officeLabel(form, office), holderNames(api, holders),
                        holders.size() + " holder(s)", true, !holders.isEmpty())));
            }
            modules.add(row("notices", "Notices", "Authority notices and announcements shell.", "Open", true, false));
            modules.add(row("laws", "Laws", "Law records and law proposals are future modules.", "Future", true, false));
            modules.add(row("proposals", "Proposals", "Authority proposal handling is future work.", "Future", true, false));
            modules.add(row("offices", "Office Management", "Appointments and removals are future UI work.", "Future", true, false));
        }

        send(player, new GovernmentUiOpenPayload(
                "seat_of_rule", "Seat of Rule",
                locked ? "Authority seat is locked." : "Authority summary for " + api.realms().officialName(realm),
                realm.id(), api.realms().officialName(realm), "default",
                LOGICAL_WIDTH, LOGICAL_HEIGHT, MINIMUM_SCALE_PERCENT,
                locked, states.eligibleCitizen(player, realm.id()), false, 0L,
                fallbackMessage(message, locked, true), "", session.id(),
                status, formRows, officeRows, modules));
    }

    private static List<GovernmentUiOpenPayload.Row> voteRows(
            Optional<GovernmentVoteState> vote,
            UUID viewer,
            boolean unlocked,
            String emptyMessage
    ) {
        if (vote.isEmpty() || vote.get().options.isEmpty()) {
            return List.of(row("empty", "Nothing Submitted", emptyMessage, "Waiting", false, false));
        }
        return vote.get().options.values().stream()
                .sorted(Comparator.comparing(option -> option.createdAt))
                .map(option -> row(option.id, option.title, option.body,
                        selected(vote, viewer, option.id) ? "Your vote" : "Vote",
                        unlocked, vote.get().winnerIds.contains(option.id)))
                .toList();
    }

    private static List<GovernmentUiOpenPayload.Row> nameProposalRows(
            Optional<GovernmentVoteState> vote,
            UUID viewer,
            boolean votingUnlocked,
            String emptyMessage
    ) {
        if (vote.isEmpty() || vote.get().options.isEmpty()) {
            return List.of(row("empty", "Waiting for Proposals", emptyMessage, "Waiting", false, false));
        }
        java.util.Map<String, Long> counts = voteCounts(vote.get());
        return vote.get().options.values().stream()
                .sorted(Comparator
                        .<GovernmentVoteOption>comparingLong(option -> counts.getOrDefault(option.id, 0L))
                        .reversed()
                        .thenComparing(option -> option.createdAt))
                .map(option -> {
                    boolean selected = selected(vote, viewer, option.id);
                    return row(option.id, "Name: " + option.title, "Tag: [" + option.tag + "]",
                            selected ? "Your vote" : votingUnlocked ? "Vote" : "Proposed",
                            votingUnlocked, selected);
                })
                .toList();
    }

    private static java.util.Map<String, Long> voteCounts(GovernmentVoteState vote) {
        java.util.Map<String, Long> counts = new java.util.LinkedHashMap<>();
        for (List<String> selections : vote.ballots.values()) {
            for (String selected : selections) {
                if (vote.options.containsKey(selected)) counts.merge(selected, 1L, Long::sum);
            }
        }
        return counts;
    }

    private static List<GovernmentUiOpenPayload.Row> citizenModules() {
        return List.of(
                row("laws", "Laws", "Citizen law visibility and voting hooks are future work.", "Future", true, false),
                row("proposals", "Proposals", "Citizen proposal creation is future work.", "Future", true, false),
                row("notices", "Notices", "Realm public notices are future work.", "Future", true, false),
                row("offices", "Offices", "View authority holders and future office requests.", "Future", true, false)
        );
    }

    private static String fallbackMessage(String message, boolean locked, boolean eligible) {
        if (message != null && !message.isBlank()) return message;
        if (locked) return "";
        if (!eligible) return "Only active citizens of this Realm can use this civic action.";
        return "";
    }

    private static boolean hasVoted(Optional<GovernmentVoteState> vote, UUID viewer) {
        return vote.isPresent() && vote.get().ballots.containsKey(viewer.toString());
    }

    private static boolean selected(Optional<GovernmentVoteState> vote, UUID viewer, String optionId) {
        return vote.isPresent() && vote.get().ballots.getOrDefault(viewer.toString(), List.of()).contains(optionId);
    }

    private static GovernmentVoteType voteType(String screenType) {
        return switch (screenType) {
            case "civic_name" -> GovernmentVoteType.REALM_NAME;
            case "civic_form" -> GovernmentVoteType.GOVERNMENT_FORM;
            case "civic_election" -> GovernmentVoteType.FOUNDING_ELECTION;
            default -> throw new IllegalArgumentException("That screen does not accept votes.");
        };
    }

    private static String screenId(GovernmentCivicScreen screen) {
        return switch (screen) {
            case REALM_NAME -> "civic_name";
            case GOVERNMENT_FORM -> "civic_form";
            case FOUNDING_ELECTION -> "civic_election";
            case CITIZEN_FEATURES -> "civic_features";
        };
    }

    private static Optional<RealmDefinition> realmForBlock(ElarionApi api, World world) {
        String worldId = world.getRegistryKey().getValue().toString();
        return api.realm().realms().ownerForWorld(worldId);
    }

    private static boolean belongsToRealm(ElarionApi api, ServerPlayerEntity player, String realmId) {
        return player != null && realmId != null && !realmId.isBlank()
                && realmId.equals(api.citizens().getOrCreate(player).realmId());
    }

    private static GovernmentUiSessionService.Session createSession(
            ServerPlayerEntity player,
            String blockType,
            String realmId,
            World world,
            BlockPos pos
    ) {
        return SESSIONS.create(
                player.getUuid(),
                blockType,
                realmId,
                world.getRegistryKey().getValue().toString(),
                pos,
                System.currentTimeMillis());
    }

    private static GovernmentUiSessionService.Session validateSession(
            ServerPlayerEntity player,
            String sessionId,
            String realmId,
            String screenType
    ) {
        if (player == null || sessionId == null || sessionId.isBlank()) return null;
        String expectedBlock = "seat_of_rule".equals(screenType) ? "seat_of_rule" : "civic_forum";
        String playerWorld = player.getWorld().getRegistryKey().getValue().toString();
        GovernmentUiSessionService.Session session = SESSIONS.validate(
                player.getUuid(),
                sessionId,
                realmId,
                expectedBlock,
                playerWorld,
                player.getX(),
                player.getY(),
                player.getZ(),
                System.currentTimeMillis()).orElse(null);
        if (session == null) return null;
        var state = player.getWorld().getBlockState(session.pos());
        if ("seat_of_rule".equals(expectedBlock) && !state.isOf(GovernmentBlocks.SEAT_OF_RULE)) return null;
        if ("civic_forum".equals(expectedBlock) && !state.isOf(GovernmentBlocks.CIVIC_FORUM)) return null;
        return session;
    }

    private static String formName(GovernmentDefinitionService definitions, RealmGovernmentState state) {
        if (state.activeGovernmentFormId().isBlank()) return "the selected Government form";
        return definitions.require(state.activeGovernmentFormId()).displayName();
    }

    private static GovernmentUiOpenPayload.Row row(
            String id,
            String title,
            String body,
            String state,
            boolean unlocked,
            boolean complete
    ) {
        return new GovernmentUiOpenPayload.Row(id, title, body, state, unlocked, complete);
    }

    private static String officeLabel(GovernmentFormDefinition form, String officeId) {
        return form.offices().stream()
                .filter(office -> office.id().equals(officeId))
                .findFirst()
                .map(office -> office.displayName().isBlank() ? office.id() : office.displayName())
                .orElse(officeId);
    }

    private static String holderNames(ElarionApi api, Set<UUID> holders) {
        if (holders == null || holders.isEmpty()) return "No citizens assigned.";
        return holders.stream()
                .map(uuid -> api.citizens().find(uuid)
                        .map(GovernmentBlockInteractions::citizenName)
                        .orElse(uuid.toString()))
                .sorted()
                .toList()
                .stream()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String citizenName(CitizenRecord citizen) {
        if (citizen.nickname() != null && !citizen.nickname().isBlank()) return citizen.nickname();
        if (citizen.lastKnownUsername() != null && !citizen.lastKnownUsername().isBlank()) {
            return citizen.lastKnownUsername();
        }
        return citizen.uuid().toString();
    }

    private static void send(ServerPlayerEntity player, GovernmentUiOpenPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

}
