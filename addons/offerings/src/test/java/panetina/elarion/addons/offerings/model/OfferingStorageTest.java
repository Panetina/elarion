package panetina.elarion.addons.offerings.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.offerings.storage.OfferingState;
import panetina.elarion.addons.offerings.storage.OfferingStorage;

import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class OfferingStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void stateRoundTrips() {
        OfferingStorage storage = new OfferingStorage(LoggerFactory.getLogger("test"));
        OfferingState state = new OfferingState();
        state.instances.put("council_hall_1", new OfferingInstance(
                "council_hall_1", "council_hall", OfferingScope.GLOBAL, "", "",
                0, 0, 0, "", Map.of("currency", 25L), Map.of(), Set.of(), System.currentTimeMillis(), 0L));
        UUID contributor = UUID.randomUUID();
        state.donations.put("council_hall_1", List.of(new OfferingDonationRecord(
                contributor, "Builder", "currency", "currency", 25L, 1234L)));

        storage.save(tempDir, state);
        OfferingState loaded = storage.load(tempDir);

        assertEquals(1, loaded.instances.size());
        assertEquals(25L, loaded.instances.get("council_hall_1").progress().get("currency"));
        assertEquals(0L, loaded.instances.get("council_hall_1").resetGeneration());
        assertEquals(contributor, loaded.donations.get("council_hall_1").getFirst().contributorId());
        assertEquals(25L, loaded.donations.get("council_hall_1").getFirst().amount());
    }
}
