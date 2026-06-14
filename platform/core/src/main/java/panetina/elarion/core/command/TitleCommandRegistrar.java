package panetina.elarion.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.TitleDefinition;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static panetina.elarion.core.command.CommandSupport.actorId;
import static panetina.elarion.core.command.CommandSupport.displayCitizen;
import static panetina.elarion.core.command.CommandSupport.suggestTitles;
import static panetina.elarion.core.command.CommandSupport.value;

final class TitleCommandRegistrar {
    private TitleCommandRegistrar() {
    }

    static LiteralArgumentBuilder<ServerCommandSource> register(ElarionApi api) {
        return literal("title")
                .then(literal("set")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("title", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestTitles(api, builder))
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String title = StringArgumentType.getString(context, "title");
                                            var grant = api.titles().grant(player, title, actorId(context.getSource()), "admin-set");
                                            if (!grant.success()) {
                                                context.getSource().sendError(Text.literal(grant.message()));
                                                return 0;
                                            }
                                            var active = api.titles().setActive(player, title, actorId(context.getSource()), "admin-set");
                                            if (!active.success()) {
                                                context.getSource().sendError(Text.literal(active.message()));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Granted and activated title " + title
                                                            + " for " + player.getGameProfile().getName()), true);
                                            return 1;
                                        }))))
                .then(literal("clear")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    api.titles().clearActive(player, actorId(context.getSource()), "admin-clear");
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Cleared active title for "
                                                    + player.getGameProfile().getName()), true);
                                    return 1;
                                })))
                .then(literal("grant")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("title", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestTitles(api, builder))
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String title = StringArgumentType.getString(context, "title");
                                            var result = api.titles().grant(player, title, actorId(context.getSource()), "admin-grant");
                                            if (!result.success()) {
                                                context.getSource().sendError(Text.literal(result.message()));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(() -> Text.literal(result.message()), true);
                                            return 1;
                                        }))))
                .then(literal("revoke")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("title", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestTitles(api, builder))
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String title = StringArgumentType.getString(context, "title");
                                            var result = api.titles().revoke(player, title, actorId(context.getSource()), "admin-revoke");
                                            if (!result.success()) {
                                                context.getSource().sendError(Text.literal(result.message()));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(() -> Text.literal(result.message()), true);
                                            return 1;
                                        }))))
                .then(literal("active")
                        .then(literal("set")
                                .then(argument("player", EntityArgumentType.player())
                                        .then(argument("title", StringArgumentType.word())
                                                .suggests((context, builder) -> suggestTitles(api, builder))
                                                .executes(context -> {
                                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                    String title = StringArgumentType.getString(context, "title");
                                                    var result = api.titles().setActive(player, title,
                                                            actorId(context.getSource()), "admin-active-set");
                                                    if (!result.success()) {
                                                        context.getSource().sendError(Text.literal(result.message()));
                                                        return 0;
                                                    }
                                                    context.getSource().sendFeedback(() -> Text.literal(result.message()), true);
                                                    return 1;
                                                }))))
                        .then(literal("clear")
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            var result = api.titles().clearActive(player,
                                                    actorId(context.getSource()), "admin-active-clear");
                                            context.getSource().sendFeedback(() -> Text.literal(result.message()), true);
                                            return 1;
                                        }))))
                .then(literal("inspect")
                        .then(argument("title", StringArgumentType.word())
                                .suggests((context, builder) -> suggestTitles(api, builder))
                                .executes(context -> {
                                    String titleId = StringArgumentType.getString(context, "title");
                                    TitleDefinition title = api.titles().find(titleId).orElse(null);
                                    if (title == null) {
                                        context.getSource().sendError(Text.literal("Unknown title: " + titleId));
                                        return 0;
                                    }
                                    CommandOutput.header(context.getSource(), "Title");
                                    CommandOutput.kv(context.getSource(), "ID", title.id());
                                    CommandOutput.kv(context.getSource(), "Display", title.displayName());
                                    CommandOutput.kv(context.getSource(), "Acquisition", title.acquisitionMode());
                                    CommandOutput.kv(context.getSource(), "Ownership", title.ownershipMode());
                                    CommandOutput.kv(context.getSource(), "Hidden", title.hiddenFromDiscovery());
                                    CommandOutput.kv(context.getSource(), "Abilities",
                                            title.abilities().isEmpty() ? "(none)" : title.abilities());
                                    if (!title.description().isBlank()) {
                                        CommandOutput.kv(context.getSource(), "Description", title.description());
                                    }
                                    return 1;
                                })))
                .then(literal("player")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    CitizenRecord citizen = api.citizens().getOrCreate(player);
                                    String unlocked = citizen.unlockedTitleIds().stream()
                                            .sorted()
                                            .reduce((left, right) -> left + ", " + right)
                                            .orElse("(none)");
                                    CommandOutput.header(context.getSource(), "Player Titles");
                                    CommandOutput.kv(context.getSource(), "Player", player.getGameProfile().getName());
                                    CommandOutput.kv(context.getSource(), "Active", value(citizen.activeTitleId()));
                                    CommandOutput.kv(context.getSource(), "Unlocked", unlocked);
                                    return 1;
                                })))
                .then(literal("claims")
                        .then(literal("list")
                                .executes(context -> {
                                    Map<String, panetina.elarion.core.storage.TitleClaimStorage.TitleClaim> claims =
                                            api.titles().claims();
                                    if (claims.isEmpty()) {
                                        CommandOutput.empty(context.getSource(), "No unique title claims.");
                                        return 0;
                                    }
                                    CommandOutput.header(context.getSource(), "Unique Title Claims");
                                    claims.entrySet().stream()
                                            .sorted(Map.Entry.comparingByKey())
                                            .forEach(entry -> context.getSource().sendFeedback(
                                                    () -> Text.literal(entry.getKey() + " -> "
                                                            + displayCitizen(api, entry.getValue().owner())),
                                                    false));
                                    return claims.size();
                                }))
                        .then(literal("release")
                                .then(argument("title", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestTitles(api, builder))
                                        .executes(context -> {
                                            String title = StringArgumentType.getString(context, "title");
                                            var result = api.titles().releaseClaim(title,
                                                    actorId(context.getSource()), "admin-release");
                                            if (!result.success()) {
                                                context.getSource().sendError(Text.literal(result.message()));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(() -> Text.literal(result.message()), true);
                                            return 1;
                                        }))))
                .then(literal("repair")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    var result = api.titles().repair(api.citizens().getOrCreate(player),
                                            actorId(context.getSource()));
                                    context.getSource().sendFeedback(() -> Text.literal(result.message()), true);
                                    return 1;
                                })))
                .then(literal("list").executes(context -> {
                    String values = api.titles().all().stream()
                            .map(TitleDefinition::id)
                            .sorted()
                            .reduce((left, right) -> left + ", " + right)
                            .orElse("(none)");
                    CommandOutput.header(context.getSource(), "Titles");
                    CommandOutput.kv(context.getSource(), "Available", values);
                    return 1;
                }));
    }
}
