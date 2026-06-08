package panetina.elarion.addons.names;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionNamesAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_names");

    @Override
    public void initialize(ElarionApi api) {
        api.abilities().register("elarion.identity.nickname.manage");
        LOGGER.info("Elarion Names addon shell initialized");
    }
}
