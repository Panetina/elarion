package panetina.elarion.addons.worlds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.worlds.api.ElarionWorldsApi;
import panetina.elarion.addons.worlds.command.WorldCommands;
import panetina.elarion.addons.worlds.config.WorldsConfigDescriptors;
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
        WorldsConfigDescriptors.register(api.system().configs(), config);
        api.system().worldResets().setOperator(new panetina.elarion.core.api.reset.WorldResetOperator() {
            @Override public boolean exists(net.minecraft.server.MinecraftServer server, String worldId) {
                return worlds.hasManagedWorld(worldId);
            }

            @Override public java.util.concurrent.CompletionStage<Void> regenerate(
                    net.minecraft.server.MinecraftServer server, String worldId) {
                return worlds.regenerate(worldId);
            }

            @Override public java.util.Collection<String> worldIds() {
                return worlds.destinationNames();
            }

            @Override public java.util.List<java.nio.file.Path> backupTargets(
                    net.minecraft.server.MinecraftServer server, String worldId) {
                return worlds.persistentWorldBackupTargets(worldId);
            }
        });

        api.system().abilities().register("elarion.world.manage");
        api.system().commands().registerAdminSubcommand(() -> WorldCommands.create(worlds));
        rules.registerEvents();
        worlds.registerEvents();
        LOGGER.info("Elarion Worlds initialized");
    }
}
