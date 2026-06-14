package panetina.elarion.addons.npcs.config;

import panetina.elarion.addons.npcs.model.DialogueDefinition;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcUiConfig;

import java.util.Map;

public record NpcConfig(
        Map<String, NpcDefinition> npcs,
        Map<String, NpcSkinProfile> skins,
        Map<String, NpcPortraitProfile> portraits,
        Map<String, DialogueDefinition> dialogues,
        NpcUiConfig ui
) {
    public NpcConfig {
        npcs = npcs == null ? Map.of() : Map.copyOf(npcs);
        skins = skins == null ? Map.of() : Map.copyOf(skins);
        portraits = portraits == null ? Map.of() : Map.copyOf(portraits);
        dialogues = dialogues == null ? Map.of() : Map.copyOf(dialogues);
        ui = ui == null ? NpcUiConfig.defaults() : ui;
    }
}
