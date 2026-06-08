package panetina.elarion.addons.communities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionCommunitiesAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_communities");

    @Override
    public void initialize(ElarionApi api) {
        api.abilities().register("elarion.community.manage");
        LOGGER.info("Elarion Communities addon shell initialized");
    }
}
