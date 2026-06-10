package panetina.elarion.addons.jail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionJailAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_jail");

    @Override
    public void initialize(ElarionApi api) {
        AddonConfigFiles.writeDefault("jail", "jail.yml", """
                defaults:
                  block-portals: true
                  block-voice-chat: true
                cells: {}
                """);
        api.system().abilities().register("elarion.jail.manage");
        LOGGER.info("Elarion Jail addon shell initialized");
    }
}
