package panetina.elarion.addons.npcs.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlacedNpcRecordTest {
    @Test
    void skinAndPortraitOverridesAreSeparate() {
        NpcDefinition definition = new NpcDefinition(
                "banker", "Banker", "", "default_skin", "default_portrait", "dialogue", true);
        PlacedNpcRecord record = new PlacedNpcRecord(
                UUID.randomUUID(),
                "banker_1",
                "banker",
                null,
                "elarion:worldheart",
                0,
                64,
                0,
                0,
                0,
                "",
                "",
                "",
                "",
                UUID.randomUUID(),
                1L);

        assertEquals("default_skin", record.skin(definition));
        assertEquals("default_portrait", record.portrait(definition));

        PlacedNpcRecord updated = record.withSkin("custom_skin").withPortrait("custom_portrait");

        assertEquals("custom_skin", updated.skin(definition));
        assertEquals("custom_portrait", updated.portrait(definition));
        assertEquals("dialogue", updated.dialogue(definition));

        PlacedNpcRecord registered = updated.withTaxJurisdiction(NpcTaxJurisdictionKind.WORLD,
                "elarion:worldheart").named("Mara");
        assertEquals(NpcTaxJurisdictionKind.WORLD, registered.taxJurisdictionKind());
        assertEquals("elarion:worldheart", registered.taxJurisdictionId());
    }
}
