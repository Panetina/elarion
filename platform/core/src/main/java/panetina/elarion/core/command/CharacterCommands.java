package panetina.elarion.core.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.model.CharacterLifecycleRecord;
import panetina.elarion.core.service.CharacterLifecycleService;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class CharacterCommands {
    private CharacterCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> admin(CharacterLifecycleService characters) {
        return literal("character")
                .then(literal("inspect")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            CharacterLifecycleRecord record = characters.find(player.getUuid()).orElse(null);
                            context.getSource().sendFeedback(() -> Text.literal(record == null
                                    ? "No character lifecycle record."
                                    : "Character " + player.getGameProfile().getName()
                                    + ": id=" + record.activeCharacterId
                                    + " generation=" + record.generation
                                    + " status=" + record.status
                                    + " eligibleAt=" + record.eligibleAt
                                    + " resetReason=" + record.resetReason
                                    + " completedResetSteps=" + record.completedResetSteps), false);
                            return record == null ? 0 : 1;
                        })))
                .then(literal("recreate-now")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            characters.finishCooldown(player.getUuid());
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "Character recreation is now available for "
                                            + player.getGameProfile().getName() + "."), true);
                            return 1;
                        })))
                .then(literal("archive")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            var archives = characters.archives(player.getUuid());
                            if (archives.isEmpty()) {
                                context.getSource().sendFeedback(() -> Text.literal("No dead character archives."), false);
                                return 0;
                            }
                            archives.forEach(archive -> context.getSource().sendFeedback(() -> Text.literal(
                                    archive.characterId + " generation=" + archive.generation
                                            + " name=" + archive.displayName
                                            + " realm=" + archive.realmId
                                            + " diedAt=" + archive.diedAt), false));
                            return archives.size();
                        })));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> test(CharacterLifecycleService characters) {
        return literal("character")
                .then(literal("finish-cooldown")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            characters.finishCooldown(player.getUuid());
                            context.getSource().sendFeedback(() -> Text.literal("Finished character cooldown."), true);
                            return 1;
                        })))
                .then(literal("trigger-true-death")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            characters.beginTrueDeath(player, "test-command", java.util.Map.of("test", "true"));
                            context.getSource().sendFeedback(() -> Text.literal("Triggered test True Death."), true);
                            return 1;
                        })))
                .then(literal("reset")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            characters.resetForTesting(player);
                            context.getSource().sendFeedback(() -> Text.literal("Reset character creation state."), true);
                            return 1;
                        })))
                .then(literal("force-active")
                        .then(argument("player", EntityArgumentType.player()).executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            characters.forceActiveForTesting(player);
                            context.getSource().sendFeedback(() -> Text.literal(
                                    "Forced character lifecycle active for "
                                            + player.getGameProfile().getName() + "."), true);
                            return 1;
                        })));
    }
}
