package panetina.elarion.addons.angling.fishing;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.component.AnglingBaitDebitCursor;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class AnglingCatchDeliveryServiceTest {
    @Test
    void baitDebitCursorIsMonotonicAndBounded() {
        Identifier baitId = Identifier.ofVanilla("cod");
        AnglingBaitDebitCursor cursor = AnglingBaitDebitCursor.EMPTY.withApplied(baitId, 2);
        assertEquals(2, cursor.applied(baitId));
        assertEquals(0, AnglingBaitDebitCursor.EMPTY.applied(baitId));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> cursor.withApplied(baitId, 1));
    }

    @Test
    void baitDebitActionPrecedesEveryCatchReward() {
        AnglingCatchReward reward = new AnglingCatchReward(
                Optional.of(new AnglingCatchReward.ItemReward(
                        Identifier.ofVanilla("cod"), 1, false, Optional.empty())),
                Optional.empty(), java.util.List.of(),
                Optional.of(new AnglingCatchReward.BaitDebit(
                        Identifier.of("elarion_angling", "elarion_angling_rod"),
                        Identifier.ofVanilla("worm"))));
        AnglingCatchCommit commit = AnglingCatchCommitTestFixtures.commit(reward);

        var actions = AnglingCatchDeliveryService.actions(commit);
        assertEquals(AnglingCatchDeliveryService.BAIT_DEBIT_ACTION, actions.getFirst().type());
        assertEquals(AnglingCatchDeliveryService.ITEM_ACTION, actions.get(1).type());
    }
}
