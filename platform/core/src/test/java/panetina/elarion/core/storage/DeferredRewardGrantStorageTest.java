package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.DeferredRewardGrant;
import panetina.elarion.core.model.RewardAction;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
