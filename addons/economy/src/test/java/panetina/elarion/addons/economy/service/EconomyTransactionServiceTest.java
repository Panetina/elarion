package panetina.elarion.addons.economy.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.economy.config.EconomyConfig;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyGovernorMode;
import panetina.elarion.addons.economy.model.EconomyTransaction;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.TransactionStatus;
import panetina.elarion.addons.economy.storage.EconomyStorage;
import panetina.elarion.core.service.ElarionTaskService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EconomyTransactionServiceTest {
    private static final EconomyConfig CONFIG = new EconomyConfig(
            1, 300_000L, false, 12, 100,
            EconomyGovernorMode.MONITOR_ONLY, 7, 10_000);

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
}
