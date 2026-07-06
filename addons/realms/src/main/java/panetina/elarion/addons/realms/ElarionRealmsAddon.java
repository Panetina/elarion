package panetina.elarion.addons.realms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.addons.realms.config.RealmConfigDescriptors;
import panetina.elarion.addons.realms.config.RealmProtectionConfig;
import panetina.elarion.addons.realms.service.RealmProtectionService;

public final class ElarionRealmsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_realms");

    @Override
    public void initialize(ElarionApi api) {
        api.system().abilities().register("elarion.realm.manage");
        RealmProtectionConfig config = RealmProtectionConfig.load();
        RealmConfigDescriptors.register(api.system().configs(), () -> config);
        RealmProtectionService protection =
                new RealmProtectionService(api, config);
        protection.register();
        LOGGER.info("Elarion Realms protection initialized");
    }
}
