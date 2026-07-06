package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ElarionConfigPreparedChangeTest {
    @Test
    void commitsOnceAndRollsBackIdempotently() {
        AtomicInteger commits = new AtomicInteger();
        AtomicInteger rollbacks = new AtomicInteger();
        ElarionConfigChangeResult expected = appliedResult();
        ElarionConfigPreparedChange change = ElarionConfigPreparedChange.of(() -> {
            commits.incrementAndGet();
            return expected;
        }, rollbacks::incrementAndGet);

        assertEquals(expected, change.commit());
        assertEquals(1, commits.get());
        assertThrows(IllegalStateException.class, change::commit);

        change.rollback();
        change.rollback();
        assertEquals(1, rollbacks.get());
    }

    @Test
    void rollbackBeforeCommitPreventsCommit() {
        AtomicInteger rollbacks = new AtomicInteger();
        ElarionConfigPreparedChange change = ElarionConfigPreparedChange.of(
                this::appliedResult, rollbacks::incrementAndGet);

        change.rollback();
        change.rollback();

        assertEquals(1, rollbacks.get());
        assertThrows(IllegalStateException.class, change::commit);
    }

    @Test
    void failedCommitCanStillRollback() {
        AtomicInteger rollbacks = new AtomicInteger();
        ElarionConfigPreparedChange change = ElarionConfigPreparedChange.of(
                () -> { throw new IllegalStateException("commit failed"); },
                rollbacks::incrementAndGet);

        assertThrows(IllegalStateException.class, change::commit);
        change.rollback();
        assertEquals(1, rollbacks.get());
    }

    private ElarionConfigChangeResult appliedResult() {
        ElarionConfigChangeRequest request = new ElarionConfigChangeRequest(
                "core", "general", "enabled", "false", "true",
                UUID.fromString("00000000-0000-0000-0000-000000000001"), "test");
        return ElarionConfigChangeResult.applied(
                request, "true", "false", true, false, "admin-config-applied");
    }
}
