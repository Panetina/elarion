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
import panetina.elarion.addons.npcs.model.NpcPresentationKind;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcTradeCatalogDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
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
                "config/elarion/addons/npcs/trades.yml",
                "config/elarion/addons/npcs/ui.yml",
                "config/elarion/addons/npcs/dialogues/*.yml"), domain.files());
        assertTrue(domain.category("definitions").isPresent());
        assertTrue(domain.category("profiles").isPresent());
        assertTrue(domain.category("trades").isPresent());
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
        assertEquals("auto",
                domain.entry("definitions", "npcs.worldheart_banker.tax-jurisdiction").orElseThrow().currentValue());
        assertEquals("unaffiliated",
                domain.entry("definitions", "npcs.worldheart_banker.faction").orElseThrow().currentValue());
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

        assertEquals(0, domain.entry("trades", "trades.count").orElseThrow().currentValue());
        assertEquals("", domain.entry("trades", "trades.ids").orElseThrow().currentValue());

        assertEquals(1, domain.entry("dialogues", "dialogues.count").orElseThrow().currentValue());
        assertEquals("worldheart_banker", domain.entry("dialogues", "dialogues.ids").orElseThrow().currentValue());
        assertEquals("intro", domain.entry("dialogues", "dialogues.worldheart_banker.root").orElseThrow()
                .currentValue());
        assertEquals(2, domain.entry("dialogues", "dialogues.worldheart_banker.nodes.count").orElseThrow()
                .currentValue());
        assertEquals("dialogue", domain.entry(
                "dialogues", "dialogues.worldheart_banker.presentations").orElseThrow().currentValue());
        assertEquals(3, domain.entry("dialogues", "dialogues.worldheart_banker.options.count").orElseThrow()
                .currentValue());
        assertEquals(1, domain.entry("dialogues", "dialogues.worldheart_banker.options.one-time-count").orElseThrow()
                .currentValue());
        assertEquals(1, domain.entry("dialogues", "dialogues.worldheart_banker.actions.count").orElseThrow()
                .currentValue());
        assertEquals(1, domain.entry("dialogues", "dialogues.worldheart_banker.actions.history-worthy-count")
                .orElseThrow().currentValue());
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
    void tradeDescriptorsExposeCatalogsAndOffers() {
        ElarionConfigDomain domain = NpcConfigDescriptors.domain(
                this::npcs, this::skins, this::portraits, this::dialogues, this::trades, this::ui);

        assertEquals(1, domain.entry("trades", "trades.count").orElseThrow().currentValue());
        assertEquals("worldheart_trader", domain.entry("trades", "trades.ids").orElseThrow().currentValue());
        assertEquals(3, domain.entry("trades", "trades.worldheart_trader.offers.count").orElseThrow()
                .currentValue());
        assertEquals("Nether Gate Ticket", domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.label").orElseThrow().currentValue());
        assertEquals("buy", domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.direction").orElseThrow().currentValue());
        assertEquals(true, domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.enabled").orElseThrow().currentValue());
        assertEquals("elarion:portal_ticket", domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.item").orElseThrow().currentValue());
        assertEquals(2, domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.count").orElseThrow().currentValue());
        assertEquals(1, domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.custom-model-data")
                .orElseThrow().currentValue());
        assertEquals("portal.ticket.nether", domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.price-key")
                .orElseThrow().currentValue());
        assertEquals("25", domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.price").orElseThrow().currentValue());
        assertEquals(12, domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.stock-limit")
                .orElseThrow().currentValue());
        assertEquals(4, domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.restock-amount")
                .orElseThrow().currentValue());
        assertEquals("1800", domain.entry(
                "trades", "trades.worldheart_trader.offers.nether_ticket.restock-interval-seconds")
                .orElseThrow().currentValue());
        assertEquals("sell", domain.entry(
                "trades", "trades.worldheart_trader.offers.cobblestone_buyback.direction")
                .orElseThrow().currentValue());
        assertEquals(true, domain.entry(
                "trades", "trades.worldheart_trader.offers.cobblestone_buyback.enabled")
                .orElseThrow().currentValue());
        assertEquals("exact_item", domain.entry(
                "trades", "trades.worldheart_trader.offers.cobblestone_buyback.sell-match")
                .orElseThrow().currentValue());
        assertEquals("vanilla_only", domain.entry(
                "trades", "trades.worldheart_trader.offers.cobblestone_buyback.component-policy")
                .orElseThrow().currentValue());
        assertEquals(64, domain.entry(
                "trades", "trades.worldheart_trader.offers.cobblestone_buyback.max-quantity")
                .orElseThrow().currentValue());
        assertEquals("placed_npc", domain.entry(
                "trades", "trades.worldheart_trader.offers.cobblestone_buyback.stock-destination")
                .orElseThrow().currentValue());
        assertEquals("cobblestone", domain.entry(
                "trades", "trades.worldheart_trader.offers.cobblestone_buyback.destination-offer")
                .orElseThrow().currentValue());
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

    @Test
    void npcDescriptorsExposeAdditionalTraderDefinitions() {
        ElarionConfigDomain domain = NpcConfigDescriptors.domain(
                () -> List.of(
                        banker("Worldheart Banker"),
                        new NpcDefinition(
                                "worldheart_trader",
                                "Worldheart Trader",
                                "Handles trade.",
                                "trader_skin",
                                "trader_portrait",
                                "worldheart_trader",
                                List.of("trade", "worldheart"),
                                "",
                                0.0D,
                                true)),
                this::skins,
                this::portraits,
                () -> List.of(new DialogueDefinition("worldheart_trader", "intro", Map.of("intro", new DialogueNode(
                        "intro",
                        "Trade?",
                        "",
                        "",
                        List.of(),
                        List.of(new DialogueOption(
                                "open_trade",
                                "Trade",
                                "Show me what you trade.",
                                "",
                                "",
                                "open_trade",
                                "trade",
                                List.of(),
                                List.of(),
                                DialoguePrompt.NONE,
                                false)))))),
                this::ui);

        assertEquals(2, domain.entry("definitions", "npcs.count").orElseThrow().currentValue());
        assertEquals("worldheart_banker, worldheart_trader",
                domain.entry("definitions", "npcs.ids").orElseThrow().currentValue());
        assertEquals("Worldheart Trader",
                domain.entry("definitions", "npcs.worldheart_trader.display-name").orElseThrow().currentValue());
        assertEquals("trader_skin",
                domain.entry("definitions", "npcs.worldheart_trader.skin").orElseThrow().currentValue());
    }

    @Test
    void dialogueDescriptorsExposeTradePresentationKind() {
        DialogueNode intro = new DialogueNode(
                "intro",
                "Hello.",
                "",
                "",
                List.of(),
                List.of(new DialogueOption(
                        "trade",
                        "Trade",
                        "Show me your goods.",
                        "",
                        "",
                        "open_trade",
                        "trade",
                        List.of(),
                        List.of(),
                        DialoguePrompt.NONE,
                        false)));
        DialogueNode trade = new DialogueNode(
                "trade",
                "Trade.",
                "",
                "",
                NpcPresentationKind.TRADE,
                List.of(),
                List.of(),
                List.of());
        ElarionConfigDomain domain = NpcConfigDescriptors.domain(
                this::npcs,
                this::skins,
                this::portraits,
                () -> List.of(new DialogueDefinition(
                        "merchant", "intro", Map.of("intro", intro, "trade", trade))),
                this::ui);

        assertEquals("dialogue, trade", domain.entry(
                "dialogues", "dialogues.merchant.presentations").orElseThrow().currentValue());
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

    private List<NpcTradeCatalogDefinition> trades() {
        return List.of(new NpcTradeCatalogDefinition("worldheart_trader", List.of(
                new NpcTradeOfferDefinition(
                        "nether_ticket",
                        "buy",
                        "Nether Gate Ticket",
                        "2 in stock",
                        "elarion:portal_ticket",
                        2,
                        "",
                        List.of(),
                        List.of(),
                        1,
                        "portal.ticket.nether",
                        25L,
                        12,
                        4,
                        1800L,
                        true),
                new NpcTradeOfferDefinition(
                        "cobblestone",
                        "buy",
                        "Cobblestone",
                        "1 block",
                        "minecraft:cobblestone",
                        1,
                        "",
                        List.of(),
                        List.of(),
                        1L,
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
                        true,
                        "exact_item",
                        "vanilla_only",
                        64,
                        "placed_npc",
                        "cobblestone"))));
    }

    private List<DialogueDefinition> dialogues() {
        DialogueCondition realmCondition = new DialogueCondition("elarion:has_realm", Map.of("realm", "oak"));
        DialogueCondition questCondition = new DialogueCondition("elarion:quest_complete", Map.of("quest", "intro"));
        DialogueAction depositAction = new DialogueAction(
                "elarion:economy_deposit_currency_amount",
                Map.of("account", "personal", "history-outcome", "a first deposit"),
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
                false,
                true);
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
