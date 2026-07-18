package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GovernmentWebProjectionPublisherTest {
    @Test
    void exposesAggregateLifecycleWithoutVoterData() {
        long now = 10_000;
        GovernmentVoteState vote = new GovernmentVoteState("ashlands", GovernmentVoteType.FOUNDING_ELECTION);
        vote.startedAt = now - 100;
        vote.endsAt = now + 100;

        assertEquals("VOTING_OPEN", GovernmentWebProjectionPublisher.status(vote, now));
        vote.runoff = true;
        assertEquals("RUNOFF_OPEN", GovernmentWebProjectionPublisher.status(vote, now));
        vote.resolved = true;
        assertEquals("RESOLVED", GovernmentWebProjectionPublisher.status(vote, now));
    }
}
