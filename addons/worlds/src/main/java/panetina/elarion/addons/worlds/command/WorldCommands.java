package panetina.elarion.addons.worlds.command;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import panetina.elarion.addons.worlds.config.WorldsConfigException;
import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.model.WorldType;
import panetina.elarion.addons.worlds.service.WorldService;
import panetina.elarion.core.command.CommandOutput;

import java.util.Arrays;
import java.util.Locale;

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
                        .then(destinationArgument(worlds).executes(context ->
                                teleport(context.getSource(), worlds,
                                        StringArgumentType.getString(context, "destination")))))
                .then(literal("info")
                        .then(worldArgument(worlds).executes(context ->
                                info(context.getSource(), worlds,
                                        StringArgumentType.getString(context, "world")))))
                .then(createNode(worlds))
                .then(removeNode(worlds));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> createNode(WorldService worlds) {
        return literal("create").then(createArguments(worlds));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?> createArguments(
            WorldService worlds
    ) {
        return argument("name", StringArgumentType.word())
                .then(argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> CommandSource.suggestMatching(
                                Arrays.stream(WorldType.values()).map(type ->
                                        type.name().toLowerCase(Locale.ROOT)), builder))
                        .executes(context -> createWorld(
                                context.getSource(),
                                worlds,
                                StringArgumentType.getString(context, "name"),
                                StringArgumentType.getString(context, "type"),
                                System.currentTimeMillis()))
                        .then(argument("seed", LongArgumentType.longArg())
                                .executes(context -> createWorld(
                                        context.getSource(),
                                        worlds,
                                        StringArgumentType.getString(context, "name"),
                                        StringArgumentType.getString(context, "type"),
                                        LongArgumentType.getLong(context, "seed")))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> removeNode(WorldService worlds) {
        return literal("remove").then(removeArguments(worlds));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<ServerCommandSource, ?> removeArguments(
            WorldService worlds
    ) {
        return worldArgument(worlds)
                .then(literal("confirm").executes(context -> removeWorld(
                        context.getSource(),
                        worlds,
                        StringArgumentType.getString(context, "world"))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String>
    worldArgument(WorldService worlds) {
        return argument("world", StringArgumentType.word()).suggests((context, builder) ->
                CommandSource.suggestMatching(worlds.definitions().keySet(), builder));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String>
    destinationArgument(WorldService worlds) {
        return argument("destination", StringArgumentType.word()).suggests((context, builder) ->
                CommandSource.suggestMatching(worlds.destinationNames(), builder));
    }

    private static int list(ServerCommandSource source, WorldService worlds) {
        CommandOutput.header(source, "Managed Worlds");
        worlds.definitions().forEach((key, definition) -> CommandOutput.bullet(source,
                key + " | " + definition.id()
                        + " | type=" + definition.type()
                        + " | state=" + (worlds.isLoaded(definition)
                        ? "loaded"
                        : definition.enabled() ? "pending" : "disabled")));
        CommandOutput.section(source, "Teleport Destinations");
        CommandOutput.line(source, worlds.destinationNames().isEmpty()
                ? "(none)"
                : String.join(", ", worlds.destinationNames()));
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

    private static int createWorld(
            ServerCommandSource source, WorldService worlds, String name, String typeName, long seed
    ) {
        try {
            WorldType type = WorldType.parse(typeName);
            ManagedWorldDefinition definition = worlds.create(name, type, seed);
            source.sendFeedback(() -> Text.literal("Created " + definition.id()
                    + " as " + type + " with seed " + seed), true);
            return 1;
        } catch (IllegalArgumentException exception) {
            source.sendError(Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static int removeWorld(ServerCommandSource source, WorldService worlds, String name) {
        if (!worlds.remove(name)) {
            source.sendError(Text.literal("Unknown world or protected lobby: " + name));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Deletion requested for " + name), true);
        return 1;
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
            if (!worlds.teleport(source.getPlayerOrThrow(), name)) {
                source.sendError(Text.literal("Unknown destination: " + name));
                return 0;
            }
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
        CommandOutput.header(source, "Managed World");
        CommandOutput.kv(source, "ID", definition.id());
        CommandOutput.kv(source, "Type", definition.type());
        CommandOutput.kv(source, "Template", definition.template());
        CommandOutput.kv(source, "Seed", definition.seed());
        CommandOutput.kv(source, "Loaded", worlds.isLoaded(definition));
        CommandOutput.kv(source, "World border", definition.border().size());
        CommandOutput.kv(source, "Block rules", definition.blockRules().size());
        CommandOutput.kv(source, "Mob rules", definition.mobRules().size());
        return 1;
    }

    private static int unknown(ServerCommandSource source, String name) {
        source.sendError(Text.literal("Unknown managed world: " + name));
        return 0;
    }
}
