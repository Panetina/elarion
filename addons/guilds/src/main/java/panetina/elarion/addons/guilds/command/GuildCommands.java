package panetina.elarion.addons.guilds.command;

import com.mojang.brigadier.Command;
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

import java.util.UUID;

/** Administrative Guild commands only. Player interaction is exclusively UI-driven. */
public final class GuildCommands {
    private GuildCommands() { }

    public static LiteralArgumentBuilder<ServerCommandSource> admin(GuildService guilds) {
        return CommandManager.literal("guild")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("reload").executes(ctx -> run(ctx.getSource(), () -> {
                    guilds.reload(panetina.elarion.addons.guilds.config.GuildConfigLoader.load());
                    CommandOutput.success(ctx.getSource(), "Guild config reloaded.", false);
                })))
                .then(CommandManager.literal("list").executes(ctx -> run(ctx.getSource(), () -> {
                    CommandOutput.header(ctx.getSource(), "Guilds");
                    if (guilds.guilds().isEmpty()) CommandOutput.empty(ctx.getSource(), "No guilds.");
                    else guilds.guilds().forEach(guild -> CommandOutput.bullet(ctx.getSource(),
                            guild.id() + " [" + guild.tag() + "] " + guild.displayName()));
                })))
                .then(CommandManager.literal("inspect").then(guildArgument(guilds)
                        .executes(ctx -> run(ctx.getSource(), () -> showGuild(ctx.getSource(), guilds,
                                StringArgumentType.getString(ctx, "guild"))))))
                .then(CommandManager.literal("delete").then(guildArgument(guilds)
                        .executes(ctx -> run(ctx.getSource(), () -> {
                            String guildId = StringArgumentType.getString(ctx, "guild");
                            guilds.delete(guildId, actor(ctx.getSource()));
                            CommandOutput.success(ctx.getSource(), "Deleted guild " + guildId + ".", true);
                        }))))
                .then(CommandManager.literal("transfer").then(guildArgument(guilds)
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(ctx -> run(ctx.getSource(), () -> {
                                    String guildId = StringArgumentType.getString(ctx, "guild");
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                    guilds.transfer(guildId, target.getUuid(), actor(ctx.getSource()));
                                    CommandOutput.success(ctx.getSource(), "Transferred " + guildId + " to "
                                            + target.getGameProfile().getName() + ".", true);
                                })))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String> guildArgument(GuildService guilds) {
        return CommandManager.argument("guild", StringArgumentType.word())
                .suggests((ctx, builder) -> CommandSource.suggestMatching(guilds.guilds().stream().map(GuildRecord::id), builder));
    }

    private static void showGuild(ServerCommandSource source, GuildService guilds, String guildId) {
        GuildRecord guild = guilds.find(guildId).orElseThrow(() -> new IllegalArgumentException("Unknown guild: " + guildId));
        CommandOutput.header(source, guild.displayName());
        CommandOutput.kv(source, "ID", guild.id());
        CommandOutput.kv(source, "Tag", "[" + guild.tag() + "]");
        CommandOutput.kv(source, "Public Tag", guild.tagHidden() ? "Hidden" : "Shown");
        CommandOutput.kv(source, "Leader", ElarionApi.get().citizens().find(guild.leaderId())
                .map(citizen -> citizen.lastKnownUsername()).filter(name -> name != null && !name.isBlank())
                .orElse(guild.leaderId().toString()));
        CommandOutput.kv(source, "Members", guild.members().size());
    }

    private static UUID actor(ServerCommandSource source) { return source.getEntity() instanceof ServerPlayerEntity player ? player.getUuid() : null; }
    private static int run(ServerCommandSource source, ThrowingRunnable action) {
        try { action.run(); return Command.SINGLE_SUCCESS; }
        catch (Exception exception) { source.sendError(Text.literal(exception.getMessage())); return 0; }
    }
    @FunctionalInterface private interface ThrowingRunnable { void run() throws Exception; }
}
