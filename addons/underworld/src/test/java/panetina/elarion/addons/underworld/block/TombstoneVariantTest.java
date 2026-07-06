package panetina.elarion.addons.underworld.block;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.underworld.model.CorpseRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TombstoneVariantTest {
    @Test
    void realmSelectionIsStable() {
        TombstoneVariant first = TombstoneVariant.forRealm("ivarstead", "corpse-a");
        TombstoneVariant second = TombstoneVariant.forRealm("ivarstead", "corpse-b");

        assertSame(first, second);
    }

    @Test
    void blankRealmUsesFallbackSeed() {
        TombstoneVariant first = TombstoneVariant.forRealm("", "corpse-a");
        TombstoneVariant second = TombstoneVariant.forRealm("", "corpse-b");

        assertNotEquals(first, second);
    }

    @Test
    void allImportedTombstonesReserveOneByTwoByOneBlocks() {
        for (TombstoneVariant variant : TombstoneVariant.values()) {
            assertEquals(1, variant.widthBlocks());
            assertEquals(2, variant.heightBlocks());
            assertEquals(1, variant.depthBlocks());
        }
    }

    @Test
    void corpseRecordTreatsVariantAsTombPlacementMarker() {
        CorpseRecord corpse = new CorpseRecord();

        assertFalse(corpse.hasTombPosition());
        corpse.tombstoneVariant = TombstoneVariant.TOMBSTONE_1.id();

        assertTrue(corpse.hasTombPosition());
    }

    @Test
    void importedModelBoundsAreRecordedInPixels() {
        assertBounds(TombstoneVariant.TOMBSTONE_1, 0.5, 0.0, 6.0, 15.5, 17.0, 11.0);
        assertBounds(TombstoneVariant.TOMBSTONE_2, 0.5, 0.0, 6.0, 15.5, 18.0, 11.0);
        assertBounds(TombstoneVariant.TOMBSTONE_3, 0.5, 0.0, 6.0, 15.5, 30.0, 13.0);
    }

    @Test
    void importedModelBoundsRequireUpperReservationButNotFullBlockSelection() {
        for (TombstoneVariant variant : TombstoneVariant.values()) {
            assertTrue(variant.maxY() > 16.0D);
            assertTrue(variant.maxX() - variant.minX() < 16.0D);
            assertTrue(variant.maxZ() - variant.minZ() < 16.0D);
        }
    }

    private static void assertBounds(
            TombstoneVariant variant, double minX, double minY, double minZ, double maxX, double maxY, double maxZ
    ) {
        assertEquals(minX, variant.minX(), 0.001D);
        assertEquals(minY, variant.minY(), 0.001D);
        assertEquals(minZ, variant.minZ(), 0.001D);
        assertEquals(maxX, variant.maxX(), 0.001D);
        assertEquals(maxY, variant.maxY(), 0.001D);
        assertEquals(maxZ, variant.maxZ(), 0.001D);
    }
}
