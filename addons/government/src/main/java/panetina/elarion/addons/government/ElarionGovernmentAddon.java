package panetina.elarion.addons.government;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionGovernmentAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_government");

    @Override
    public void initialize(ElarionApi api) {
        api.abilities().register("elarion.government.manage");
        LOGGER.info("Elarion Government addon shell initialized");
    }
}
