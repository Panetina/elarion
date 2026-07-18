package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentVoteStateTest {
    @Test
    void proposalAndVoteWindowsAreBoundedByTime() {
        GovernmentVoteState vote = new GovernmentVoteState("realm1", GovernmentVoteType.REALM_NAME);
        long now = 1_000L;

        vote.startProposalIfNeeded(now, Duration.ofHours(24));

        assertTrue(vote.proposalActive(now + 1L));
        assertFalse(vote.proposalEnded(now + 1L));
        assertTrue(vote.proposalEnded(now + Duration.ofHours(24).toMillis()));

        vote.startIfNeeded(now + Duration.ofHours(24).toMillis(), Duration.ofHours(24));

        assertTrue(vote.active(now + Duration.ofHours(24).toMillis() + 1L));
        assertFalse(vote.ended(now + Duration.ofHours(24).toMillis() + 1L));
        assertTrue(vote.ended(now + Duration.ofHours(48).toMillis()));
    }

    @Test
    void runoffKeepsOnlyTiedOptionsAndStartsNextRound() {
        GovernmentVoteState vote = new GovernmentVoteState("realm1", GovernmentVoteType.GOVERNMENT_FORM);
        vote.round = 1;
        vote.options.put("monarchy", GovernmentVoteOption.governmentForm(
                "monarchy", "Monarchy", "One ruler."));
        vote.options.put("republic", GovernmentVoteOption.governmentForm(
                "republic", "Republic", "Elected leadership."));

        GovernmentVoteState runoff = vote.runoff(List.of("monarchy", "republic"), 10_000L, Duration.ofHours(12));

        assertEquals(2, runoff.round);
        assertTrue(runoff.runoff);
        assertEquals(List.of("monarchy", "republic"), List.copyOf(runoff.options.keySet()));
        assertEquals(10_000L, runoff.startedAt);
        assertEquals(10_000L + Duration.ofHours(12).toMillis(), runoff.endsAt);
    }

    @Test
    void foundingElectionCanUseNominationWindowBeforeVotingWindow() {
        GovernmentVoteState vote = new GovernmentVoteState("realm1", GovernmentVoteType.FOUNDING_ELECTION);
        long now = 50_000L;

        vote.startProposalIfNeeded(now, Duration.ofHours(24));

        assertTrue(vote.proposalActive(now + 1L));
        assertFalse(vote.active(now + 1L));

        long votingStart = now + Duration.ofHours(24).toMillis();
        assertTrue(vote.proposalEnded(votingStart));

        vote.startIfNeeded(votingStart, Duration.ofHours(24));

        assertTrue(vote.active(votingStart + 1L));
        assertFalse(vote.ended(votingStart + 1L));
        assertTrue(vote.ended(votingStart + Duration.ofHours(24).toMillis()));
    }
}
