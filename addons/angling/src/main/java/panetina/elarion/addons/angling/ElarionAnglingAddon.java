package panetina.elarion.addons.angling;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.angling.fishing.AnglingCatchCommitCoordinator;
import panetina.elarion.addons.angling.fishing.AnglingCatchDeliveryService;
import panetina.elarion.addons.angling.fishing.AnglingFishingService;
import panetina.elarion.addons.angling.registry.AnglingRegistries;
import panetina.elarion.addons.angling.network.AnglingNetworking;
import panetina.elarion.addons.angling.metric.AnglingMetricDescriptors;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.storage.JsonStateStorage;

import java.util.Objects;

/**
 * Fabric bootstrap boundary for the Angling port.
 *
 * <p>Completed foundation and content slices register here. Fishing outcomes
 * remain disabled until their server-authoritative runtime, tests, and parity
 * dispositions validate together.</p>
 */
public final class ElarionAnglingAddon implements ElarionAddon {
    public static final String MOD_ID = "elarion_angling";
    public static final String SOURCE_REVISION = "016161dfc2d556d20fa641cd275e18c539256d4d";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void initialize(ElarionApi api) {
        Objects.requireNonNull(api, "api");
        AnglingRegistries.initialize();
        AnglingNetworking.registerPayloadTypes();
        api.metrics().registerDescriptors(AnglingMetricDescriptors.ALL);
        AnglingCatchDeliveryService delivery = new AnglingCatchDeliveryService(api);
        AnglingCatchCommitCoordinator catches = new AnglingCatchCommitCoordinator(api, delivery);
        AnglingFishingService fishing = new AnglingFishingService(api, catches);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var root = JsonStateStorage.elarionRoot(server);
            delivery.bind(server, root);
            catches.bind(root);
            fishing.bind(server);
            long recovered = catches.snapshot().recoveredCatches();
            if (recovered > 0) LOGGER.info("Recovered {} incomplete Angling catch transactions", recovered);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            fishing.unbind();
            catches.shutdown();
            delivery.unbind();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                delivery.reconcile(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                delivery.unload(handler.player.getUuid()));
        LOGGER.info("Elarion Angling foundation and completed content registries initialized; fishing remains disabled");
    }
}
