package panetina.elarion.addons.worlds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionWorldsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_worlds");

    @Override
    public void initialize(ElarionApi api) {
        api.abilities().register("elarion.world.manage");
        LOGGER.info("Elarion Worlds addon shell initialized");
    }
}
