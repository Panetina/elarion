package panetina.elarion.addons.tablist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionTablistAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_tablist");

    @Override
    public void initialize(ElarionApi api) {
        api.system().abilities().register("elarion.tablist.manage");
        LOGGER.info("Elarion Tablist addon shell initialized");
    }
}
