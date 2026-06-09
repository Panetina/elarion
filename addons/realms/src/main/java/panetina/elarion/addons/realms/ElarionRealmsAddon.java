package panetina.elarion.addons.realms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionRealmsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_realms");

    @Override
    public void initialize(ElarionApi api) {
        api.abilities().register("elarion.realm.manage");
        LOGGER.info("Elarion Realms addon shell initialized");
    }
}
