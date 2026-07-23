package panetina.elarion.addons.quests;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.quests.api.ElarionQuestsApi;
import panetina.elarion.addons.quests.command.QuestCommands;
import panetina.elarion.addons.quests.config.QuestConfigDescriptors;
import panetina.elarion.addons.quests.config.QuestConfigLoader;
import panetina.elarion.addons.quests.service.QuestDefinitionService;
import panetina.elarion.addons.quests.service.QuestProfileContributor;
import panetina.elarion.addons.quests.service.QuestRegistryHandlers;
import panetina.elarion.addons.quests.service.QuestStateService;
import panetina.elarion.addons.quests.storage.QuestStorage;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.api.reset.PlayerResetHandler;
import panetina.elarion.core.api.reset.PlayerResetResult;

public final class ElarionQuestsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_quests");

    @Override
    public void initialize(ElarionApi api) {
        QuestDefinitionService definitions = new QuestDefinitionService(new QuestConfigLoader(
                LOGGER, null, api.registries().conditions()::contains, api.registries().actions()::contains));
        QuestStateService states = new QuestStateService(LOGGER, api, definitions, new QuestStorage(LOGGER));
        new ElarionQuestsApi(definitions, states);
        api.system().playerResets().register(new PlayerResetHandler() {
            @Override public String id() { return "elarion_quests"; }
            @Override public java.util.Map<String, Long> preview(net.minecraft.server.MinecraftServer server) {
                return java.util.Map.of();
            }
            @Override public java.util.List<java.nio.file.Path> backupTargets(net.minecraft.server.MinecraftServer server) {
                return java.util.List.of(panetina.elarion.core.storage.JsonStateStorage
                        .addonStateRoot(server, "quests").resolve("state.json"));
            }
            @Override public PlayerResetResult reset(panetina.elarion.core.api.reset.PlayerResetContext context) {
                return PlayerResetResult.of("questRuntimeRecords", states.resetAllRuntimeState());
            }
        });
        api.system().profiles().registerContributor(new QuestProfileContributor(api.playerStats()));

        api.system().abilities().register("elarion.quest.admin");
        QuestRegistryHandlers.register(api, definitions, states);
        definitions.load();
        QuestConfigDescriptors.register(api.system().configs(), definitions::all);
        api.system().commands().registerAdminSubcommand(() -> QuestCommands.create(definitions, states));
        api.system().commands().registerHelpDescription(
                "/e quest ...", "Manage data-driven quest definitions and runtime questline state.");

        ServerLifecycleEvents.SERVER_STARTED.register(states::bind);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> states.save());
        ServerTickEvents.END_SERVER_TICK.register(states::tick);

        LOGGER.info("Elarion quests initialized with {} questline definitions", definitions.all().size());
    }
}
