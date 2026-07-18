package panetina.elarion.addons.economy.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.economy.config.EconomyConfig;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyGovernorMode;
import panetina.elarion.addons.economy.model.EconomyOperationKey;
import panetina.elarion.addons.economy.model.EconomyTransaction;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.TransactionStatus;
import panetina.elarion.addons.economy.storage.EconomyState;
import panetina.elarion.addons.economy.storage.EconomyStorage;
import panetina.elarion.core.service.ElarionTaskService;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.service.WorldheartGovernanceService;
import panetina.elarion.core.storage.WorldheartAuthorityStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EconomyTransactionServiceTest {
    private static final EconomyConfig CONFIG = new EconomyConfig(
            1, 300_000L, false, 2_592_000_000L, 10_000, 12, 100,
            EconomyGovernorMode.MONITOR_ONLY, 7, 10_000,
            false, 86_400_000L, 25, 100L, 1L, 100, 0, 0);

    @TempDir
    Path root;

    private final List<ElarionTaskService> taskServices = new ArrayList<>();

    @AfterEach
    void shutdownTasks() {
        taskServices.forEach(ElarionTaskService::shutdown);
    }

    @Test
    void recordsExplicitAuditFieldsAndPreservesBalancesOnRejectedTransfer() {
        List<EconomyTransaction> history = new ArrayList<>();
        EconomyTransactionService service = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), history);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        var reward = service.execute(EconomyTransactionType.REWARD,
                EconomyAccount.MINT, EconomyAccount.player(first), 100L, null,
                "Test reward", "elarion:test", Map.of("test", "true"));
        var transfer = service.execute(EconomyTransactionType.TRANSFER,
                EconomyAccount.player(first), EconomyAccount.player(second), 30L, first,
                "Test transfer", "elarion:test", Map.of());
        var rejected = service.execute(EconomyTransactionType.TRANSFER,
                EconomyAccount.player(first), EconomyAccount.player(second), 100L, first,
                "Rejected transfer", "elarion:test", Map.of());

        assertTrue(reward.successful());
        assertTrue(transfer.successful());
        assertFalse(rejected.successful());
        assertEquals(TransactionStatus.INSUFFICIENT_FUNDS, rejected.status());
        assertEquals(70L, service.balance(EconomyAccount.player(first)));
        assertEquals(30L, service.balance(EconomyAccount.player(second)));

        EconomyTransaction recorded = transfer.transaction();
        assertEquals(EconomyTransactionType.TRANSFER, recorded.type());
        assertEquals(EconomyAccount.player(first), recorded.fromAccount());
        assertEquals(EconomyAccount.player(second), recorded.toAccount());
        assertEquals(first, recorded.actor());
        assertEquals("elarion:test", recorded.sourceSystem());
        assertEquals(100L, recorded.fromBalanceBefore());
        assertEquals(70L, recorded.fromBalanceAfter());
        assertEquals(0L, recorded.toBalanceBefore());
        assertEquals(30L, recorded.toBalanceAfter());
        assertNotNull(rejected.transaction());
        assertFalse(rejected.transaction().success());
        assertEquals(70L, rejected.transaction().fromBalanceBefore());
        assertEquals(70L, rejected.transaction().fromBalanceAfter());
        var invalidAmount = service.execute(EconomyTransactionType.FEE,
                EconomyAccount.player(first), EconomyAccount.BURN, 0L, first,
                "Invalid fee", "elarion:test", Map.of());
        assertEquals(TransactionStatus.INVALID_AMOUNT, invalidAmount.status());
        assertNotNull(invalidAmount.transaction());
        assertFalse(invalidAmount.transaction().success());
        assertEquals(0L, invalidAmount.transaction().amount());
        var invalidFlow = service.execute(EconomyTransactionType.TRANSFER,
                EconomyAccount.MINT, EconomyAccount.player(first), 10L, first,
                "Mislabeled mint", "elarion:test", Map.of());
        assertEquals(TransactionStatus.INVALID_TYPE_FLOW, invalidFlow.status());
        assertFalse(invalidFlow.transaction().success());
        assertEquals(70L, service.balance(EconomyAccount.player(first)));
        assertEquals(2, history.size(), "Only successful transactions should enter Core history");
    }

    @Test
    void journalReplaysAfterRestartAndShutdownSnapshotRoundTrips() {
        UUID player = UUID.randomUUID();
        EconomyStorage storage = new EconomyStorage(LoggerFactory.getLogger("economy-test"), root);
        EconomyTransactionService first = service(storage, new ArrayList<>());
        assertTrue(first.reward(EconomyAccount.player(player), 75L, null,
                "Restart reward", "elarion:test").successful());

        EconomyTransactionService replayed = service(
                new EconomyStorage(LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        assertEquals(75L, replayed.balance(EconomyAccount.player(player)));
        replayed.shutdown();

        EconomyTransactionService snapshotted = service(
                new EconomyStorage(LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        assertEquals(75L, snapshotted.balance(EconomyAccount.player(player)));
    }

    @Test
    void idempotentOperationReplaysWithoutChargingTwiceAndRejectsConflicts() {
        EconomyTransactionService service = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        UUID player = UUID.randomUUID();
        EconomyOperationKey operation = new EconomyOperationKey("elarion_npcs:purchase", UUID.randomUUID());
        assertTrue(service.reward(EconomyAccount.player(player), 100L, null,
                "Seed", "elarion:test").successful());

        var first = service.executeOnce(operation, EconomyTransactionType.FEE,
                EconomyAccount.player(player), EconomyAccount.BURN, 25L, player,
                "NPC purchase", "elarion_npcs", Map.of("offer", "stone"));
        var replay = service.executeOnce(operation, EconomyTransactionType.FEE,
                EconomyAccount.player(player), EconomyAccount.BURN, 25L, player,
                "NPC purchase", "elarion_npcs", Map.of("offer", "stone"));
        var conflict = service.executeOnce(operation, EconomyTransactionType.FEE,
                EconomyAccount.player(player), EconomyAccount.BURN, 30L, player,
                "NPC purchase", "elarion_npcs", Map.of("offer", "stone"));

        assertTrue(first.successful());
        assertTrue(replay.successful());
        assertEquals(first.transaction().id(), replay.transaction().id());
        assertEquals(75L, service.balance(EconomyAccount.player(player)));
        assertEquals(TransactionStatus.IDEMPOTENCY_CONFLICT, conflict.status());
        assertEquals(first.transaction().id(), service.receipt(operation).orElseThrow().transaction().id());
    }

    @Test
    void rejectedIdempotentOperationAlsoReplaysDeterministically() {
        EconomyTransactionService service = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        UUID player = UUID.randomUUID();
        EconomyOperationKey operation = new EconomyOperationKey("elarion_npcs:purchase", UUID.randomUUID());

        var first = service.executeOnce(operation, EconomyTransactionType.FEE,
                EconomyAccount.player(player), EconomyAccount.BURN, 25L, player,
                "NPC purchase", "elarion_npcs", Map.of());
        assertEquals(TransactionStatus.INSUFFICIENT_FUNDS, first.status());

        assertTrue(service.reward(EconomyAccount.player(player), 100L, null,
                "Later seed", "elarion:test").successful());
        var replay = service.executeOnce(operation, EconomyTransactionType.FEE,
                EconomyAccount.player(player), EconomyAccount.BURN, 25L, player,
                "NPC purchase", "elarion_npcs", Map.of());

        assertEquals(TransactionStatus.INSUFFICIENT_FUNDS, replay.status());
        assertEquals(first.transaction().id(), replay.transaction().id());
        assertEquals(100L, service.balance(EconomyAccount.player(player)));
    }

    @Test
    void operationReceiptRebuildsFromJournalBeforeSnapshotAndSurvivesSnapshotRestart() {
        UUID player = UUID.randomUUID();
        EconomyOperationKey operation = new EconomyOperationKey("elarion_npcs:purchase", UUID.randomUUID());
        EconomyTransactionService first = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        assertTrue(first.reward(EconomyAccount.player(player), 80L, null,
                "Seed", "elarion:test").successful());
        var charged = first.executeOnce(operation, EconomyTransactionType.FEE,
                EconomyAccount.player(player), EconomyAccount.BURN, 20L, player,
                "NPC purchase", "elarion_npcs", Map.of("offer", "ticket"));
        assertTrue(charged.successful());

        EconomyTransactionService journalReplayed = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        var replay = journalReplayed.executeOnce(operation, EconomyTransactionType.FEE,
                EconomyAccount.player(player), EconomyAccount.BURN, 20L, player,
                "NPC purchase", "elarion_npcs", Map.of("offer", "ticket"));
        assertEquals(charged.transaction().id(), replay.transaction().id());
        assertEquals(60L, journalReplayed.balance(EconomyAccount.player(player)));

        journalReplayed.shutdown();
        EconomyTransactionService snapshotted = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        assertEquals(charged.transaction().id(),
                snapshotted.receipt(operation).orElseThrow().transaction().id());
        assertEquals(60L, snapshotted.balance(EconomyAccount.player(player)));
    }

    @Test
    void rewardOnceCreditsPlayerWalletIdempotentlyAndSurvivesRestart() {
        UUID player = UUID.randomUUID();
        EconomyOperationKey operation = new EconomyOperationKey("elarion_npcs:trade_sell", UUID.randomUUID());
        EconomyTransactionService first = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());

        var payout = first.rewardOnce(operation, EconomyAccount.player(player), 45L, player,
                "NPC trade sale payout", "elarion_npcs", Map.of("offer", "cobblestone"));
        var replay = first.rewardOnce(operation, EconomyAccount.player(player), 45L, player,
                "NPC trade sale payout", "elarion_npcs", Map.of("offer", "cobblestone"));
        var conflict = first.rewardOnce(operation, EconomyAccount.player(player), 46L, player,
                "NPC trade sale payout", "elarion_npcs", Map.of("offer", "cobblestone"));

        assertTrue(payout.successful());
        assertTrue(replay.successful());
        assertEquals(payout.transaction().id(), replay.transaction().id());
        assertEquals(TransactionStatus.IDEMPOTENCY_CONFLICT, conflict.status());
        assertEquals(45L, first.balance(EconomyAccount.player(player)));
        first.shutdown();

        EconomyTransactionService restarted = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        var restartReplay = restarted.rewardOnce(operation, EconomyAccount.player(player), 45L, player,
                "NPC trade sale payout", "elarion_npcs", Map.of("offer", "cobblestone"));

        assertTrue(restartReplay.successful());
        assertEquals(payout.transaction().id(), restartReplay.transaction().id());
        assertEquals(45L, restarted.balance(EconomyAccount.player(player)));
    }

    @Test
    void schemaOneStateMigratesWithBackupAndPreservesBalances() throws Exception {
        UUID player = UUID.randomUUID();
        Path stateFile = root.resolve("economy-state.json");
        Files.writeString(stateFile, """
                {
                  "schemaVersion": 1,
                  "lastAppliedSequence": 0,
                  "wallets": {"%s": 42},
                  "treasuries": {"oak": 9}
                }
                """.formatted(player), StandardCharsets.UTF_8);

        EconomyStorage storage = new EconomyStorage(LoggerFactory.getLogger("economy-test"), root);
        var migrated = storage.load(null);

        assertEquals(42L, migrated.wallets().get(player.toString()));
        assertEquals(9L, migrated.treasuries().get("oak"));
        assertTrue(migrated.operationReceipts().isEmpty());
        assertTrue(Files.exists(root.resolve("economy-state.json.schema-v1.bak")));
        assertTrue(Files.readString(stateFile).contains("\"schemaVersion\": 3"));
    }

    @Test
    void schemaTwoStateMigratesWithDedicatedWorldheartTreasury() throws Exception {
        Path stateFile = root.resolve("economy-state.json");
        Files.writeString(stateFile, """
                {
                  "schemaVersion": 2,
                  "lastAppliedSequence": 0,
                  "wallets": {},
                  "treasuries": {"oak": 9},
                  "operationReceipts": {}
                }
                """, StandardCharsets.UTF_8);

        EconomyState migrated = new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root).load(null);

        assertEquals(9L, migrated.treasuries().get("oak"));
        assertEquals(0L, migrated.worldheartTreasury());
        assertTrue(Files.exists(root.resolve("economy-state.json.schema-v2.bak")));
        assertTrue(Files.readString(stateFile).contains("\"schemaVersion\": 3"));
    }

    @Test
    void operationReceiptIndexEvictsOldestEntryAtConfiguredBound() {
        EconomyConfig bounded = new EconomyConfig(
                1, 300_000L, false, 2_592_000_000L, 100, 12, 100,
                EconomyGovernorMode.MONITOR_ONLY, 7, 10_000,
                false, 86_400_000L, 25, 100L, 1L, 100, 0, 0);
        EconomyTransactionService service = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>(), bounded);
        UUID player = UUID.randomUUID();
        List<EconomyOperationKey> operations = new ArrayList<>();

        for (int index = 0; index < 101; index++) {
            EconomyOperationKey operation = new EconomyOperationKey("elarion_test:reward", UUID.randomUUID());
            operations.add(operation);
            assertTrue(service.executeOnce(operation, EconomyTransactionType.REWARD,
                    EconomyAccount.MINT, EconomyAccount.player(player), 1L, null,
                    "Bounded reward", "elarion_test", Map.of("index", Integer.toString(index))).successful());
        }

        assertTrue(service.receipt(operations.getFirst()).isEmpty());
        assertTrue(service.receipt(operations.getLast()).isPresent());
        assertEquals(101L, service.balance(EconomyAccount.player(player)));
    }

    @Test
    void unsupportedStateSchemaFailsClosedWithoutReplacingFile() throws Exception {
        Path stateFile = root.resolve("economy-state.json");
        String unsupported = "{\"schemaVersion\":99,\"wallets\":{},\"treasuries\":{}}";
        Files.writeString(stateFile, unsupported, StandardCharsets.UTF_8);
        EconomyStorage storage = new EconomyStorage(LoggerFactory.getLogger("economy-test"), root);

        assertThrows(IllegalStateException.class, () -> storage.load(null));
        assertEquals(unsupported, Files.readString(stateFile));
    }

    @Test
    void recentQueriesReturnNewestMatchesWithoutChangingBalances() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        EconomyTransactionService service = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());

        assertTrue(service.reward(EconomyAccount.player(first), 10L, null,
                "First reward", "elarion:test").successful());
        assertTrue(service.reward(EconomyAccount.player(second), 20L, null,
                "Second reward", "elarion:test").successful());
        assertTrue(service.sink(EconomyAccount.player(second), 5L, second,
                "Second sink", "elarion:test").successful());

        List<EconomyTransaction> recent = service.recent(transaction -> true, 2);

        assertEquals(2, recent.size());
        assertEquals("Second sink", recent.get(0).reason());
        assertEquals("Second reward", recent.get(1).reason());
        assertEquals(10L, service.balance(EconomyAccount.player(first)));
        assertEquals(15L, service.balance(EconomyAccount.player(second)));
    }

    @Test
    void failedJournalWriteCannotChangeBalances() {
        EconomyStorage failing = new EconomyStorage(LoggerFactory.getLogger("economy-test"), root) {
            @Override
            public void append(
                    net.minecraft.server.MinecraftServer server,
                    EconomyTransaction transaction,
                    boolean force
            ) throws IOException {
                throw new IOException("expected test failure");
            }
        };
        EconomyTransactionService service = service(failing, new ArrayList<>());
        UUID player = UUID.randomUUID();

        var result = service.reward(EconomyAccount.player(player), 50L, null,
                "Failed reward", "elarion:test");

        assertEquals(TransactionStatus.PERSISTENCE_FAILED, result.status());
        assertEquals(0L, service.balance(EconomyAccount.player(player)));
    }

    @Test
    void treasuryGrantMovesRealmCurrencyToPlayerAndSinkBurnsThem() {
        List<EconomyTransaction> history = new ArrayList<>();
        EconomyTransactionService service = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), history);
        UUID player = UUID.randomUUID();

        var treasuryReward = service.reward(EconomyAccount.realm("oak"), 200L, null,
                "Treasury seed", "elarion:test");
        var grant = service.execute(EconomyTransactionType.TREASURY_GRANT,
                EconomyAccount.realm("oak"), EconomyAccount.player(player), 75L, player,
                "Public grant", "elarion:test", Map.of());
        var sink = service.sink(EconomyAccount.player(player), 25L, player,
                "Fee sink", "elarion:test");

        assertTrue(treasuryReward.successful());
        assertTrue(grant.successful());
        assertTrue(sink.successful());
        assertEquals(125L, service.balance(EconomyAccount.realm("oak")));
        assertEquals(50L, service.balance(EconomyAccount.player(player)));
        assertEquals(EconomyTransactionType.TREASURY_GRANT, grant.transaction().type());
        assertEquals(EconomyTransactionType.SINK, sink.transaction().type());
        assertEquals(3, history.size());
    }

    @Test
    void worldheartAndRealmTaxesRouteToSeparatePersistentTreasuries() {
        EconomyTransactionService service = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        UUID player = UUID.randomUUID();
        assertTrue(service.reward(EconomyAccount.player(player), 100L, null,
                "Seed", "elarion:test").successful());

        assertTrue(service.execute(EconomyTransactionType.TAX,
                EconomyAccount.player(player), EconomyAccount.WORLDHEART_TREASURY, 15L, player,
                "Worldheart tax", "elarion:test", Map.of()).successful());
        assertTrue(service.execute(EconomyTransactionType.TAX,
                EconomyAccount.player(player), EconomyAccount.realm("oak"), 10L, player,
                "Realm tax", "elarion:test", Map.of()).successful());

        assertEquals(15L, service.balance(EconomyAccount.WORLDHEART_TREASURY));
        assertEquals(10L, service.balance(EconomyAccount.realm("oak")));
        assertEquals(75L, service.balance(EconomyAccount.player(player)));
        service.shutdown();

        EconomyTransactionService restarted = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        assertEquals(15L, restarted.balance(EconomyAccount.WORLDHEART_TREASURY));
        assertEquals(10L, restarted.balance(EconomyAccount.realm("oak")));
    }

    @Test
    void physicalPublicRevenueCreditsStableTreasuriesIdempotently() {
        EconomyTransactionService service = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root), new ArrayList<>());
        UUID actor = UUID.randomUUID();
        EconomyOperationKey operation = new EconomyOperationKey("elarion_npcs:trade_buy", UUID.randomUUID());

        var first = service.executeOnce(operation, EconomyTransactionType.PUBLIC_REVENUE,
                EconomyAccount.PHYSICAL_CURRENCY, EconomyAccount.WORLDHEART_TREASURY, 27L, actor,
                "NPC trade purchase", "elarion_npcs", Map.of("subtotal", "25", "tax", "2"));
        var replay = service.executeOnce(operation, EconomyTransactionType.PUBLIC_REVENUE,
                EconomyAccount.PHYSICAL_CURRENCY, EconomyAccount.WORLDHEART_TREASURY, 27L, actor,
                "NPC trade purchase", "elarion_npcs", Map.of("subtotal", "25", "tax", "2"));
        var realmRevenue = service.execute(EconomyTransactionType.PUBLIC_REVENUE,
                EconomyAccount.PHYSICAL_CURRENCY, EconomyAccount.realm("oak"), 10L, actor,
                "Realm trade purchase", "elarion_npcs", Map.of());

        assertTrue(first.successful());
        assertTrue(replay.successful());
        assertEquals(first.transaction().id(), replay.transaction().id());
        assertTrue(realmRevenue.successful());
        assertEquals(27L, service.balance(EconomyAccount.WORLDHEART_TREASURY));
        assertEquals(10L, service.balance(EconomyAccount.realm("oak")));
    }

    @Test
    void changingWorldheartAuthorityDoesNotMoveTreasuryIntoRulerWallet() {
        UUID ruler = UUID.randomUUID();
        EconomyTransactionService economy = service(new EconomyStorage(
                LoggerFactory.getLogger("economy-test"), root.resolve("economy")), new ArrayList<>());
        assertTrue(economy.reward(EconomyAccount.WORLDHEART_TREASURY, 250L, null,
                "Treasury seed", "elarion:test").successful());
        WorldheartGovernanceService governance = new WorldheartGovernanceService(
                new WorldheartAuthorityStorage(LoggerFactory.getLogger("worldheart-test"), root.resolve("authority")),
                ruler::equals, id -> java.util.Optional.of("Ruler"), new ElarionEventBus());
        governance.bind(null);

        governance.setPlayerAuthority(ruler, null);

        assertEquals(250L, economy.balance(EconomyAccount.WORLDHEART_TREASURY));
        assertEquals(0L, economy.balance(EconomyAccount.player(ruler)));
        governance.setSystemAuthority(null);
        assertEquals(250L, economy.balance(EconomyAccount.WORLDHEART_TREASURY));
        assertEquals(0L, economy.balance(EconomyAccount.player(ruler)));
    }

    @Test
    void bankInterestRunsInBoundedBatchesWhenEnabled() {
        EconomyConfig interestConfig = new EconomyConfig(
                1, 300_000L, false, 2_592_000_000L, 10_000, 12, 100,
                EconomyGovernorMode.MONITOR_ONLY, 7, 10_000,
                true, 0L, 1_000, 50L, 1L, 1, 0, 0);
        EconomyTransactionService service = service(
                new EconomyStorage(LoggerFactory.getLogger("economy-test"), root),
                new ArrayList<>(),
                interestConfig);
        UUID first = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID second = UUID.fromString("00000000-0000-0000-0000-000000000002");
        assertTrue(service.reward(EconomyAccount.player(first), 100L, null,
                "Seed first", "elarion:test").successful());
        assertTrue(service.reward(EconomyAccount.player(second), 100L, null,
                "Seed second", "elarion:test").successful());

        service.tick();

        assertEquals(110L, service.balance(EconomyAccount.player(first)));
        assertEquals(100L, service.balance(EconomyAccount.player(second)));

        service.tick();

        assertEquals(110L, service.balance(EconomyAccount.player(first)));
        assertEquals(110L, service.balance(EconomyAccount.player(second)));
    }

    private EconomyTransactionService service(
            EconomyStorage storage,
            List<EconomyTransaction> history
    ) {
        ElarionTaskService tasks = new ElarionTaskService(
                LoggerFactory.getLogger("economy-task-test"), 1, 1, 32, 8, 1_000_000L);
        taskServices.add(tasks);
        EconomyTransactionService service = new EconomyTransactionService(
                LoggerFactory.getLogger("economy-test"), storage, CONFIG,
                realm -> realm.equals("oak"), history::add, tasks);
        service.bind(null);
        return service;
    }

    private EconomyTransactionService service(
            EconomyStorage storage,
            List<EconomyTransaction> history,
            EconomyConfig config
    ) {
        ElarionTaskService tasks = new ElarionTaskService(
                LoggerFactory.getLogger("economy-task-test"), 1, 1, 32, 8, 1_000_000L);
        taskServices.add(tasks);
        EconomyTransactionService service = new EconomyTransactionService(
                LoggerFactory.getLogger("economy-test"), storage, config,
                realm -> realm.equals("oak"), history::add, tasks);
        service.bind(null);
        return service;
    }
}
