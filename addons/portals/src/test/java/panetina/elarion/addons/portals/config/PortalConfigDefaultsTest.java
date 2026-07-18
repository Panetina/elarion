package panetina.elarion.addons.portals.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class PortalConfigDefaultsTest {
    @Test
    void ancientGateDefaultsTravelFromRealmWorldsToWorldheart() {
        String routes = PortalConfigDefaults.ROUTES;

        assertRoutePair(routes, "realm1", "elarion:realm_world_1", "elarion:worldheart");
        assertRoutePair(routes, "realm2", "elarion:realm_world_2", "elarion:worldheart");
        assertRoutePair(routes, "realm3", "elarion:realm_world_3", "elarion:worldheart");
    }

    @Test
    void scheduledDimensionGateDefaultsDepartFromWorldheart() {
        String routes = PortalConfigDefaults.ROUTES;

        assertRoutePair(routes, "nether", "elarion:worldheart", "minecraft:the_nether");
        assertRoutePair(routes, "end", "elarion:worldheart", "minecraft:the_end");
    }

    @Test
    void neutralGateDefaultCanBeConfiguredFromAnyWorldToAnyWorld() {
        assertRoutePair(PortalConfigDefaults.ROUTES, "neutral", "*", "*");
    }

    private static void assertRoutePair(String routes, String routeId, String sourceDimension, String destinationDimension) {
        String marker = "\n  " + routeId + ":\n";
        int start = routes.indexOf(marker);
        assertTrue(start >= 0, "missing route " + routeId);
        int nextRoute = routes.indexOf("\n\n  ", start + marker.length());
        String block = nextRoute >= 0 ? routes.substring(start, nextRoute) : routes.substring(start);
        assertTrue(block.contains("source-dimension: \"" + sourceDimension + "\""), routeId + " has the wrong source dimension");
        assertTrue(block.contains("destination-dimension: \"" + destinationDimension + "\""), routeId + " has the wrong destination dimension");
    }
}
