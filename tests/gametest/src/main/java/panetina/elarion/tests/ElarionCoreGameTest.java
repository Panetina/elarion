package panetina.elarion.tests;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import panetina.elarion.addons.offerings.OfferingsBlocks;
import panetina.elarion.addons.worlds.api.ElarionWorldsApi;
import panetina.elarion.addons.offerings.api.ElarionOfferingsApi;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.EconomyItems;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.npcs.api.ElarionNpcApi;
import panetina.elarion.addons.government.api.ElarionGovernmentApi;
import panetina.elarion.addons.government.GovernmentBlocks;
import panetina.elarion.addons.groups.api.ElarionGroupsApi;
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
        String realmId = api.realms().all().iterator().next().id();
        context.assertTrue(api.nicknames() != null,
                "Nickname policy API should be available");
        context.assertTrue(api.history() != null,
                "History API should be available");
        ElarionEconomyApi economy = ElarionEconomyApi.get();
        context.assertTrue(Registries.ITEM.containsId(EconomyItems.CURRENCY_ID),
                "The Elarion CURRENCY must be registered");
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
                realmId,
                realmId,
                Map.of(),
                "The Realm " + realmId + " entered the Chronicle during a server test."));
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
        commands.assertRegistered("group");
        commands.assertRegistered("gc");
        commands.assertRegistered("lc");
        commands.assertRegistered("banish");
        commands.assertRegistered("unbanish");

        commands.assertPermission("e", 0, false);
        commands.assertPermission("e", 4, true);
        commands.assertPermission("list", 0, false);
        commands.assertPermission("list", 4, true);
        commands.assertPermission("seed", 0, false);
        commands.assertPermission("seed", 4, false);
        commands.assertPermission("random", 0, false);
        commands.assertPermission("random", 4, true);
        commands.assertPermission("banish", 0, false);
        commands.assertPermission("banish", 4, true);
        commands.assertPermission("unbanish", 0, false);
        commands.assertPermission("unbanish", 4, true);
        commands.assertDispatches("banish list", 4);
        commands.assertExecutes("random value 1..10", 4);
        commands.assertExecutes("e history category gametest 1", 4);
        commands.assertExecutes("e history chronicle list 1", 4);
        commands.assertExecutes("e history chronicle inspect " + previousWeekStart() + " 1", 4);
        commands.assertExecutes("e economy treasury get " + realmId, 4);
        commands.assertExecutes("e economy treasury give " + realmId + " 5", 4);
        commands.assertExecutes("e economy transactions realm " + realmId + " 5", 4);
        commands.assertExecutes("e economy pulse", 4);
        commands.assertFails("e economy pulse", 0);

        ElarionOfferingsApi offerings = ElarionOfferingsApi.get();
        context.assertTrue(Registries.BLOCK.containsId(OfferingsBlocks.SHRINE_OF_FOUNDATION_ID),
                "Shrine of Foundation block must be registered");
        context.assertTrue(Registries.ITEM.containsId(OfferingsBlocks.SHRINE_OF_FOUNDATION_ITEM_ID),
                "Shrine of Foundation item must be registered");
        context.assertTrue(Registries.ITEM_GROUP.containsId(OfferingsBlocks.ITEM_GROUP_ID),
                "offering items should have an addon-owned Creative tab");
        context.assertTrue(offerings.definition("council_hall").isPresent(),
                "Expected default Council Hall offering project");
        commands.assertExecutes("e offerings reload", 4);
        commands.assertExecutes("e offerings projects", 4);
        commands.assertExecutes("e offerings inspect council_hall", 4);
        commands.assertExecutes("e offerings start realm " + realmId + " council_hall", 4);
        var offeringInstance = offerings.instances().stream()
                .filter(instance -> instance.projectId().equals("council_hall"))
                .findFirst()
                .orElseThrow();
        commands.assertExecutes("e offerings state " + offeringInstance.id(), 4);
        commands.assertExecutes("e offerings complete " + offeringInstance.id(), 4);
        commands.assertExecutes("e offerings delete " + offeringInstance.id(), 4);
        context.assertTrue(offerings.instances().stream()
                        .noneMatch(instance -> instance.id().equals(offeringInstance.id())),
                "Deleted offering instance must no longer be available");
        commands.assertFails("e offerings projects", 0);
        commands.assertFails("e contributions projects", 4);

        ElarionNpcApi npcs = ElarionNpcApi.get();
        context.assertTrue(npcs.definitions().npc("worldheart_banker").isPresent(),
                "Expected default Worldheart Banker NPC definition");
        commands.assertExecutes("e npc reload", 4);
        commands.assertDispatches("e npc list", 4);
        commands.assertDispatches("e npc repair all", 4);
        commands.assertExecutes("e npc dialogue inspect worldheart_banker", 4);
        commands.assertFails("e npc list", 0);
        commands.assertFails("e npc inspect missing_npc", 4);
        commands.assertFails("e npc remove missing_npc", 4);

        commands.assertExecutes("e portal list", 4);
        commands.assertExecutes("e portal guide nether", 4);
        commands.assertExecutes("e portal inspect nether", 4);
        commands.assertExecutes("e portal repair nether", 4);
        commands.assertFails("e portal list", 0);
        commands.assertFails("e portal endpoint set nether a_gate", 4);
        commands.assertFails("e portal guide missing_route", 4);

        ElarionGroupsApi groups = ElarionGroupsApi.get();
        context.assertTrue(groups.all() != null, "Groups API should be available");
        commands.assertExecutes("e groups reload", 4);
        commands.assertExecutes("e groups list", 4);
        commands.assertFails("e groups list", 0);
        commands.assertFails("e groups inspect missing_group", 4);
        commands.assertFails("group info", 4);
        commands.assertFails("gc hello", 4);
        commands.assertFails("lc hello", 4);

        ElarionGovernmentApi government = ElarionGovernmentApi.get();
        context.assertTrue(Registries.BLOCK.containsId(GovernmentBlocks.CIVIC_FORUM_ID),
                "Civic Forum block must be registered");
        context.assertTrue(Registries.BLOCK.containsId(GovernmentBlocks.SEAT_OF_RULE_ID),
                "Seat of Rule block must be registered");
        context.assertTrue(Registries.ITEM_GROUP.containsId(GovernmentBlocks.ITEM_GROUP_ID),
                "Government blocks should have an addon-owned Creative tab");
        context.assertTrue(government.definitions().form("republic").isPresent(),
                "Expected default Republic government form");
        context.assertTrue(government.definitions().form("monarchy").isPresent(),
                "Expected default Monarchy government form");
        context.assertTrue(government.definitions().form("theocracy").isEmpty(),
                "Theocracy should not be an active government form");
        context.assertTrue(government.definitions().form("confederation").isEmpty(),
                "Confederation should not be an active government form");
        commands.assertExecutes("e government reload", 4);
        commands.assertExecutes("e government forms", 4);
        commands.assertExecutes("e government inspect republic", 4);
        commands.assertFails("e government inspect confederation", 4);
        commands.assertExecutes("e government state " + realmId, 4);
        commands.assertExecutes("e government audience " + realmId, 4);
        commands.assertExecutes("e government laws " + realmId, 4);
        commands.assertExecutes("e government gates " + realmId, 4);
        commands.assertExecutes("e government identity set " + realmId + " OAK Oak", 4);
        government.states().setVotedColor(realmId, "gold");
        commands.assertExecutes("e government state " + realmId, 4);
        commands.assertExecutes("e government gates " + realmId, 4);
        commands.assertExecutes("e government set-form " + realmId + " republic", 4);
        UUID president = UUID.randomUUID();
        var presidentCitizen = api.citizens().getOrCreate(president, "GameTestPresident");
        presidentCitizen.setRealmId(realmId);
        api.citizens().save(presidentCitizen, "gametest-office-holder");
        government.states().assignOffice(realmId, "president", president);
        commands.assertExecutes("e government founding complete " + realmId, 4);
        commands.assertExecutes("e government authority cleanup", 4);
        context.assertEquals("republic", government.states().realm(realmId).activeGovernmentFormId(),
                "OP dev government form command should update government addon state only");
        context.assertEquals("Oak", government.states().realm(realmId).votedDisplayName(),
                "OP dev identity command should update Government identity overlay only");
        context.assertEquals("gold", government.states().realm(realmId).votedColor(),
                "Government color vote state should update Realm presentation");
        context.assertTrue(government.states().realm(realmId).officeHolders()
                        .getOrDefault("president", java.util.Set.of()).contains(president),
                "Government office assignment should persist in Realm government state");
        context.assertTrue(government.gates(realmId).foundingElectionComplete(),
                "OP dev founding command should mark founding election complete");
        commands.assertFails("e government forms", 0);
        commands.assertFails("e government inspect missing_form", 4);
        commands.assertFails("e government proposal inspect " + realmId + " missing_proposal", 4);
        commands.assertFails("e government law archive " + realmId + " missing_law", 4);
        commands.assertFails("e government law restore " + realmId + " missing_law", 4);
        commands.assertFails("e government office assign " + realmId + " president missing_player", 4);
        commands.assertFails("e test government advance " + realmId, 4);
        commands.assertExecutes("e government reset " + realmId, 4);
        commands.assertFails("e government reset " + realmId, 0);
        context.assertEquals("", government.states().realm(realmId).activeGovernmentFormId(),
                "Government admin reset should clear the active form for only the targeted Realm");
        commands.assertExecutes("e test government reset " + realmId, 4);
        commands.assertFails("e test government reset " + realmId, 0);
        context.assertEquals("", government.states().realm(realmId).activeGovernmentFormId(),
                "Government test reset should clear the active form for only the targeted Realm");

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
