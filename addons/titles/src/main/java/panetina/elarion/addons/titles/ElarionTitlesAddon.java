package panetina.elarion.addons.titles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionTitlesAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_titles");

    @Override
    public void initialize(ElarionApi api) {
        api.system().abilities().register("elarion.title.render_under_username");
        LOGGER.info("Elarion Titles renderer initialized");
    }
}
