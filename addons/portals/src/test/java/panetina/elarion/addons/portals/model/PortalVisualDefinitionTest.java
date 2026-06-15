package panetina.elarion.addons.portals.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortalVisualDefinitionTest {
    @Test
    void appliesBrightnessAndOpacity() {
        PortalVisualDefinition visual = new PortalVisualDefinition(
                0x804020, 0.5F, 0.5F, 2,
                "minecraft:block/nether_portal", "minecraft:compass", 0);
        assertEquals(0x80402010, visual.argb());
        assertEquals("minecraft:compass", visual.statusIconItem());
    }

    @Test
    void supportsSeparateStatusIcon() {
        PortalVisualDefinition visual = new PortalVisualDefinition(
                0x804020, 1.0F, 1.0F, 2,
                "minecraft:block/nether_portal",
                "elarion:portal_ticket",
                "minecraft:netherrack",
                0);

        assertEquals("minecraft:netherrack", visual.statusIconItem());
    }
}
