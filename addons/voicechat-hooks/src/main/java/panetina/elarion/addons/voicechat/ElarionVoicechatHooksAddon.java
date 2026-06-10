package panetina.elarion.addons.voicechat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionVoicechatHooksAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_voicechat_hooks");

    @Override
    public void initialize(ElarionApi api) {
        api.system().abilities().register("elarion.voicechat.override");
        LOGGER.info("Elarion Voice Chat Hooks addon shell initialized");
    }
}
