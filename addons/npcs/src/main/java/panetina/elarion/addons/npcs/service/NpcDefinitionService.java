package panetina.elarion.addons.npcs.service;

import org.slf4j.Logger;
import panetina.elarion.addons.npcs.config.NpcConfig;
import panetina.elarion.addons.npcs.config.NpcConfigLoader;
import panetina.elarion.addons.npcs.model.DialogueDefinition;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcUiConfig;
import panetina.elarion.core.api.ElarionApi;

import java.util.Collection;
import java.util.Optional;

public final class NpcDefinitionService {
    private final Logger logger;
    private final NpcConfigLoader loader;
    private NpcConfig config = new NpcConfig(null, null, null, null, null);

    public NpcDefinitionService(Logger logger, NpcConfigLoader loader) {
        this.logger = logger;
        this.loader = loader;
    }

    public void load(ElarionApi api) {
        config = loader.load(api);
    }

    public void reload(ElarionApi api) {
        load(api);
        logger.info("Reloaded NPC definitions");
    }

    public Collection<NpcDefinition> npcs() {
        return config.npcs().values();
    }

    public Collection<NpcSkinProfile> skins() {
        return config.skins().values();
    }

    public Collection<NpcPortraitProfile> portraits() {
        return config.portraits().values();
    }

    public Collection<DialogueDefinition> dialogues() {
        return config.dialogues().values();
    }

    public Optional<NpcDefinition> npc(String id) {
        return Optional.ofNullable(config.npcs().get(id));
    }

    public Optional<NpcSkinProfile> skin(String id) {
        return Optional.ofNullable(config.skins().get(id));
    }

    public Optional<NpcPortraitProfile> portrait(String id) {
        return Optional.ofNullable(config.portraits().get(id));
    }

    public Optional<DialogueDefinition> dialogue(String id) {
        return Optional.ofNullable(config.dialogues().get(id));
    }

    public NpcUiConfig ui() {
        return config.ui();
    }
}
