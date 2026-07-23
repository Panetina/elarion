package panetina.elarion.core.api;

import org.junit.jupiter.api.Test;

import static net.minecraft.server.command.CommandManager.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionCommandRegistryTest {
    @Test
    void rootCommandsRemainSeparateFromAdminSubcommands() {
        ElarionCommandRegistry registry = new ElarionCommandRegistry();
        registry.registerRootCommand(() -> literal("banish"));
        registry.registerAdminSubcommand(() -> literal("death"));

        assertEquals("banish", registry.rootCommands().getFirst().get().getLiteral());
        assertEquals("death", registry.adminCommands().getFirst().get().getLiteral());
    }
}
