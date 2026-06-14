package panetina.elarion.addons.offerings.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class OfferingInstanceTest {
    @Test
    void realmInstanceIdsUseReadableofferingPrefix() {
        OfferingInstance instance = new OfferingInstance(
                "offering_realm_oak_2", "council_hall", OfferingScope.REALM, "oak",
                "", 0, 0, 0, "", Map.of(), Map.of(), Set.of(), System.currentTimeMillis(), 0L);

        assertEquals("offering_realm_oak_2", instance.id());
    }

    @Test
    void progressAndContributorTotalsAccumulate() {
        UUID contributor = UUID.randomUUID();
        OfferingInstance instance = base()
                .withProgress("item:minecraft:stone", 5, contributor)
                .withProgress("item:minecraft:stone", 7, contributor);

        assertEquals(12, instance.progress().get("item:minecraft:stone"));
        assertEquals(12, instance.contributorTotals().get(contributor.toString()));
    }

    @Test
    void resetKeepsIdentityAndAnchorButClearsProgress() {
        OfferingInstance instance = base()
                .withProgress("currency", 5, UUID.randomUUID())
                .withCompletion(System.currentTimeMillis(), Set.of("m1"))
                .withAnchor("anchor_1")
                .reset();

        assertEquals("instance_1", instance.id());
        assertEquals("anchor_1", instance.anchorId());
        assertEquals(Map.of(), instance.progress());
        assertFalse(instance.completed());
    }

    private static OfferingInstance base() {
        return new OfferingInstance("instance_1", "project", OfferingScope.REALM, "oak",
                "", 0, 0, 0, "", Map.of(), Map.of(), Set.of(), System.currentTimeMillis(), 0L);
    }
}
