package panetina.elarion.addons.underworld.command;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class BanishCommandsTest {
    @Test
    void registersTimedPermanentListAndReleasePaths() {
        CommandNode<ServerCommandSource> banish = BanishCommands.banish(null).build();
        assertNotNull(banish.getChild("list"));
        CommandNode<ServerCommandSource> player = banish.getChild("player");
        assertNotNull(player);
        assertNotNull(player.getChild("permanent").getChild("reason"));
        assertNotNull(player.getChild("minutes").getChild("reason"));

        CommandNode<ServerCommandSource> unbanish = BanishCommands.unbanish(null).build();
        assertNotNull(unbanish.getChild("player"));
    }
}
