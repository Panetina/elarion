package panetina.elarion.addons.angling.registry;

import org.junit.jupiter.api.Test;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingAttachments;
import net.minecraft.component.DataComponentTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingRegistryIdentityTest {
    @Test
    void frozenSoundAndParticleCountsRemainStable() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
        AnglingDataComponents.initialize();
        AnglingAttachments.initialize();
        AnglingItems.initialize();
        AnglingSounds.initialize();
        AnglingParticles.initialize();
        AnglingEntities.initialize();
        assertEquals(11, AnglingSounds.ALL.size());
        assertEquals("elarion_angling:valley_notification",
                net.minecraft.registry.Registries.PARTICLE_TYPE.getId(AnglingParticles.VALLEY_NOTIFICATION).toString());
        assertEquals("elarion_angling:king_cry",
                net.minecraft.registry.Registries.SOUND_EVENT.getId(AnglingSounds.KING_CRY).toString());
        assertEquals("elarion_angling:fishing_bob",
                net.minecraft.registry.Registries.ENTITY_TYPE.getId(AnglingEntities.FISHING_BOBBER).toString());
        assertFalse(AnglingEntities.FISHING_BOBBER.isSaveable());
        assertFalse(AnglingEntities.FISHING_BOBBER.isSummonable());
        assertEquals("elarion_angling:bucketed_fish",
                net.minecraft.registry.Registries.DATA_COMPONENT_TYPE.getId(AnglingDataComponents.BUCKETED_FISH).toString());
        assertEquals("elarion_angling:tackle_box_fishes",
                net.minecraft.registry.Registries.DATA_COMPONENT_TYPE.getId(AnglingDataComponents.TACKLE_BOX_FISHES).toString());
        assertEquals("elarion_angling:signed_guide",
                net.minecraft.registry.Registries.DATA_COMPONENT_TYPE.getId(AnglingDataComponents.SIGNED_GUIDE).toString());
        assertEquals("elarion_angling:tackle_skin",
                net.minecraft.registry.Registries.DATA_COMPONENT_TYPE.getId(AnglingDataComponents.TACKLE_SKIN).toString());
        assertEquals("elarion_angling:modifiers",
                net.minecraft.registry.Registries.DATA_COMPONENT_TYPE.getId(AnglingDataComponents.MODIFIERS).toString());
        assertEquals("elarion_angling:bait_debit_cursor",
                AnglingAttachments.BAIT_DEBIT_CURSOR.identifier().toString());
        assertEquals(198, AnglingItems.snapshot().size());
        assertEquals(1, AnglingItems.require("elarion_angling_rod").getMaxCount());
        assertEquals("elarion_angling:cerulean_crystalback_minnow",
                net.minecraft.registry.Registries.ITEM.getId(
                        AnglingItems.require("cerulean_crystalback_minnow")).toString());
        assertEquals(1, AnglingItems.require("amethyst_hook").getMaxCount());
        assertTrue(AnglingItems.snapshot().containsKey(net.minecraft.util.Identifier.of(
                        "elarion_angling", "aloe_bream")));
        assertEquals("elarion_angling:fish",
                net.minecraft.registry.Registries.ENTITY_TYPE.getId(AnglingEntities.FISH).toString());
        assertTrue(AnglingItems.require("magma_crab").getComponents().contains(DataComponentTypes.FIRE_RESISTANT));
        var rawFood = AnglingItems.require("starcaught_fish").getComponents().get(DataComponentTypes.FOOD);
        assertNotNull(rawFood);
        assertEquals(2, rawFood.nutrition());
        assertEquals(0.4F, rawFood.saturation());
        assertTrue(rawFood.canAlwaysEat());
        assertEquals(AnglingItems.require("fish_bones"), rawFood.usingConvertsTo().orElseThrow().getItem());
        var cookedFood = AnglingItems.require("cooked_starcaught_fish").getComponents().get(DataComponentTypes.FOOD);
        assertNotNull(cookedFood);
        assertEquals(6, cookedFood.nutrition());
        assertEquals(24.0F, cookedFood.saturation());
        assertTrue(cookedFood.canAlwaysEat());
        assertEquals(AnglingItems.require("fish_bones"), cookedFood.usingConvertsTo().orElseThrow().getItem());
        for (net.minecraft.util.Identifier id : AnglingItems.snapshot().keySet()) {
            if (id.getPath().equals("default_minigame") || id.getPath().equals("default_catch")) continue;
            assertNotNull(getClass().getClassLoader().getResource(
                            "assets/elarion_angling/models/item/" + id.getPath() + ".json"),
                    () -> "missing item model for " + id);
        }
    }
}
