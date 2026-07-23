package panetina.elarion.core.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CommandPolicyTest {
    @Test
    void disablesVanillaSayAlongsideOtherBypassCommands() {
        assertTrue(CommandPolicy.disabledCommands().contains("say"));
    }
}
