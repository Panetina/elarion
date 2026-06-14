package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentGateStatusTest {
    @Test
    void gatesFollowFoundationAndFoundingOrder() {
        GovernmentGateStatus beforeFoundation = new GovernmentGateStatus(
                "realm1", false, false, false, false, false, false);

        assertTrue(beforeFoundation.nameVoteVisible());
        assertFalse(beforeFoundation.nameVoteUnlocked());
        assertFalse(beforeFoundation.governmentChoicesVisible());

        GovernmentGateStatus afterNameAndFoundationTwo = new GovernmentGateStatus(
                "realm1", true, true, false, true, false, false);

        assertTrue(afterNameAndFoundationTwo.governmentChoicesVisible());
        assertTrue(afterNameAndFoundationTwo.governmentVoteUnlocked());
        assertFalse(afterNameAndFoundationTwo.foundingElectionUnlocked());

        GovernmentGateStatus founded = new GovernmentGateStatus(
                "realm1", true, true, true, true, true, true);

        assertFalse(founded.foundingElectionUnlocked());
        assertTrue(founded.seatOfRuleUnlocked());
    }
}
