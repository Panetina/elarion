package panetina.elarion.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionCommandRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

final class HelpCommandRegistrar {
    private HelpCommandRegistrar() {
    }

    static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            ElarionCommandRegistry extensions
    ) {
        dispatcher.register(literal("help")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    List<String> commands = new ArrayList<>();
                    dispatcher.getRoot().getChildren().stream()
                            .filter(node -> node.canUse(source))
                            .map(node -> node.getName())
                            .filter(name -> !name.contains(":"))
                            .distinct()
                            .sorted()
                            .forEach(commands::add);
                    CommandOutput.header(source, "Commands Available");
                    commands.forEach(command -> source.sendFeedback(
                            () -> Text.literal(extensions.helpDescription(command)
                                    .orElseGet(() -> CommandPolicy.description(command))), false));
                    return commands.size();
                })
                .then(argument("command", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            dispatcher.getRoot().getChildren().stream()
                                    .filter(node -> node.canUse(context.getSource()))
                                    .map(node -> node.getName())
                                    .filter(name -> !name.contains(":"))
                                    .sorted()
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            String name = StringArgumentType.getString(context, "command");
                            var node = dispatcher.getRoot().getChild(name);
                            if (node == null || !node.canUse(source)) {
                                source.sendError(Text.literal("Unknown or unavailable command: /" + name));
                                return 0;
                            }
                            CommandOutput.header(source, "/" + name);
                            CommandOutput.line(source, extensions.helpDescription(name)
                                    .orElseGet(() -> CommandPolicy.description(name)));
                            CommandOutput.section(source, "Usage");
                            dispatcher.getSmartUsage(node, source).values().stream()
                                    .sorted(Comparator.naturalOrder())
                                    .forEach(usage -> source.sendFeedback(
                                            () -> Text.literal("/" + name + " " + usage), false));
                            return 1;
                        })));
    }
}
