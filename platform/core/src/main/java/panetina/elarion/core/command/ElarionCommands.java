package panetina.elarion.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.api.ElarionCommandRegistry;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.CommunityDefinition;
import panetina.elarion.core.model.TitleDefinition;

import java.util.function.Supplier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ElarionCommands {
    private ElarionCommands() {}

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            ElarionApi api,
            CoreConfigManager config,
            ElarionCommandRegistry extensions
    ) {
        dispatcher.register(literal("cc")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> api.chat().sendCommunityMessage(
                                context.getSource().getPlayerOrThrow(),
                                StringArgumentType.getString(context, "message")) ? 1 : 0)));

        LiteralArgumentBuilder<ServerCommandSource> root = literal("e")
                .requires(source -> source.hasPermissionLevel(4))
                .then(communityCommands(api))
                .then(citizenCommands(api))
                .then(titleCommands(api))
                .then(abilityCommands(api))
                .then(rewardCommands(api))
                .then(literal("reload").executes(context -> {
                    config.load();
                    api.titles().all().forEach(title -> title.abilities().forEach(api.abilities()::register));
                    api.communities().initializeScoreboardTeams(context.getSource().getServer());
                    for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                        api.communities().applyCurrentScoreboardTeam(player);
                    }
                    context.getSource().sendFeedback(() -> Text.literal("Elarion configuration reloaded."), true);
                    return 1;
                }));

        for (Supplier<LiteralArgumentBuilder<ServerCommandSource>> extension : extensions.adminCommands()) {
            root.then(extension.get());
        }
        dispatcher.register(root);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> communityCommands(ElarionApi api) {
        return literal("community")
                .then(literal("add")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("community", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            api.communities().all().forEach(value -> builder.suggest(value.id()));
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String id = StringArgumentType.getString(context, "community");
                                            if (!api.communities().assign(player, id)) {
                                                context.getSource().sendError(Text.literal("Unknown community: " + id));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Assigned " + player.getGameProfile().getName() + " to " + id), true);
                                            return 1;
                                        }))))
                .then(literal("remove")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    api.communities().remove(player);
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Removed " + player.getGameProfile().getName() + " from their community"), true);
                                    return 1;
                                })))
                .then(literal("list").executes(context -> {
                    String values = api.communities().all().stream()
                            .map(CommunityDefinition::id)
                            .sorted()
                            .reduce((left, right) -> left + ", " + right)
                            .orElse("(none)");
                    context.getSource().sendFeedback(() -> Text.literal("Communities: " + values), false);
                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> citizenCommands(ElarionApi api) {
        return literal("citizen")
                .then(literal("info")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    CitizenRecord citizen = api.citizens().getOrCreate(player);
                                    String info = "Citizen " + player.getGameProfile().getName()
                                            + " | community=" + value(citizen.communityId())
                                            + " | title=" + value(citizen.titleId())
                                            + " | nickname=" + value(citizen.nickname())
                                            + " | status=" + citizen.status();
                                    context.getSource().sendFeedback(() -> Text.literal(info), false);
                                    return 1;
                                })))
                .then(literal("nickname")
                        .then(literal("set")
                                .then(argument("player", EntityArgumentType.player())
                                        .then(argument("nickname", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                    String nickname = StringArgumentType.getString(context, "nickname").trim();
                                                    api.citizens().update(player, "nickname-set", citizen -> citizen.setNickname(nickname));
                                                    context.getSource().sendFeedback(
                                                            () -> Text.literal("Set nickname for " + player.getGameProfile().getName()), true);
                                                    return 1;
                                                }))))
                        .then(literal("clear")
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            api.citizens().update(player, "nickname-cleared", citizen -> citizen.setNickname(null));
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Cleared nickname for " + player.getGameProfile().getName()), true);
                                            return 1;
                                        }))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> titleCommands(ElarionApi api) {
        return literal("title")
                .then(literal("set")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("title", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            api.titles().all().forEach(value -> builder.suggest(value.id()));
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String title = StringArgumentType.getString(context, "title");
                                            if (!api.titles().assign(player, title)) {
                                                context.getSource().sendError(Text.literal("Unknown title: " + title));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Set title " + title + " for " + player.getGameProfile().getName()), true);
                                            return 1;
                                        }))))
                .then(literal("clear")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    api.titles().clear(player);
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Cleared title for " + player.getGameProfile().getName()), true);
                                    return 1;
                                })))
                .then(literal("list").executes(context -> {
                    String values = api.titles().all().stream()
                            .map(TitleDefinition::id)
                            .sorted()
                            .reduce((left, right) -> left + ", " + right)
                            .orElse("(none)");
                    context.getSource().sendFeedback(() -> Text.literal("Titles: " + values), false);
                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> abilityCommands(ElarionApi api) {
        return literal("ability")
                .then(literal("check")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("ability", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String ability = StringArgumentType.getString(context, "ability");
                                            boolean allowed = api.abilities().has(api.citizens().getOrCreate(player), ability);
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal(player.getGameProfile().getName() + " ability " + ability + ": " + allowed), false);
                                            return allowed ? 1 : 0;
                                        }))))
                .then(literal("grant")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("ability", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String ability = StringArgumentType.getString(context, "ability");
                                            api.citizens().update(player, "ability-granted", citizen -> api.abilities().grant(citizen, ability));
                                            context.getSource().sendFeedback(() -> Text.literal("Granted " + ability), true);
                                            return 1;
                                        }))))
                .then(literal("revoke")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("ability", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String ability = StringArgumentType.getString(context, "ability");
                                            api.citizens().update(player, "ability-revoked", citizen -> api.abilities().revoke(citizen, ability));
                                            context.getSource().sendFeedback(() -> Text.literal("Revoked " + ability), true);
                                            return 1;
                                        }))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> rewardCommands(ElarionApi api) {
        return literal("reward")
                .then(literal("run")
                        .then(argument("reward", StringArgumentType.word())
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String reward = StringArgumentType.getString(context, "reward");
                                            boolean success = api.rewards().executeReward(reward, player);
                                            if (!success) {
                                                context.getSource().sendError(Text.literal("Unknown or failed reward: " + reward));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(() -> Text.literal("Executed reward " + reward), true);
                                            return 1;
                                        }))));
    }

    private static String value(String value) {
        return value == null || value.isBlank() ? "(none)" : value;
    }
}
