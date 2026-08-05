package panetina.elarion.addons.angling.fishing;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.addons.angling.persistence.AnglingBaitDebitLedger;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AnglingBaitDebitLedgerTest {
    @Test
    void exactRetryAndRestartDoNotDuplicateCanonicalDebit(@TempDir Path root) throws Exception {
        Identifier baitId = Identifier.ofVanilla("cod");
        AnglingCatchReward reward = new AnglingCatchReward(
                Optional.of(new AnglingCatchReward.ItemReward(
                        Identifier.ofVanilla("salmon"), 1, false, Optional.empty())),
                Optional.empty(), java.util.List.of(),
                Optional.of(new AnglingCatchReward.BaitDebit(
                        Identifier.of("elarion_angling", "elarion_angling_rod"), baitId)));
        UUID actorId = UUID.randomUUID();
        AnglingCatchCommit first = AnglingCatchCommitTestFixtures.commit(
                reward, actorId, UUID.randomUUID(), 1);
        AnglingCatchCommit second = AnglingCatchCommitTestFixtures.commit(
                reward, actorId, UUID.randomUUID(), 2);

        AnglingBaitDebitLedger ledger = new AnglingBaitDebitLedger();
        ledger.bind(root);
        assertEquals(1, ledger.record(first));
        assertEquals(2, ledger.record(second));
        assertEquals(2, ledger.record(second));
        assertEquals(2, ledger.totals(actorId).get(baitId));
        ledger.shutdown();

        AnglingBaitDebitLedger restored = new AnglingBaitDebitLedger();
        restored.bind(root);
        assertEquals(2, restored.totals(actorId).get(baitId));
        restored.shutdown();
    }

    @Test
    void uncheckpointedAppendReplaysOnceAfterCrashRestart(@TempDir Path root) throws Exception {
        Identifier baitId = Identifier.ofVanilla("worm");
        AnglingCatchReward reward = new AnglingCatchReward(
                Optional.of(new AnglingCatchReward.ItemReward(
                        Identifier.ofVanilla("cod"), 1, false, Optional.empty())),
                Optional.empty(), java.util.List.of(),
                Optional.of(new AnglingCatchReward.BaitDebit(
                        Identifier.of("elarion_angling", "elarion_angling_rod"), baitId)));
        UUID actorId = UUID.randomUUID();
        AnglingCatchCommit commit = AnglingCatchCommitTestFixtures.commit(
                reward, actorId, UUID.randomUUID(), 1);

        AnglingBaitDebitLedger beforeCrash = new AnglingBaitDebitLedger();
        beforeCrash.bind(root);
        assertEquals(1, beforeCrash.record(commit));

        AnglingBaitDebitLedger afterRestart = new AnglingBaitDebitLedger();
        afterRestart.bind(root);
        assertEquals(1, afterRestart.totals(actorId).get(baitId));
        assertEquals(1, afterRestart.record(commit));
        assertEquals(1, afterRestart.totals(actorId).get(baitId));
        afterRestart.shutdown();
    }
}
