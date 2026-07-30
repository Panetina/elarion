package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentVoteDeadlineIndexTest {
    @Test
    void rebuildReturnsOnlyExpiredVotesInCanonicalMapOrder() {
        GovernmentVoteDeadlineIndex index = new GovernmentVoteDeadlineIndex();
        GovernmentVoteState first = startedVote("realm1", GovernmentVoteType.REALM_NAME, 30L);
        GovernmentVoteState second = startedVote("realm2", GovernmentVoteType.REALM_COLOR, 10L);
        GovernmentVoteState future = startedVote("realm3", GovernmentVoteType.GOVERNMENT_FORM, 50L);
        GovernmentVoteState unresolvedWithoutVotingWindow = new GovernmentVoteState(
                "realm4", GovernmentVoteType.FOUNDING_ELECTION);
        GovernmentVoteState resolved = startedVote("realm5", GovernmentVoteType.REALM_NAME, 5L);
        resolved.resolved = true;
        Map<String, GovernmentVoteState> votes = new LinkedHashMap<>();
        votes.put("first", first);
        votes.put("second", second);
        votes.put("future", future);
        votes.put("waiting", unresolvedWithoutVotingWindow);
        votes.put("resolved", resolved);

        index.rebuild(votes);

        assertEquals(List.of(first, second), expiredVotes(index, 40L));
        assertEquals(3, index.scheduledCount());
    }

    @Test
    void updateReschedulesMutableVoteWithoutLeavingStaleDeadline() {
        GovernmentVoteDeadlineIndex index = new GovernmentVoteDeadlineIndex();
        GovernmentVoteState vote = startedVote("realm1", GovernmentVoteType.REALM_NAME, 10L);
        index.update("vote", vote);

        vote.endsAt = 30L;
        index.update("vote", vote);

        assertTrue(expiredVotes(index, 20L).isEmpty());
        assertEquals(List.of(vote), expiredVotes(index, 30L));

        vote.resolved = true;
        index.update("vote", vote);

        assertTrue(expiredVotes(index, Long.MAX_VALUE).isEmpty());
        assertEquals(0, index.scheduledCount());
    }

    @Test
    void replacementPreservesOrderWhileRemovalAndReinsertionMovesToEnd() {
        GovernmentVoteDeadlineIndex index = new GovernmentVoteDeadlineIndex();
        GovernmentVoteState first = startedVote("realm1", GovernmentVoteType.REALM_NAME, 10L);
        GovernmentVoteState second = startedVote("realm2", GovernmentVoteType.REALM_COLOR, 10L);
        index.update("first", first);
        index.update("second", second);

        GovernmentVoteState replacement = startedVote("realm1", GovernmentVoteType.REALM_NAME, 10L);
        index.update("first", replacement);
        assertEquals(List.of(replacement, second), expiredVotes(index, 10L));

        index.remove("first");
        index.update("first", replacement);
        assertEquals(List.of(second, replacement), expiredVotes(index, 10L));
    }

    @Test
    void unscheduledProposalWindowsKeepCreationOrderWhenVotingStartsLater() {
        GovernmentVoteDeadlineIndex index = new GovernmentVoteDeadlineIndex();
        GovernmentVoteState first = new GovernmentVoteState("realm1", GovernmentVoteType.REALM_NAME);
        GovernmentVoteState second = new GovernmentVoteState("realm2", GovernmentVoteType.REALM_COLOR);
        index.update("first", first);
        index.update("second", second);

        second.startedAt = 1L;
        second.endsAt = 10L;
        index.update("second", second);
        first.startedAt = 1L;
        first.endsAt = 10L;
        index.update("first", first);

        assertEquals(List.of(first, second), expiredVotes(index, 10L));
    }

    @Test
    void realmAndGlobalResetClearOnlyTheirRuntimeDeadlines() {
        GovernmentVoteDeadlineIndex index = new GovernmentVoteDeadlineIndex();
        GovernmentVoteState first = startedVote("realm1", GovernmentVoteType.REALM_NAME, 10L);
        GovernmentVoteState second = startedVote("realm2", GovernmentVoteType.REALM_COLOR, 10L);
        index.rebuild(new LinkedHashMap<>(Map.of("first", first, "second", second)));

        index.removeRealm(" REALM1 ");

        assertEquals(List.of(second), expiredVotes(index, 10L));
        index.clear();
        assertTrue(expiredVotes(index, Long.MAX_VALUE).isEmpty());
    }

    @Test
    void expiryCarriesCanonicalMapKeyAndKeepsLegacyZeroDeadlineSemantics() {
        GovernmentVoteDeadlineIndex index = new GovernmentVoteDeadlineIndex();
        GovernmentVoteState vote = startedVote("realm1", GovernmentVoteType.REALM_NAME, 0L);
        index.rebuild(new LinkedHashMap<>(Map.of("legacy-key", vote)));

        List<GovernmentVoteDeadlineIndex.DueVote> due = index.expired(0L);

        assertEquals(1, due.size());
        assertEquals("legacy-key", due.getFirst().key());
        assertEquals(vote, due.getFirst().vote());
    }

    private static List<GovernmentVoteState> expiredVotes(GovernmentVoteDeadlineIndex index, long now) {
        return index.expired(now).stream().map(GovernmentVoteDeadlineIndex.DueVote::vote).toList();
    }

    private static GovernmentVoteState startedVote(String realmId, GovernmentVoteType type, long endsAt) {
        GovernmentVoteState vote = new GovernmentVoteState(realmId, type);
        vote.startedAt = 1L;
        vote.endsAt = endsAt;
        return vote;
    }
}
