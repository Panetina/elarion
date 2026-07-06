package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConfigApplyAuditJournalTest {
    private static final UUID ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @TempDir
    Path tempDir;

    @Test
    void prepareAndCommitAppendDurablePhases() throws Exception {
        ElarionConfigApplyAuditJournal journal = journal();

        ElarionConfigApplyAuditSession session = journal.prepare(record("old", "new"));
        session.committed();

        List<ElarionConfigApplyAuditJournal.Entry> entries = journal.readTailEntries(10);
        assertEquals(2, entries.size());
        assertEquals(ElarionConfigApplyAuditPhase.PREPARED, entries.get(0).phase());
        assertEquals(ElarionConfigApplyAuditPhase.COMMITTED, entries.get(1).phase());
        assertEquals(entries.get(0).auditId(), entries.get(1).auditId());
        assertEquals(ACTOR, entries.get(0).record().actorId());
        assertEquals("config.apply.test", entries.get(0).record().auditEventType());
        assertTrue(Files.size(journal.file()) > 0L);

        ElarionConfigApplyAuditJournal.Recovery recovery = journal.recoverUnresolvedTail(10);
        assertTrue(recovery.unresolved().isEmpty());
        assertEquals(2, recovery.linesScanned());
        assertFalse(recovery.tailTruncated());
    }

    @Test
    void recoveryReportsPreparedWithoutTerminalOutcome() {
        ElarionConfigApplyAuditJournal journal = journal();

        journal.prepare(record("oak", "spruce"));

        ElarionConfigApplyAuditJournal.Recovery recovery = journal.recoverUnresolvedTail(10);
        assertEquals(1, recovery.unresolved().size());
        ElarionConfigApplyAuditJournal.PendingAudit pending = recovery.unresolved().getFirst();
        assertEquals("oak", pending.record().oldDisplayValue());
        assertEquals("spruce", pending.record().newDisplayValue());
        assertEquals("core:test:enabled", pending.record().target().targetKey());
    }

    @Test
    void terminalRollbackAndFailureClearPendingAudit() {
        ElarionConfigApplyAuditJournal journal = journal();

        ElarionConfigApplyAuditSession rolledBack = journal.prepare(record("one", "two"));
        rolledBack.rolledBack("owner commit failed");
        ElarionConfigApplyAuditSession failed = journal.prepare(record("three", "four"));
        failed.failed("rollback failed");

        List<ElarionConfigApplyAuditJournal.Entry> entries = journal.readTailEntries(10);
        assertEquals(ElarionConfigApplyAuditPhase.ROLLED_BACK, entries.get(1).phase());
        assertEquals("owner commit failed", entries.get(1).failure());
        assertEquals(ElarionConfigApplyAuditPhase.FAILED, entries.get(3).phase());
        assertEquals("rollback failed", entries.get(3).failure());
        assertTrue(journal.recoverUnresolvedTail(10).unresolved().isEmpty());
    }

    @Test
    void recoveryIsBoundedToRequestedTail() {
        ElarionConfigApplyAuditJournal journal = journal();

        ElarionConfigApplyAuditSession first = journal.prepare(record("a", "b"));
        first.committed();
        ElarionConfigApplyAuditSession second = journal.prepare(record("c", "d"));
        second.committed();
        journal.prepare(record("pending", "value"));

        ElarionConfigApplyAuditJournal.Recovery recovery = journal.recoverUnresolvedTail(2);

        assertEquals(1, recovery.unresolved().size());
        assertEquals("pending", recovery.unresolved().getFirst().record().oldDisplayValue());
        assertEquals(2, recovery.linesScanned());
        assertTrue(recovery.tailTruncated());
    }

    @Test
    void sessionRejectsSecondTerminalOutcome() {
        ElarionConfigApplyAuditJournal journal = journal();
        ElarionConfigApplyAuditSession session = journal.prepare(record("old", "new"));

        session.committed();

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> session.rolledBack("too late"));
        assertTrue(failure.getMessage().contains("already closed"));
        assertEquals(2, journal.readTailEntries(10).size());
    }

    @Test
    void appendFailurePropagatesBeforeSessionIsReturned() throws Exception {
        Path occupiedParent = tempDir.resolve("occupied");
        Files.writeString(occupiedParent, "not a directory", StandardCharsets.UTF_8);
        ElarionConfigApplyAuditJournal journal = new ElarionConfigApplyAuditJournal(
                occupiedParent.resolve("config-changes.jsonl"));

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> journal.prepare(record("old", "new")));

        assertTrue(failure.getMessage().contains("Failed to append config audit journal"));
    }

    @Test
    void journalPathUsesCoreAuditDirectory() {
        assertEquals(
                tempDir.resolve("core").resolve("audit").resolve("config-changes.jsonl"),
                ElarionConfigApplyAuditJournal.journalPath(tempDir));
    }

    private ElarionConfigApplyAuditJournal journal() {
        return new ElarionConfigApplyAuditJournal(
                ElarionConfigApplyAuditJournal.journalPath(tempDir));
    }

    private static ElarionConfigApplyAuditRecord record(String oldValue, String newValue) {
        return new ElarionConfigApplyAuditRecord(
                new ElarionConfigEditTarget("core", "test", "enabled"),
                ACTOR,
                "test reason",
                oldValue,
                newValue,
                true,
                false,
                "config.apply.test",
                List.of("config/elarion/core/test.yml"));
    }
}
