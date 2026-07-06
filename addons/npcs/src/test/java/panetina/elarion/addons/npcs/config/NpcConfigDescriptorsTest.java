package panetina.elarion.addons.npcs.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.npcs.model.DialogueAction;
import panetina.elarion.addons.npcs.model.DialogueCondition;
import panetina.elarion.addons.npcs.model.DialogueDefinition;
import panetina.elarion.addons.npcs.model.DialogueNode;
import panetina.elarion.addons.npcs.model.DialogueOption;
import panetina.elarion.addons.npcs.model.DialoguePrompt;
import panetina.elarion.addons.npcs.model.DialogueTextVariant;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcUiConfig;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcConfigDescriptorsTest {
    @Test
    void registersNpcDomain() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();

        NpcConfigDescriptors.register(registry, this::npcs, this::skins, this::portraits, this::dialogues, this::ui);

        ElarionConfigDomain domain = registry.domain("npcs").orElseThrow();
        assertEquals("npcs", domain.id());
        assertEquals("addons:npcs", domain.ownerModule());
        assertEquals("NPCs", domain.label());
        assertEquals("/e npc reload", domain.reloadCommand());
        assertEquals(List.of(
                "config/elarion/addons/npcs/npcs.yml",
                "config/elarion/addons/npcs/skins.yml",
                "config/elarion/addons/npcs/portraits.yml",
                "config/elarion/addons/npcs/ui.yml",
                "config/elarion/addons/npcs/dialogues/*.yml"), domain.files());
        assertTrue(domain.category("definitions").isPresent());
        assertTrue(domain.category("profiles").isPresent());
        assertTrue(domain.category("dialogues").isPresent());
        assertTrue(domain.category("ui").isPresent());
    }

    @Test
    void domainExposesNpcProfileDialogueAndUiValues() {
        ElarionConfigDomain domain = NpcConfigDescriptors.domain(
                this::npcs, this::skins, this::portraits, this::dialogues, this::ui);

        assertEquals(1, domain.entry("definitions", "npcs.count").orElseThrow().currentValue());
        assertEquals("worldheart_banker", domain.entry("definitions", "npcs.ids").orElseThrow().currentValue());
        assertEquals("Worldheart Banker",
                domain.entry("definitions", "npcs.worldheart_banker.display-name").orElseThrow().currentValue());
        assertEquals("banker_skin",
                domain.entry("definitions", "npcs.worldheart_banker.skin").orElseThrow().currentValue());
        assertEquals("npcs.banker",
                domain.entry("definitions", "npcs.worldheart_banker.required-ability").orElseThrow().currentValue());
        assertEquals("6.5",
                domain.entry("definitions", "npcs.worldheart_banker.interaction-range-blocks")
                        .orElseThrow().currentValue());

        assertEquals(1, domain.entry("profiles", "skins.count").orElseThrow().currentValue());
        assertEquals("banker_skin", domain.entry("profiles", "skins.ids").orElseThrow().currentValue());
        assertEquals("player_body", domain.entry("profiles", "skins.banker_skin.type").orElseThrow().currentValue());
        assertEquals(1, domain.entry("profiles", "portraits.count").orElseThrow().currentValue());
        assertEquals("banker_portrait", domain.entry("profiles", "portraits.ids").orElseThrow().currentValue());
        assertEquals("player_head",
                domain.entry("profiles", "portraits.banker_portrait.type").orElseThrow().currentValue());

        assertEquals(1, domain.entry("dialogues", "dialogues.count").orElseThrow().currentValue());
        assertEquals("worldheart_banker", domain.entry("dialogues", "dialogues.ids").orElseThrow().currentValue());
        assertEquals("intro", domain.entry("dialogues", "dialogues.worldheart_banker.root").orElseThrow()
                .currentValue());
        assertEquals(2, domain.entry("dialogues", "dialogues.worldheart_banker.nodes.count").orElseThrow()
                .currentValue());
        assertEquals(3, domain.entry("dialogues", "dialogues.worldheart_banker.options.count").orElseThrow()
                .currentValue());
        assertEquals(1, domain.entry("dialogues", "dialogues.worldheart_banker.actions.count").orElseThrow()
                .currentValue());
        assertEquals("elarion:economy_deposit_currency_amount",
                domain.entry("dialogues", "dialogues.worldheart_banker.actions.types").orElseThrow().currentValue());
        assertEquals(3, domain.entry("dialogues", "dialogues.worldheart_banker.conditions.count").orElseThrow()
                .currentValue());
        assertEquals("elarion:has_realm, elarion:quest_complete",
                domain.entry("dialogues", "dialogues.worldheart_banker.conditions.types").orElseThrow()
                        .currentValue());
        assertEquals(1, domain.entry("dialogues", "dialogues.worldheart_banker.variants.count").orElseThrow()
                .currentValue());
        assertEquals(1, domain.entry("dialogues", "dialogues.worldheart_banker.prompts.count").orElseThrow()
                .currentValue());

        assertEquals(430, domain.entry("ui", "ui.panel-width").orElseThrow().currentValue());
        assertEquals("7.0", domain.entry("ui", "ui.default-interaction-range-blocks").orElseThrow()
                .currentValue());
        assertEquals(true, domain.entry("ui", "ui.typing-enabled").orElseThrow().currentValue());
    }

    @Test
    void npcEntriesReadCurrentSupplierValues() {
        AtomicReference<List<NpcDefinition>> currentNpcs = new AtomicReference<>(npcs());
        ElarionConfigDomain domain = NpcConfigDescriptors.domain(
                currentNpcs::get, this::skins, this::portraits, this::dialogues, this::ui);

        ElarionConfigEntry<?> displayName = domain.entry(
                "definitions", "npcs.worldheart_banker.display-name").orElseThrow();
        assertEquals("Worldheart Banker", displayName.currentValue());

        currentNpcs.set(List.of(banker("Bank Clerk")));

        assertEquals("Bank Clerk", displayName.currentValue());
    }

    private List<NpcDefinition> npcs() {
        return List.of(banker("Worldheart Banker"));
    }

    private NpcDefinition banker(String displayName) {
        return new NpcDefinition(
                "worldheart_banker",
                displayName,
                "Handles deposits.",
                "banker_skin",
                "banker_portrait",
                "worldheart_banker",
                List.of("bank", "worldheart"),
                "npcs.banker",
                6.5D,
                true);
    }

    private List<NpcSkinProfile> skins() {
        return List.of(new NpcSkinProfile(
                "banker_skin",
                "Banker Skin",
                "player_body",
                "elarion:textures/entity/npc/banker.png",
                "Panyel",
                "texture",
                "elarion:textures/entity/npc/fallback.png",
                ""));
    }

    private List<NpcPortraitProfile> portraits() {
        return List.of(new NpcPortraitProfile(
                "banker_portrait",
                "Banker Portrait",
                "player_head",
                "",
                "Panyel",
                "texture",
                "elarion:textures/gui/npc/fallback.png"));
    }

    private List<DialogueDefinition> dialogues() {
        DialogueCondition realmCondition = new DialogueCondition("elarion:has_realm", Map.of("realm", "oak"));
        DialogueCondition questCondition = new DialogueCondition("elarion:quest_complete", Map.of("quest", "intro"));
        DialogueAction depositAction = new DialogueAction(
                "elarion:economy_deposit_currency_amount",
                Map.of("account", "personal"),
                true);
        DialoguePrompt prompt = new DialoguePrompt(
                "number",
                "How much?",
                "elarion:economy_deposit_currency_amount",
                9,
                1L,
                0L);
        DialogueOption deposit = new DialogueOption(
                "deposit",
                "Deposit",
                "I want to deposit.",
                "",
                "",
                "done",
                List.of(realmCondition),
                List.of(depositAction),
                prompt,
                false);
        DialogueOption close = new DialogueOption(
                "close",
                "Close",
                "Goodbye.",
                "",
                "",
                "",
                List.of(),
                List.of(),
                DialoguePrompt.NONE,
                true);
        DialogueNode intro = new DialogueNode(
                "intro",
                "Welcome.",
                "",
                "",
                List.of(realmCondition),
                List.of(new DialogueTextVariant(
                        "after_quest",
                        "Welcome back.",
                        "",
                        "",
                        List.of(questCondition))),
                List.of(deposit, close));
        DialogueNode done = new DialogueNode(
                "done",
                "Done.",
                "",
                "",
                List.of(),
                List.of(),
                List.of(close));
        return List.of(new DialogueDefinition("worldheart_banker", "intro", Map.of("intro", intro, "done", done)));
    }

    private NpcUiConfig ui() {
        return new NpcUiConfig(
                430,
                260,
                350,
                65,
                19,
                5,
                7,
                17,
                21,
                17,
                5,
                9,
                78,
                58,
                2,
                66,
                38,
                true,
                true,
                true,
                false,
                7.0D,
                true,
                48,
                true,
                false,
                5);
    }
}
