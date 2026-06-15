package panetina.elarion.addons.government.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GovernmentStorageTest {
    @TempDir
    Path root;

    @Test
    void stateRoundTrips() {
        GovernmentState state = new GovernmentState();
        state.realms.put("realm1", RealmGovernmentState.empty("realm1")
                .withVotedIdentity("Oak", "OAK")
                .withForm("republic")
                .withFoundingElectionComplete());
        GovernmentVoteState vote = new GovernmentVoteState("realm1", GovernmentVoteType.REALM_NAME);
        vote.resolved = true;
        vote.winnerIds = java.util.List.of("oak");
        vote.resultTotals = Map.of("oak", 7L, "vale", 4L);
        state.votes.put("realm1:realm_name", vote);

        GovernmentStorage storage = new GovernmentStorage(LoggerFactory.getLogger("government-test"), root);
        storage.save(root, state);
        GovernmentState loaded = storage.load(root);

        assertEquals("republic", loaded.realms.get("realm1").activeGovernmentFormId());
        assertEquals("Oak", loaded.realms.get("realm1").votedDisplayName());
        assertEquals("OAK", loaded.realms.get("realm1").votedTag());
        assertEquals(true, loaded.realms.get("realm1").foundingElectionCompletedAt() > 0L);
        assertEquals(7L, loaded.votes.get("realm1:realm_name").resultTotals.get("oak"));
    }
}
