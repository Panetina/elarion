package panetina.elarion.addons.groups.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.addons.groups.model.GroupRecord;
import panetina.elarion.addons.groups.service.GroupService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.command.CommandOutput;

import java.util.Locale;
import java.util.UUID;

public final class GroupCommands {
    private GroupCommands() {
    }

    public static void registerPlayerCommands(
            CommandDispatcher<ServerCommandSource> dispatcher,
            ElarionApi api,
            GroupService groups
    ) {
        api.system().commands().registerHelpDescription("group",
                "/group create|invite|accept|kick|leave|transfer|info - Manage your public group.");
        api.system().commands().registerHelpDescription("gc",
                "/gc <message> - Send a message to your group.");

        dispatcher.register(CommandManager.literal("gc")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> run(ctx.getSource(), () -> groups.sendGroupMessage(
                                ctx.getSource().getPlayerOrThrow(),
                                StringArgumentType.getString(ctx, "message"))))));

        dispatcher.register(CommandManager.literal("group")
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("id", StringArgumentType.word())
                                .then(CommandManager.argument("tag", StringArgumentType.word())
                                        .then(CommandManager.argument("display-name", StringArgumentType.greedyString())
                                                .executes(ctx -> run(ctx.getSource(), () -> {
                                                    GroupRecord group = groups.create(
                                                            ctx.getSource().getPlayerOrThrow(),
                                                            StringArgumentType.getString(ctx, "id"),
                                                            StringArgumentType.getString(ctx, "tag"),
                                                            StringArgumentType.getString(ctx, "display-name"));
                                                    ctx.getSource().sendFeedback(
                                                            () -> Text.literal("Created group "
                                                                    + group.displayName() + " [" + group.tag() + "]."),
                                                            false);
                                                }))))))
                .then(CommandManager.literal("invite")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    groups.invite(ctx.getSource().getPlayerOrThrow(), target);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Invited " + target.getGameProfile().getName()
                                                    + " to your group."), false);
                                }))))
                .then(CommandManager.literal("accept")
                        .then(CommandManager.argument("group", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        groups.groups().stream().map(GroupRecord::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    GroupRecord group = groups.accept(ctx.getSource().getPlayerOrThrow(),
                                            StringArgumentType.getString(ctx, "group"));
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Joined " + group.displayName()
                                                    + " [" + group.tag() + "]."), false);
                                }))))
                .then(CommandManager.literal("kick")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    groups.kick(ctx.getSource().getPlayerOrThrow(), target);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Removed " + target.getGameProfile().getName()
                                                    + " from your group."), false);
                                }))))
                .then(CommandManager.literal("leave")
                        .executes(ctx -> run(ctx.getSource(), () -> {
                            groups.leave(ctx.getSource().getPlayerOrThrow());
                            ctx.getSource().sendFeedback(() -> Text.literal("Left your group."), false);
                        })))
                .then(CommandManager.literal("transfer")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    groups.transfer(ctx.getSource().getPlayerOrThrow(), target);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Transferred group leadership to "
                                                    + target.getGameProfile().getName() + "."), false);
                                }))))
                .then(CommandManager.literal("info")
                        .executes(ctx -> run(ctx.getSource(), () -> showOwnGroup(ctx.getSource(), groups)))
                        .then(CommandManager.argument("group", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        groups.groups().stream().map(GroupRecord::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> showGroup(ctx.getSource(), groups,
                                        StringArgumentType.getString(ctx, "group")))))));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> admin(GroupService groups) {
        return CommandManager.literal("groups")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reload")
                        .executes(ctx -> run(ctx.getSource(), () -> {
                            groups.reload(panetina.elarion.addons.groups.config.GroupConfigLoader.load());
                            CommandOutput.success(ctx.getSource(), "Group config reloaded.", false);
                        })))
                .then(CommandManager.literal("list")
                        .executes(ctx -> run(ctx.getSource(), () -> {
                            CommandOutput.header(ctx.getSource(), "Groups");
                            if (groups.groups().isEmpty()) {
                                CommandOutput.empty(ctx.getSource(), "No groups.");
                            } else {
                                groups.groups().forEach(group -> CommandOutput.bullet(ctx.getSource(),
                                        group.id() + " [" + group.tag() + "] " + group.displayName()));
                            }
                        })))
                .then(CommandManager.literal("inspect")
                        .then(CommandManager.argument("group", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        groups.groups().stream().map(GroupRecord::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> showGroup(ctx.getSource(), groups,
                                        StringArgumentType.getString(ctx, "group"))))))
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("group", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        groups.groups().stream().map(GroupRecord::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    String groupId = StringArgumentType.getString(ctx, "group");
                                    groups.delete(groupId, actor(ctx.getSource()));
                                    CommandOutput.success(ctx.getSource(), "Deleted group " + groupId + ".", true);
                                }))))
                .then(CommandManager.literal("transfer")
                        .then(CommandManager.argument("group", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        groups.groups().stream().map(GroupRecord::id), builder))
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(ctx -> run(ctx.getSource(), () -> {
                                            String groupId = StringArgumentType.getString(ctx, "group");
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                            groups.transfer(groupId, target.getUuid(), actor(ctx.getSource()));
                                            CommandOutput.success(ctx.getSource(),
                                                    "Transferred " + groupId + " to "
                                                            + target.getGameProfile().getName() + ".", true);
                                        })))));
    }

    private static void showOwnGroup(ServerCommandSource source, GroupService groups) throws Exception {
        GroupRecord group = groups.groupFor(source.getPlayerOrThrow().getUuid())
                .orElseThrow(() -> new IllegalArgumentException("You are not in a group."));
        showGroup(source, groups, group.id());
    }

    private static void showGroup(ServerCommandSource source, GroupService groups, String groupId) {
        GroupRecord group = groups.find(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown group: " + groupId));
        CommandOutput.header(source, group.displayName());
        CommandOutput.kv(source, "ID", group.id());
        CommandOutput.kv(source, "Tag", "[" + group.tag() + "]");
        CommandOutput.kv(source, "Leader", displayCitizen(group.leaderId()));
        CommandOutput.kv(source, "Members", group.members().size());
        CommandOutput.kv(source, "Confederation Eligible In Realm", "see Government delegate screen");
    }

    private static String displayCitizen(UUID id) {
        return ElarionApi.get().citizens().find(id)
                .map(citizen -> {
                    String name = citizen.lastKnownUsername();
                    return name == null || name.isBlank() ? id.toString() : name;
                })
                .orElse(id.toString());
    }

    private static UUID actor(ServerCommandSource source) {
        return source.getEntity() instanceof ServerPlayerEntity player ? player.getUuid() : null;
    }

    private static int run(ServerCommandSource source, ThrowingRunnable action) {
        try {
            action.run();
            return Command.SINGLE_SUCCESS;
        } catch (Exception exception) {
            source.sendError(Text.literal(exception.getMessage()));
            return 0;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
