package panetina.elarion.core.api;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public final class ElarionCommandRegistry {
    private final List<Supplier<LiteralArgumentBuilder<ServerCommandSource>>> rootCommands =
            new CopyOnWriteArrayList<>();
    private final List<Supplier<LiteralArgumentBuilder<ServerCommandSource>>> adminCommands =
            new CopyOnWriteArrayList<>();
    private final List<Supplier<LiteralArgumentBuilder<ServerCommandSource>>> testCommands =
            new CopyOnWriteArrayList<>();
    private final Map<String, String> helpDescriptions = new ConcurrentHashMap<>();

    public void registerRootCommand(Supplier<LiteralArgumentBuilder<ServerCommandSource>> command) {
        rootCommands.add(command);
    }

    public List<Supplier<LiteralArgumentBuilder<ServerCommandSource>>> rootCommands() {
        return List.copyOf(rootCommands);
    }

    public void registerAdminSubcommand(Supplier<LiteralArgumentBuilder<ServerCommandSource>> command) {
        adminCommands.add(command);
    }

    public List<Supplier<LiteralArgumentBuilder<ServerCommandSource>>> adminCommands() {
        return List.copyOf(adminCommands);
    }

    public void registerTestSubcommand(Supplier<LiteralArgumentBuilder<ServerCommandSource>> command) {
        testCommands.add(command);
    }

    public List<Supplier<LiteralArgumentBuilder<ServerCommandSource>>> testCommands() {
        return List.copyOf(testCommands);
    }

    public void registerHelpDescription(String command, String description) {
        helpDescriptions.put(command, description);
    }

    public Optional<String> helpDescription(String command) {
        return Optional.ofNullable(helpDescriptions.get(command));
    }
}
