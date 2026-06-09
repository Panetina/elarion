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

        context.assertTrue(api.communities().all().iterator().hasNext(),
                "Elarion Core should load configured communities in a game-test server");
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
        context.assertTrue(worlds.resolve("community_world_1") != null,
                "community_world_1 should be loaded");
        context.assertTrue(worlds.resolve("community_world_2") != null,
                "community_world_2 should be loaded");
        context.assertTrue(worlds.resolve("community_world_3") != null,
                "community_world_3 should be loaded");

        var server = context.getWorld().getServer();
        var lobby = worlds.resolve("lobby");
        var communityWorld = worlds.resolve("community_world_1");
        double lobbySize = lobby.getWorldBorder().getSize();
        double communitySize = communityWorld.getWorldBorder().getSize();
        server.getCommandManager().executeWithPrefix(
                server.getCommandSource().withWorld(communityWorld).withLevel(4),
                "worldborder set 321");
        context.assertEquals(communityWorld.getWorldBorder().getSize(), 321.0,
                "The command should change the source world's border");
        context.assertEquals(lobby.getWorldBorder().getSize(), lobbySize,
                "Changing a community border must not change the lobby border");
        communityWorld.getWorldBorder().setSize(communitySize);
        context.complete();
    }
}
