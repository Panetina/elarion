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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void recoverableNullRowsDoNotDiscardValidPortalState() throws Exception {
        UUID player = UUID.randomUUID();
        Files.writeString(root.resolve("state.json"), """
                {
                  "routes": {
                    "nether": {"routeId": "nether", "unlocked": true},
                    "damaged-endpoint": {
                      "routeId": "damaged-endpoint",
                      "source": {"worldId": "minecraft:overworld", "bounds": null}
                    },
                    "discarded": null,
                    "": {"routeId": "blank"}
                  },
                  "entitlements": {
                    "%s|nether": {
                      "playerId": "%s",
                      "routeId": "nether",
                      "createdAt": 10,
                      "sourceWindowStart": 5
                    },
                    "discarded": null,
                    "missing-player": {"routeId": "nether"},
                    "missing-route": {"playerId": "%s"}
                  },
                  "freePassages": {
                    "%s|realm1": "RETURN_AVAILABLE",
                    "discarded": null,
                    "": "COMPLETED"
                  }
                }
                """.formatted(player, player, player, player));
        PortalStorage storage = new PortalStorage(LoggerFactory.getLogger("portal-test"), root);

        PortalState loaded = storage.load(root);

        assertEquals(2, loaded.routes.size());
        assertTrue(loaded.routes.get("nether").unlocked);
        assertNull(loaded.routes.get("damaged-endpoint").source);
        assertEquals(1, loaded.entitlements.size());
        assertEquals("nether", loaded.entitlements.get(player + "|nether").routeId());
        assertEquals(1, loaded.freePassages.size());
        assertEquals(PortalFreePassageState.RETURN_AVAILABLE,
                loaded.freePassages.get(player + "|realm1"));
        assertTrue(Files.exists(root.resolve("state.json")));
    }

    @Test
    void explicitNullCollectionsLoadAsMutableEmptyCollections() throws Exception {
        Files.writeString(root.resolve("state.json"), """
                {"routes": null, "entitlements": null, "freePassages": null}
                """);
        PortalStorage storage = new PortalStorage(LoggerFactory.getLogger("portal-test"), root);

        PortalState loaded = storage.load(root);

        loaded.routes.put("nether", new PortalRouteState("nether"));
        assertEquals(1, loaded.routes.size());
        assertTrue(loaded.entitlements.isEmpty());
        assertTrue(loaded.freePassages.isEmpty());
        assertTrue(Files.exists(root.resolve("state.json")));
    }
}
