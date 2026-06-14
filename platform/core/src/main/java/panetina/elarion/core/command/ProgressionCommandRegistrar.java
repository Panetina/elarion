package panetina.elarion.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static panetina.elarion.core.command.CommandSupport.suggestProgressionRules;

final class ProgressionCommandRegistrar {
    private ProgressionCommandRegistrar() {
    }

    static LiteralArgumentBuilder<ServerCommandSource> register(ElarionApi api) {
        return literal("progression")
                .then(literal("inspect")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    var stats = api.playerStats().get(player.getUuid());
                                    Map<String, Long> progress = api.progression().progressFor(player.getUuid());
                                    CommandOutput.header(context.getSource(), "Progression");
                                    CommandOutput.kv(context.getSource(), "Player", player.getGameProfile().getName());
                                    CommandOutput.section(context.getSource(), "Stats");
                                    CommandOutput.kv(context.getSource(), "Zombie kills", stats.zombieKills());
                                    CommandOutput.kv(context.getSource(), "Dragon kills", stats.dragonKills());
                                    if (stats.customCounters().isEmpty()) {
                                        CommandOutput.kv(context.getSource(), "Custom counters", "(none)");
                                    } else {
                                        stats.customCounters().entrySet().stream()
                                                .sorted(Map.Entry.comparingByKey())
                                                .forEach(entry -> CommandOutput.kv(
                                                        context.getSource(), entry.getKey(), entry.getValue()));
                                    }
                                    CommandOutput.section(context.getSource(), "Title Progress");
                                    if (progress.isEmpty()) {
                                        CommandOutput.empty(context.getSource(), "No title progress recorded.");
                                    } else {
                                        progress.entrySet().stream()
                                                .sorted(Map.Entry.comparingByKey())
                                                .forEach(entry -> CommandOutput.kv(
                                                        context.getSource(), entry.getKey(), entry.getValue()));
                                    }
                                    return 1;
                                })))
                .then(literal("event")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("event", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String event = StringArgumentType.getString(context, "event");
                                            api.progression().recordCustom(player, event, "");
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Recorded progression event " + event
                                                            + " for " + player.getGameProfile().getName()), true);
                                            return 1;
                                        })
                                        .then(argument("subject", StringArgumentType.word())
                                                .executes(context -> {
                                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                    String event = StringArgumentType.getString(context, "event");
                                                    String subject = StringArgumentType.getString(context, "subject");
                                                    api.progression().recordCustom(player, event, subject);
                                                    context.getSource().sendFeedback(
                                                            () -> Text.literal("Recorded progression event " + event
                                                                    + " subject=" + subject + " for "
                                                                    + player.getGameProfile().getName()), true);
                                                    return 1;
                                                })))))
                .then(literal("reset")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    api.progression().resetProgress(player.getUuid(), "");
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Reset all title progress for "
                                                    + player.getGameProfile().getName()), true);
                                    return 1;
                                })
                                .then(argument("rule", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestProgressionRules(api, builder))
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String rule = StringArgumentType.getString(context, "rule");
                                            api.progression().resetProgress(player.getUuid(), rule);
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Reset progression rule " + rule
                                                            + " for " + player.getGameProfile().getName()), true);
                                            return 1;
                                        }))))
                .then(literal("test-rule")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("rule", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestProgressionRules(api, builder))
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String rule = StringArgumentType.getString(context, "rule");
                                            boolean matches = api.progression().testRule(player, rule);
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Rule " + rule + " matches current player state: "
                                                            + matches), false);
                                            return matches ? 1 : 0;
                                        }))));
    }
}
