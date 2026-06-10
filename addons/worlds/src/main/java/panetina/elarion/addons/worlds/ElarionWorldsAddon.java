package panetina.elarion.addons.worlds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.worlds.api.ElarionWorldsApi;
import panetina.elarion.addons.worlds.command.WorldCommands;
import panetina.elarion.addons.worlds.config.WorldsConfigManager;
import panetina.elarion.addons.worlds.service.WorldRuleService;
import panetina.elarion.addons.worlds.service.WorldService;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionWorldsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_worlds");

    @Override
    public void initialize(ElarionApi api) {
        WorldsConfigManager config = new WorldsConfigManager(LOGGER);
        config.load();
        WorldService worlds = new WorldService(LOGGER, api, config);
        WorldRuleService rules = new WorldRuleService(LOGGER, api, worlds);
        new ElarionWorldsApi(worlds);

        api.system().abilities().register("elarion.world.manage");
        api.system().commands().registerAdminSubcommand(() -> WorldCommands.create(worlds));
        rules.registerEvents();
        worlds.registerEvents();
        LOGGER.info("Elarion Worlds initialized");
    }
}
