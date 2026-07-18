package panetina.elarion.tests;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.TestContext;

final class CommandGameTestSupport {
    private final TestContext context;
    private final MinecraftServer server;
    private final CommandDispatcher<ServerCommandSource> dispatcher;

    CommandGameTestSupport(TestContext context) {
        this.context = context;
        this.server = context.getWorld().getServer();
        this.dispatcher = server.getCommandManager().getDispatcher();
    }

    void assertRegistered(String command) {
        context.assertTrue(root().getChild(command) != null,
                "/" + command + " should be registered");
    }

    void assertRemoved(String command) {
        context.assertTrue(root().getChild(command) == null,
                "/" + command + " should be removed from the command tree");
    }

    void assertPermission(String command, int level, boolean expected) {
        CommandNode<ServerCommandSource> node = root().getChild(command);
        context.assertTrue(node != null, "/" + command + " should exist before permission checks");
        boolean actual = node.canUse(server.getCommandSource().withLevel(level));
        context.assertEquals(expected, actual,
                "/" + command + " permission mismatch for level " + level);
    }

    void assertExecutes(String command, int level) {
        try {
            int result = dispatcher.execute(command, server.getCommandSource().withLevel(level));
            context.assertTrue(result > 0, "/" + command + " should execute successfully");
        } catch (CommandSyntaxException exception) {
            throw new AssertionError("/" + command + " should execute successfully", exception);
        }
    }

    void assertDispatches(String command, int level) {
        try {
            dispatcher.execute(command, server.getCommandSource().withLevel(level));
        } catch (CommandSyntaxException exception) {
            throw new AssertionError("/" + command + " should dispatch without a syntax failure", exception);
        }
    }

    void assertFails(String command, int level) {
        try {
            int result = dispatcher.execute(command, server.getCommandSource().withLevel(level));
            context.assertTrue(result <= 0, "/" + command + " should fail");
        } catch (CommandSyntaxException exception) {
            // Brigadier parse/permission failures are valid command failure outcomes.
        }
    }

    private CommandNode<ServerCommandSource> root() {
        return dispatcher.getRoot();
    }
}
