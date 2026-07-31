package panetina.elarion.addons.offerings.service;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.offerings.model.OfferingAnchor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OfferingAnchorLocationIndexTest {
    @Test
    void resolvesLocationsWithoutScanningTheCanonicalCollection() {
        OfferingAnchorLocationIndex index = new OfferingAnchorLocationIndex();
        List<OfferingAnchor> anchors = new ArrayList<>();
        for (int entry = 0; entry < 1_000; entry++) {
            anchors.add(anchor("anchor-" + entry, entry, 64, -entry));
        }
        index.rebuild(anchors);

        assertEquals("anchor-777", index.find("elarion:realm", new BlockPos(777, 64, -777))
                .orElseThrow().id());
        assertEquals(1_000, index.size());
    }

    @Test
    void duplicateLocationsPreserveCanonicalMapOrderAndSafeRemoval() {
        OfferingAnchor first = anchor("first", 1, 2, 3);
        OfferingAnchor duplicate = anchor("duplicate", 1, 2, 3);
        OfferingAnchorLocationIndex index = new OfferingAnchorLocationIndex();
        index.rebuild(List.of(first, duplicate));

        assertEquals("first", index.find("elarion:realm", new BlockPos(1, 2, 3)).orElseThrow().id());
        index.remove(duplicate);
        assertEquals("first", index.find("elarion:realm", new BlockPos(1, 2, 3)).orElseThrow().id());
        index.add(duplicate);
        index.remove(first);
        assertEquals("duplicate", index.find("elarion:realm", new BlockPos(1, 2, 3)).orElseThrow().id());
        index.remove(duplicate);
        assertTrue(index.find("elarion:realm", new BlockPos(1, 2, 3)).isEmpty());
    }

    private static OfferingAnchor anchor(String id, int x, int y, int z) {
        return new OfferingAnchor(id, "instance", "elarion:realm", x, y, z, null, 1L);
    }
}
