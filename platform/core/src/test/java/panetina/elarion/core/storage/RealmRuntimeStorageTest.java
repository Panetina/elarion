package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.RealmDecision;
import panetina.elarion.core.model.RealmDecisionStatus;
import panetina.elarion.core.model.RealmDecisionType;
import panetina.elarion.core.model.RealmRelationship;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RealmRuntimeStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsCanonicalRealmRuntimeState() {
        Path file = tempDir.resolve("realm-state.json");
        RealmRuntimeStorage storage = new RealmRuntimeStorage(LoggerFactory.getLogger("test"));
        RealmRuntimeStorage.RealmRuntimeState state = new RealmRuntimeStorage.RealmRuntimeState();
        UUID decisionId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        RealmDecision decision = new RealmDecision(decisionId, RealmDecisionType.PROPOSE_ALLIANCE,
                "realm1", "realm2", leaderId, 100L, 1_000L, RealmDecisionStatus.PENDING,
                Map.of(leaderId, true));
        state.relationships().put("realm1|realm2", RealmRelationship.ALLY);
        state.hiddenRealms().add("realm3");
        state.decisions().put(decisionId, decision);

        storage.save(file, state);
        RealmRuntimeStorage.RealmRuntimeState loaded = storage.load(file);

        assertEquals(Map.of("realm1|realm2", RealmRelationship.ALLY), loaded.relationships());
        assertEquals(java.util.Set.of("realm3"), loaded.hiddenRealms());
        assertEquals(RealmDecisionStatus.PENDING, loaded.decisions().get(decisionId).status());
        assertEquals(Map.of(leaderId, true), loaded.decisions().get(decisionId).votes());
    }

    @Test
    void recoverableNullRowsDoNotDiscardValidRealmRuntimeState() throws Exception {
        Path file = tempDir.resolve("realm-state.json");
        UUID decisionId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID voterId = UUID.randomUUID();
        Files.writeString(file, """
                {
                  "relationships": {
                    "realm1|realm2": "ALLY",
                    "realm1|realm3": null,
                    "realm2|realm3": "UNKNOWN",
                    "realm1|realm4": "HIDDEN"
                  },
                  "hiddenRealms": ["realm3", null, "", " REALM4 "],
                  "decisions": [
                    null,
                    {
                      "id": "%s",
                      "type": "PROPOSE_ALLIANCE",
                      "declaringRealmId": "realm1",
                      "receivingRealmId": "realm2",
                      "leaderId": "%s",
                      "createdAt": 100,
                      "expiresAt": 1000,
                      "status": null,
                      "votes": {
                        "%s": true,
                        "invalid-voter": false,
                        "%s": null
                      }
                    },
                    {}
                  ]
                }
                """.formatted(decisionId, leaderId, voterId, UUID.randomUUID()));
        RealmRuntimeStorage storage = new RealmRuntimeStorage(LoggerFactory.getLogger("test"));

        RealmRuntimeStorage.RealmRuntimeState loaded = storage.load(file);

        assertEquals(Map.of("realm1|realm2", RealmRelationship.ALLY), loaded.relationships());
        assertEquals(java.util.Set.of("realm3", "realm4"), loaded.hiddenRealms());
        assertEquals(1, loaded.decisions().size());
        RealmDecision decision = loaded.decisions().get(decisionId);
        assertEquals(RealmDecisionStatus.PENDING, decision.status());
        assertEquals(Map.of(voterId, true), decision.votes());
        assertTrue(Files.exists(file));
        try (var files = Files.list(tempDir)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().contains(".corrupt-")));
        }
    }

    @Test
    void explicitNullCollectionsLoadAsEmpty() throws Exception {
        Path file = tempDir.resolve("realm-state.json");
        Files.writeString(file, """
                {"relationships": null, "hiddenRealms": null, "decisions": null}
                """);
        RealmRuntimeStorage storage = new RealmRuntimeStorage(LoggerFactory.getLogger("test"));

        RealmRuntimeStorage.RealmRuntimeState loaded = storage.load(file);

        assertTrue(loaded.relationships().isEmpty());
        assertTrue(loaded.hiddenRealms().isEmpty());
        assertTrue(loaded.decisions().isEmpty());
        assertTrue(Files.exists(file));
    }
}
