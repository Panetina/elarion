package panetina.elarion.addons.npcs.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.npcs.model.DialogueAction;
import panetina.elarion.addons.npcs.model.DialogueCondition;
import panetina.elarion.addons.npcs.model.DialogueDefinition;
import panetina.elarion.addons.npcs.model.DialogueNode;
import panetina.elarion.addons.npcs.model.DialogueOption;
import panetina.elarion.addons.npcs.model.DialoguePrompt;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcUiConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class NpcConfigValidatorTest {
    @Test
    void acceptsValidNpcDialogueGraph() {
        List<String> errors = new ArrayList<>();

        NpcConfigValidator.validate(
                Map.of("banker", new NpcDefinition("banker", "Banker", "", "skin", "portrait", "dialogue", true)),
                Map.of("skin", new NpcSkinProfile("skin", "Skin", "player_body", "", "Panyel",
                        "placeholder", "", "")),
                Map.of("portrait", new NpcPortraitProfile("portrait", "Portrait", "player_head", "", "Panyel",
                        "placeholder", "")),
                Map.of("dialogue", dialogue("dialogue", "intro", "", "close")),
                Set.of("has_realm")::contains,
                Set.of("close")::contains,
                errors);

        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void reportsBrokenReferencesClearly() {
        List<String> errors = new ArrayList<>();

        NpcConfigValidator.validate(
                Map.of("banker", new NpcDefinition(
                        "banker",
                        "Banker",
                        "",
                        "missing_skin",
                        "missing_portrait",
                        "missing",
                        List.of(""),
                        "badability",
                        -1.0D,
                        true)),
                Map.of(),
                Map.of(),
                Map.of("dialogue", dialogue("dialogue", "missing_root", "missing_next", "unknown_action")),
                Set.of()::contains,
                Set.of("close")::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains("unknown skin missing_skin")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("unknown portrait missing_portrait")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("unknown dialogue missing")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("required-ability must be namespaced")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("interaction-range-blocks cannot be negative")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("tags cannot be blank")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("missing root node missing_root")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("invalid next node missing_next")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("unknown condition has_realm")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("unknown action unknown_action")));
    }

    @Test
    void reportsUnknownSkinAndPortraitTypes() {
        List<String> errors = new ArrayList<>();

        NpcConfigValidator.validate(
                Map.of(),
                Map.of("bad_skin", new NpcSkinProfile("bad_skin", "Bad", "bad", "", "", "", "", "")),
                Map.of("bad_portrait", new NpcPortraitProfile("bad_portrait", "Bad", "bad", "", "", "", "")),
                Map.of(),
                Set.of()::contains,
                Set.of()::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains("skin bad_skin: unknown type bad")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("portrait bad_portrait: unknown type bad")));
    }

    @Test
    void acceptsSupportedSkinAndPortraitProfileTypes() {
        List<String> errors = new ArrayList<>();

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(
                        "placeholder", new NpcSkinProfile("placeholder", "Placeholder", "placeholder", "", "", "", "", ""),
                        "texture", new NpcSkinProfile("texture", "Texture", "texture", "elarion:textures/entity/npc/mara.png", "", "", "", ""),
                        "player_body", new NpcSkinProfile("player_body", "Player Body", "player_body", "", "Panyel",
                                "texture", "elarion:textures/entity/npc/fallback.png", "")),
                Map.of(
                        "placeholder", new NpcPortraitProfile("placeholder", "Placeholder", "placeholder", "", "", "", ""),
                        "texture", new NpcPortraitProfile("texture", "Texture", "texture", "elarion:textures/gui/npc/mara.png", "", "", ""),
                        "player_head", new NpcPortraitProfile("player_head", "Player Head", "player_head", "", "Panyel",
                                "texture", "elarion:textures/gui/npc/fallback.png")),
                Map.of(),
                Set.of()::contains,
                Set.of()::contains,
                errors);

        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void keepsDialogueSoundAndVoiceMetadata() {
        DialogueOption option = new DialogueOption(
                "ask",
                "",
                "",
                "minecraft:ui.button.click",
                "voice/banker/ask.ogg",
                "intro",
                List.of(),
                List.of(),
                DialoguePrompt.NONE,
                false);
        DialogueNode node = new DialogueNode(
                "intro",
                "Hello",
                "minecraft:entity.villager.ambient",
                "voice/banker/intro.ogg",
                List.of(),
                List.of(option));

        assertEquals("ask", option.buttonText());
        assertEquals("ask", option.playerText());
        assertEquals("minecraft:ui.button.click", option.sound());
        assertEquals("voice/banker/ask.ogg", option.voice());
        assertEquals("minecraft:entity.villager.ambient", node.sound());
        assertEquals("voice/banker/intro.ogg", node.voice());
    }

    @Test
    void defaultsEnableTypingAndRelationPresentation() {
        NpcUiConfig defaults = NpcUiConfig.defaults();

        assertTrue(defaults.showRelationBar());
        assertTrue(defaults.typingEnabled());
        assertTrue(defaults.typingClickCompletes());
        assertEquals(45, defaults.typingCharactersPerSecond());
        assertEquals(16, defaults.compactButtonHeight());
        assertEquals(2, defaults.optionColumnsWide());
        assertEquals(60, defaults.minimumUiScalePercent());
        assertEquals(18, defaults.optionRowHeight());
        assertEquals(6, defaults.visibleOptionRows());
        assertEquals(6, defaults.scrollbarWidth());
        assertEquals(6.0D, defaults.defaultInteractionRangeBlocks());
    }

    @Test
    void validatesNumberPromptActionsAndBounds() {
        List<String> errors = new ArrayList<>();
        DialoguePrompt prompt = new DialoguePrompt(
                "number",
                "How many currency?",
                "elarion:economy_deposit_currency_amount",
                10,
                1L,
                0L);
        DialogueNode node = new DialogueNode(
                "intro",
                "Hello",
                "",
                "",
                List.of(),
                List.of(new DialogueOption(
                        "deposit",
                        "Deposit",
                        "I will deposit.",
                        "",
                        "",
                        "",
                        List.of(),
                        List.of(),
                        prompt,
                        false)));

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("dialogue", new DialogueDefinition("dialogue", "intro", Map.of("intro", node))),
                Set.of()::contains,
                Set.of("elarion:economy_deposit_currency_amount")::contains,
                errors);

        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void reportsInvalidNumberPromptClearly() {
        List<String> errors = new ArrayList<>();
        DialoguePrompt prompt = new DialoguePrompt("text", "", "missing", 11, 5L, 2L);
        DialogueNode node = new DialogueNode(
                "intro",
                "Hello",
                "",
                "",
                List.of(),
                List.of(new DialogueOption(
                        "deposit",
                        "Deposit",
                        "I will deposit.",
                        "",
                        "",
                        "",
                        List.of(),
                        List.of(),
                        prompt,
                        false)));

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("dialogue", new DialogueDefinition("dialogue", "intro", Map.of("intro", node))),
                Set.of()::contains,
                Set.of()::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains("unknown prompt type text")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("prompt question cannot be blank")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("unknown prompt action missing")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("prompt max-digits must be between 1 and 10")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("prompt min-amount cannot be greater than max-amount")));
    }

    @Test
    void npcDefinitionKeepsServiceMetadataImmutable() {
        NpcDefinition definition = new NpcDefinition(
                "banker",
                "Banker",
                "",
                "skin",
                "portrait",
                "dialogue",
                List.of("bank", "worldheart"),
                "elarion.bank.use",
                8.0D,
                true);

        assertEquals(List.of("bank", "worldheart"), definition.tags());
        assertEquals("elarion.bank.use", definition.requiredAbility());
        assertEquals(8.0D, definition.interactionRangeBlocks());
    }

    @Test
    void nestedDialoguePathBecomesStableSlashId() {
        assertEquals("quest_pack/mara",
                NpcConfigLoader.dialogueId(
                        Path.of("dialogues"),
                        Path.of("dialogues", "quest_pack", "mara.yml")));
    }

    private static DialogueDefinition dialogue(String id, String root, String next, String action) {
        DialogueNode node = new DialogueNode(
                "intro",
                "Hello",
                "minecraft:entity.villager.ambient",
                "",
                List.of(),
                List.of(new DialogueOption(
                        "option",
                        "Option",
                        "Player option",
                        "minecraft:ui.button.click",
                        "",
                        next,
                        List.of(new DialogueCondition("has_realm", Map.of("realm", "oak"))),
                        List.of(new DialogueAction(action, Map.of(), false)),
                        DialoguePrompt.NONE,
                        false)));
        return new DialogueDefinition(id, root, Map.of("intro", node));
    }
}
