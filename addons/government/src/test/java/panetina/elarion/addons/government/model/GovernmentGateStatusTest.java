package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentGateStatusTest {
    @Test
    void gatesFollowFoundationAndFoundingOrder() {
        GovernmentGateStatus beforeFoundation = new GovernmentGateStatus(
                "realm1", false, false, false, false, false, false, false);

        assertTrue(beforeFoundation.nameVoteVisible());
        assertFalse(beforeFoundation.nameVoteUnlocked());
        assertFalse(beforeFoundation.governmentChoicesVisible());

        GovernmentGateStatus afterNameBeforeColor = new GovernmentGateStatus(
                "realm1", true, false, false, true, false, false, false);

        assertTrue(afterNameBeforeColor.colorVoteVisible());
        assertTrue(afterNameBeforeColor.colorVoteUnlocked());
        assertFalse(afterNameBeforeColor.governmentChoicesVisible());

        GovernmentGateStatus afterColorAndFoundationTwo = new GovernmentGateStatus(
                "realm1", true, true, false, true, true, false, false);

        assertTrue(afterColorAndFoundationTwo.governmentChoicesVisible());
        assertTrue(afterColorAndFoundationTwo.governmentVoteUnlocked());
        assertFalse(afterColorAndFoundationTwo.foundingElectionUnlocked());

        GovernmentGateStatus readyForElection = new GovernmentGateStatus(
                "realm1", true, true, true, true, true, true, false);

        assertTrue(readyForElection.foundingElectionUnlocked());
        assertFalse(readyForElection.seatOfRuleUnlocked());

        GovernmentGateStatus founded = new GovernmentGateStatus(
                "realm1", true, true, true, true, true, true, true);

        assertFalse(founded.foundingElectionUnlocked());
        assertTrue(founded.seatOfRuleUnlocked());
    }

    @Test
    void lockMessagesNameTheBlockingFoundationLevel() {
        GovernmentGateStatus noFoundation = new GovernmentGateStatus(
                "realm1", false, false, false, false, false, false, false);
        GovernmentGateStatus beforeGovernmentVote = new GovernmentGateStatus(
                "realm1", true, false, false, true, true, false, false);
        GovernmentGateStatus beforeElection = new GovernmentGateStatus(
                "realm1", true, true, false, true, true, true, false);

        assertEquals(
                "Locked: complete Foundation I at the Shrine before Realm naming opens.",
                noFoundation.nameVoteLockMessage());
        assertEquals(
                "Locked: complete Foundation II at the Shrine before Government voting opens.",
                beforeGovernmentVote.governmentVoteLockMessage());
        assertEquals(
                "Locked: complete Foundation III at the Shrine before founding elections open.",
                beforeElection.foundingElectionLockMessage());
        assertEquals(
                "Locked: complete Foundation III at the Shrine before the Seat of Rule opens.",
                beforeElection.seatOfRuleLockMessage());
    }
}
