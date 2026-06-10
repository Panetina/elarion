package panetina.elarion.addons.underworld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionUnderworldAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_underworld");

    @Override
    public void initialize(ElarionApi api) {
        AddonConfigFiles.writeDefault("underworld", "underworld.yml", """
                defaults:
                  block-portals: true
                  block-voice-chat: true
                  spawn-world: "minecraft:overworld"
                """);
        api.system().abilities().register("elarion.underworld.manage");
        LOGGER.info("Elarion Underworld addon shell initialized");
    }
}
