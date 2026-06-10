package panetina.elarion.tests;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import panetina.elarion.addons.worlds.api.ElarionWorldsApi;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.EconomyItems;
import panetina.elarion.addons.economy.model.EconomyAccount;
import net.minecraft.registry.Registries;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.model.PublicHistoryConsumer;
import panetina.elarion.core.model.PublicHistoryQuery;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.UUID;

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
        ElarionEconomyApi economy = ElarionEconomyApi.get();
        context.assertTrue(Registries.ITEM.containsId(EconomyItems.SIGIL_ID),
                "The Elarion Sigil must be registered");
        context.assertTrue(Registries.ITEM_GROUP.containsId(EconomyItems.ITEM_GROUP_ID),
                "Economy items should have an addon-owned Creative tab");
        UUID economyPlayer = UUID.randomUUID();
        var economyReward = economy.reward(EconomyAccount.player(economyPlayer), 25L, null,
                "GameTest reward", "elarion:gametest");
        context.assertTrue(economyReward.successful(), "Economy reward should succeed");
        context.assertEquals(economy.wallet(economyPlayer), 25L,
                "Economy wallet should reflect the audited reward");

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
        var indexes = api.history().recentIndexes(1);
        context.assertTrue(!indexes.isEmpty(), "Expected a monthly history index");
        context.assertTrue(indexes.getFirst().entries().stream()
                        .anyMatch(entry -> entry.category().equals("gametest")),
                "Expected the monthly history index to include the game-test event");
        api.history().record(new HistoryEvent(
                UUID.randomUUID(),
                previousWeekTimestamp(),
                "realm",
                "chronicle-source",
                null,
                "realm",
                "oak",
                "oak",
                Map.of(),
                "The Realm of Oak entered the Chronicle during a server test."));
        api.publicHistory().generateChronicles();
        context.assertTrue(api.publicHistory().recentChronicles(1).stream()
                        .flatMap(archive -> archive.entries().stream())
                        .anyMatch(entry -> entry.type().equals("chronicle-source")),
                "Expected previous-week events to generate a Chronicle archive");
        var publicHistory = api.publicHistory().query(
                PublicHistoryQuery.forConsumer(PublicHistoryConsumer.GUI_SEARCH)
                        .matchingText("core-services")
                        .limitedTo(5));
        context.assertTrue(publicHistory.entries().stream()
                        .anyMatch(entry -> entry.category().equals("gametest")),
                "Expected public history search to include live indexed events");

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
        var commands = new CommandGameTestSupport(context);
        for (String command : new String[]{"msg", "tell", "teammsg", "tm", "me"}) {
            commands.assertRemoved(command);
        }
        commands.assertRegistered("w");
        commands.assertRegistered("pm");
        commands.assertRegistered("r");
        commands.assertRegistered("yell");
        commands.assertRegistered("help");

        commands.assertPermission("e", 0, false);
        commands.assertPermission("e", 4, true);
        commands.assertPermission("list", 0, false);
        commands.assertPermission("list", 4, true);
        commands.assertPermission("seed", 0, false);
        commands.assertPermission("seed", 4, false);
        commands.assertPermission("random", 0, false);
        commands.assertPermission("random", 4, true);
        commands.assertExecutes("random value 1..10", 4);
        commands.assertExecutes("e history category gametest 1", 4);
        commands.assertExecutes("e history chronicle list 1", 4);
        commands.assertExecutes("e history chronicle inspect " + previousWeekStart() + " 1", 4);
        commands.assertExecutes("e economy treasury get oak", 4);
        commands.assertExecutes("e economy pulse", 4);

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

    private static long previousWeekTimestamp() {
        return previousWeekStart().plusDays(3).atTime(12, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private static LocalDate previousWeekStart() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate currentWeekStart = LocalDate.now(zone)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return currentWeekStart.minusWeeks(1);
    }
}
