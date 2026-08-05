package panetina.elarion.addons.guilds.command;

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
import panetina.elarion.addons.guilds.model.GuildRecord;
import panetina.elarion.addons.guilds.service.GuildService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.command.CommandOutput;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public final class GuildCommands {
    private GuildCommands() {
    }

    public static void registerPlayerCommands(
            CommandDispatcher<ServerCommandSource> dispatcher,
            ElarionApi api,
            GuildService guilds,
            Consumer<ServerPlayerEntity> openGuildScreen
    ) {
        api.system().commands().registerHelpDescription("guild",
                "/guild - Open your Guild management screen. Create a Guild through a Guild Registrar.");
        api.system().commands().registerHelpDescription("gc",
                "/gc <message> - Send a message to your guild.");

        dispatcher.register(CommandManager.literal("gc")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> run(ctx.getSource(), () -> guilds.sendGuildMessage(
                                ctx.getSource().getPlayerOrThrow(),
                                StringArgumentType.getString(ctx, "message"))))));

        dispatcher.register(CommandManager.literal("guild")
                .executes(ctx -> run(ctx.getSource(), () -> openGuildScreen.accept(ctx.getSource().getPlayerOrThrow())))
                .then(CommandManager.literal("invite")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    guilds.invite(ctx.getSource().getPlayerOrThrow(), target);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Invited " + target.getGameProfile().getName()
                                                    + " to your guild."), false);
                                }))))
                .then(CommandManager.literal("accept")
                        .then(CommandManager.argument("guild", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        guilds.guilds().stream().map(GuildRecord::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    GuildRecord guild = guilds.accept(ctx.getSource().getPlayerOrThrow(),
                                            StringArgumentType.getString(ctx, "guild"));
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Joined " + guild.displayName()
                                                    + " [" + guild.tag() + "]."), false);
                                }))))
                .then(CommandManager.literal("kick")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    guilds.kick(ctx.getSource().getPlayerOrThrow(), target);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Removed " + target.getGameProfile().getName()
                                                    + " from your guild."), false);
                                }))))
                .then(CommandManager.literal("leave")
                        .executes(ctx -> run(ctx.getSource(), () -> {
                            guilds.leave(ctx.getSource().getPlayerOrThrow());
                            ctx.getSource().sendFeedback(() -> Text.literal("Left your guild."), false);
                        })))
                .then(CommandManager.literal("transfer")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    guilds.transfer(ctx.getSource().getPlayerOrThrow(), target);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Transferred guild leadership to "
                                                    + target.getGameProfile().getName() + "."), false);
                                }))))
                .then(CommandManager.literal("tag")
                        .then(CommandManager.literal("hide")
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    guilds.setTagHidden(ctx.getSource().getPlayerOrThrow(), true);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Your guild tag is now hidden publicly."), false);
                                })))
                        .then(CommandManager.literal("show")
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    guilds.setTagHidden(ctx.getSource().getPlayerOrThrow(), false);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Your guild tag is now shown publicly."), false);
                                }))))
                .then(CommandManager.literal("info")
                        .executes(ctx -> run(ctx.getSource(), () -> showOwnGuild(ctx.getSource(), guilds)))
                        .then(CommandManager.argument("guild", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        guilds.guilds().stream().map(GuildRecord::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> showGuild(ctx.getSource(), guilds,
                                        StringArgumentType.getString(ctx, "guild")))))));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> admin(GuildService guilds) {
        return CommandManager.literal("guilds")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reload")
                        .executes(ctx -> run(ctx.getSource(), () -> {
                            guilds.reload(panetina.elarion.addons.guilds.config.GuildConfigLoader.load());
                            CommandOutput.success(ctx.getSource(), "Guild config reloaded.", false);
                        })))
                .then(CommandManager.literal("list")
                        .executes(ctx -> run(ctx.getSource(), () -> {
                            CommandOutput.header(ctx.getSource(), "Guilds");
                            if (guilds.guilds().isEmpty()) {
                                CommandOutput.empty(ctx.getSource(), "No guilds.");
                            } else {
                                guilds.guilds().forEach(guild -> CommandOutput.bullet(ctx.getSource(),
                                        guild.id() + " [" + guild.tag() + "] " + guild.displayName()));
                            }
                        })))
                .then(CommandManager.literal("inspect")
                        .then(CommandManager.argument("guild", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        guilds.guilds().stream().map(GuildRecord::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> showGuild(ctx.getSource(), guilds,
                                        StringArgumentType.getString(ctx, "guild"))))))
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("guild", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        guilds.guilds().stream().map(GuildRecord::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    String guildId = StringArgumentType.getString(ctx, "guild");
                                    guilds.delete(guildId, actor(ctx.getSource()));
                                    CommandOutput.success(ctx.getSource(), "Deleted guild " + guildId + ".", true);
                                }))))
                .then(CommandManager.literal("transfer")
                        .then(CommandManager.argument("guild", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        guilds.guilds().stream().map(GuildRecord::id), builder))
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(ctx -> run(ctx.getSource(), () -> {
                                            String guildId = StringArgumentType.getString(ctx, "guild");
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                            guilds.transfer(guildId, target.getUuid(), actor(ctx.getSource()));
                                            CommandOutput.success(ctx.getSource(),
                                                    "Transferred " + guildId + " to "
                                                            + target.getGameProfile().getName() + ".", true);
                                        })))));
    }

    private static void showOwnGuild(ServerCommandSource source, GuildService guilds) throws Exception {
        GuildRecord guild = guilds.guildFor(source.getPlayerOrThrow().getUuid())
                .orElseThrow(() -> new IllegalArgumentException("You are not in a guild."));
        showGuild(source, guilds, guild.id());
    }

    private static void showGuild(ServerCommandSource source, GuildService guilds, String guildId) {
        GuildRecord guild = guilds.find(guildId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown guild: " + guildId));
        CommandOutput.header(source, guild.displayName());
        CommandOutput.kv(source, "ID", guild.id());
        CommandOutput.kv(source, "Tag", "[" + guild.tag() + "]");
        CommandOutput.kv(source, "Public Tag", guild.tagHidden() ? "Hidden" : "Shown");
        CommandOutput.kv(source, "Leader", displayCitizen(guild.leaderId()));
        CommandOutput.kv(source, "Members", guild.members().size());
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
