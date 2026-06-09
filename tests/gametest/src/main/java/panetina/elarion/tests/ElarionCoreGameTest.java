package panetina.elarion.tests;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import panetina.elarion.addons.worlds.api.ElarionWorldsApi;
import panetina.elarion.core.api.ElarionApi;

import java.util.Map;

public final class ElarionCoreGameTest implements FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void coreApiLoadsInsideMinecraftServer(TestContext context) {
        ElarionApi api = ElarionApi.get();

        context.assertTrue(api.realms().all().iterator().hasNext(),
                "Elarion Core should load configured realms in a game-test server");
        context.assertTrue(api.nicknames() != null,
                "Nickname policy API should be available");
        context.assertTrue(api.history() != null,
                "History API should be available");

        api.history().record(
                "gametest",
                "history-roundtrip",
                null,
                "test",
                "core-services",
                "",
                Map.of("source", "fabric-gametest")
        );

        var events = api.history().forCategory("gametest", 1);
        context.assertTrue(!events.isEmpty(), "Expected the recorded history event");
        context.assertEquals("history-roundtrip", events.getFirst().type(),
                "History event type did not round-trip");

        ElarionWorldsApi worlds = ElarionWorldsApi.get();
        context.assertEquals(worlds.definitions().size(), 4,
                "Expected the lobby and three default managed world definitions");
        context.assertTrue(worlds.resolve("lobby") != null,
                "lobby destination should resolve");
        context.assertTrue(worlds.resolve("realm_world_1") != null,
                "realm_world_1 should be loaded");
        context.assertTrue(worlds.resolve("realm_world_2") != null,
                "realm_world_2 should be loaded");
        context.assertTrue(worlds.resolve("realm_world_3") != null,
                "realm_world_3 should be loaded");

        var server = context.getWorld().getServer();
        var commandRoot = server.getCommandManager().getDispatcher().getRoot();
        for (String command : new String[]{"msg", "tell", "teammsg", "tm", "me"}) {
            context.assertTrue(commandRoot.getChild(command) == null,
                    "/" + command + " should be removed from the command tree");
        }
        context.assertTrue(commandRoot.getChild("w") != null,
                "/w should be registered");
        context.assertTrue(commandRoot.getChild("r") != null,
                "/r should be registered");
        context.assertTrue(commandRoot.getChild("help") != null,
                "/help should be registered");

        var playerSource = server.getCommandSource().withLevel(0);
        var operatorSource = server.getCommandSource().withLevel(4);
        context.assertTrue(!commandRoot.getChild("list").canUse(playerSource),
                "/list should reject non-operators");
        context.assertTrue(commandRoot.getChild("list").canUse(operatorSource),
                "/list should allow permission level 4");
        context.assertTrue(!commandRoot.getChild("seed").canUse(playerSource),
                "/seed should reject non-operators");
        context.assertTrue(!commandRoot.getChild("seed").canUse(operatorSource),
                "/seed should remain unavailable when show-seed is not true");

        var lobby = worlds.resolve("lobby");
        var realmWorld = worlds.resolve("realm_world_1");
        double lobbySize = lobby.getWorldBorder().getSize();
        double realmSize = realmWorld.getWorldBorder().getSize();
        server.getCommandManager().executeWithPrefix(
                server.getCommandSource().withWorld(realmWorld).withLevel(4),
                "worldborder set 321");
        context.assertEquals(realmWorld.getWorldBorder().getSize(), 321.0,
                "The command should change the source world's border");
        context.assertEquals(lobby.getWorldBorder().getSize(), lobbySize,
                "Changing a realm border must not change the lobby border");
        realmWorld.getWorldBorder().setSize(realmSize);
        context.complete();
    }
}
