package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealmGovernmentStateTest {
    @Test
    void officeAssignmentAndRemovalArePersistentStateMutations() {
        UUID citizen = UUID.randomUUID();
        RealmGovernmentState state = RealmGovernmentState.empty("realm1")
                .withForm("republic")
                .withOfficeHolder("president", citizen);

        assertTrue(state.officeHolders().get("president").contains(citizen));

        RealmGovernmentState updated = state.withoutOfficeHolder("president", citizen);

        assertFalse(updated.officeHolders().containsKey("president"));
    }

    @Test
    void colorVoteTimestampIsSetWhenColorIsRecorded() {
        RealmGovernmentState state = RealmGovernmentState.empty("realm1")
                .withVotedIdentity("Oak", "OAK")
                .withVotedColor("gold");

        assertTrue(state.nameVoteCompletedAt() > 0L);
        assertTrue(state.colorVoteCompletedAt() > 0L);
    }

    @Test
    void proposalAndLawIdsMutateIndependently() {
        RealmGovernmentState state = RealmGovernmentState.empty("realm1")
                .withPendingProposal("proposal_1")
                .withActiveLaw("law_1");

        assertTrue(state.pendingProposalIds().contains("proposal_1"));
        assertTrue(state.activeLawIds().contains("law_1"));

        RealmGovernmentState updated = state
                .withoutPendingProposal("proposal_1")
                .withoutActiveLaw("law_1");

        assertFalse(updated.pendingProposalIds().contains("proposal_1"));
        assertFalse(updated.activeLawIds().contains("law_1"));
    }

    @Test
    void reopeningLeadershipElectionClearsOnlyTheCompletionMarker() {
        UUID monarch = UUID.randomUUID();
        RealmGovernmentState state = RealmGovernmentState.empty("realm1")
                .withVotedIdentity("Oak", "OAK")
                .withVotedColor("gold")
                .withForm("monarchy")
                .withOfficeHolder("monarch", monarch)
                .withFoundingElectionComplete();

        RealmGovernmentState reopened = state.withFoundingElectionReopened();

        assertEquals(0L, reopened.foundingElectionCompletedAt());
        assertEquals("monarchy", reopened.activeGovernmentFormId());
        assertTrue(reopened.officeHolders().get("monarch").contains(monarch));
        assertEquals("Oak", reopened.votedDisplayName());
    }
}
