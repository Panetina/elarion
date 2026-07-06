package panetina.elarion.addons.underworld.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CorpseRecordTest {
    @Test
    void legacyCorpseDefaultsStayProtectedUntilMigratedByService() {
        CorpseRecord corpse = new CorpseRecord();

        assertEquals(0L, corpse.publicLootStartedAt);
        assertEquals(0L, corpse.decaysAt);
        assertEquals(-1, corpse.selectedHotbarSlot);
        assertEquals("", corpse.victimName);
    }

    @Test
    void storedItemStackDefaultsToInventoryFallbackWithoutLegacyMetadata() {
        StoredItemStack stack = new StoredItemStack("minecraft:stone", 4);

        assertEquals("", stack.sourceType);
        assertEquals("", stack.sourceId);
        assertEquals("", stack.sourceLabel);
        assertEquals(-1, stack.slotIndex);
        assertEquals("", stack.equipmentSlot);
    }
}
