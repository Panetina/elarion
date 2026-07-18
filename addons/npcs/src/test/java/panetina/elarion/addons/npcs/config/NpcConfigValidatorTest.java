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
import panetina.elarion.addons.npcs.model.NpcPresentationKind;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcTradeCatalogDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeEnchantmentDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
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
    void reportsInvalidTaxJurisdictionPolicy() {
        List<String> errors = new ArrayList<>();
        NpcDefinition npc = new NpcDefinition(
                "merchant", "Merchant", "", "", "", "", "",
                "world:not a world", List.of(), "", 0.0D, false);

        NpcConfigValidator.validate(
                Map.of("merchant", npc), Map.of(), Map.of(), Map.of(), Map.of(),
                ignored -> true, ignored -> true, errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains("invalid tax-jurisdiction")));
    }

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
    void acceptsNonMutatingTradePresentationShell() {
        List<String> errors = new ArrayList<>();
        DialogueOption buy = new DialogueOption(
                "buy",
                "Buy",
                "Show me what you sell.",
                "",
                "",
                "buy",
                "trade",
                List.of(),
                List.of(),
                DialoguePrompt.NONE,
                false);
        DialogueOption back = new DialogueOption(
                "back",
                "Back",
                "Let us talk.",
                "",
                "",
                "back",
                "intro",
                List.of(),
                List.of(),
                DialoguePrompt.NONE,
                false);
        DialogueNode intro = new DialogueNode(
                "intro",
                "Hello.",
                "",
                "",
                List.of(),
                List.of(buy));
        DialogueNode trade = new DialogueNode(
                "trade",
                "Trade.",
                "",
                "",
                NpcPresentationKind.TRADE,
                List.of(),
                List.of(),
                List.of(back));

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("dialogue", new DialogueDefinition(
                        "dialogue", "intro", Map.of("intro", intro, "trade", trade))),
                Set.of()::contains,
                Set.of()::contains,
                errors);

        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void acceptsNpcTradeCatalogReferences() {
        List<String> errors = new ArrayList<>();

        NpcConfigValidator.validate(
                Map.of("trader", new NpcDefinition(
                        "trader", "Trader", "", "skin", "portrait", "dialogue",
                        "market", List.of("trade"), "", 0.0D, true)),
                Map.of("skin", new NpcSkinProfile("skin", "Skin", "player_body", "", "Panyel",
                        "placeholder", "", "")),
                Map.of("portrait", new NpcPortraitProfile("portrait", "Portrait", "player_head", "", "Panyel",
                        "placeholder", "")),
                Map.of("dialogue", dialogue("dialogue", "intro", "", "close")),
                Map.of("market", new NpcTradeCatalogDefinition("market", List.of(
                        new NpcTradeOfferDefinition(
                                "ticket",
                                "buy",
                                "Nether Gate Ticket",
                                "2 in stock",
                                "minecraft:paper",
                                2,
                                "Gate Ticket",
                                List.of("Valid for one opening window."),
                                List.of(new NpcTradeEnchantmentDefinition("minecraft:protection", 1)),
                                1,
                                "portal.ticket.nether",
                                25L,
                                true),
                        new NpcTradeOfferDefinition(
                                "cobblestone_buyback",
                                "sell",
                                "Cobblestone",
                                "Trader buys clean stone.",
                                "minecraft:cobblestone",
                                1,
                                "",
                                List.of(),
                                List.of(),
                                0,
                                "npc.sell.cobblestone",
                                1L,
                                0,
                                0,
                                0L,
                                false,
                                "exact_item",
                                "vanilla_only",
                                64,
                                "placed_npc",
                                "ticket")))),
                Set.of("has_realm")::contains,
                Set.of("close")::contains,
                errors);

        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void rejectsPlacedNpcSellOffersWithoutValidBuyDestination() {
        List<String> errors = new ArrayList<>();

        NpcConfigValidator.validate(
                Map.of("trader", new NpcDefinition(
                        "trader", "Trader", "", "skin", "portrait", "dialogue",
                        "market", List.of("trade"), "", 0.0D, true)),
                Map.of("skin", new NpcSkinProfile("skin", "Skin", "player_body", "", "Panyel",
                        "placeholder", "", "")),
                Map.of("portrait", new NpcPortraitProfile("portrait", "Portrait", "player_head", "", "Panyel",
                        "placeholder", "")),
                Map.of("dialogue", dialogue("dialogue", "intro", "", "close")),
                Map.of("market", new NpcTradeCatalogDefinition("market", List.of(
                        new NpcTradeOfferDefinition(
                                "missing_destination",
                                "sell",
                                "Cobblestone",
                                "Trader buys clean stone.",
                                "minecraft:cobblestone",
                                1,
                                "",
                                List.of(),
                                List.of(),
                                0,
                                "npc.sell.cobblestone",
                                1L,
                                0,
                                0,
                                0L,
                                true,
                                "exact_item",
                                "vanilla_only",
                                64,
                                "placed_npc"),
                        new NpcTradeOfferDefinition(
                                "wrong_destination",
                                "sell",
                                "Stone",
                                "Trader buys clean stone.",
                                "minecraft:stone",
                                1,
                                "",
                                List.of(),
                                List.of(),
                                0,
                                "npc.sell.stone",
                                1L,
                                0,
                                0,
                                0L,
                                true,
                                "exact_item",
                                "vanilla_only",
                                64,
                                "placed_npc",
                                "missing_destination")))),
                Set.of("has_realm")::contains,
                Set.of("close")::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains(
                "missing_destination: destination-offer is required")));
        assertTrue(errors.stream().anyMatch(error -> error.contains(
                "wrong_destination: destination-offer must point to a buy offer")));
    }

    @Test
    void rejectsBrokenNpcTradeCatalogs() {
        List<String> errors = new ArrayList<>();

        NpcConfigValidator.validate(
                Map.of("trader", new NpcDefinition(
                        "trader", "Trader", "", "skin", "portrait", "dialogue",
                        "missing_catalog", List.of("trade"), "", 0.0D, true)),
                Map.of("skin", new NpcSkinProfile("skin", "Skin", "player_body", "", "Panyel",
                        "placeholder", "", "")),
                Map.of("portrait", new NpcPortraitProfile("portrait", "Portrait", "player_head", "", "Panyel",
                        "placeholder", "")),
                Map.of("dialogue", dialogue("dialogue", "intro", "", "close")),
                Map.of("bad", new NpcTradeCatalogDefinition("bad", List.of(new NpcTradeOfferDefinition(
                        "",
                        "sell",
                        "",
                        "",
                        "bad id",
                        0,
                        "",
                        List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"),
                        List.of(new NpcTradeEnchantmentDefinition("bad id", 0)),
                        -1,
                        "Bad Price Key!",
                        0L,
                        true)))),
                Set.of("has_realm")::contains,
                Set.of("close")::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains("unknown trade catalog missing_catalog")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("offer : id cannot be blank")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("max-quantity must be between 1 and 64")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("label cannot be blank")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("invalid item id")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("count must be between 1 and 64")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("custom-model-data cannot be negative")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("price-key must use lowercase")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("price must be positive")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("lore cannot exceed 8 lines")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("invalid enchantment id")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("enchantment level must be between 1 and 255")));
    }

    @Test
    void rejectsMutatingTradePresentationOptions() {
        List<String> errors = new ArrayList<>();
        DialoguePrompt prompt = new DialoguePrompt(
                "number",
                "How many?",
                "elarion:future_trade",
                3,
                1L,
                0L);
        DialogueOption buy = new DialogueOption(
                "buy",
                "Buy",
                "I buy it.",
                "",
                "",
                "buy",
                "",
                List.of(),
                List.of(new DialogueAction("elarion:future_trade", Map.of(), true)),
                prompt,
                false);
        DialogueNode trade = new DialogueNode(
                "trade",
                "Trade.",
                "",
                "",
                NpcPresentationKind.TRADE,
                List.of(),
                List.of(),
                List.of(buy));

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("dialogue", new DialogueDefinition("dialogue", "trade", Map.of("trade", trade))),
                Set.of()::contains,
                Set.of("elarion:future_trade")::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains(
                "trade presentation options cannot execute actions")));
        assertTrue(errors.stream().anyMatch(error -> error.contains(
                "trade presentation options cannot use prompts")));
    }

    @Test
    void reportsUnreachableDialogueNodes() {
        List<String> errors = new ArrayList<>();
        DialogueNode intro = new DialogueNode(
                "intro", "Hello", "", "", List.of(), List.of());
        DialogueNode hidden = new DialogueNode(
                "hidden", "You should not see this", "", "", List.of(), List.of());

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("dialogue", new DialogueDefinition(
                        "dialogue", "intro", Map.of("intro", intro, "hidden", hidden))),
                Set.of()::contains,
                Set.of()::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains(
                "node hidden: unreachable from root intro")), errors.toString());
    }

    @Test
    void acceptsReachableTerminalDialogueNodes() {
        List<String> errors = new ArrayList<>();
        DialogueNode intro = new DialogueNode(
                "intro", "Hello", "", "", List.of(),
                List.of(new DialogueOption(
                        "finish",
                        "Finish",
                        "Goodbye",
                        "",
                        "",
                        "ending",
                        List.of(),
                        List.of(),
                        DialoguePrompt.NONE,
                        false)));
        DialogueNode ending = new DialogueNode(
                "ending", "Done", "", "", List.of(), List.of());

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("dialogue", new DialogueDefinition(
                        "dialogue", "intro", Map.of("intro", intro, "ending", ending))),
                Set.of()::contains,
                Set.of()::contains,
                errors);

        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void reportsDuplicateOptionIdsWithinNode() {
        List<String> errors = new ArrayList<>();
        DialogueNode intro = new DialogueNode(
                "intro",
                "Hello",
                "",
                "",
                List.of(),
                List.of(
                        new DialogueOption("repeat", "One", "One", "", "", "", List.of(), List.of(),
                                DialoguePrompt.NONE, false),
                        new DialogueOption("repeat", "Two", "Two", "", "", "", List.of(), List.of(),
                                DialoguePrompt.NONE, false)));

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("dialogue", new DialogueDefinition("dialogue", "intro", Map.of("intro", intro))),
                Set.of()::contains,
                Set.of()::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains("duplicate option id")), errors.toString());
    }

    @Test
    void reportsDuplicateVariantIdsWithinNode() {
        List<String> errors = new ArrayList<>();
        DialogueNode intro = new DialogueNode(
                "intro",
                "Hello",
                "",
                "",
                NpcPresentationKind.DIALOGUE,
                List.of(),
                List.of(
                        new panetina.elarion.addons.npcs.model.DialogueTextVariant(
                                "memory", "One", "", "", List.of()),
                        new panetina.elarion.addons.npcs.model.DialogueTextVariant(
                                "memory", "Two", "", "", List.of())),
                List.of());

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("dialogue", new DialogueDefinition("dialogue", "intro", Map.of("intro", intro))),
                Set.of()::contains,
                Set.of()::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains("duplicate variant id")), errors.toString());
    }

    @Test
    void reportsServicePresentationWithoutExitOption() {
        List<String> errors = new ArrayList<>();
        DialogueNode trade = new DialogueNode(
                "trade",
                "Trade",
                "",
                "",
                NpcPresentationKind.TRADE,
                List.of(),
                List.of(),
                List.of(new DialogueOption(
                        "refresh", "Refresh", "Refresh", "", "", "trade",
                        List.of(), List.of(), DialoguePrompt.NONE, false)));

        NpcConfigValidator.validate(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("dialogue", new DialogueDefinition("dialogue", "trade", Map.of("trade", trade))),
                Set.of()::contains,
                Set.of()::contains,
                errors);

        assertTrue(errors.stream().anyMatch(error -> error.contains(
                "trade presentation should provide an exit option")), errors.toString());
    }

    @Test
    void validatesDurableStoryAndHistoryMetadata() {
        List<String> errors = new ArrayList<>();
        DialogueOption option = new DialogueOption(
                "pledge", "Pledge", "I pledge.", "", "", "", "ending",
                List.of(),
                List.of(
                        new DialogueAction("elarion_npcs:set_story_flag", Map.of(), false),
                        new DialogueAction("elarion_npcs:set_reentry_node", Map.of("node", "missing"), false),
                        new DialogueAction("elarion_npcs:set_ending", Map.of("ending", "allied"), true)),
                DialoguePrompt.NONE, false, true);
        DialogueNode root = new DialogueNode("root", "Choose", "", "", List.of(), List.of(option));
        DialogueNode ending = new DialogueNode("ending", "Done", "", "", List.of(), List.of());

        NpcConfigValidator.validate(Map.of(), Map.of(), Map.of(),
                Map.of("story", new DialogueDefinition("story", "root", Map.of("root", root, "ending", ending))),
                Set.of()::contains,
                Set.of("elarion_npcs:set_story_flag", "elarion_npcs:set_reentry_node",
                        "elarion_npcs:set_ending")::contains,
                errors);

        assertTrue(option.oneTime());
        assertTrue(errors.stream().anyMatch(error -> error.contains("story flag action requires flag")), errors.toString());
        assertTrue(errors.stream().anyMatch(error -> error.contains("invalid re-entry node missing")), errors.toString());
        assertTrue(errors.stream().anyMatch(error -> error.contains("history-worthy action requires history-outcome")),
                errors.toString());
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
