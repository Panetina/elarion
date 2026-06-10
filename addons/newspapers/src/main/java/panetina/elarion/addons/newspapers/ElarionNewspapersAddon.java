package panetina.elarion.addons.newspapers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionNewspapersAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_newspapers");

    @Override
    public void initialize(ElarionApi api) {
        api.system().abilities().register("elarion.newspaper.publish");
        api.system().abilities().register("elarion.newspaper.manage");
        LOGGER.info("Elarion Newspapers addon shell initialized");
    }
}
