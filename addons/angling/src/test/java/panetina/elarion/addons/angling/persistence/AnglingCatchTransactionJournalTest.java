package panetina.elarion.addons.angling.persistence;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.addons.angling.fishing.AnglingCatchCommit;
import panetina.elarion.addons.angling.fishing.AnglingCatchCommitFactory;
import panetina.elarion.addons.angling.fishing.AnglingCatchReward;
import panetina.elarion.core.metric.MetricScope;
import panetina.elarion.core.metric.MetricUpdate;
import panetina.elarion.core.metric.MetricUpdateBatch;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingCatchTransactionJournalTest {
    @Test
    void roundTripsStagesAndCompactsOnlyIncompleteTransactions(@TempDir Path root) throws Exception {
        AnglingCatchTransactionJournal journal = new AnglingCatchTransactionJournal();
        AnglingCatchCommit first = commit(UUID.randomUUID(), UUID.randomUUID(), 1);
        AnglingCatchCommit second = commit(UUID.randomUUID(), UUID.randomUUID(), 1);

        journal.appendRequest(root, first);
        journal.appendStage(root, first.telemetry().eventId(), AnglingCatchTransactionJournal.Stage.PROJECTED);
        journal.appendRequest(root, second);
        journal.appendStage(root, second.telemetry().eventId(), AnglingCatchTransactionJournal.Stage.PROJECTED);
        journal.appendStage(root, second.telemetry().eventId(), AnglingCatchTransactionJournal.Stage.DELIVERED);

        Map<UUID, AnglingCatchTransactionJournal.Pending> pending = journal.loadPending(root);
        assertEquals(Set.of(first.telemetry().eventId()), pending.keySet());
        assertEquals(AnglingCatchTransactionJournal.Stage.PROJECTED,
                pending.get(first.telemetry().eventId()).stage());

        journal.compact(root, pending);
        assertEquals(pending, journal.loadPending(root));
        assertEquals(2, Files.readAllLines(AnglingCatchTransactionJournal.file(root)).size());
    }

    @Test
    void rejectsRequestedAsAStageAndOversizedInput(@TempDir Path root) throws Exception {
        AnglingCatchTransactionJournal journal = new AnglingCatchTransactionJournal();
        assertThrows(IllegalArgumentException.class, () -> journal.appendStage(
                root, UUID.randomUUID(), AnglingCatchTransactionJournal.Stage.REQUESTED));

        Path file = AnglingCatchTransactionJournal.file(root);
        Files.createDirectories(file.getParent());
        Files.writeString(file, "x".repeat(AnglingCatchTransactionJournal.MAX_LINE_CHARACTERS + 1),
                StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> journal.loadPending(root));
    }

    private static AnglingCatchCommit commit(UUID event, UUID actor, long sequence) {
        Identifier source = AnglingCatchCommitFactory.SOURCE;
        Identifier fish = Identifier.of("elarion_angling", "journal_fish");
        long occurredAt = 1_780_000_000_000L;
        CatchTelemetryEvent telemetry = new CatchTelemetryEvent(
                event, occurredAt, actor, source, fish, Identifier.of("elarion_angling", "common"), 1,
                Identifier.ofVanilla("overworld"), Identifier.ofVanilla("overworld"),
                Identifier.ofVanilla("plains"), Map.of());
        MetricUpdate update = new MetricUpdate(Identifier.of("elarion_angling", "catch/count"),
                panetina.elarion.core.metric.MetricOperation.ADD, 1, Set.of(MetricScope.global()), Map.of());
        MetricUpdateBatch metrics = new MetricUpdateBatch(source,
                AnglingCatchCommitFactory.sourcePartition(actor), sequence, event, actor, occurredAt, null,
                List.of(update));
        return new AnglingCatchCommit(telemetry, metrics,
                new AnglingCatchReward(java.util.Optional.of(new AnglingCatchReward.ItemReward(
                        Identifier.of("elarion_angling", "starcaught_bucket"), 1, true,
                        java.util.Optional.of(new AnglingCatchReward.ContainedItem(
                                Identifier.ofVanilla("cod"), 1, true)))),
                        java.util.Optional.of(new AnglingCatchReward.EntityReward(
                                Identifier.ofVanilla("creeper"), 12.5, 64, -8.5)),
                        List.of(new AnglingCatchReward.ItemReward(
                                Identifier.ofVanilla("name_tag"), 1, false, java.util.Optional.empty(),
                                "bounded-exact-stack")),
                        java.util.Optional.of(new AnglingCatchReward.BaitDebit(
                                Identifier.of("elarion_angling", "elarion_angling_rod"),
                                Identifier.ofVanilla("cod")))));
    }
}
