package panetina.elarion.addons.government.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.service.GovernmentDefinitionService;
import panetina.elarion.addons.government.service.GovernmentStateService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.command.CommandOutput;

import java.util.Set;

public final class GovernmentCommands {
    private GovernmentCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> create(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states
    ) {
        return CommandManager.literal("government")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reload").executes(ctx -> run(ctx.getSource(), () -> {
                    definitions.load();
                    CommandOutput.success(ctx.getSource(), "Government definitions reloaded.", false);
                })))
                .then(CommandManager.literal("forms").executes(ctx -> run(ctx.getSource(), () -> {
                    CommandOutput.header(ctx.getSource(), "Government Forms");
                    for (GovernmentFormDefinition form : definitions.forms()) {
                        CommandOutput.bullet(ctx.getSource(), form.id() + " - " + form.displayName()
                                + (form.enabled() ? "" : " [disabled]"));
                    }
                })))
                .then(CommandManager.literal("inspect")
                        .then(CommandManager.argument("form", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        definitions.forms().stream().map(GovernmentFormDefinition::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    GovernmentFormDefinition form = definitions.require(
                                            StringArgumentType.getString(ctx, "form"));
                                    CommandOutput.header(ctx.getSource(), form.displayName());
                                    CommandOutput.kv(ctx.getSource(), "ID", form.id());
                                    CommandOutput.kv(ctx.getSource(), "Enabled", form.enabled());
                                    CommandOutput.kv(ctx.getSource(), "Description", form.description());
                                    CommandOutput.kv(ctx.getSource(), "Official Name", form.officialNameTemplate());
                                    CommandOutput.kv(ctx.getSource(), "Authority Offices",
                                            form.authorityOffices().isEmpty()
                                                    ? "-" : String.join(", ", form.authorityOffices()));
                                    CommandOutput.kv(ctx.getSource(), "Group Delegates",
                                            form.confederationDelegatesRepresentGroups());
                                    CommandOutput.section(ctx.getSource(), "Offices");
                                    form.offices().forEach(office -> CommandOutput.bullet(ctx.getSource(),
                                            office.id() + " max=" + office.maxHolders()));
                                    CommandOutput.section(ctx.getSource(), "Roles");
                                    form.actions().forEach((role, actions) -> CommandOutput.bullet(ctx.getSource(),
                                            role + " -> " + String.join(", ", actions)));
                                }))))
                .then(CommandManager.literal("state")
                        .then(CommandManager.argument("realm", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        api.realms().all().stream().map(realm -> realm.id()), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    var state = states.realm(StringArgumentType.getString(ctx, "realm"));
                                    CommandOutput.header(ctx.getSource(), "Government State " + state.realmId());
                                    CommandOutput.kv(ctx.getSource(), "Form",
                                            state.activeGovernmentFormId().isBlank()
                                                    ? "-" : state.activeGovernmentFormId());
                                    CommandOutput.kv(ctx.getSource(), "Voted name",
                                            state.votedDisplayName().isBlank() ? "-" : state.votedDisplayName());
                                    CommandOutput.kv(ctx.getSource(), "Voted tag",
                                            state.votedTag().isBlank() ? "-" : state.votedTag());
                                    CommandOutput.kv(ctx.getSource(), "Founding election complete",
                                            state.foundingElectionCompletedAt() > 0L);
                                    CommandOutput.kv(ctx.getSource(), "Offices", state.officeHolders().size());
                                    CommandOutput.kv(ctx.getSource(), "Active laws", state.activeLawIds().size());
                                    CommandOutput.kv(ctx.getSource(), "Pending proposals",
                                            state.pendingProposalIds().size());
                                }))))
                .then(CommandManager.literal("gates")
                        .then(CommandManager.argument("realm", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        api.realms().all().stream().map(realm -> realm.id()), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    var gates = states.gates(StringArgumentType.getString(ctx, "realm"));
                                    CommandOutput.header(ctx.getSource(), "Government Gates " + gates.realmId());
                                    CommandOutput.kv(ctx.getSource(), "Foundation I", gates.foundationI());
                                    CommandOutput.kv(ctx.getSource(), "Foundation II", gates.foundationII());
                                    CommandOutput.kv(ctx.getSource(), "Foundation III", gates.foundationIII());
                                    CommandOutput.kv(ctx.getSource(), "Name vote visible", gates.nameVoteVisible());
                                    CommandOutput.kv(ctx.getSource(), "Name vote unlocked", gates.nameVoteUnlocked());
                                    CommandOutput.kv(ctx.getSource(), "Government choices visible", gates.governmentChoicesVisible());
                                    CommandOutput.kv(ctx.getSource(), "Government vote unlocked", gates.governmentVoteUnlocked());
                                    CommandOutput.kv(ctx.getSource(), "Founding election unlocked", gates.foundingElectionUnlocked());
                                    CommandOutput.kv(ctx.getSource(), "Seat of Rule unlocked", gates.seatOfRuleUnlocked());
                                }))))
                .then(CommandManager.literal("set-form")
                        .then(CommandManager.argument("realm", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        api.realms().all().stream().map(realm -> realm.id()), builder))
                                .then(CommandManager.argument("form", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                definitions.forms().stream().map(GovernmentFormDefinition::id),
                                                builder))
                                        .executes(ctx -> run(ctx.getSource(), () -> {
                                            var state = states.setForm(
                                                    StringArgumentType.getString(ctx, "realm"),
                                                    StringArgumentType.getString(ctx, "form"));
                                            CommandOutput.success(ctx.getSource(),
                                                    "Government form set to " + state.activeGovernmentFormId() + ".",
                                                    false);
                                        })))))
                .then(CommandManager.literal("identity")
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("realm", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                api.realms().all().stream().map(realm -> realm.id()), builder))
                                        .then(CommandManager.argument("tag", StringArgumentType.word())
                                                .then(CommandManager.argument("display-name", StringArgumentType.greedyString())
                                                        .executes(ctx -> run(ctx.getSource(), () -> {
                                                            var state = states.setVotedIdentity(
                                                                    StringArgumentType.getString(ctx, "realm"),
                                                                    StringArgumentType.getString(ctx, "display-name"),
                                                                    StringArgumentType.getString(ctx, "tag"));
                                                            CommandOutput.success(ctx.getSource(),
                                                                    "Recorded Realm identity "
                                                                            + state.votedDisplayName()
                                                                            + " [" + state.votedTag() + "].",
                                                                    true);
                                                        })))))))
                .then(CommandManager.literal("founding")
                        .then(CommandManager.literal("complete")
                                .then(CommandManager.argument("realm", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                api.realms().all().stream().map(realm -> realm.id()), builder))
                                        .executes(ctx -> run(ctx.getSource(), () -> {
                                            states.markFoundingElectionComplete(
                                                    StringArgumentType.getString(ctx, "realm"));
                                            CommandOutput.success(ctx.getSource(),
                                                    "Marked founding election complete.", true);
                                        })))))
                .then(CommandManager.literal("authority")
                        .then(CommandManager.literal("cleanup")
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    int removed = states.removeInactiveAuthority(System.currentTimeMillis());
                                    CommandOutput.success(ctx.getSource(),
                                            "Authority inactivity cleanup removed " + removed + " office holder(s).",
                                            true);
                                }))))
                .then(CommandManager.literal("test")
                        .then(CommandManager.literal("advance")
                                .then(CommandManager.argument("realm", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                api.realms().all().stream().map(realm -> realm.id()), builder))
                                        .executes(ctx -> run(ctx.getSource(), () ->
                                                CommandOutput.success(ctx.getSource(),
                                                        states.advanceCurrentWindow(
                                                                StringArgumentType.getString(ctx, "realm")),
                                                        true))))))
                .then(CommandManager.literal("block")
                        .then(CommandManager.literal("remove")
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                    HitResult hit = player.raycast(8.0D, 0.0F, false);
                                    if (!(hit instanceof BlockHitResult blockHit)) {
                                        throw new IllegalArgumentException("Look directly at a Government block.");
                                    }
                                    var blockState = player.getWorld().getBlockState(blockHit.getBlockPos());
                                    if (!blockState.isOf(panetina.elarion.addons.government.GovernmentBlocks.CIVIC_FORUM)
                                            && !blockState.isOf(
                                            panetina.elarion.addons.government.GovernmentBlocks.SEAT_OF_RULE)) {
                                        throw new IllegalArgumentException(
                                                "The targeted block is not a Civic Forum or Seat of Rule.");
                                    }
                                    player.getWorld().setBlockState(
                                            blockHit.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                                    CommandOutput.success(ctx.getSource(), "Government block removed safely.", true);
                                }))))
                .then(officeCommands(api, definitions, states));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> officeCommands(
            ElarionApi api,
            GovernmentDefinitionService definitions,
            GovernmentStateService states
    ) {
        return CommandManager.literal("office")
                .then(CommandManager.literal("assign")
                        .then(CommandManager.argument("realm", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        api.realms().all().stream().map(realm -> realm.id()), builder))
                                .then(CommandManager.argument("office", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestOffices(states, definitions,
                                                StringArgumentType.getString(ctx, "realm"), builder))
                                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                                .executes(ctx -> run(ctx.getSource(), () -> {
                                                    String realm = StringArgumentType.getString(ctx, "realm");
                                                    String office = StringArgumentType.getString(ctx, "office");
                                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                                                    states.assignOffice(realm, office, player.getUuid());
                                                    CommandOutput.success(ctx.getSource(),
                                                            "Assigned " + player.getGameProfile().getName()
                                                                    + " to " + office + ".", true);
                                                }))))))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("realm", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        api.realms().all().stream().map(realm -> realm.id()), builder))
                                .then(CommandManager.argument("office", StringArgumentType.word())
                                        .suggests((ctx, builder) -> suggestOffices(states, definitions,
                                                StringArgumentType.getString(ctx, "realm"), builder))
                                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                                .executes(ctx -> run(ctx.getSource(), () -> {
                                                    String realm = StringArgumentType.getString(ctx, "realm");
                                                    String office = StringArgumentType.getString(ctx, "office");
                                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                                                    states.removeOffice(realm, office, player.getUuid());
                                                    CommandOutput.success(ctx.getSource(),
                                                            "Removed " + player.getGameProfile().getName()
                                                                    + " from " + office + ".", true);
                                                }))))));
    }

    public static void registerLeaderChat(
            com.mojang.brigadier.CommandDispatcher<ServerCommandSource> dispatcher,
            ElarionApi api,
            GovernmentStateService states
    ) {
        api.system().commands().registerHelpDescription("lc",
                "/lc <message> - Send a message to same-Realm Government authority holders.");
        dispatcher.register(CommandManager.literal("lc")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> run(ctx.getSource(), () -> sendAuthorityChat(
                                api, states, ctx.getSource().getPlayerOrThrow(),
                                StringArgumentType.getString(ctx, "message"))))));
    }

    private static void sendAuthorityChat(
            ElarionApi api,
            GovernmentStateService states,
            ServerPlayerEntity sender,
            String message
    ) {
        if (message == null || message.isBlank()) return;
        var citizen = api.citizens().getOrCreate(sender);
        String realm = citizen.realmId();
        if (realm.isBlank()) throw new IllegalArgumentException("You are not assigned to a Realm.");
        if (!states.isAuthority(realm, sender.getUuid())) {
            throw new IllegalArgumentException("You do not hold a Government authority office.");
        }
        Set<java.util.UUID> recipients = states.authorityHolders(realm);
        Text output = Text.literal("[LC] ").formatted(Formatting.GOLD)
                .append(api.identities().resolve(sender).chatName())
                .append(Text.literal(" \u00bb " + message).formatted(Formatting.GRAY));
        for (ServerPlayerEntity recipient : sender.getServer().getPlayerManager().getPlayerList()) {
            if (recipients.contains(recipient.getUuid())) {
                recipient.sendMessage(output, false);
            } else if (api.chat().isChatSpy(recipient)) {
                recipient.sendMessage(Text.literal("[Spy:LC:" + realm + "] ")
                        .append(api.identities().resolve(sender).chatName())
                        .append(Text.literal(": " + message).formatted(Formatting.GRAY)), false);
            }
        }
        api.history().record("chat", "authority-message", sender.getUuid(), "realm", realm,
                realm, java.util.Map.of("channel", "authority"));
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestOffices(
            GovernmentStateService states,
            GovernmentDefinitionService definitions,
            String realm,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        try {
            var state = states.realm(realm);
            if (!state.activeGovernmentFormId().isBlank()) {
                return CommandSource.suggestMatching(definitions.require(state.activeGovernmentFormId())
                        .offices().stream().map(office -> office.id()), builder);
            }
        } catch (RuntimeException ignored) {
        }
        return builder.buildFuture();
    }

    private static int run(ServerCommandSource source, ThrowingRunnable action) {
        try {
            action.run();
            return Command.SINGLE_SUCCESS;
        } catch (Exception exception) {
            source.sendError(net.minecraft.text.Text.literal(exception.getMessage()));
            return 0;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
