package panetina.elarion.addons.angling.delight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

import java.util.Objects;

/** Fabric bootstrap boundary for the separate Farmer's Delight integration. */
public final class ElarionAnglingDelightAddon implements ElarionAddon {
    public static final String MOD_ID = "elarion_angling_delight";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void initialize(ElarionApi api) {
        Objects.requireNonNull(api, "api");
        LOGGER.info("Elarion Angling Delight Fabric port foundation initialized; content remains disabled");
    }
}
