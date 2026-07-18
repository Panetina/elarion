package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentProposalRecordTest {
    @Test
    void citizenRatificationVotesAreSeparateFromAuthorityReviewVotes() {
        UUID author = UUID.randomUUID();
        UUID authority = UUID.randomUUID();
        UUID citizen = UUID.randomUUID();
        GovernmentProposalRecord proposal = GovernmentProposalRecord.create(
                        "realm1_proposal_no_theft",
                        "realm1",
                        author,
                        "law",
                        "No Theft",
                        "Make theft illegal.",
                        10L)
                .withVote(authority, true)
                .withStatus(GovernmentProposalStatus.CITIZEN_RATIFICATION, authority, 20L)
                .withCitizenVote(citizen, true);

        assertEquals(GovernmentProposalStatus.CITIZEN_RATIFICATION, proposal.status());
        assertTrue(proposal.reviewVotes().get(authority));
        assertTrue(proposal.citizenVotes().get(citizen));
        assertEquals(1, proposal.reviewVotes().size());
        assertEquals(1, proposal.citizenVotes().size());
    }

    @Test
    void finalTextClearsPriorReviewVotesBeforeCouncilReview() {
        UUID author = UUID.randomUUID();
        UUID president = UUID.randomUUID();
        UUID council = UUID.randomUUID();
        GovernmentProposalRecord proposal = GovernmentProposalRecord.create(
                        "realm1_proposal_tax_law",
                        "realm1",
                        author,
                        "law",
                        "Tax",
                        "Add tax.",
                        10L)
                .withVote(president, true)
                .withStatus(GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION, president, 20L)
                .withFinalText("Road Tax", "Embers pay a small road tax.", president, 30L)
                .withStatusAndClearedReview(GovernmentProposalStatus.FINAL_TEXT_REVIEW, president, 30L)
                .withVote(council, true);

        assertEquals(GovernmentProposalStatus.FINAL_TEXT_REVIEW, proposal.status());
        assertEquals("Road Tax", proposal.finalTitle());
        assertEquals("Embers pay a small road tax.", proposal.finalBody());
        assertEquals(1, proposal.reviewVotes().size());
        assertTrue(proposal.reviewVotes().get(council));
    }

    @Test
    void sponsorSurvivesFinalTextReviewAndRewrite() {
        UUID author = UUID.randomUUID();
        UUID sponsor = UUID.randomUUID();
        UUID reviewer = UUID.randomUUID();
        GovernmentProposalRecord proposal = GovernmentProposalRecord.create(
                        "realm1_proposal_gate_law",
                        "realm1",
                        author,
                        "law",
                        "Gate Law",
                        "Regulate the gate.",
                        10L)
                .withVote(UUID.randomUUID(), true)
                .withVote(sponsor, true)
                .withStatusAndSponsor(GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION, sponsor, sponsor, 20L)
                .withFinalText("Ancient Gate Law", "The gate must be guarded.", sponsor, 30L)
                .withStatusAndClearedReview(GovernmentProposalStatus.FINAL_TEXT_REVIEW, sponsor, 30L)
                .withVote(reviewer, false)
                .withStatusAndClearedReview(GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION, reviewer, 40L);

        assertEquals(sponsor, proposal.sponsorId());
        assertEquals(GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION, proposal.status());
        assertEquals(0, proposal.reviewVotes().size());
    }
}
