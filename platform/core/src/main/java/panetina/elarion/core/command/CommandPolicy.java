package panetina.elarion.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import panetina.elarion.core.mixin.CommandNodeAccessor;

import java.util.Properties;
import java.util.Set;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.minecraft.server.command.CommandManager.literal;

public final class CommandPolicy {
    private static final Set<String> DISABLED_COMMANDS = Set.of(
            "msg", "tell", "teammsg", "tm", "me");
    private static final Set<String> OP_ONLY_COMMANDS = Set.of("random", "seed");
    private CommandPolicy() {}

    public static void applyVanillaPolicy(CommandDispatcher<ServerCommandSource> dispatcher) {
        DISABLED_COMMANDS.forEach(command -> remove(dispatcher, command));
        OP_ONLY_COMMANDS.forEach(command -> requireOperatorLevelFour(dispatcher, command));
        remove(dispatcher, "w");
        remove(dispatcher, "list");
        remove(dispatcher, "help");
    }

    public static String description(String command) {
        return "/" + command + " - Available server command.";
    }

    public static Set<String> disabledCommands() {
        return DISABLED_COMMANDS;
    }

    private static void requireOperatorLevelFour(
            CommandDispatcher<ServerCommandSource> dispatcher,
            String command
    ) {
        CommandNode<ServerCommandSource> vanilla = dispatcher.getRoot().getChild(command);
        if (vanilla == null) return;
        remove(dispatcher, command);
        dispatcher.register(literal(command)
                .requires(source -> source.hasPermissionLevel(4)
                        && (!command.equals("seed") || showSeedEnabled()))
                .redirect(vanilla));
    }

    private static boolean showSeedEnabled() {
        Path propertiesPath = Path.of("server.properties");
        if (Files.notExists(propertiesPath)) return false;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(propertiesPath, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return Boolean.parseBoolean(properties.getProperty("show-seed", "false"));
        } catch (IOException ignored) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static void remove(
            CommandDispatcher<ServerCommandSource> dispatcher,
            String command
    ) {
        CommandNodeAccessor<ServerCommandSource> root =
                (CommandNodeAccessor<ServerCommandSource>) (Object) dispatcher.getRoot();
        root.elarion$children().remove(command);
        root.elarion$literals().remove(command);
        root.elarion$arguments().remove(command);
    }
}
