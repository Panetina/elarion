package panetina.elarion.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.api.ElarionCommandRegistry;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

final class PlayerCommandRegistrar {
    private PlayerCommandRegistrar() {
    }

    static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            ElarionApi api,
            CoreConfigManager config,
            ElarionCommandRegistry extensions
    ) {
        registerHelpDescriptions(config, extensions);
        registerChat(dispatcher, api);
        registerList(dispatcher, api);
        registerSpy(dispatcher, api);
        HelpCommandRegistrar.register(dispatcher, extensions);
    }

    private static void registerHelpDescriptions(CoreConfigManager config, ElarionCommandRegistry extensions) {
        extensions.registerHelpDescription("help",
                "/help [command] - Show commands available to you.");
        extensions.registerHelpDescription("rc",
                "/rc <message> - Send a message to members of your Realm.");
        extensions.registerHelpDescription("ac",
                "/ac <message> - Send a message to your Realm and allied Realms.");
        extensions.registerHelpDescription("r",
                "/r <message> - Reply to the last player who privately messaged you.");
        extensions.registerHelpDescription("w",
                "/w <message> - Whisper to players within "
                        + config.whisperChatRadius() + " blocks.");
        extensions.registerHelpDescription("pm",
                "/pm <player> <message> - Privately message a citizen in your Realm or a GLOBAL Realm.");
        extensions.registerHelpDescription("yell",
                "/yell <message> - Yell to players within " + config.yellChatRadius()
                        + " blocks; " + config.yellChatCooldownSeconds() + "-second cooldown.");
        extensions.registerHelpDescription("list",
                "/list - List online players. Requires OP level 4.");
        extensions.registerHelpDescription("spy",
                "/spy chat - Toggle OP-only chat moderation view. /spy is a modular admin namespace.");
    }

    private static void registerChat(CommandDispatcher<ServerCommandSource> dispatcher, ElarionApi api) {
        dispatcher.register(literal("rc")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> api.chat().sendRealmMessage(
                                context.getSource().getPlayerOrThrow(),
                                StringArgumentType.getString(context, "message")) ? 1 : 0)));

        dispatcher.register(literal("ac")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> api.chat().sendAllianceMessage(
                                context.getSource().getPlayerOrThrow(),
                                StringArgumentType.getString(context, "message")) ? 1 : 0)));

        dispatcher.register(literal("pm")
                .then(argument("player", EntityArgumentType.player())
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(context -> api.privateMessages().privateMessage(
                                        context.getSource().getPlayerOrThrow(),
                                        EntityArgumentType.getPlayer(context, "player"),
                                        StringArgumentType.getString(context, "message")) ? 1 : 0))));

        dispatcher.register(literal("w")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> api.chat().sendWhisperMessage(
                                context.getSource().getPlayerOrThrow(),
                                StringArgumentType.getString(context, "message")) ? 1 : 0)));

        dispatcher.register(literal("yell")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> api.chat().sendYellMessage(
                                context.getSource().getPlayerOrThrow(),
                                StringArgumentType.getString(context, "message")) ? 1 : 0)));

        dispatcher.register(literal("r")
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(context -> api.privateMessages().reply(
                                context.getSource().getPlayerOrThrow(),
                                StringArgumentType.getString(context, "message")) ? 1 : 0)));
    }

    private static void registerList(CommandDispatcher<ServerCommandSource> dispatcher, ElarionApi api) {
        dispatcher.register(literal("list")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(context -> {
                    List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
                    CommandOutput.header(context.getSource(), "Online Players");
                    CommandOutput.kv(context.getSource(), "Total", players.size());
                    Map<String, List<String>> grouped = new LinkedHashMap<>();
                    players.stream()
                            .sorted(Comparator.comparing(player ->
                                    String.valueOf(api.citizens().getOrCreate(player).realmId())))
                            .forEach(player -> {
                                CitizenRecord citizen = api.citizens().getOrCreate(player);
                                String group = api.realms().forCitizen(citizen)
                                        .map(api.realms()::shortName)
                                        .orElse("UNASSIGNED");
                                grouped.computeIfAbsent(group, ignored -> new ArrayList<>())
                                        .add(api.identities().resolve(player).displayName().getString());
                            });
                    grouped.forEach((realm, names) -> {
                        names.sort(String.CASE_INSENSITIVE_ORDER);
                        CommandOutput.section(context.getSource(), realm + " (" + names.size() + ")");
                        CommandOutput.line(context.getSource(), String.join(", ", names));
                    });
                    return players.size();
                }));
    }

    private static void registerSpy(CommandDispatcher<ServerCommandSource> dispatcher, ElarionApi api) {
        dispatcher.register(literal("spy")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("chat")
                        .executes(context -> api.chat().toggleChatSpy(
                                context.getSource().getPlayerOrThrow()) ? 1 : 0)));
    }
}
