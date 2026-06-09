package panetina.elarion.addons.worlds.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import panetina.elarion.addons.worlds.config.WorldsConfigException;
import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.service.WorldService;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class WorldCommands {
    private WorldCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> create(WorldService worlds) {
        return literal("world")
                .then(literal("list").executes(context -> list(context.getSource(), worlds)))
                .then(literal("reload").executes(context -> reload(context.getSource(), worlds)))
                .then(literal("load")
                        .then(worldArgument(worlds).executes(context ->
                                load(context.getSource(), worlds,
                                        StringArgumentType.getString(context, "world")))))
                .then(literal("unload")
                        .then(worldArgument(worlds).executes(context ->
                                unload(context.getSource(), worlds,
                                        StringArgumentType.getString(context, "world")))))
                .then(literal("tp")
                        .then(worldArgument(worlds).executes(context ->
                                teleport(context.getSource(), worlds,
                                        StringArgumentType.getString(context, "world")))))
                .then(literal("info")
                        .then(worldArgument(worlds).executes(context ->
                                info(context.getSource(), worlds,
                                        StringArgumentType.getString(context, "world")))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String>
    worldArgument(WorldService worlds) {
        return argument("world", StringArgumentType.word()).suggests((context, builder) ->
                CommandSource.suggestMatching(worlds.definitions().keySet(), builder));
    }

    private static int list(ServerCommandSource source, WorldService worlds) {
        source.sendFeedback(() -> Text.literal("Managed worlds:"), false);
        worlds.definitions().forEach((key, definition) -> source.sendFeedback(() -> Text.literal(
                " - " + key + " (" + definition.id() + "): "
                        + (worlds.isLoaded(definition) ? "loaded" : definition.enabled() ? "pending" : "disabled")),
                false));
        return worlds.definitions().size();
    }

    private static int reload(ServerCommandSource source, WorldService worlds) {
        try {
            worlds.reload();
            source.sendFeedback(() -> Text.literal("Reloaded Elarion Worlds configuration."), true);
            return 1;
        } catch (WorldsConfigException exception) {
            exception.errors().forEach(error -> source.sendError(Text.literal(error)));
            source.sendError(Text.literal("The previous Worlds configuration remains active."));
            return 0;
        } catch (RuntimeException exception) {
            source.sendError(Text.literal("World reload failed: " + exception.getMessage()));
            return 0;
        }
    }

    private static int load(ServerCommandSource source, WorldService worlds, String name) {
        ManagedWorldDefinition definition = worlds.findDefinition(name);
        if (definition == null) return unknown(source, name);
        worlds.open(definition);
        source.sendFeedback(() -> Text.literal("Loaded " + definition.id()), true);
        return 1;
    }

    private static int unload(ServerCommandSource source, WorldService worlds, String name) {
        if (!worlds.unload(name)) {
            source.sendError(Text.literal("World is unknown or not loaded: " + name));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Requested world unload for " + name), true);
        return 1;
    }

    private static int teleport(ServerCommandSource source, WorldService worlds, String name) {
        try {
            if (!worlds.teleport(source.getPlayerOrThrow(), name)) return unknown(source, name);
            source.sendFeedback(() -> Text.literal("Teleported to " + name), false);
            return 1;
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            source.sendError(Text.literal("This command must be run by a player."));
            return 0;
        }
    }

    private static int info(ServerCommandSource source, WorldService worlds, String name) {
        ManagedWorldDefinition definition = worlds.findDefinition(name);
        if (definition == null) return unknown(source, name);
        source.sendFeedback(() -> Text.literal(definition.id()
                + " template=" + definition.template()
                + " seed=" + definition.seed()
                + " loaded=" + worlds.isLoaded(definition)
                + " block-rules=" + definition.blockRules().size()
                + " mob-rules=" + definition.mobRules().size()), false);
        return 1;
    }

    private static int unknown(ServerCommandSource source, String name) {
        source.sendError(Text.literal("Unknown managed world: " + name));
        return 0;
    }
}
