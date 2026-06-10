package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionTaskServiceTest {
    @Test
    void serverQueueRejectsWhenFull() {
        ElarionTaskService tasks = new ElarionTaskService(
                LoggerFactory.getLogger("test"), 1, 1, 1, 10, TimeUnit.MILLISECONDS.toNanos(1));

        assertTrue(tasks.enqueueServer("first", () -> {}));
        assertFalse(tasks.enqueueServer("second", () -> {}));

        ElarionTaskService.Snapshot snapshot = tasks.snapshot();
        assertEquals(1, snapshot.queuedServerTasks());
        assertEquals(1, snapshot.rejectedServerTasks());
        assertEquals(1L, snapshot.rejectedByFamily().get("second"));

        tasks.shutdown();
    }

    @Test
    void serverQueueAppliesBoundedNumberPerTick() {
        ElarionTaskService tasks = new ElarionTaskService(
                LoggerFactory.getLogger("test"), 1, 1, 10, 2, TimeUnit.MILLISECONDS.toNanos(100));
        AtomicInteger counter = new AtomicInteger();

        tasks.enqueueServer("one", counter::incrementAndGet);
        tasks.enqueueServer("two", counter::incrementAndGet);
        tasks.enqueueServer("three", counter::incrementAndGet);

        tasks.tickServerQueue();

        assertEquals(2, counter.get());
        assertEquals(1, tasks.snapshot().queuedServerTasks());

        tasks.shutdown();
    }

    @Test
    void computeTasksRunOnBackgroundExecutor() throws Exception {
        ElarionTaskService tasks = new ElarionTaskService(
                LoggerFactory.getLogger("test"), 1, 1, 10, 10, TimeUnit.MILLISECONDS.toNanos(1));

        int value = tasks.submitCompute("answer", () -> 42).get(5, TimeUnit.SECONDS);

        assertEquals(42, value);
        ElarionTaskService.Snapshot snapshot = tasks.snapshot();
        assertEquals(1, snapshot.compute().submittedTasks());
        assertEquals(1, snapshot.compute().completedTasks());
        assertEquals(0, snapshot.compute().failedTasks());
        assertEquals(1L, snapshot.compute().completedByFamily().get("answer"));
        tasks.shutdown();
    }

    @Test
    void ioTasksReportFamilyDiagnostics() throws Exception {
        ElarionTaskService tasks = new ElarionTaskService(
                LoggerFactory.getLogger("test"), 1, 1, 10, 10, TimeUnit.MILLISECONDS.toNanos(1));

        tasks.submitIo("history-write:batch", () -> {}).get(5, TimeUnit.SECONDS);

        ElarionTaskService.Snapshot snapshot = tasks.snapshot();
        assertEquals(1, snapshot.io().submittedTasks());
        assertEquals(1, snapshot.io().completedTasks());
        assertEquals(0, snapshot.io().failedTasks());
        assertEquals(1L, snapshot.io().submittedByFamily().get("history-write"));
        assertEquals(1L, snapshot.io().completedByFamily().get("history-write"));
        tasks.shutdown();
    }

    @Test
    void budgetClampsInvalidValues() {
        ElarionTaskService.Budget budget = new ElarionTaskService.Budget(0, -1, 0, -3, 0);

        assertEquals(1, budget.ioWorkers());
        assertEquals(1, budget.computeWorkers());
        assertEquals(1, budget.maxQueuedServerTasks());
        assertEquals(1, budget.maxServerAppliesPerTick());
        assertEquals(1L, budget.maxServerApplyNanos());
    }

    @Test
    void snapshotReportsConfiguredMonitoringAndMaxApplyTime() {
        ElarionTaskConfig.Settings settings = new ElarionTaskConfig.Settings(
                "unknown_online_host",
                "likely",
                new ElarionTaskService.Budget(1, 2, 10, 10, TimeUnit.MILLISECONDS.toNanos(100)),
                new ElarionTaskConfig.Monitoring(
                        TimeUnit.MILLISECONDS.toNanos(45),
                        3,
                        TimeUnit.MILLISECONDS.toNanos(50),
                        30,
                        true,
                        false,
                        TimeUnit.MILLISECONDS.toNanos(35),
                        TimeUnit.MILLISECONDS.toNanos(45),
                        TimeUnit.MILLISECONDS.toNanos(50)
                ),
                false,
                List.of("example warning")
        );
        ElarionTaskService tasks = new ElarionTaskService(LoggerFactory.getLogger("test"), settings);

        tasks.enqueueServer("family:first", () -> {});
        tasks.tickServerQueue();

        ElarionTaskService.Snapshot snapshot = tasks.snapshot();
        assertEquals("unknown_online_host", snapshot.hardwareProfile());
        assertEquals("likely", snapshot.cpuSharingRisk());
        assertEquals(1, snapshot.ioWorkers());
        assertEquals(2, snapshot.computeWorkers());
        assertEquals(3, snapshot.queueWarningThreshold());
        assertTrue(snapshot.maxTickNanos() >= snapshot.lastTickNanos());
        assertTrue(snapshot.worldSamplesEnabled());
        assertFalse(snapshot.realmSamplesEnabled());
        assertEquals(35.0D, snapshot.headroomWarmMillis());
        assertEquals(List.of("example warning"), snapshot.validationWarnings());

        tasks.shutdown();
    }
}
