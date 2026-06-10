package panetina.elarion.addons.contributions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionContributionsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_contributions");

    @Override
    public void initialize(ElarionApi api) {
        AddonConfigFiles.writeDefault("contributions", "projects.yml", """
                projects:
                  example_project:
                    title: "Example Realm Project"
                    currency: "minecraft:diamond"
                    levels:
                      - required: 100
                        description: "First milestone"
                        reward: "welcome"
                """);
        api.system().abilities().register("elarion.contribution.manage");
        api.progressionApi().rewards().registerHandler("contribution-event", (context, action) -> {
            api.system().events().emitProgression(new panetina.elarion.core.event.ElarionEventBus.ProgressionEvent(
                    action.parameters().getOrDefault("event", "contribution.event"),
                    context.player().getUuid(),
                    action.parameters().getOrDefault("project", context.rewardId())));
            return true;
        });
        LOGGER.info("Elarion Contributions addon shell initialized");
    }
}
