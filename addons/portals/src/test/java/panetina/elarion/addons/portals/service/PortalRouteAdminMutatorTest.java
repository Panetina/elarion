package panetina.elarion.addons.portals.service;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.portals.model.PortalBounds;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalEndpointRole;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.storage.PortalState;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortalRouteAdminMutatorTest {
    @Test
    void rejectsOverlapWithAnotherRouteInTheSameWorld() {
        PortalState state = new PortalState();
        PortalRouteState first = new PortalRouteState("first");
        first.source = endpoint("minecraft:overworld", 0, 0, 3, 4);
        state.routes.put(first.routeId, first);

        PortalEndpoint overlapping = endpoint("minecraft:overworld", 0, 3, 3, 7);

        assertThrows(IllegalArgumentException.class, () -> PortalRouteAdminMutator.ensureNoOverlap(
                state, "second", PortalEndpointRole.SOURCE, overlapping));
    }

    @Test
    void permitsReplacingTheSameEndpointAndUsingAnotherWorld() {
        PortalState state = new PortalState();
        PortalRouteState route = new PortalRouteState("route");
        route.source = endpoint("minecraft:overworld", 0, 0, 3, 4);
        state.routes.put(route.routeId, route);

        assertDoesNotThrow(() -> PortalRouteAdminMutator.ensureNoOverlap(
                state, route.routeId, PortalEndpointRole.SOURCE, route.source));
        assertDoesNotThrow(() -> PortalRouteAdminMutator.ensureNoOverlap(
                state, "other", PortalEndpointRole.SOURCE,
                endpoint("minecraft:the_nether", 0, 0, 3, 4)));
    }

    private static PortalEndpoint endpoint(String world, int minX, int minY, int maxX, int maxY) {
        return new PortalEndpoint(world, PortalBounds.between(
                new BlockPos(minX, minY, 0), new BlockPos(maxX, maxY, 0)));
    }
}
