package panetina.elarion.addons.portals.storage;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.portals.model.PortalAxis;
import panetina.elarion.addons.portals.model.PortalBounds;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalReturnEntitlement;
import panetina.elarion.addons.portals.model.PortalRouteState;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalStorageTest {
    @TempDir
    Path root;

    @Test
    void roundTripsRoutesAndEntitlements() {
        PortalState state = new PortalState();
        PortalRouteState route = new PortalRouteState("nether");
        route.unlocked = true;
        route.source = new PortalEndpoint("minecraft:overworld",
                new PortalBounds(0, 1, 2, 0, 4, 5, PortalAxis.X));
        state.routes.put("nether", route);
        UUID player = UUID.randomUUID();
        state.entitlements.put(player + "|nether",
                new PortalReturnEntitlement(player, "nether", 10, 5));
        state.freePassages.put(player + "|realm1", PortalFreePassageState.RETURN_AVAILABLE);
        PortalStorage storage = new PortalStorage(LoggerFactory.getLogger("portal-test"), root);

        storage.save(root, state);
        PortalState loaded = storage.load(root);

        assertTrue(loaded.routes.get("nether").unlocked);
        assertEquals(route.source, loaded.routes.get("nether").source);
        assertEquals("nether", loaded.entitlements.get(player + "|nether").routeId());
        assertEquals(PortalFreePassageState.RETURN_AVAILABLE,
                loaded.freePassages.get(player + "|realm1"));
    }
}
