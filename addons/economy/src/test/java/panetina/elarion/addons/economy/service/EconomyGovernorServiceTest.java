package panetina.elarion.addons.economy.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.economy.config.EconomyConfig;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyGovernorMode;
import panetina.elarion.addons.economy.model.EconomyHealth;
import panetina.elarion.addons.economy.storage.EconomyStorage;
import panetina.elarion.core.service.ElarionTaskService;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EconomyGovernorServiceTest {
    @TempDir
    Path root;
    private ElarionTaskService tasks;

    @AfterEach
    void shutdown() {
        if (tasks != null) tasks.shutdown();
    }

    @Test
    void monitorOnlyPulseUsesAuditedFaucetsAndSinks() {
        EconomyConfig config = new EconomyConfig(
                1, 300_000L, false, 2_592_000_000L, 10_000, 12, 100,
                EconomyGovernorMode.MONITOR_ONLY, 7, 10_000,
                false, 86_400_000L, 25, 100L, 1L, 100, 0, 0);
        tasks = new ElarionTaskService(
                LoggerFactory.getLogger("economy-task-test"), 1, 1, 32, 8, 1_000_000L);
        EconomyTransactionService transactions = new EconomyTransactionService(
                LoggerFactory.getLogger("economy-test"),
                new EconomyStorage(LoggerFactory.getLogger("economy-test"), root),
                config, realm -> true, transaction -> { }, tasks);
        transactions.bind(null);
        UUID player = UUID.randomUUID();
        transactions.reward(EconomyAccount.player(player), 100L, null, "Reward", "elarion:test");
        transactions.sink(EconomyAccount.player(player), 100L, null, "Sink", "elarion:test");

        var pulse = new EconomyGovernorService(transactions).pulse();

        assertEquals(EconomyGovernorMode.MONITOR_ONLY, pulse.mode());
        assertEquals(EconomyHealth.HEALTHY, pulse.health());
        assertEquals(100L, pulse.createdInWindow());
        assertEquals(100L, pulse.destroyedInWindow());
        assertEquals(0L, pulse.trackedSupply());
    }
}
