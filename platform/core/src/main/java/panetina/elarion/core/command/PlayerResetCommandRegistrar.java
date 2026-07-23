package panetina.elarion.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.server.command.ServerCommandSource;
import panetina.elarion.core.service.PlayerResetService;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class PlayerResetCommandRegistrar {
    static final int ADMIN_PERMISSION_LEVEL = 4;

    private PlayerResetCommandRegistrar() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> register(PlayerResetService resets) {
        return literal("reset")
                .requires(source -> source.hasPermissionLevel(ADMIN_PERMISSION_LEVEL))
                .then(literal("players")
                        .executes(context -> preview(context.getSource(), resets))
                        .then(literal("confirm")
                                .then(argument("token", StringArgumentType.word())
                                        .executes(context -> confirm(context.getSource(), resets,
                                                StringArgumentType.getString(context, "token")))))
                        .then(literal("cancel")
                                .then(argument("token", StringArgumentType.word())
                                        .executes(context -> cancel(context.getSource(), resets,
                                                StringArgumentType.getString(context, "token"))))));
    }

    private static int preview(ServerCommandSource source, PlayerResetService resets) {
        PlayerResetService.Preview preview = resets.preview(source.getServer(), executorKey(source));
        long players = Math.max(preview.counts().getOrDefault("players", 0L),
                preview.counts().getOrDefault("citizens", 0L));
        long tombstones = preview.counts().getOrDefault("tombstones", 0L);
        long limbo = preview.counts().getOrDefault("limboWorlds", 0L);
        long operators = preview.counts().getOrDefault("operators", 0L);
        long whitelistEntries = preview.counts().getOrDefault("whitelistEntries", 0L);
        long cachedProfiles = preview.counts().getOrDefault("cachedProfiles", 0L);
        source.sendFeedback(() -> Text.literal(
                "This will permanently reset all player, citizen, Shrine, tombstone and Afterlife progression "
                        + "while preserving worlds, buildings, NPCs, Shrines and portals. It also clears all "
                        + "operators, whitelist entries and cached profiles. Everyone must be re-whitelisted "
                        + "and operators must be restored from the server console or bridge.\n\n"
                        + "Players affected: " + players + "\n"
                        + "Tombstones affected: " + tombstones + "\n"
                        + "Personal Limbo worlds affected: " + limbo + "\n"
                        + "Operators removed: " + operators + "\n"
                        + "Whitelist entries removed: " + whitelistEntries + "\n"
                        + "Cached profiles removed: " + cachedProfiles + "\n\nAre you sure?"), false);
        String token = preview.token();
        MutableText confirm = Text.literal("[CONFIRM RESET]").formatted(Formatting.BOLD, Formatting.RED)
                .styled(style -> style.withClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND, "/e reset players confirm " + token)));
        MutableText cancel = Text.literal(" [CANCEL]").formatted(Formatting.GRAY)
                .styled(style -> style.withClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND, "/e reset players cancel " + token)));
        source.sendFeedback(() -> confirm.append(cancel), false);
        return 1;
    }

    private static int confirm(ServerCommandSource source, PlayerResetService resets, String token) {
        String key = executorKey(source);
        String name = source.getName();
        source.sendFeedback(() -> Text.literal("Reset confirmed. Players are being disconnected and state is being backed up."), true);
        source.getServer().execute(() -> {
            try {
                PlayerResetService.Execution result = resets.execute(source.getServer(), key, name, token);
                source.getServer().sendMessage(Text.literal("Player reset complete. Backup: " + result.backup()
                        + "; changed: " + result.changed()));
            } catch (Exception exception) {
                source.getServer().sendMessage(Text.literal("Player reset failed before completion: "
                        + exception.getMessage()).formatted(Formatting.RED));
            }
        });
        return 1;
    }

    private static int cancel(ServerCommandSource source, PlayerResetService resets, String token) {
        if (!resets.cancel(executorKey(source), token)) {
            source.sendError(Text.literal("That reset confirmation expired or belongs to another executor."));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Player reset cancelled."), false);
        return 1;
    }

    private static String executorKey(ServerCommandSource source) {
        try {
            UUID id = source.getPlayerOrThrow().getUuid();
            return "player:" + id;
        } catch (Exception ignored) {
            return "source:" + source.getName().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
