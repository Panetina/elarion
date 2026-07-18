package panetina.elarion.addons.npcs.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.npcs.model.NpcTaxJurisdictionKind;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class NpcTaxJurisdictionResolverTest {
    private final NpcTaxJurisdictionResolver resolver = new NpcTaxJurisdictionResolver(
            world -> "elarion:realm_world_1".equals(world) ? Optional.of("oak") : Optional.empty());

    @Test
    void autoPrefersRealmOwnerAndFallsBackToWorld() {
        PlacedNpcRecord realm = resolver.resolve(record("elarion:realm_world_1"), "auto");
        PlacedNpcRecord world = resolver.resolve(record("elarion:worldheart"), "auto");

        assertEquals(NpcTaxJurisdictionKind.REALM, realm.taxJurisdictionKind());
        assertEquals("oak", realm.taxJurisdictionId());
        assertEquals(NpcTaxJurisdictionKind.WORLD, world.taxJurisdictionKind());
        assertEquals("elarion:worldheart", world.taxJurisdictionId());
    }

    @Test
    void explicitPoliciesRequireMatchingPlacementWorld() {
        assertEquals("oak", resolver.resolve(record("elarion:realm_world_1"), "realm:oak")
                .taxJurisdictionId());
        assertEquals("elarion:worldheart", resolver.resolve(record("elarion:worldheart"),
                "world:elarion:worldheart").taxJurisdictionId());

        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve(record("elarion:worldheart"), "realm:oak"));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve(record("elarion:realm_world_1"), "world:elarion:worldheart"));
    }

    @Test
    void validatesPolicyShape() {
        assertEquals(true, NpcTaxJurisdictionResolver.validPolicy("auto"));
        assertEquals(true, NpcTaxJurisdictionResolver.validPolicy("realm:oak"));
        assertEquals(true, NpcTaxJurisdictionResolver.validPolicy("world:minecraft:the_nether"));
        assertEquals(false, NpcTaxJurisdictionResolver.validPolicy("realm:"));
        assertEquals(false, NpcTaxJurisdictionResolver.validPolicy("world:not a world"));
    }

    private static PlacedNpcRecord record(String worldId) {
        return new PlacedNpcRecord(UUID.randomUUID(), "merchant_1", "merchant", null, worldId,
                0, 64, 0, 0, 0, "", "", "", "", UUID.randomUUID(), 1L);
    }
}
