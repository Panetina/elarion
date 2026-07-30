package panetina.elarion.addons.offerings.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.offerings.storage.OfferingState;
import panetina.elarion.addons.offerings.storage.OfferingStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void recoverableNullRowsDoNotDiscardValidOfferingState() throws Exception {
        UUID contributor = UUID.randomUUID();
        Files.writeString(tempDir.resolve("state.json"), """
                {
                  "instances": {
                    "council_hall_1": {
                      "id": "council_hall_1",
                      "projectId": "council_hall",
                      "scope": "GLOBAL",
                      "progress": {"currency": 25}
                    },
                    "discarded": null,
                    "": {"id": "blank"}
                  },
                  "anchors": {
                    "shrine_1": {
                      "id": "shrine_1",
                      "instanceId": "council_hall_1",
                      "worldId": "minecraft:overworld"
                    },
                    "discarded": null
                  },
                  "realmFlags": {
                    "realm1": ["global_notifications", null, ""],
                    "discarded": null,
                    "": ["ignored"]
                  },
                  "projectCounters": {
                    "council_hall": 2,
                    "discarded": null,
                    "": 3
                  },
                  "donations": {
                    "council_hall_1": [
                      {
                        "contributorId": "%s",
                        "contributorName": "Builder",
                        "requirementKey": "currency",
                        "type": "currency",
                        "amount": 25,
                        "createdAt": 1234
                      },
                      null
                    ],
                    "discarded": null,
                    "": []
                  }
                }
                """.formatted(contributor));
        OfferingStorage storage = new OfferingStorage(LoggerFactory.getLogger("test"));

        OfferingState loaded = storage.load(tempDir);

        assertEquals(Set.of("global_notifications"), loaded.realmFlags.get("realm1"));
        assertEquals(1, loaded.instances.size());
        assertEquals(1, loaded.anchors.size());
        assertEquals(1, loaded.projectCounters.size());
        assertEquals(1, loaded.donations.size());
        assertEquals(contributor, loaded.donations.get("council_hall_1").getFirst().contributorId());
        assertTrue(Files.exists(tempDir.resolve("state.json")));
    }

    @Test
    void explicitNullCollectionsLoadAsMutableEmptyCollections() throws Exception {
        Files.writeString(tempDir.resolve("state.json"), """
                {
                  "instances": null,
                  "anchors": null,
                  "realmFlags": null,
                  "projectCounters": null,
                  "donations": null
                }
                """);
        OfferingStorage storage = new OfferingStorage(LoggerFactory.getLogger("test"));

        OfferingState loaded = storage.load(tempDir);

        loaded.realmFlags.put("realm1", new java.util.LinkedHashSet<>());
        loaded.projectCounters.put("council_hall", 1);
        assertTrue(loaded.instances.isEmpty());
        assertTrue(loaded.anchors.isEmpty());
        assertEquals(1, loaded.realmFlags.size());
        assertEquals(1, loaded.projectCounters.size());
        assertTrue(loaded.donations.isEmpty());
        assertTrue(Files.exists(tempDir.resolve("state.json")));
    }
}
