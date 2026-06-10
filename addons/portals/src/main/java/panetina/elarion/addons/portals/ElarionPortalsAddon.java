package panetina.elarion.addons.portals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionPortalsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_portals");

    @Override
    public void initialize(ElarionApi api) {
        AddonConfigFiles.writeDefault("portals", "portals.yml", """
                portals:
                  ancient_gate:
                    state: "DORMANT"
                    owning-realm: "oak"
                    foreign-access-ability: "elarion.portal.foreign_access"
                """);
        api.system().abilities().register("elarion.portal.foreign_access");
        api.system().abilities().register("elarion.portal.manage");
        api.progressionApi().rewards().registerHandler("portal-state", (context, action) -> {
            api.system().events().emitProgression(new panetina.elarion.core.event.ElarionEventBus.ProgressionEvent(
                    "portal.state",
                    context.player().getUuid(),
                    action.parameters().getOrDefault("portal", context.rewardId())));
            return true;
        });
        LOGGER.info("Elarion Portals addon shell initialized");
    }
}
