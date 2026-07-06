package panetina.elarion.addons.underworld.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.addons.underworld.model.CorpseRecord;
import panetina.elarion.addons.underworld.model.ElarionDeathType;
import panetina.elarion.addons.underworld.model.SoulState;
import panetina.elarion.addons.underworld.model.UnderworldSession;
import panetina.elarion.addons.underworld.service.UnderworldService;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class DeathCommands {
    private DeathCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> create(UnderworldService service) {
        return literal("death")
                .then(literal("reload").executes(context -> {
                    service.reload();
                    context.getSource().sendFeedback(() -> Text.literal("Underworld configuration reloaded."), true);
                    return 1;
                }))
                .then(literal("inspect")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            UnderworldSession session = service.activeSession(player.getUuid()).orElse(null);
                            SoulState soul = service.soul(player.getUuid()).orElse(null);
                            context.getSource().sendFeedback(() -> Text.literal(
                                    player.getGameProfile().getName()
                                            + " underworld=" + (session == null ? "none" : session.remainingMillis + "ms")
                                            + " fractures=" + (soul == null ? 0 : soul.fractures)
                                            + " trueDeath=" + (soul != null && soul.trueDeath)), false);
                            return 1;
                        })))
                .then(literal("corpse")
                        .then(literal("list").executes(context -> {
                            for (CorpseRecord corpse : service.corpses()) {
                                context.getSource().sendFeedback(() -> Text.literal(
                                        corpse.corpseId + " victim=" + corpse.victimId
                                                + " type=" + corpse.deathType
                                                + " world=" + corpse.worldId
                                                + " items=" + corpse.protectedVictimItems.size()
                                                + " pvp=" + corpse.pvpLootItems.size()), false);
                            }
                            return service.corpses().size();
                        }))
                        .then(literal("inspect")
                                .then(argument("corpseId", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                                service.corpses().stream().map(corpse -> corpse.corpseId), builder))
                                        .executes(context -> {
                                    String id = StringArgumentType.getString(context, "corpseId");
                                    CorpseRecord corpse = service.corpse(id)
                                            .orElseThrow(() -> new IllegalArgumentException("Unknown corpse: " + id));
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            id + " victim=" + corpse.victimId
                                                    + " killer=" + corpse.killerId
                                                    + " recovered=" + corpse.victimRecovered
                                                    + " pvpClaimed=" + corpse.pvpLootClaimed), false);
                                    return 1;
                                })))
                        .then(literal("recover")
                                .then(argument("corpseId", StringArgumentType.word())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                                service.corpses().stream().map(corpse -> corpse.corpseId), builder))
                                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                            String id = StringArgumentType.getString(context, "corpseId");
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            service.adminRecover(id, player);
                                            context.getSource().sendFeedback(() -> Text.literal(
                                                    "Recovered corpse " + id + " to "
                                                            + player.getGameProfile().getName() + "."), true);
                                            return 1;
                                        })))))
                .then(literal("vault")
                        .then(literal("recover")
                                .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    int recovered = service.recoverVault(player);
                                    context.getSource().sendFeedback(() -> Text.literal(recovered == 0
                                            ? "No recovery-vault stacks could be moved."
                                            : "Recovered " + recovered + " stack(s) from the recovery vault."), true);
                                    return recovered;
                                }))))
                .then(literal("underworld")
                        .then(literal("send")
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> send(service, context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                service.config().pveTimerMinutes()))
                                        .then(argument("minutes", IntegerArgumentType.integer(1, 1440))
                                                .executes(context -> send(service, context.getSource(),
                                                        EntityArgumentType.getPlayer(context, "player"),
                                                        IntegerArgumentType.getInteger(context, "minutes"))))))
                        .then(literal("return")
                                .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    boolean returned = service.returnPlayer(player);
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            returned
                                                    ? "Returned " + player.getGameProfile().getName()
                                                    + " from the Underworld."
                                                    : player.getGameProfile().getName()
                                                    + " has no active Underworld session."), true);
                                    return returned ? 1 : 0;
                                }))))
                .then(literal("soul")
                        .then(literal("inspect")
                                .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    SoulState soul = service.soul(player.getUuid()).orElse(null);
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "Soul " + player.getGameProfile().getName()
                                                    + ": fractures=" + (soul == null ? 0 : soul.fractures)
                                                    + " trueDeath=" + (soul != null && soul.trueDeath)), false);
                                    return 1;
                                })))
                        .then(literal("add-fracture")
                                .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    SoulState soul = service.addFracture(player);
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "Added fracture to " + player.getGameProfile().getName()
                                                    + " (" + soul.fractures + ")."), true);
                                    return 1;
                                })))
                        .then(literal("remove-fracture")
                                .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    SoulState soul = service.removeFracture(player);
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "Removed fracture from " + player.getGameProfile().getName()
                                                    + " (" + soul.fractures + ")."), true);
                                    return 1;
                                })))
                        .then(literal("clear-fractures")
                                .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    service.clearFractures(player.getUuid());
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "Cleared soul fractures for " + player.getGameProfile().getName() + "."),
                                            true);
                                    return 1;
                                }))));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> createTest(UnderworldService service) {
        return literal("death")
                .then(literal("send")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("minutes", IntegerArgumentType.integer(1, 1440))
                                        .executes(context -> send(service, context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "minutes"))))))
                .then(literal("return")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            service.forceReturnPlayer(player);
                            context.getSource().sendFeedback(() -> Text.literal("Force-returned test player."), true);
                            return 1;
                        })))
                .then(literal("fracture")
                        .then(literal("add")
                                .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    SoulState soul = service.addFracture(player);
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "Added test fracture (" + soul.fractures + ")."), true);
                                    return 1;
                                })))
                        .then(literal("remove")
                                .then(argument("player", EntityArgumentType.player()).executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    SoulState soul = service.removeFracture(player);
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            "Removed test fracture (" + soul.fractures + ")."), true);
                                    return 1;
                                })))
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            SoulState soul = service.addFracture(player);
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "Added test fracture (" + soul.fractures + ")."), true);
                            return 1;
                        })))
                .then(literal("clear")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            service.resetPlayer(player.getUuid());
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "Cleared Underworld state for " + player.getGameProfile().getName() + "."), true);
                            return 1;
                        })))
                .then(literal("reset-state").executes(context -> {
                    service.resetAll();
                    context.getSource().sendFeedback(() -> Text.literal(
                            "Cleared all Underworld sessions, fractures, corpses, and combat tags.")
                            .formatted(Formatting.YELLOW), true);
                    return 1;
                }));
    }

    private static int send(
            UnderworldService service,
            ServerCommandSource source,
            ServerPlayerEntity player,
            int minutes
    ) {
        service.sendPlayerToUnderworld(player, minutes, ElarionDeathType.ADMIN);
        source.sendFeedback(() -> Text.literal(
                "Sent " + player.getGameProfile().getName() + " to the Underworld for "
                        + minutes + " minutes."), true);
        return 1;
    }
}
