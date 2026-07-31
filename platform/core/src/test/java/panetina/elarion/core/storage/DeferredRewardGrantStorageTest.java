package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.DeferredRewardGrant;
import panetina.elarion.core.model.RewardAction;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DeferredRewardGrantStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsPendingAndDeliveredReceipts() {
        Path file = tempDir.resolve("reward-grants.json");
        DeferredRewardGrantStorage storage =
                new DeferredRewardGrantStorage(LoggerFactory.getLogger("test"), file);
        UUID recipient = UUID.randomUUID();
        DeferredRewardGrant grant = new DeferredRewardGrant(
                "offering:test:m1:" + recipient,
                recipient,
                "elarion_offerings",
                "reward",
                List.of(new RewardAction("message", Map.of("text", "done"))),
                Set.of(0),
                100L,
                200L);

        storage.save(file, Map.of(grant.id(), grant));
        Map<String, DeferredRewardGrant> loaded = storage.load(file);

        assertEquals(grant, loaded.get(grant.id()));
    }

    @Test
    void recoverableNullRowsDoNotDiscardValidGrants() throws Exception {
        Path file = tempDir.resolve("reward-grants.json");
        UUID recipient = UUID.randomUUID();
        Files.writeString(file, """
                {
                  "grants": {
                    "grant-1": {
                      "id": "grant-1",
                      "recipientId": "%s",
                      "sourceSystem": "elarion_offerings",
                      "sourceId": "milestone",
                      "actions": [{"type": "message", "parameters": {"text": "done"}}],
                      "completedActions": [],
                      "createdAt": 100,
                      "deliveredAt": 0
                    },
                    "broken": null
                  }
                }
                """.formatted(recipient));
        DeferredRewardGrantStorage storage =
                new DeferredRewardGrantStorage(LoggerFactory.getLogger("test"), file);

        Map<String, DeferredRewardGrant> loaded = storage.load(file);

        assertEquals(1, loaded.size());
        assertEquals(recipient, loaded.get("grant-1").recipientId());
        assertTrue(Files.exists(file));
        try (var files = Files.list(tempDir)) {
            assertTrue(files.noneMatch(path ->
                    path.getFileName().toString().startsWith("reward-grants.json.corrupt-")));
        }
    }

    @Test
    void explicitNullGrantMapLoadsAsEmpty() throws Exception {
        Path file = tempDir.resolve("reward-grants.json");
        Files.writeString(file, """
                {"grants": null}
                """);
        DeferredRewardGrantStorage storage =
                new DeferredRewardGrantStorage(LoggerFactory.getLogger("test"), file);

        assertTrue(storage.load(file).isEmpty());
        assertTrue(Files.exists(file));
    }
}
