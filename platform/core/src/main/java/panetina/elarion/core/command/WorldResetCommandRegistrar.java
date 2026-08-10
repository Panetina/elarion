package panetina.elarion.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.core.service.WorldResetService;

import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class WorldResetCommandRegistrar {
    private WorldResetCommandRegistrar() {}

    public static LiteralArgumentBuilder<ServerCommandSource> register(WorldResetService resets,
                                                                         Supplier<java.util.Collection<String>> worlds) {
        return literal("world")
                .requires(source -> source.hasPermissionLevel(4))
                .then(argument("world", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(worlds.get(), builder))
                        .executes(context -> preview(context.getSource(), resets,
                                StringArgumentType.getString(context, "world")))
                        .then(literal("confirm").then(argument("token", StringArgumentType.word())
                                .executes(context -> confirm(context.getSource(), resets,
                                        StringArgumentType.getString(context, "token")))))
                        .then(literal("cancel").then(argument("token", StringArgumentType.word())
                                .executes(context -> cancel(context.getSource(), resets,
                                        StringArgumentType.getString(context, "token"))))));
    }

    private static int preview(ServerCommandSource source, WorldResetService resets, String world) {
        try {
            WorldResetService.Preview preview = resets.preview(source.getServer(), key(source), world);
            source.sendFeedback(() -> Text.literal("This will delete the managed world terrain and all world-scoped NPCs, Shrines, offerings, and portal endpoints. Configured definitions and the world slot will be recreated.\n\n"
                    + "World: " + world + "\n" + "Affected: " + preview.counts() + "\n\nAre you sure?"), false);
            String token = preview.token();
            MutableText confirm = Text.literal("[CONFIRM WORLD RESET]").formatted(Formatting.BOLD, Formatting.RED)
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/e reset world " + world + " confirm " + token)));
            MutableText cancel = Text.literal(" [CANCEL]").formatted(Formatting.GRAY)
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/e reset world " + world + " cancel " + token)));
            source.sendFeedback(() -> confirm.append(cancel), false);
            return 1;
        } catch (RuntimeException exception) {
            source.sendError(Text.literal(exception.getMessage() == null ? "World reset preview failed." : exception.getMessage()));
            return 0;
        }
    }

    private static int confirm(ServerCommandSource source, WorldResetService resets, String token) {
        try {
            source.sendFeedback(() -> Text.literal("World reset confirmed. The old dimension is being removed before regeneration."), true);
            resets.execute(source.getServer(), key(source), source.getName(), token).whenComplete((result, failure) ->
                    source.getServer().execute(() -> {
                        if (failure != null) {
                            source.sendError(Text.literal("World reset failed: " + failure.getMessage()));
                            return;
                        }
                        source.sendFeedback(() -> Text.literal("World reset complete for " + result.worldId() + ". Backup: " + result.backup()
                                + "; changed: " + result.changed()), true);
                    }));
            return 1;
        } catch (Exception exception) {
            source.sendError(Text.literal("World reset failed: " + exception.getMessage()));
            return 0;
        }
    }

    private static int cancel(ServerCommandSource source, WorldResetService resets, String token) {
        if (!resets.cancel(key(source), token)) {
            source.sendError(Text.literal("That world reset confirmation expired or belongs to another executor."));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("World reset cancelled."), false);
        return 1;
    }

    private static String key(ServerCommandSource source) {
        try { return "player:" + source.getPlayerOrThrow().getUuid(); }
        catch (Exception ignored) { return "source:" + source.getName().toLowerCase(java.util.Locale.ROOT); }
    }
}
