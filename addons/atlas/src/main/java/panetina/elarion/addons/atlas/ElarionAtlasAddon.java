package panetina.elarion.addons.atlas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

/**
 * Common Atlas shell entrypoint. Functional services stay absent until their
 * owner contracts and bounded storage paths are implemented.
 */
public final class ElarionAtlasAddon implements ElarionAddon {
    public static final String MOD_ID = "elarion_atlas";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void initialize(ElarionApi ignored) {
        LOGGER.info("Elarion Atlas addon shell initialized; map services remain disabled");
    }
}
