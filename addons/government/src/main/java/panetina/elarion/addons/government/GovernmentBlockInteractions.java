package panetina.elarion.addons.government;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentGateStatus;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.addons.government.service.GovernmentDefinitionService;
import panetina.elarion.addons.government.service.GovernmentStateService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.addons.government.network.GovernmentUiActionPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class GovernmentBlockInteractions {
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
                openCivicForum(api, definitions, states, serverPlayer, world);
                return ActionResult.SUCCESS;
            }
            if (blockState.isOf(GovernmentBlocks.SEAT_OF_RULE)) {
                openSeatOfRule(api, definitions, states, serverPlayer, world);
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
        if (player == null || payload == null) {
            return;
        }

        player.sendMessage(Text.literal("Government UI actions are not implemented yet."), false);
    }

    private static void openCivicForum(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            World world
    ) {
        Optional<RealmDefinition> realm = realmForBlock(api, world);
        if (realm.isEmpty()) {
            player.sendMessage(Text.literal("This Civic Forum must be placed in a Realm-owned world."), false);
            return;
        }
        RealmGovernmentState state = states.realm(realm.get().id());
        GovernmentGateStatus gates = states.gates(realm.get().id());
        List<GovernmentUiOpenPayload.Row> stages = new ArrayList<>();
        stages.add(row("name_vote", "Realm Name",
                "Citizens choose the Realm display name and public tag.",
                status(gates.nameVoteUnlocked(), gates.nameChosen(), "Locked until Foundation I"),
                gates.nameVoteUnlocked(), gates.nameChosen()));
        stages.add(row("government_vote", "Government Form",
                "Citizens choose Monarchy, Republic, Theocracy, or Confederation.",
                status(gates.governmentVoteUnlocked(), gates.governmentChosen(), "Locked until Foundation II"),
                gates.governmentVoteUnlocked(), gates.governmentChosen()));
        stages.add(row("founding_election", "Founding Election",
                "The first authority holders are chosen for the selected form.",
                status(gates.foundingElectionUnlocked(), gates.foundingElectionComplete(),
                        "Locked until Foundation III"),
                gates.foundingElectionUnlocked(), gates.foundingElectionComplete()));
        stages.add(row("seat_of_rule", "Seat of Rule",
                "Authority modules unlock after the first founding election.",
                gates.seatOfRuleUnlocked() ? "Unlocked" : "Locked until founding election completes",
                gates.seatOfRuleUnlocked(), gates.seatOfRuleUnlocked()));

        List<GovernmentUiOpenPayload.Row> forms = new ArrayList<>();
        if (gates.governmentChoicesVisible()) {
            for (GovernmentFormDefinition form : definitions.forms()) {
                if (!form.enabled()) continue;
                forms.add(row(form.id(), form.displayName(), form.description(),
                        form.id().equals(state.activeGovernmentFormId()) ? "Chosen" : "Available",
                        gates.governmentVoteUnlocked(), form.id().equals(state.activeGovernmentFormId())));
            }
        } else {
            forms.add(row("locked", "Government Forms",
                    "The four founding forms become visible after the Realm name is chosen.",
                    "Hidden", false, false));
        }

        ServerPlayNetworking.send(player, new GovernmentUiOpenPayload(
                "civic_forum",
                "Civic Forum",
                "Founding path for " + realmDisplay(realm.get(), state),
                realm.get().id(),
                realmDisplay(realm.get(), state),
                "default",
                840,
                560,
                70,
                false,
                false,
                false,
                0L,
                "",
                "",
                stages,
                forms,
                List.<GovernmentUiOpenPayload.Row>of(),
                List.of(row("future_votes", "Voting Modules",
                        "Name voting, government voting, nominations, and elections are not active yet.",
                        "Future", false, false))));
    }

    private static void openSeatOfRule(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states,
            ServerPlayerEntity player,
            World world
    ) {
        Optional<RealmDefinition> realm = realmForBlock(api, world);
        if (realm.isEmpty()) {
            player.sendMessage(Text.literal("This Seat of Rule must be placed in a Realm-owned world."), false);
            return;
        }
        RealmGovernmentState state = states.realm(realm.get().id());
        GovernmentGateStatus gates = states.gates(realm.get().id());
        List<GovernmentUiOpenPayload.Row> stages = List.of(row("seat_gate", "Seat Lock",
                "The Seat opens after Foundation III and the first founding election.",
                gates.seatOfRuleUnlocked() ? "Unlocked" : "Locked", gates.seatOfRuleUnlocked(),
                gates.seatOfRuleUnlocked()));
        List<GovernmentUiOpenPayload.Row> modules = new ArrayList<>();
        if (!gates.seatOfRuleUnlocked()) {
            modules.add(row("locked", "Authority Locked",
                    "Complete Foundation III and the founding election in the Civic Forum first.",
                    "Locked", false, false));
            sendSeat(player, realm.get(), state, "Seat of Rule", "Authority seat is locked.",
                    stages, List.of(), List.of(), modules);
            return;
        }
        if (state.activeGovernmentFormId().isBlank()) {
            modules.add(row("missing_form", "No Government Chosen",
                    "The Civic Forum must finish the government vote before this Seat can operate.",
                    "Waiting", false, false));
            sendSeat(player, realm.get(), state, "Seat of Rule", "No Government form has been chosen.",
                    stages, List.of(), List.of(), modules);
            return;
        }
        GovernmentFormDefinition form = definitions.require(state.activeGovernmentFormId());
        List<GovernmentUiOpenPayload.Row> forms = List.of(row(form.id(), form.displayName(),
                officialName(form, realm.get(), state), "Active", true, true));
        List<GovernmentUiOpenPayload.Row> offices = new ArrayList<>();
        if (state.officeHolders().isEmpty()) {
            offices.add(row("empty", "Offices", "No offices are currently filled.", "Vacant", true, false));
        } else {
            state.officeHolders().forEach((office, holders) -> offices.add(row(office, officeLabel(form, office),
                    holderNames(api, holders), holders.size() + " holder(s)", true, !holders.isEmpty())));
        }
        modules.add(row("announcements", "Announcements", "Realm notices and public decrees.", "Future", false, false));
        modules.add(row("laws", "Laws", "Law proposals, votes, and active law records.", "Future", false, false));
        modules.add(row("decisions", "Decisions", "Authority decisions, offices, and civic actions.", "Future", false, false));
        sendSeat(player, realm.get(), state, "Seat of Rule",
                "Authority summary for " + officialName(form, realm.get(), state),
                stages, forms, offices, modules);
    }

    private static void sendSeat(
            ServerPlayerEntity player,
            RealmDefinition realm,
            RealmGovernmentState state,
            String title,
            String subtitle,
            List<GovernmentUiOpenPayload.Row> stages,
            List<GovernmentUiOpenPayload.Row> forms,
            List<GovernmentUiOpenPayload.Row> offices,
            List<GovernmentUiOpenPayload.Row> modules
    ) {
        ServerPlayNetworking.send(player, new GovernmentUiOpenPayload(
                "seat_of_rule",
                title,
                subtitle,
                realm.id(),
                realmDisplay(realm, state),
                "default",
                840,
                560,
                70,
                false,
                false,
                false,
                0L,
                "",
                "",
                stages,
                forms,
                offices,
                modules));
    }

    private static Optional<RealmDefinition> realmForBlock(ElarionApi api, World world) {
        String worldId = world.getRegistryKey().getValue().toString();
        return api.realm().realms().ownerForWorld(worldId);
    }

    private static String status(boolean unlocked, boolean complete, String lockedReason) {
        if (complete) return "complete";
        return unlocked ? "unlocked" : lockedReason;
    }

    private static String realmDisplay(RealmDefinition realm, RealmGovernmentState state) {
        return state.votedDisplayName().isBlank() ? realm.displayName() : state.votedDisplayName();
    }

    private static String officialName(
            GovernmentFormDefinition form,
            RealmDefinition realm,
            RealmGovernmentState state
    ) {
        String realmName = realmDisplay(realm, state);
        return form.officialNameTemplate().replace("%realm%", realmName)
                .replace("%REALM%", realmName.toUpperCase(Locale.ROOT))
                .replace("%realm_lower%", realmName.toLowerCase(Locale.ROOT));
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

    private static String holderNames(ElarionApi api, java.util.Set<UUID> holders) {
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
}
