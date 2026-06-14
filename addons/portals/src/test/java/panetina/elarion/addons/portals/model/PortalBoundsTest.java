package panetina.elarion.addons.portals.model;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalBoundsTest {
    @Test
    void detectsEverySupportedPlane() {
        assertEquals(PortalAxis.X, PortalBounds.between(new BlockPos(4, 2, 3), new BlockPos(4, 6, 8)).axis());
        assertEquals(PortalAxis.Y, PortalBounds.between(new BlockPos(4, 2, 3), new BlockPos(8, 2, 9)).axis());
        assertEquals(PortalAxis.Z, PortalBounds.between(new BlockPos(4, 2, 3), new BlockPos(8, 7, 3)).axis());
    }

    @Test
    void rejectsThickAndAmbiguousSelections() {
        assertThrows(IllegalArgumentException.class,
                () -> PortalBounds.between(new BlockPos(0, 0, 0), new BlockPos(2, 2, 2)));
        assertThrows(IllegalArgumentException.class,
                () -> PortalBounds.between(new BlockPos(0, 0, 0), new BlockPos(0, 0, 4)));
        assertThrows(IllegalArgumentException.class,
                () -> PortalBounds.between(new BlockPos(0, 0, 0), new BlockPos(0, 0, 0)));
    }

    @Test
    void normalizesAndEnumeratesInclusiveBlocks() {
        PortalBounds bounds = PortalBounds.between(new BlockPos(5, 4, 2), new BlockPos(5, 2, 4));
        assertEquals(9, bounds.volume());
        assertEquals(9, bounds.positions().size());
        assertTrue(bounds.contains(new BlockPos(5, 3, 3)));
    }
}
