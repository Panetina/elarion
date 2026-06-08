package panetina.elarion.core.api;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public final class ElarionCommandRegistry {
    private final List<Supplier<LiteralArgumentBuilder<ServerCommandSource>>> adminCommands =
            new CopyOnWriteArrayList<>();

    public void registerAdminSubcommand(Supplier<LiteralArgumentBuilder<ServerCommandSource>> command) {
        adminCommands.add(command);
    }

    public List<Supplier<LiteralArgumentBuilder<ServerCommandSource>>> adminCommands() {
        return List.copyOf(adminCommands);
    }
}
