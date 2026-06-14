package panetina.elarion.addons.npcs.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class NpcPlacementServiceTest {
    @Test
    void yawFacesMinecraftCardinalDirections() {
        assertEquals(0.0F, NpcPlacementService.yawToward(0, 0, 0, 1), 0.001F);
        assertEquals(-90.0F, NpcPlacementService.yawToward(0, 0, 1, 0), 0.001F);
        assertEquals(90.0F, NpcPlacementService.yawToward(0, 0, -1, 0), 0.001F);
        assertEquals(180.0F, Math.abs(NpcPlacementService.yawToward(0, 0, 0, -1)), 0.001F);
    }

    @Test
    void reconciliationPrefersStoredAnchorAndFallsBackDeterministically() {
        UUID first = UUID.randomUUID();
        UUID stored = UUID.randomUUID();
        UUID third = UUID.randomUUID();

        assertEquals(stored, NpcPlacementService.chooseCanonicalId(stored, List.of(first, stored, third)));
        assertEquals(first, NpcPlacementService.chooseCanonicalId(UUID.randomUUID(), List.of(first, third)));
        assertNull(NpcPlacementService.chooseCanonicalId(stored, List.of()));
    }
}
