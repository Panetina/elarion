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
import panetina.elarion.addons.government.model.GovernmentFoundingPhase;
import panetina.elarion.addons.government.model.GovernmentGateStatus;
import panetina.elarion.addons.government.model.GovernmentLawRecord;
import panetina.elarion.addons.government.model.GovernmentOfficeTermRecord;
import panetina.elarion.addons.government.model.GovernmentProposalRecord;
import panetina.elarion.addons.government.model.GovernmentProposalStatus;
import panetina.elarion.addons.government.model.GovernmentVoteOption;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.government.network.GovernmentUiActionPayload;
import panetina.elarion.addons.government.network.GovernmentUiFeedbackPayload;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.addons.government.network.GovernmentHeraldrySavePayload;
import panetina.elarion.addons.government.service.GovernmentDefinitionService;
import panetina.elarion.addons.government.service.GovernmentStateService;
import panetina.elarion.addons.government.service.GovernmentUiSessionService;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;
import panetina.elarion.addons.economy.model.EconomyTaxCategory;
import panetina.elarion.addons.government.network.GovernmentTaxPolicySnapshotPayload;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.PublicHistoryEntry;
import panetina.elarion.core.model.RealmDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GovernmentBlockInteractions {
    private static final int LOGICAL_WIDTH = 760;
    private static final int LOGICAL_HEIGHT = 500;
    private static final int MINIMUM_SCALE_PERCENT = 55;
    private static final double INTERACTION_RANGE_SQUARED = 64.0D;
    private static final long SESSION_TTL_MILLIS = 5 * 60 * 1000L;
    private static final Pattern UUID_TEXT_PATTERN = Pattern.compile(
            "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b");
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
            String message = "Open the civic block again before using that action.";
            player.sendMessage(Text.literal(message), false);
            ServerPlayNetworking.send(player, new GovernmentUiFeedbackPayload(message));
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
                case "back", "open_parent" -> {
                    if (screenType.startsWith("seat_module_")) {
                        sendSeatSnapshot(api, definitions, states, player, realmId, session, "");
                    } else {
                        sendCivicSnapshot(api, definitions, states, player, realmId, session, "");
                    }
                    return;
                }
                case "open_module" -> {
                    if (isCivicScreen(screenType)) {
                        if (!"civic_features".equals(screenType) && !screenType.startsWith("civic_module_")) {
                            sendCivicSnapshot(api, definitions, states, player, realmId, session,
                                    foundingRequirementMessage(states, definitions, player, realmId));
                            return;
                        }
                        sendCivicModuleSnapshot(api, definitions, states, player, realmId, session,
                                payload.targetId(), "");
                    } else {
                        sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                                payload.targetId(), "");
                    }
                    return;
                }
                case "create_proposal" -> {
                    states.createProposal(player, realmId, payload.targetId(), payload.value(), payload.secondaryValue());
                    sendCivicModuleSnapshot(api, definitions, states, player, realmId, session,
                            "audience", "Audience request submitted.");
                    return;
                }
                case "approve_proposal" -> {
                    states.reviewProposal(player, realmId, payload.targetId(), true);
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            "review", "Proposal review recorded.");
                    return;
                }
                case "reject_proposal" -> {
                    states.reviewProposal(player, realmId, payload.targetId(), false);
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            "review", "Proposal review recorded.");
                    return;
                }
                case "ratify_proposal" -> {
                    states.ratifyProposal(player, realmId, payload.targetId(), true);
                    if ("civic_features".equals(screenType)) {
                        sendCivicSnapshot(api, definitions, states, player, realmId, session,
                                "Your law vote was recorded.");
                    } else {
                        sendCivicSnapshot(api, definitions, states, player, realmId, session,
                                "Your law vote was recorded.");
                    }
                    return;
                }
                case "oppose_proposal" -> {
                    states.ratifyProposal(player, realmId, payload.targetId(), false);
                    if ("civic_features".equals(screenType)) {
                        sendCivicSnapshot(api, definitions, states, player, realmId, session,
                                "Your law vote was recorded.");
                    } else {
                        sendCivicSnapshot(api, definitions, states, player, realmId, session,
                                "Your law vote was recorded.");
                    }
                    return;
                }
                case "archive_law" -> {
                    states.archiveRecord(player, realmId, payload.targetId());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            "laws", "Law archived.");
                    return;
                }
                case "archive_record" -> {
                    states.archiveRecord(player, realmId, payload.targetId());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            moduleFromScreen(screenType), "Record archived.");
                    return;
                }
                case "restore_record" -> {
                    states.restoreRecord(player, realmId, payload.targetId());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            "archive", "Record restored.");
                    return;
                }
                case "finalize_proposal" -> {
                    GovernmentProposalRecord finalized = states.finalizeProposal(
                            player, realmId, payload.targetId(), payload.value(), payload.secondaryValue());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            moduleFromCategory(finalized.category()), "Proposal finalized into an official record.");
                    return;
                }
                case "add_law_record", "add_notice_record", "add_rule_record", "add_project_record" -> {
                    String category = directRecordCategory(payload.action());
                    GovernmentLawRecord created = states.createDirectRecord(player, realmId, category,
                            payload.value(), payload.secondaryValue());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            moduleFromCategory(category), "Record created.");
                    return;
                }
                case "add_law_vote" -> {
                    states.createRepublicLawVote(player, realmId, payload.value(), payload.secondaryValue());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            "laws", "Republic law vote opened.");
                    return;
                }
                case "send_notice" -> {
                    states.sendNotice(player, realmId, payload.value(), payload.secondaryValue());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            moduleFromScreen(screenType), "Notice sent.");
                    return;
                }
                case "appoint_office" -> {
                    states.appointOffice(player, realmId, payload.targetId(), payload.value());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            "offices", "Office appointed.");
                    return;
                }
                case "remove_office" -> {
                    states.removeOfficeByActor(player, realmId, payload.targetId(), payload.value());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            "offices", "Office holder removed.");
                    return;
                }
                case "resign_office" -> {
                    states.resignOffice(player, realmId, payload.targetId());
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            "offices", "Office resigned.");
                    return;
                }
                case "set_tax_rate" -> {
                    if (!states.canDirectCreateRecords(player, realmId)) {
                        throw new IllegalArgumentException("Only the active Realm authority may set tax policy.");
                    }
                    EconomyTaxCategory category = EconomyTaxCategory.fromId(payload.targetId());
                    long expectedRevision = Long.parseLong(payload.secondaryValue());
                    ElarionEconomyApi.get().setTaxRates(EconomyTaxAuthority.realm(realmId, ""), expectedRevision,
                            java.util.Map.of(category, Integer.parseInt(payload.value())));
                    sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                            "taxes", "Tax policy updated.");
                    return;
                }
                default -> message = "Unknown Government UI action.";
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            message = exception.getMessage() == null ? "Government action failed." : exception.getMessage();
        }

        if ("seat_of_rule".equals(screenType) || screenType.startsWith("seat_module_")) {
            if (screenType.startsWith("seat_module_")) {
                sendSeatModuleSnapshot(api, definitions, states, player, realmId, session,
                        moduleFromScreen(screenType), message);
            } else {
                sendSeatSnapshot(api, definitions, states, player, realmId, session, message);
            }
        } else {
            sendCivicSnapshot(api, definitions, states, player, realmId, session, message);
        }
    }

    /** Applies the fixed-size heraldry asset only from a current Seat of Rule session. */
    public static void handleHeraldrySave(
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            GovernmentHeraldrySavePayload payload
    ) {
        if (player == null || payload == null) return;
        GovernmentUiSessionService.Session session = validateSession(player, payload.sessionId(), payload.realmId(), "seat_of_rule");
        if (session == null) {
            player.sendMessage(Text.literal("Open the Seat of Rule again before saving heraldry."), false);
            return;
        }
        try {
            states.setHeraldry(player, payload.realmId(), payload.pixels());
            sendSeatSnapshot(ElarionApi.get(), definitions, states, player, payload.realmId(), session,
                    "Realm heraldry saved.");
        } catch (IllegalArgumentException exception) {
            player.sendMessage(Text.literal(exception.getMessage()), false);
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
            player.sendMessage(Text.literal("Only Embers of this Realm may use its Civic Forum."), false);
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
            throw new IllegalArgumentException("Only Embers of this Realm may open its Civic Forum.");
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
            player.sendMessage(Text.literal("Only Embers of this Realm may use its Seat of Rule."), false);
            return;
        }
        if (!states.canOpenSeatOfRule(player, realm.get().id())) {
            player.sendMessage(Text.literal("Only the Realm ruler may open the Seat of Rule.")
                    .formatted(net.minecraft.util.Formatting.RED), false);
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
        var heraldry = states.heraldry(realm.id());
        ServerPlayNetworking.send(player, new panetina.elarion.addons.government.network.GovernmentHeraldrySnapshotPayload(
                realm.id(), heraldry.revision(), heraldry.paletteIndices()));
        GovernmentGateStatus gates = states.gates(realm.id());
        GovernmentCivicScreen screen = states.currentCivicScreen(realm.id());
        boolean eligible = states.eligibleCitizen(player, realm.id());
        String lockMessage = gates.screenLockMessage(screen);
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
                String phase = locked ? lockMessage
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
                rows.add(row("government_form", "Government Form",
                        "The first vote starts a 24h window. Plurality wins; ties trigger a runoff.",
                        locked ? gates.governmentVoteLockMessage() : eligible ? "Ready" : "Active Embers only",
                        !locked && eligible, false));
                boolean formRowsUnlocked = !locked && eligible;
                String activeFormId = state.activeGovernmentFormId();
                forms = governmentFormRows(definitions, vote, player.getUuid(), formRowsUnlocked, activeFormId);
            }
            case REALM_COLOR -> {
                title = "Realm Color";
                subtitle = "Choose the public color for " + api.realms().officialName(realm) + ".";
                Optional<GovernmentVoteState> vote = states.existingVote(realm.id(), GovernmentVoteType.REALM_COLOR);
                voted = hasVoted(vote, player.getUuid());
                voteEndsAt = vote.map(candidate -> candidate.endsAt).orElse(0L);
                boolean colorRowsUnlocked = eligible;
                rows.add(row("realm_color", "Realm Color",
                        "The first vote starts a 24h window. The winning vanilla color is applied after resolution.",
                        eligible ? "Ready" : "Active Embers only",
                        eligible, false));
                forms = colorRows(states, vote, player.getUuid(), colorRowsUnlocked, state.votedColor());
            }
            case FOUNDING_ELECTION -> {
                GovernmentFoundingPhase phase = states.foundingPhase(player, realm.id());
                title = phase.title().isBlank() ? foundingElectionStageTitle(state) : phase.title();
                locked = !gates.foundingElectionUnlocked();
                Optional<GovernmentVoteState> vote = states.existingVote(realm.id(), GovernmentVoteType.FOUNDING_ELECTION);
                long now = System.currentTimeMillis();
                boolean nominationEnded = phase.votingOpen();
                primaryAction = phase.canNominate() ? "nominate_self" : "";
                voted = hasVoted(vote, player.getUuid());
                voteEndsAt = vote.map(candidate -> nominationEnded ? candidate.endsAt : candidate.proposalEndsAt)
                        .orElse(0L);
                String status = locked ? gates.foundingElectionLockMessage()
                        : phase.votingOpen() && vote.map(candidate -> candidate.active(now)).orElse(false) ? phase.phaseLabel()
                        : phase.nominationsOpen() ? phase.phaseLabel()
                        : phase.nominationReason();
                subtitle = status + " - " + foundingElectionSubtitle(definitions, state);
                rows.add(expandableRow("leadership_election", title,
                        foundingElectionRules(state),
                        locked ? gates.foundingElectionLockMessage()
                                : phase.canNominate() ? "Nominate"
                                : phase.votingOpen() ? "Vote"
                                : phase.nominationReason(),
                        !locked && eligible && (phase.canNominate() || phase.votingOpen()), false));
                offices = voteRows(vote, player.getUuid(), !locked && eligible && nominationEnded,
                        "No candidates have nominated yet.");
            }
            case CITIZEN_FEATURES -> {
                title = "Current Votes";
                subtitle = "Active Ember votes and recent results for " + api.realms().officialName(realm) + ".";
                rows.addAll(currentVoteRows(api, states, player.getUuid(), realm.id()));
                modules = citizenModules();
            }
            default -> throw new IllegalStateException("Unhandled Civic screen " + screen);
        }

        send(player, new GovernmentUiOpenPayload(
                screenId(screen), title, subtitle, realm.id(), api.realms().officialName(realm), "default",
                LOGICAL_WIDTH, LOGICAL_HEIGHT, MINIMUM_SCALE_PERCENT,
                locked, eligible, voted, voteEndsAt,
                fallbackMessage(message, locked, eligible, lockMessage), primaryAction,
                session.id(), screenId(screen), "", screenId(screen), title, true, false,
                "civic_forum", "current_votes", formLabel(definitions, state),
                authorityLabel(api, definitions, state), "Ember Assembly",
                selectedColor(realm, state), "civic_crest", "",
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
        String lockMessage = gates.seatOfRuleLockMessage();
        List<GovernmentUiOpenPayload.Row> status = new ArrayList<>();
        List<GovernmentUiOpenPayload.Row> formRows = new ArrayList<>();
        List<GovernmentUiOpenPayload.Row> officeRows = new ArrayList<>();
        String primaryAction = "";

        if (locked) {
            status.add(row("seat_locked", "Seat Locked",
                    lockMessage,
                    "Locked", false, false));
        } else if (state.activeGovernmentFormId().isBlank()) {
            status.add(row("missing_form", "No Government Chosen",
                    "The Civic Forum must finish the government form vote first.", "Waiting", false, false));
        } else {
            GovernmentFormDefinition form = definitions.require(state.activeGovernmentFormId());
            formRows.add(row(form.id(), form.displayName(), form.description(), "Current", true, true));
            if (state.officeHolders().isEmpty()) {
                officeRows.add(row("vacant", "Offices", "No offices are currently filled.", "Vacant", true, false));
            } else {
                officeRows.addAll(officeRows(api, states, form, state));
            }
            if ("monarchy".equals(form.id())) {
                boolean canReviewProposals = states.canReviewProposals(player, realm.id());
                status.addAll(proposalRows(api, states.proposals(realm.id()), canReviewProposals, player.getUuid(),
                        states, player, realm.id()));
            } else {
                status.addAll(currentVoteRows(api, states, player.getUuid(), realm.id()));
            }
            primaryAction = "";
        }

        send(player, new GovernmentUiOpenPayload(
                "seat_of_rule", "Seat of Rule",
                locked ? "Authority seat is locked." : "Authority summary for " + api.realms().officialName(realm),
                realm.id(), api.realms().officialName(realm), "default",
                LOGICAL_WIDTH, LOGICAL_HEIGHT, MINIMUM_SCALE_PERCENT,
                locked, states.eligibleCitizen(player, realm.id()), false, 0L,
                fallbackMessage(message, locked, true, lockMessage), primaryAction, session.id(),
                "seat_of_rule", "", "seat_of_rule", "Seat of Rule", true, false,
                "seat_of_rule", "review", formLabel(definitions, state),
                authorityLabel(api, definitions, state), "Authority Seat",
                selectedColor(realm, state), "seat_crest", "",
                status, formRows, officeRows, seatModules()));
    }

    private static void sendCivicModuleSnapshot(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            String realmId,
            GovernmentUiSessionService.Session session,
            String moduleId,
            String message
    ) {
        RealmDefinition realm = api.realms().find(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Realm " + realmId));
        if (states.currentCivicScreen(realm.id()) != GovernmentCivicScreen.CITIZEN_FEATURES) {
            sendCivicSnapshot(api, definitions, states, player, realm.id(), session,
                    foundingRequirementMessage(states, definitions, player, realm.id()));
            return;
        }
        String module = normalizeCivicModule(moduleId);
        List<GovernmentUiOpenPayload.Row> rows;
        String title;
        String subtitle;
        String primaryAction = "";
        if ("laws".equals(module)) {
            title = "Active Laws";
            subtitle = "Read the current civic records for " + api.realms().officialName(realm) + ".";
            rows = recordRows(api, states.laws(realm.id()), false, true, "laws");
        } else if ("projects".equals(module)) {
            title = "Projects";
            subtitle = "Read approved Realm project records.";
            rows = recordRows(api, states.laws(realm.id()), false, true, "realm_project");
        } else if ("offices".equals(module)) {
            title = "Offices";
            subtitle = "Current authority holders for " + api.realms().officialName(realm) + ".";
            RealmGovernmentState government = states.realm(realm.id());
            if (government.activeGovernmentFormId().isBlank()) {
                rows = List.of(row("empty", "No Offices", "The Realm has not chosen a Government form yet.", "Waiting", false, false));
            } else {
                GovernmentFormDefinition form = definitions.require(government.activeGovernmentFormId());
                rows = officeRows(api, states, form, government);
            }
        } else if ("history".equals(module)) {
            title = "Civic History";
            subtitle = "Government chronicle entries for " + api.realms().officialName(realm) + ".";
            rows = historyRows(api, states.governmentHistory(realm.id(), 40), states.laws(realm.id()), false);
        } else {
            title = "Audience Requests";
            subtitle = "Request an audience with the Monarch of " + api.realms().officialName(realm) + ".";
            rows = proposalRows(api, states.proposals(realm.id()), false, player.getUuid(), states, player, realm.id());
            primaryAction = "monarchy".equals(states.realm(realm.id()).activeGovernmentFormId())
                    && states.eligibleCitizen(player, realm.id()) ? "create_proposal" : "";
        }
        send(player, new GovernmentUiOpenPayload(
                "civic_module_" + module, title, subtitle, realm.id(), api.realms().officialName(realm), "default",
                LOGICAL_WIDTH, LOGICAL_HEIGHT, MINIMUM_SCALE_PERCENT,
                false, states.eligibleCitizen(player, realm.id()), false, 0L,
                fallbackMessage(message, false, states.eligibleCitizen(player, realm.id()), ""), primaryAction,
                session.id(), "civic_module_" + module, "civic_features", "civic_features", title, false, true,
                "civic_forum", module, formLabel(definitions, states.realm(realm.id())),
                authorityLabel(api, definitions, states.realm(realm.id())), "Ember Assembly",
                selectedColor(realm, states.realm(realm.id())), "civic_crest", "",
                rows, List.of(), "offices".equals(module) ? rows : List.of(), citizenModules()));
    }

    private static void sendSeatModuleSnapshot(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            String realmId,
            GovernmentUiSessionService.Session session,
            String moduleId,
            String message
    ) {
        RealmDefinition realm = api.realms().find(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Realm " + realmId));
        GovernmentFormDefinition form = definitions.require(states.realm(realm.id()).activeGovernmentFormId());
        String module = normalizeSeatModule(moduleId);
        List<GovernmentUiOpenPayload.Row> rows;
        String title;
        String subtitle;
        String primaryAction = "";
        boolean directRecords = states.canDirectCreateRecords(player, realm.id());
        boolean republicLawVotes = states.canCreateRepublicLawVotes(player, realm.id());
        boolean canReviewProposals = states.canReviewProposals(player, realm.id());
        if ("laws".equals(module)) {
            title = "Laws";
            subtitle = "Create, vote, read, and archive active laws.";
            rows = recordRows(api, states.laws(realm.id()), true, true, "laws");
            primaryAction = directRecords ? "add_law_record" : republicLawVotes ? "add_law_vote" : "";
        } else if ("projects".equals(module)) {
            title = "Projects";
            subtitle = "Approved Realm project records.";
            rows = recordRows(api, states.laws(realm.id()), true, true, "realm_project");
            primaryAction = directRecords ? "add_project_record" : "";
        } else if ("archive".equals(module)) {
            title = "Archive";
            subtitle = "Archived records and Government chronicle entries.";
            rows = historyRows(api, states.governmentHistory(realm.id(), 60), states.laws(realm.id()), true);
        } else if ("offices".equals(module)) {
            title = "Offices";
            subtitle = "Authority holders, tenure, and office tools.";
            rows = officeRows(api, states, form, states.realm(realm.id()));
        } else if ("taxes".equals(module)) {
            title = "Realm Taxes";
            subtitle = "Economy-owned service tax policy for " + api.realms().officialName(realm) + ".";
            boolean canEdit = states.canDirectCreateRecords(player, realm.id());
            var policy = ElarionEconomyApi.get().taxPolicy(EconomyTaxAuthority.realm(realm.id(), ""));
            ServerPlayNetworking.send(player, new GovernmentTaxPolicySnapshotPayload(
                    realm.id(),
                    policy.revision(),
                    api.realms().officialName(realm) + " Realm treasury",
                    java.util.Arrays.stream(EconomyTaxCategory.values())
                            .map(category -> new GovernmentTaxPolicySnapshotPayload.Entry(
                                    category.id(), taxLabel(category), policy.rates().getOrDefault(category, 0)))
                            .toList()));
            rows = java.util.Arrays.stream(EconomyTaxCategory.values()).<GovernmentUiOpenPayload.Row>map(category -> row(category.id(),
                    taxLabel(category), "Current rate: " + percent(policy.rates().getOrDefault(category, 0))
                            + ". This applies only to this Realm.",
                    policy.rates().getOrDefault(category, 0) + " bp / revision " + policy.revision(), canEdit, false)).toList();
            primaryAction = canEdit ? "set_tax_rate" : "";
        } else {
            title = "Audience";
            subtitle = "Approve or reject audience requests.";
            rows = proposalRows(api, states.proposals(realm.id()), canReviewProposals, player.getUuid(),
                    states, player, realm.id());
            primaryAction = "";
        }
        send(player, new GovernmentUiOpenPayload(
                "seat_module_" + module, title, subtitle, realm.id(), api.realms().officialName(realm), "default",
                LOGICAL_WIDTH, LOGICAL_HEIGHT, MINIMUM_SCALE_PERCENT,
                false, states.eligibleCitizen(player, realm.id()), false, 0L,
                fallbackMessage(message, false, true, ""), primaryAction, session.id(),
                "seat_module_" + module, "seat_of_rule", "seat_of_rule", title, false, true,
                "seat_of_rule", module, formLabel(definitions, states.realm(realm.id())),
                authorityLabel(api, definitions, states.realm(realm.id())), "Authority Seat",
                selectedColor(realm, states.realm(realm.id())), "seat_crest", "",
                rows, List.of(), "offices".equals(module) ? rows : List.of(), List.of()));
    }

    private static List<GovernmentUiOpenPayload.Row> currentVoteRows(
            ElarionApi api,
            GovernmentStateService states,
            UUID viewer,
            String realmId
    ) {
        long now = System.currentTimeMillis();
        long recentCutoff = now - 24L * 60L * 60L * 1000L;
        List<GovernmentUiOpenPayload.Row> rows = new ArrayList<>();
        List<GovernmentProposalRecord> proposals = states.proposals(realmId);
        proposals.stream()
                .filter(proposal -> proposal.status() == GovernmentProposalStatus.CITIZEN_RATIFICATION)
                .forEach(proposal -> rows.add(proposalVoteRow(api, states, proposal, viewer, "active_vote")));
        proposals.stream()
                .filter(proposal -> proposal.resolvedAt() >= recentCutoff)
                .filter(proposal -> proposal.status() == GovernmentProposalStatus.ENACTED
                        || proposal.status() == GovernmentProposalStatus.REJECTED)
                .forEach(proposal -> rows.add(proposalVoteRow(api, states, proposal, viewer, "recent_vote")));
        if (rows.isEmpty()) {
            return List.of(row("empty", "No Active Votes",
                    "No active Ember votes are open. Recent Government outcomes appear here for 24 hours.",
                    "Empty", false, false));
        }
        return rows;
    }

    private static GovernmentUiOpenPayload.Row proposalVoteRow(
            ElarionApi api,
            GovernmentStateService states,
            GovernmentProposalRecord proposal,
            UUID viewer,
            String kind
    ) {
        boolean ratification = proposal.status() == GovernmentProposalStatus.CITIZEN_RATIFICATION;
        long approvals = proposal.citizenVotes().values().stream().filter(Boolean::booleanValue).count();
        long rejections = proposal.citizenVotes().values().stream().filter(value -> !value).count();
        long threshold = ratification ? Math.max(1, states.activeCitizenThreshold(proposal.realmId())) : 0L;
        String title = !proposal.finalTitle().isBlank() ? proposal.finalTitle() : proposal.title();
        String body = uiTextForDisplay(api, !proposal.finalBody().isBlank() ? proposal.finalBody() : proposal.body());
        String actor = citizenName(api, proposal.authorId());
        return new GovernmentUiOpenPayload.Row(
                proposal.id(),
                uiTextForDisplay(api, title),
                categoryLabel(proposal.category()) + " - " + body,
                ratification ? (proposal.citizenVotes().containsKey(viewer) ? "Voted" : "Active")
                        : statusLabel(proposal.status()),
                ratification,
                proposal.status() == GovernmentProposalStatus.ENACTED,
                proposal.citizenVotes().containsKey(viewer),
                approvals + rejections,
                kind,
                iconForCategory(proposal.category()),
                categoryLabel(proposal.category()),
                actor,
                ratification ? "Ember vote" : statusLabel(proposal.status()),
                approvals,
                rejections,
                threshold,
                ratification ? proposal.resolvedAt() : proposal.resolvedAt());
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
        GovernmentVoteState state = vote.get();
        java.util.Map<String, Long> counts = voteCounts(state);
        return state.options.values().stream()
                .sorted(voteOptionComparator(state, counts))
                .map(option -> {
                    long count = counts.getOrDefault(option.id, 0L);
                    boolean ownCandidate = viewer != null && viewer.equals(option.proposedBy);
                    String stateLabel = unlocked ? voteCountLabel(count)
                            : ownCandidate ? "You are nominated" : "Candidate";
                    return choiceRow(option.id, option.title, option.body,
                            stateLabel,
                            unlocked, state.winnerIds.contains(option.id),
                            selected(vote, viewer, option.id) || ownCandidate && !unlocked, count);
                })
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
                .sorted(voteOptionComparator(vote.get(), counts))
                .map(option -> {
                    boolean selected = selected(vote, viewer, option.id);
                    long count = counts.getOrDefault(option.id, 0L);
                    return choiceRow(option.id,
                            "Name: " + option.title,
                            "Tag: [" + option.tag + "]",
                            votingUnlocked ? voteCountLabel(count) : "Proposed",
                            votingUnlocked, vote.get().winnerIds.contains(option.id),
                            selected, count);
                })
                .toList();
    }

    private static List<GovernmentUiOpenPayload.Row> governmentFormRows(
            GovernmentDefinitionService definitions,
            Optional<GovernmentVoteState> vote,
            UUID viewer,
            boolean votingUnlocked,
            String activeFormId
    ) {
        if (vote.isPresent() && !vote.get().options.isEmpty()) {
            GovernmentVoteState state = vote.get();
            java.util.Map<String, Long> counts = voteCounts(state);
            return state.options.values().stream()
                    .sorted(voteOptionComparator(state, counts))
                    .map(option -> {
                        long count = counts.getOrDefault(option.id, 0L);
                        String formId = option.formId.isBlank() ? option.id : option.formId;
                        return choiceRow(option.id, option.title, option.body,
                                voteCountLabel(count), votingUnlocked, formId.equals(activeFormId),
                                selected(vote, viewer, option.id), count);
                    })
                    .toList();
        }
        return definitions.forms().stream()
                .filter(GovernmentFormDefinition::enabled)
                .map(form -> choiceRow(form.id(), form.displayName(), form.description(),
                        "Vote", votingUnlocked, form.id().equals(activeFormId), false, 0L))
                .toList();
    }

    private static List<GovernmentUiOpenPayload.Row> colorRows(
            GovernmentStateService states,
            Optional<GovernmentVoteState> vote,
            UUID viewer,
            boolean votingUnlocked,
            String currentColor
    ) {
        if (vote.isPresent() && !vote.get().options.isEmpty()) {
            GovernmentVoteState state = vote.get();
            java.util.Map<String, Long> counts = voteCounts(state);
            return state.options.values().stream()
                    .sorted(voteOptionComparator(state, counts))
                    .map(option -> {
                        boolean selected = selected(vote, viewer, option.id);
                        boolean current = option.id.equals(currentColor);
                        long count = counts.getOrDefault(option.id, 0L);
                        return choiceRow(option.id, option.title, option.body,
                                voteCountLabel(count), votingUnlocked, current,
                                selected, count);
                    })
                    .toList();
        }
        return states.realmColorOptions().stream()
                .map(color -> choiceRow(color, colorLabel(color),
                        "Apply " + colorLabel(color) + " to Realm text and markers.",
                        "Vote", votingUnlocked, color.equals(currentColor), false, 0L))
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

    private static Comparator<GovernmentVoteOption> voteOptionComparator(
            GovernmentVoteState vote,
            java.util.Map<String, Long> counts
    ) {
        return Comparator
                .<GovernmentVoteOption>comparingLong(option -> counts.getOrDefault(option.id, 0L))
                .reversed()
                .thenComparingLong(option -> option.createdAt)
                .thenComparing(option -> option.id);
    }

    private static String voteCountLabel(long count) {
        return count == 1L ? "1 vote" : count + " votes";
    }

    private static String colorLabel(String color) {
        String normalized = color == null ? "" : color.trim().toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
        String[] parts = normalized.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.isEmpty() ? "Color" : builder.toString();
    }

    private static List<GovernmentUiOpenPayload.Row> citizenModules() {
        return List.of(
                navigationRow("audience", "Audience", "Request an audience with the Monarch.", "Open", true, false),
                navigationRow("laws", "Laws", "Read active law and civic records.", "Open", true, false),
                navigationRow("projects", "Projects", "Read approved project records.", "Open", true, false),
                navigationRow("offices", "Offices", "View current authority holders.", "Open", true, false),
                navigationRow("history", "History", "Read archived civic records.", "Open", true, false)
        );
    }

    static List<GovernmentUiOpenPayload.Row> seatModuleRows(GovernmentFormDefinition form) {
        List<GovernmentUiOpenPayload.Row> rows = new ArrayList<>();
        if ("monarchy".equals(form.id())) {
            rows.add(navigationRow("review", "Audience", "Review pending audience requests.", "Open", true, false));
        }
        rows.add(navigationRow("laws", "Laws", "View and archive active laws.", "Open", true, false));
        rows.add(navigationRow("projects", "Projects", "View approved project records.", "Open", true, false));
        rows.add(navigationRow("offices", "Offices", "View and manage authority holders.", "Open", true, false));
        rows.add(navigationRow("archive", "Archive", "Restore archived civic records.", "Open", true, false));
        return rows;
    }

    private static List<GovernmentUiOpenPayload.Row> officeRows(
            ElarionApi api,
            GovernmentStateService states,
            GovernmentFormDefinition form,
            RealmGovernmentState government
    ) {
        if (form == null || form.offices().isEmpty()) {
            return List.of(row("empty", "No Offices", "This Government form has no configured offices.", "Empty", false, false));
        }
        List<GovernmentUiOpenPayload.Row> rows = new ArrayList<>();
        for (var office : form.offices()) {
            Set<UUID> holders = government.officeHolders().getOrDefault(office.id(), Set.of());
            String names = holderNames(api, holders);
            boolean filled = !holders.isEmpty();
            List<GovernmentOfficeTermRecord> terms = states.activeOfficeTerms(government.realmId(), office.id());
            long chosenAt = terms.stream().mapToLong(GovernmentOfficeTermRecord::chosenAt).filter(value -> value > 0L)
                    .min().orElse(government.foundingElectionCompletedAt());
            long approved = terms.stream().mapToLong(GovernmentOfficeTermRecord::approvedCount).sum();
            long rejected = terms.stream().mapToLong(GovernmentOfficeTermRecord::rejectedCount).sum();
            String body = office.description();
            rows.add(new GovernmentUiOpenPayload.Row(
                    office.id(),
                    office.displayName().isBlank() ? office.id() : office.displayName(),
                    body,
                    filled ? "Filled" : "Vacant",
                    true,
                    filled,
                    false,
                    holders.size(),
                    "office",
                    "office",
                    "Office",
                    names,
                    filled ? "Elected " + timeLabel(chosenAt) : "Vacant",
                    approved,
                    rejected,
                    office.maxHolders(),
                    chosenAt));
        }
        return rows;
    }

    private static List<GovernmentUiOpenPayload.Row> proposalRows(
            ElarionApi api,
            List<GovernmentProposalRecord> proposals,
            boolean authorityView,
            UUID viewer,
            GovernmentStateService states,
            ServerPlayerEntity player,
            String realmId
    ) {
        if (proposals.isEmpty()) {
            return List.of(row("empty", "No Audience Requests", "No audience requests are recorded yet.", "Waiting", false, false));
        }
        List<GovernmentProposalRecord> visible = proposals.stream()
                .filter(GovernmentBlockInteractions::visibleProposal)
                .toList();
        if (visible.isEmpty()) {
            return List.of(row("empty", "No Active Audience Requests",
                    "Resolved requests disappear from this active list.", "Empty", false, false));
        }
        return visible.stream()
                .map(proposal -> {
                    boolean canAct = authorityView && states.canActOnProposal(player, realmId, proposal);
                    String title = proposal.status() == GovernmentProposalStatus.CITIZEN_RATIFICATION
                            && !proposal.finalTitle().isBlank()
                            ? proposal.finalTitle()
                            : proposal.title();
                    String body = proposal.status() == GovernmentProposalStatus.CITIZEN_RATIFICATION
                            && !proposal.finalBody().isBlank()
                            ? proposal.finalBody()
                            : proposal.body();
                    String state = proposal.status() == GovernmentProposalStatus.PENDING
                            ? canAct ? "Review" : "Pending"
                            : proposal.status() == GovernmentProposalStatus.FINAL_TEXT_REVIEW
                            ? canAct ? "Review" : "Wording review"
                            : proposal.status() == GovernmentProposalStatus.CITIZEN_RATIFICATION
                            ? authorityView ? "Ember Vote"
                            : proposal.citizenVotes().containsKey(viewer) ? "Voted" : "Ratify"
                            : proposal.status() == GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION
                            ? canAct ? "Finalize" : "Approved"
                            : statusLabel(proposal.status());
                    boolean unlocked = canAct
                            || (!authorityView && proposal.status() == GovernmentProposalStatus.CITIZEN_RATIFICATION);
                    long approveCount = authorityView
                            ? proposal.reviewVotes().values().stream().filter(Boolean::booleanValue).count()
                            : proposal.citizenVotes().values().stream().filter(Boolean::booleanValue).count();
                    long rejectCount = authorityView
                            ? proposal.reviewVotes().values().stream().filter(value -> !value).count()
                            : proposal.citizenVotes().values().stream().filter(value -> !value).count();
                    long threshold = proposal.status() == GovernmentProposalStatus.CITIZEN_RATIFICATION
                            ? Math.max(1, states.activeCitizenThreshold(realmId))
                            : 0L;
                    String actor = citizenName(api, proposal.authorId());
                    return actionRow(proposal.id(), uiTextForDisplay(api, title),
                            categoryLabel(proposal.category()) + " - " + uiTextForDisplay(api, body),
                            state,
                            unlocked,
                            proposal.status() == GovernmentProposalStatus.ENACTED,
                            iconForCategory(proposal.category()), categoryLabel(proposal.category()), actor,
                            proposal.status() == GovernmentProposalStatus.CITIZEN_RATIFICATION
                                    ? "Ember vote" : authorityView ? "Audience review" : "Audience",
                            approveCount, rejectCount, threshold, proposal.createdAt());
                })
                .toList();
    }

    private static boolean visibleProposal(GovernmentProposalRecord proposal) {
        return proposal.status() == GovernmentProposalStatus.PENDING
                || proposal.status() == GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION
                || proposal.status() == GovernmentProposalStatus.FINAL_TEXT_REVIEW
                || proposal.status() == GovernmentProposalStatus.CITIZEN_RATIFICATION;
    }

    private static List<GovernmentUiOpenPayload.Row> lawRows(ElarionApi api, List<GovernmentLawRecord> laws, boolean authorityView) {
        return recordRows(api, laws, authorityView, true, "");
    }

    private static List<GovernmentUiOpenPayload.Row> recordRows(
            ElarionApi api,
            List<GovernmentLawRecord> laws,
            boolean authorityView,
            boolean active,
            String category
    ) {
        List<GovernmentLawRecord> records = laws.stream()
                .filter(law -> law.active() == active)
                .filter(law -> recordMatchesCategory(law, category))
                .toList();
        if (records.isEmpty()) {
            return List.of(row("empty", active ? "No Active Records" : "No Archived Records",
                    active ? "No active civic records are recorded yet." : "No archived civic records are recorded yet.",
                    "Empty", false, false));
        }
        return records.stream()
                .map(law -> {
                    String state = authorityView ? active ? "Archive" : "Restore" : active ? "Active" : "Archived";
                    return new GovernmentUiOpenPayload.Row(law.id(), uiTextForDisplay(api, law.title()),
                        categoryLabel(law.category()) + " - " + uiTextForDisplay(api, law.body()),
                        state, true, active, false, 0L,
                        authorityView ? "record_action" : "record",
                        iconForCategory(law.category()), categoryLabel(law.category()),
                        citizenName(api, law.enactedBy()),
                        active ? "Active" : "Archived",
                        0L, 0L, 0L, law.enactedAt());
                })
                .toList();
    }

    private static boolean recordMatchesCategory(GovernmentLawRecord law, String category) {
        if (category == null || category.isBlank()) return true;
        if ("laws".equals(category)) return "law".equals(law.category()) || "civic_rule".equals(law.category());
        return category.equals(law.category()) || "civic_rule".equals(category) && "other".equals(law.category());
    }

    private static List<GovernmentUiOpenPayload.Row> historyRows(
            ElarionApi api,
            List<PublicHistoryEntry> history,
            List<GovernmentLawRecord> laws,
            boolean authorityView
    ) {
        List<GovernmentUiOpenPayload.Row> rows = new ArrayList<>();
        for (PublicHistoryEntry entry : history) {
            if (!GovernmentChronicleText.visibleInArchive(entry)) continue;
            String actorName = historyActorName(api, entry);
            ChronicleProjection projection = api.publicHistory().project(entry, new ChronicleRenderContext(actorName));
            rows.add(new GovernmentUiOpenPayload.Row(
                    entry.eventId().toString(),
                    projection.title(),
                    uiTextForDisplay(api, projection.body()),
                    timeLabel(entry.timestamp()),
                    true,
                    true,
                    false,
                    0L,
                    "history",
                    iconForHistory(entry),
                    projection.category(),
                    actorName,
                    projection.detailLabel(),
                    0L,
                    0L,
                    0L,
                    entry.timestamp()));
        }
        if (authorityView) {
            List<GovernmentUiOpenPayload.Row> archived = recordRows(api, laws, true, false, "");
            if (!archived.isEmpty() && !"empty".equals(archived.getFirst().id())) rows.addAll(archived);
        }
        if (!rows.isEmpty()) return rows;
        return List.of(row("empty", "No Civic History",
                "No civic records have been created yet.", "Empty", false, false));
    }

    private static String formLabel(GovernmentDefinitionService definitions, RealmGovernmentState state) {
        if (state == null || state.activeGovernmentFormId().isBlank()) return "Unchosen";
        return definitions.form(state.activeGovernmentFormId())
                .map(GovernmentFormDefinition::displayName)
                .filter(label -> !label.isBlank())
                .orElse(state.activeGovernmentFormId());
    }

    private static String authorityLabel(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            RealmGovernmentState state
    ) {
        if (state == null || state.activeGovernmentFormId().isBlank()) return "Unchosen";
        GovernmentFormDefinition form = definitions.require(state.activeGovernmentFormId());
        return authorityLabel(form, state, uuid -> api.citizens().find(uuid)
                .map(GovernmentBlockInteractions::citizenName)
                .orElse("Unknown Ember"));
    }

    static String authorityLabel(
            GovernmentFormDefinition form,
            RealmGovernmentState state,
            java.util.function.Function<UUID, String> nameResolver
    ) {
        if (form == null || state == null || state.activeGovernmentFormId().isBlank()) return "Unchosen";
        String officeId = primaryOfficeId(form);
        String officeLabel = officeLabel(form, officeId);
        Set<UUID> holders = state.officeHolders().getOrDefault(officeId, Set.of());
        int maxHolders = Math.max(1, officeMaxHolders(form, officeId));
        if (holders.isEmpty()) return officeLabel + " Vacant";
        List<String> names = holders.stream()
                .map(uuid -> nameResolver.apply(uuid))
                .filter(name -> name != null && !name.isBlank())
                .sorted()
                .toList();
        if (holders.size() == 1 && !names.isEmpty()) return officeLabel + " " + names.getFirst();
        String plural = officeLabel.endsWith("s") ? officeLabel : officeLabel + "s";
        return plural + " " + holders.size() + "/" + maxHolders;
    }

    private static String primaryOfficeId(GovernmentFormDefinition form) {
        return switch (form.id()) {
            case "monarchy" -> "monarch";
            case "republic" -> "president";
            default -> form.authorityOffices().stream()
                    .filter(office -> !"officer".equals(office))
                    .findFirst()
                    .orElseGet(() -> form.offices().isEmpty() ? "office" : form.offices().getFirst().id());
        };
    }

    private static int officeMaxHolders(GovernmentFormDefinition form, String officeId) {
        return form.offices().stream()
                .filter(office -> office.id().equals(officeId))
                .findFirst()
                .map(office -> Math.max(1, office.maxHolders()))
                .orElse(1);
    }

    private static String selectedColor(RealmDefinition realm, RealmGovernmentState state) {
        if (state != null && !state.votedColor().isBlank()) return state.votedColor();
        return realm == null || realm.color() == null || realm.color().isBlank() ? "white" : realm.color();
    }

    private static String statusLabel(GovernmentProposalStatus status) {
        String lower = status.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String categoryLabel(String category) {
        String normalized = category == null ? "" : category.replace('_', ' ');
        if (normalized.isBlank()) return "Proposal";
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static String directRecordCategory(String action) {
        return switch (action) {
            case "add_law_record" -> "law";
            case "add_notice_record" -> "public_notice";
            case "add_rule_record" -> "civic_rule";
            case "add_project_record" -> "realm_project";
            default -> "other";
        };
    }

    private static String moduleFromCategory(String category) {
        return switch (category) {
            case "law", "civic_rule" -> "laws";
            case "public_notice" -> "review";
            case "realm_project" -> "projects";
            default -> "laws";
        };
    }

    private static String moduleFromScreen(String screenType) {
        if (screenType == null) return "laws";
        if (screenType.startsWith("seat_module_")) return normalizeSeatModule(screenType.substring("seat_module_".length()));
        if (screenType.startsWith("civic_module_")) return normalizeCivicModule(screenType.substring("civic_module_".length()));
        return "laws";
    }

    private static String normalizeCivicModule(String moduleId) {
        return switch (moduleId == null ? "" : moduleId) {
            case "laws", "rules", "notices" -> "laws";
            case "audience", "proposals" -> "audience";
            case "projects" -> "projects";
            case "offices" -> "offices";
            case "taxes", "tax" -> "taxes";
            case "history", "archive" -> "history";
            default -> "audience";
        };
    }

    private static List<GovernmentUiOpenPayload.Row> seatModules() {
        return List.of(
                row("laws", "Laws", "Official laws and civic rules.", "Open", true, false),
                row("projects", "Projects", "Approved Realm projects.", "Open", true, false),
                row("offices", "Offices", "Authority holders and appointments.", "Open", true, false),
                row("taxes", "Taxes", "Economy-owned Realm tax policy.", "Open", true, false),
                row("archive", "Archive", "Archived records and history.", "Open", true, false));
    }

    private static String taxLabel(EconomyTaxCategory category) {
        return switch (category) {
            case NPC_TRADE -> "NPC Trade";
            case PORTAL_SERVICE -> "Portal Services";
            case MARKETPLACE -> "Marketplace";
            case GENERAL_SERVICE -> "General Services";
        };
    }

    private static String percent(int basisPoints) {
        return String.format(java.util.Locale.ROOT, "%.2f%%", basisPoints / 100.0D);
    }

    private static String normalizeSeatModule(String moduleId) {
        return switch (moduleId == null ? "" : moduleId) {
            case "laws", "rules" -> "laws";
            case "projects" -> "projects";
            case "offices" -> "offices";
            case "archive", "history" -> "archive";
            case "notices", "audience" -> "review";
            default -> "review";
        };
    }

    private static String fallbackMessage(String message, boolean locked, boolean eligible, String lockMessage) {
        if (message != null && !message.isBlank()) return message;
        if (locked) return lockMessage == null ? "" : lockMessage;
        if (!eligible) return "Only active Embers of this Realm can use this civic action.";
        return "";
    }

    private static String foundingRequirementMessage(
            GovernmentStateService states,
            GovernmentDefinitionService definitions,
            ServerPlayerEntity player,
            String realmId
    ) {
        RealmGovernmentState government = states.realm(realmId);
        if (government.activeGovernmentFormId().isBlank()) {
            return "Finish founding first: choose a Government form before laws, offices, and history open.";
        }
        GovernmentFormDefinition form = definitions.require(government.activeGovernmentFormId());
        GovernmentFoundingPhase phase = states.foundingPhase(player, realmId);
        String reason = phase.nominationReason().isBlank() ? phase.phaseLabel() : phase.nominationReason();
        return "Finish founding first: " + reason;
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
            case "civic_color" -> GovernmentVoteType.REALM_COLOR;
            case "civic_form" -> GovernmentVoteType.GOVERNMENT_FORM;
            case "civic_election" -> GovernmentVoteType.FOUNDING_ELECTION;
            default -> throw new IllegalArgumentException("That screen does not accept votes.");
        };
    }

    private static String screenId(GovernmentCivicScreen screen) {
        return switch (screen) {
            case REALM_NAME -> "civic_name";
            case REALM_COLOR -> "civic_color";
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
        String expectedBlock = expectedBlockForScreen(screenType);
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

    static String expectedBlockForScreen(String screenType) {
        String safeScreen = screenType == null ? "" : screenType;
        return "seat_of_rule".equals(safeScreen) || safeScreen.startsWith("seat_module_")
                ? "seat_of_rule" : "civic_forum";
    }

    static boolean isCivicScreen(String screenType) {
        return "civic_forum".equals(expectedBlockForScreen(screenType));
    }

    private static String formName(GovernmentDefinitionService definitions, RealmGovernmentState state) {
        if (state.activeGovernmentFormId().isBlank()) return "the selected Government form";
        return definitions.require(state.activeGovernmentFormId()).displayName();
    }

    private static String foundingElectionSubtitle(
            GovernmentDefinitionService definitions,
            RealmGovernmentState state
    ) {
        if ("republic".equals(state.activeGovernmentFormId())) {
            return "Elect one President. The President drafts laws; Embers ratify them with Yes or No votes.";
        }
        return "Choose the first authority holders for " + formName(definitions, state) + ".";
    }

    private static String foundingElectionStageTitle(RealmGovernmentState state) {
        return switch (state.activeGovernmentFormId()) {
            case "republic" -> "President Election";
            case "monarchy" -> "Monarch Election";
            default -> "Leadership Election";
        };
    }

    private static String foundingElectionRules(RealmGovernmentState state) {
        return switch (state.activeGovernmentFormId()) {
            case "republic" -> "President nominations run first. Voting opens after nominations close. Each Ember may approve one President candidate.";
            default -> "Nominations run first. Voting opens after nominations close. Multi-seat offices accept multiple approvals.";
        };
    }

    private static GovernmentUiOpenPayload.Row row(
            String id,
            String title,
            String body,
            String state,
            boolean unlocked,
            boolean complete
    ) {
        return new GovernmentUiOpenPayload.Row(id, title, body, state, unlocked, complete, "static");
    }

    private static GovernmentUiOpenPayload.Row choiceRow(
            String id,
            String title,
            String body,
            String state,
            boolean unlocked,
            boolean complete
    ) {
        return new GovernmentUiOpenPayload.Row(id, title, body, state, unlocked, complete, "choice");
    }

    private static GovernmentUiOpenPayload.Row choiceRow(
            String id,
            String title,
            String body,
            String state,
            boolean unlocked,
            boolean complete,
            boolean selectedByViewer,
            long voteCount
    ) {
        return new GovernmentUiOpenPayload.Row(id, title, body, state, unlocked, complete,
                selectedByViewer, voteCount, "choice");
    }

    private static GovernmentUiOpenPayload.Row navigationRow(
            String id,
            String title,
            String body,
            String state,
            boolean unlocked,
            boolean complete
    ) {
        return new GovernmentUiOpenPayload.Row(id, title, body, state, unlocked, complete, "navigation");
    }

    private static GovernmentUiOpenPayload.Row expandableRow(
            String id,
            String title,
            String body,
            String state,
            boolean unlocked,
            boolean complete
    ) {
        return new GovernmentUiOpenPayload.Row(id, title, body, state, unlocked, complete, "expandable");
    }

    private static GovernmentUiOpenPayload.Row actionRow(
            String id,
            String title,
            String body,
            String state,
            boolean unlocked,
            boolean complete
    ) {
        return new GovernmentUiOpenPayload.Row(id, title, body, state, unlocked, complete, "action_detail");
    }

    private static GovernmentUiOpenPayload.Row actionRow(
            String id,
            String title,
            String body,
            String state,
            boolean unlocked,
            boolean complete,
            String iconId,
            String category,
            String actorName,
            String metricLabel,
            long approveCount,
            long rejectCount,
            long threshold,
            long createdAt
    ) {
        return new GovernmentUiOpenPayload.Row(id, title, body, state, unlocked, complete,
                false, approveCount + rejectCount, "action_detail", iconId, category, actorName, metricLabel,
                approveCount, rejectCount, threshold, createdAt);
    }

    private static String iconForCategory(String category) {
        return switch (category == null ? "" : category) {
            case "law" -> "law";
            case "public_notice" -> "notice";
            case "civic_rule" -> "office";
            case "realm_project" -> "published_record";
            default -> "proposal";
        };
    }

    private static String iconForHistory(PublicHistoryEntry entry) {
        String type = entry == null ? "" : entry.type();
        if (type.contains("office") || type.contains("election")) return "office";
        if (type.contains("record") || type.contains("law")) return "law";
        if (type.contains("color")) return "realm_color";
        if (type.contains("name")) return "realm_name";
        if (type.contains("notice")) return "notice";
        if (type.contains("proposal")) return "proposal";
        return "history";
    }

    private static String historyTitle(PublicHistoryEntry entry) {
        return GovernmentChronicleText.historyTitle(entry == null ? "" : entry.type());
    }

    private static String historyActorName(ElarionApi api, PublicHistoryEntry entry) {
        if (entry == null || entry.actorId() == null) return "Realm Government";
        return api.citizens().find(entry.actorId())
                .map(GovernmentBlockInteractions::citizenName)
                .orElse("Former Ember");
    }

    private static String historyDetailLabel(PublicHistoryEntry entry) {
        return GovernmentChronicleText.project(entry, "").detailLabel();
    }

    private static String timeLabel(long timestamp) {
        if (timestamp <= 0L) return "Unknown";
        long seconds = Math.max(0L, (System.currentTimeMillis() - timestamp) / 1000L);
        long days = seconds / 86_400L;
        if (days > 0L) return days + "d ago";
        long hours = seconds / 3_600L;
        if (hours > 0L) return hours + "h ago";
        long minutes = seconds / 60L;
        return minutes <= 0L ? "Now" : minutes + "m ago";
    }

    private static String officeLabel(GovernmentFormDefinition form, String officeId) {
        return form.offices().stream()
                .filter(office -> office.id().equals(officeId))
                .findFirst()
                .map(office -> office.displayName().isBlank() ? office.id() : office.displayName())
                .orElse(officeId);
    }

    private static String holderNames(ElarionApi api, Set<UUID> holders) {
        if (holders == null || holders.isEmpty()) return "No Embers assigned.";
        return holders.stream()
                .map(uuid -> citizenName(api, uuid))
                .sorted()
                .toList()
                .stream()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String citizenName(ElarionApi api, UUID uuid) {
        if (api == null || uuid == null) return "";
        return api.citizens().find(uuid)
                .map(GovernmentBlockInteractions::citizenName)
                .filter(name -> !name.isBlank())
                .orElse("Unknown Ember");
    }

    private static String citizenName(CitizenRecord citizen) {
        if (citizen.nickname() != null && !citizen.nickname().isBlank()) return citizen.nickname();
        if (citizen.lastKnownUsername() != null && !citizen.lastKnownUsername().isBlank()) {
            return citizen.lastKnownUsername();
        }
        return "Unknown Ember";
    }

    static String uiTextForDisplay(ElarionApi api, String text) {
        if (text == null || text.isBlank()) return "";
        Matcher matcher = UUID_TEXT_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String replacement = "Unknown Ember";
            try {
                UUID uuid = UUID.fromString(matcher.group());
                String name = citizenName(api, uuid);
                if (!name.isBlank()) replacement = name;
            } catch (IllegalArgumentException ignored) {
                // Keep the UI player-facing even if malformed persisted text looks UUID-like.
            }
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static void send(ServerPlayerEntity player, GovernmentUiOpenPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

}
