package panetina.elarion.addons.government.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.government.model.RealmGovernmentState;

import java.nio.file.Path;

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

        GovernmentStorage storage = new GovernmentStorage(LoggerFactory.getLogger("government-test"), root);
        storage.save(root, state);
        GovernmentState loaded = storage.load(root);

        assertEquals("republic", loaded.realms.get("realm1").activeGovernmentFormId());
        assertEquals("Oak", loaded.realms.get("realm1").votedDisplayName());
        assertEquals("OAK", loaded.realms.get("realm1").votedTag());
        assertEquals(true, loaded.realms.get("realm1").foundingElectionCompletedAt() > 0L);
    }
}
