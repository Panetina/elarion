package panetina.elarion.addons.underworld.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.addons.underworld.model.BanishmentRecord;
import panetina.elarion.addons.underworld.service.UnderworldService;

import java.time.Instant;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class BanishCommands {
    private static final int MAX_MINUTES = 5_256_000;

    private BanishCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> banish(UnderworldService service) {
        return literal("banish")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("list").executes(context -> {
                    int count = 0;
                    for (BanishmentRecord record : service.banishments()) {
                        String duration = record.permanent()
                                ? "permanent" : "until " + Instant.ofEpochMilli(record.expiresAt);
                        context.getSource().sendFeedback(() -> Text.literal(
                                record.playerName + " - " + duration + " - " + record.reason), false);
                        count++;
                    }
                    if (count == 0) {
                        context.getSource().sendFeedback(() -> Text.literal("No active banishments."), false);
                    }
                    return count;
                }))
                .then(argument("player", EntityArgumentType.player())
                        .then(literal("permanent")
                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> execute(
                                                service,
                                                context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                0L,
                                                true,
                                                StringArgumentType.getString(context, "reason")))))
                        .then(argument("minutes", IntegerArgumentType.integer(1, MAX_MINUTES))
                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> execute(
                                                service,
                                                context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "minutes") * 60_000L,
                                                false,
                                                StringArgumentType.getString(context, "reason"))))));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> unbanish(UnderworldService service) {
        return literal("unbanish")
                .requires(source -> source.hasPermissionLevel(4))
                .then(argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                service.banishments().stream().map(record -> record.playerName), builder))
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "player");
                            BanishmentRecord removed = service.unbanish(target).orElse(null);
                            if (removed == null) {
                                context.getSource().sendError(Text.literal("No active banishment for " + target + "."));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "Lifted the banishment of " + removed.playerName + "."), true);
                            return 1;
                        }));
    }

    private static int execute(
            UnderworldService service,
            ServerCommandSource source,
            ServerPlayerEntity player,
            long durationMillis,
            boolean permanent,
            String reason
    ) {
        if (reason == null || reason.isBlank()) {
            source.sendError(Text.literal("A banishment reason is required."));
            return 0;
        }
        BanishmentRecord record = service.banish(
                player, durationMillis, permanent, reason, source.getName());
        source.sendFeedback(() -> Text.literal(
                "Banished " + record.playerName + " "
                        + (record.permanent() ? "permanently" : "until " + Instant.ofEpochMilli(record.expiresAt))
                        + ". Reason: " + record.reason), true);
        return 1;
    }
}
