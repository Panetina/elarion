package panetina.elarion.addons.npcs.config;

import panetina.elarion.addons.npcs.model.DialogueAction;
import panetina.elarion.addons.npcs.model.DialogueCondition;
import panetina.elarion.addons.npcs.model.DialogueDefinition;
import panetina.elarion.addons.npcs.model.DialogueNode;
import panetina.elarion.addons.npcs.model.DialogueOption;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcUiConfig;
import panetina.elarion.addons.npcs.model.NpcTradeCatalogDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NpcConfigDescriptors {
    private NpcConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<Collection<NpcDefinition>> npcs,
            Supplier<Collection<NpcSkinProfile>> skins,
            Supplier<Collection<NpcPortraitProfile>> portraits,
            Supplier<Collection<DialogueDefinition>> dialogues,
            Supplier<Collection<NpcTradeCatalogDefinition>> trades,
            Supplier<NpcUiConfig> ui
    ) {
        registry.registerDomain(domain(npcs, skins, portraits, dialogues, trades, ui));
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<Collection<NpcDefinition>> npcs,
            Supplier<Collection<NpcSkinProfile>> skins,
            Supplier<Collection<NpcPortraitProfile>> portraits,
            Supplier<Collection<DialogueDefinition>> dialogues,
            Supplier<NpcUiConfig> ui
    ) {
        register(registry, npcs, skins, portraits, dialogues, List::of, ui);
    }

    public static ElarionConfigDomain domain(
            Supplier<Collection<NpcDefinition>> npcs,
            Supplier<Collection<NpcSkinProfile>> skins,
            Supplier<Collection<NpcPortraitProfile>> portraits,
            Supplier<Collection<DialogueDefinition>> dialogues,
            Supplier<Collection<NpcTradeCatalogDefinition>> trades,
            Supplier<NpcUiConfig> ui
    ) {
        List<NpcDefinition> npcSnapshot = sortedNpcs(npcs);
        List<NpcSkinProfile> skinSnapshot = sortedSkins(skins);
        List<NpcPortraitProfile> portraitSnapshot = sortedPortraits(portraits);
        List<DialogueDefinition> dialogueSnapshot = sortedDialogues(dialogues);
        List<NpcTradeCatalogDefinition> tradeSnapshot = sortedTrades(trades);
        NpcUiConfig uiSnapshot = safeUi(ui);
        return new ElarionConfigDomain(
                "npcs",
                "addons:npcs",
                "NPCs",
                "Static NPC definitions, visual profiles, dialogue summaries, and dialogue UI settings.",
                List.of(
                        "config/elarion/addons/npcs/npcs.yml",
                        "config/elarion/addons/npcs/skins.yml",
                        "config/elarion/addons/npcs/portraits.yml",
                        "config/elarion/addons/npcs/trades.yml",
                        "config/elarion/addons/npcs/ui.yml",
                        "config/elarion/addons/npcs/dialogues/*.yml"),
                "/e npc reload",
                List.of(
                        new ElarionConfigCategory(
                                "definitions",
                                "NPC Definitions",
                                "Current loaded static NPC definition summaries.",
                                npcEntries(npcs, npcSnapshot)),
                        new ElarionConfigCategory(
                                "profiles",
                                "Visual Profiles",
                                "Current loaded skin and portrait profile summaries.",
                                profileEntries(skins, portraits, skinSnapshot, portraitSnapshot)),
                        new ElarionConfigCategory(
                                "dialogues",
                                "Dialogues",
                                "Current loaded dialogue graph summaries.",
                                dialogueEntries(dialogues, dialogueSnapshot)),
                        new ElarionConfigCategory(
                                "trades",
                                "Trade Catalogs",
                                "Current loaded read-only merchant catalog summaries.",
                                tradeEntries(trades, tradeSnapshot)),
                        new ElarionConfigCategory(
                                "ui",
                                "Dialogue UI",
                                "NPC dialogue layout, typing, and feedback settings.",
                                uiEntries(ui, uiSnapshot))));
    }

    public static ElarionConfigDomain domain(
            Supplier<Collection<NpcDefinition>> npcs,
            Supplier<Collection<NpcSkinProfile>> skins,
            Supplier<Collection<NpcPortraitProfile>> portraits,
            Supplier<Collection<DialogueDefinition>> dialogues,
            Supplier<NpcUiConfig> ui
    ) {
        return domain(npcs, skins, portraits, dialogues, List::of, ui);
    }

    private static List<ElarionConfigEntry<?>> npcEntries(
            Supplier<Collection<NpcDefinition>> npcs,
            List<NpcDefinition> snapshot
    ) {
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry("npcs.count", "NPC Count",
                "Number of currently loaded static NPC definitions.",
                "npcs.yml.npcs",
                snapshot.size(),
                () -> sortedNpcs(npcs).size(),
                0,
                Integer.MAX_VALUE));
        entries.add(stringEntry("npcs.ids", "NPC IDs",
                "Comma-separated static NPC definition IDs currently known to NPCs.",
                "npcs.yml.npcs",
                ids(snapshot, NpcDefinition::id),
                () -> ids(sortedNpcs(npcs), NpcDefinition::id),
                false));
        for (NpcDefinition npc : snapshot) {
            entries.add(npcStringEntry(npc, "display-name", "Display Name",
                    "NPC display name.",
                    npcs, NpcDefinition::displayName));
            entries.add(npcStringEntry(npc, "description", "Description",
                    "NPC description.",
                    npcs, NpcDefinition::description, false));
            entries.add(npcBoolEntry(npc, "enabled", "Enabled",
                    "Whether this NPC definition can be placed/interacted with.",
                    npcs, NpcDefinition::enabled));
            entries.add(npcStringEntry(npc, "skin", "Skin Profile",
                    "Skin profile used by the in-world NPC body.",
                    npcs, NpcDefinition::skin, false));
            entries.add(npcStringEntry(npc, "portrait", "Portrait Profile",
                    "Portrait profile used by the dialogue UI.",
                    npcs, NpcDefinition::portrait, false));
            entries.add(npcStringEntry(npc, "dialogue", "Dialogue",
                    "Dialogue graph opened by this NPC.",
                    npcs, NpcDefinition::dialogue, false));
            entries.add(npcStringEntry(npc, "faction", "Faction",
                    "Stable reputation faction: worldheart, underworld, realm:<id>, or a custom id.",
                    npcs, NpcDefinition::faction, false));
            entries.add(npcStringEntry(npc, "trade-catalog", "Trade Catalog",
                    "Optional read-only trade catalog shown by trade presentation nodes.",
                    npcs, NpcDefinition::tradeCatalog, false));
            entries.add(npcStringEntry(npc, "tax-jurisdiction", "Tax Jurisdiction",
                    "Placement policy: auto, realm:<id>, or world:<namespaced-id>.",
                    npcs, NpcDefinition::taxJurisdiction));
            entries.add(npcStringEntry(npc, "tags", "Tags",
                    "Admin/filtering tags on this NPC definition.",
                    npcs, value -> String.join(", ", value.tags()), false));
            entries.add(npcStringEntry(npc, "required-ability", "Required Ability",
                    "Optional Core ability required to interact with this NPC.",
                    npcs, NpcDefinition::requiredAbility, false));
            entries.add(npcStringEntry(npc, "interaction-range-blocks", "Interaction Range",
                    "NPC-specific interaction range; 0 uses the UI default.",
                    npcs, value -> Double.toString(value.interactionRangeBlocks()), false));
        }
        return entries;
    }

    private static List<ElarionConfigEntry<?>> tradeEntries(
            Supplier<Collection<NpcTradeCatalogDefinition>> trades,
            List<NpcTradeCatalogDefinition> snapshot
    ) {
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry("trades.count", "Trade Catalog Count",
                "Number of currently loaded read-only trade catalogs.",
                "trades.yml.trades", snapshot.size(), () -> sortedTrades(trades).size(),
                0, Integer.MAX_VALUE));
        entries.add(stringEntry("trades.ids", "Trade Catalog IDs",
                "Comma-separated trade catalog IDs currently known to NPCs.",
                "trades.yml.trades", ids(snapshot, NpcTradeCatalogDefinition::id),
                () -> ids(sortedTrades(trades), NpcTradeCatalogDefinition::id), false));
        for (NpcTradeCatalogDefinition catalog : snapshot) {
            entries.add(intEntry(tradeId(catalog, "offers.count"), tradeLabel(catalog, "Offer Count"),
                    "Number of parsed offers in this catalog.", tradePath(catalog, "offers"),
                    catalog.offers().size(), () -> currentTrade(trades, catalog).offers().size(), 0, 128));
            for (NpcTradeOfferDefinition offer : catalog.offers()) {
                String prefix = "offers." + key(offer.id()) + ".";
                entries.add(stringEntry(tradeId(catalog, prefix + "label"),
                        tradeLabel(catalog, offer.id() + " Label"), "Server-authored offer label.",
                        tradePath(catalog, "offers." + offer.id() + ".label"), offer.label(),
                        () -> currentOffer(trades, catalog, offer).label(), false));
                entries.add(stringEntry(tradeId(catalog, prefix + "direction"),
                        tradeLabel(catalog, offer.id() + " Direction"),
                        "Offer direction. Buy rows spend physical Sigils; Sell rows use server escrow and wallet payout.",
                        tradePath(catalog, "offers." + offer.id() + ".direction"), offer.direction(),
                        () -> currentOffer(trades, catalog, offer).direction(),
                        List.of("buy", "sell")));
                entries.add(boolEntry(tradeId(catalog, prefix + "enabled"),
                        tradeLabel(catalog, offer.id() + " Enabled"),
                        "Whether this offer is active.",
                        tradePath(catalog, "offers." + offer.id() + ".enabled"), offer.enabled(),
                        () -> currentOffer(trades, catalog, offer).enabled()));
                entries.add(stringEntry(tradeId(catalog, prefix + "item"),
                        tradeLabel(catalog, offer.id() + " Item"), "Item preview registry ID.",
                        tradePath(catalog, "offers." + offer.id() + ".item"), offer.itemId(),
                        () -> currentOffer(trades, catalog, offer).itemId(), false));
                entries.add(intEntry(tradeId(catalog, prefix + "count"),
                        tradeLabel(catalog, offer.id() + " Count"),
                        "Configured unit stack count for this offer.",
                        tradePath(catalog, "offers." + offer.id() + ".count"),
                        offer.count(),
                        () -> currentOffer(trades, catalog, offer).count(), 1, 64));
                entries.add(intEntry(tradeId(catalog, prefix + "custom-model-data"),
                        tradeLabel(catalog, offer.id() + " Custom Model Data"),
                        "Optional item custom model data used for server-authored preview art.",
                        tradePath(catalog, "offers." + offer.id() + ".custom-model-data"),
                        offer.customModelData(),
                        () -> currentOffer(trades, catalog, offer).customModelData(), 0, Integer.MAX_VALUE));
                entries.add(stringEntry(tradeId(catalog, prefix + "price-key"),
                        tradeLabel(catalog, offer.id() + " Price Key"),
                        "Optional Economy pricing hook for future taxes, inflation, or dynamic merchant pricing.",
                        tradePath(catalog, "offers." + offer.id() + ".price-key"), offer.priceKey(),
                        () -> currentOffer(trades, catalog, offer).priceKey(), false));
                entries.add(stringEntry(tradeId(catalog, prefix + "price"),
                        tradeLabel(catalog, offer.id() + " Price"), "Display-only price in Sigils.",
                        tradePath(catalog, "offers." + offer.id() + ".price"), Long.toString(offer.price()),
                        () -> Long.toString(currentOffer(trades, catalog, offer).price()), false));
                entries.add(intEntry(tradeId(catalog, prefix + "stock-limit"),
                        tradeLabel(catalog, offer.id() + " Stock Limit"),
                        "Maximum units available per placed NPC. Zero means unlimited.",
                        tradePath(catalog, "offers." + offer.id() + ".stock-limit"),
                        offer.stockLimit(),
                        () -> currentOffer(trades, catalog, offer).stockLimit(), 0, 100_000));
                entries.add(intEntry(tradeId(catalog, prefix + "restock-amount"),
                        tradeLabel(catalog, offer.id() + " Restock Amount"),
                        "Units restored each restock interval. Zero means refill to the stock limit.",
                        tradePath(catalog, "offers." + offer.id() + ".restock-amount"),
                        offer.restockAmount(),
                        () -> currentOffer(trades, catalog, offer).restockAmount(), 0, 100_000));
                entries.add(stringEntry(tradeId(catalog, prefix + "restock-interval-seconds"),
                        tradeLabel(catalog, offer.id() + " Restock Interval"),
                        "Seconds between lazy restocks. Zero disables automatic restocking.",
                        tradePath(catalog, "offers." + offer.id() + ".restock-interval-seconds"),
                        Long.toString(offer.restockIntervalSeconds()),
                        () -> Long.toString(currentOffer(trades, catalog, offer).restockIntervalSeconds()), false));
                entries.add(stringEntry(tradeId(catalog, prefix + "sell-match"),
                        tradeLabel(catalog, offer.id() + " Sell Match"),
                        "Sell-only item matching policy used by server escrow validation.",
                        tradePath(catalog, "offers." + offer.id() + ".sell-match"),
                        offer.sellMatch(),
                        () -> currentOffer(trades, catalog, offer).sellMatch(),
                        List.of("exact_item", "exact_stack")));
                entries.add(stringEntry(tradeId(catalog, prefix + "component-policy"),
                        tradeLabel(catalog, offer.id() + " Component Policy"),
                        "Sell-only component policy used when matching item components.",
                        tradePath(catalog, "offers." + offer.id() + ".component-policy"),
                        offer.componentPolicy(),
                        () -> currentOffer(trades, catalog, offer).componentPolicy(),
                        List.of("vanilla_only", "exact_components")));
                entries.add(intEntry(tradeId(catalog, prefix + "max-quantity"),
                        tradeLabel(catalog, offer.id() + " Max Quantity"),
                        "Sell-only maximum accepted quantity for one sale request.",
                        tradePath(catalog, "offers." + offer.id() + ".max-quantity"),
                        offer.maxQuantity(),
                        () -> currentOffer(trades, catalog, offer).maxQuantity(), 0, 64));
                entries.add(stringEntry(tradeId(catalog, prefix + "stock-destination"),
                        tradeLabel(catalog, offer.id() + " Stock Destination"),
                        "Sell-only destination for sold items after escrow and payout.",
                        tradePath(catalog, "offers." + offer.id() + ".stock-destination"),
                        offer.stockDestination(),
                        () -> currentOffer(trades, catalog, offer).stockDestination(),
                        List.of("none", "placed_npc")));
                entries.add(stringEntry(tradeId(catalog, prefix + "destination-offer"),
                        tradeLabel(catalog, offer.id() + " Destination Offer"),
                        "Sell-only BUY offer ID that receives stock when stock-destination is placed_npc.",
                        tradePath(catalog, "offers." + offer.id() + ".destination-offer"),
                        offer.destinationOfferId(),
                        () -> currentOffer(trades, catalog, offer).destinationOfferId(), false));
            }
        }
        return entries;
    }

    private static List<ElarionConfigEntry<?>> profileEntries(
            Supplier<Collection<NpcSkinProfile>> skins,
            Supplier<Collection<NpcPortraitProfile>> portraits,
            List<NpcSkinProfile> skinSnapshot,
            List<NpcPortraitProfile> portraitSnapshot
    ) {
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry("skins.count", "Skin Profile Count",
                "Number of currently loaded NPC skin profiles.",
                "skins.yml.skins",
                skinSnapshot.size(),
                () -> sortedSkins(skins).size(),
                0,
                Integer.MAX_VALUE));
        entries.add(stringEntry("skins.ids", "Skin Profile IDs",
                "Comma-separated skin profile IDs currently known to NPCs.",
                "skins.yml.skins",
                ids(skinSnapshot, NpcSkinProfile::id),
                () -> ids(sortedSkins(skins), NpcSkinProfile::id),
                false));
        entries.add(intEntry("portraits.count", "Portrait Profile Count",
                "Number of currently loaded NPC portrait profiles.",
                "portraits.yml.portraits",
                portraitSnapshot.size(),
                () -> sortedPortraits(portraits).size(),
                0,
                Integer.MAX_VALUE));
        entries.add(stringEntry("portraits.ids", "Portrait Profile IDs",
                "Comma-separated portrait profile IDs currently known to NPCs.",
                "portraits.yml.portraits",
                ids(portraitSnapshot, NpcPortraitProfile::id),
                () -> ids(sortedPortraits(portraits), NpcPortraitProfile::id),
                false));
        for (NpcSkinProfile skin : skinSnapshot) {
            entries.add(skinStringEntry(skin, "display-name", "Display Name",
                    "Skin profile display name.",
                    skins, NpcSkinProfile::displayName));
            entries.add(skinStringEntry(skin, "type", "Type",
                    "Skin profile type.",
                    skins, NpcSkinProfile::type,
                    List.of("placeholder", "texture", "player_body")));
            entries.add(skinStringEntry(skin, "texture", "Texture",
                    "Configured skin texture ID.",
                    skins, NpcSkinProfile::texture, false));
            entries.add(skinStringEntry(skin, "player-name", "Player Name",
                    "Player name used by player_body profiles.",
                    skins, NpcSkinProfile::playerName, false));
            entries.add(skinStringEntry(skin, "fallback-type", "Fallback Type",
                    "Fallback skin profile type.",
                    skins, NpcSkinProfile::fallbackType,
                    List.of("placeholder", "texture")));
            entries.add(skinStringEntry(skin, "fallback-texture", "Fallback Texture",
                    "Fallback skin texture ID.",
                    skins, NpcSkinProfile::fallbackTexture, false));
            entries.add(skinStringEntry(skin, "adapter", "Adapter",
                    "Optional renderer adapter key.",
                    skins, NpcSkinProfile::adapter, false));
        }
        for (NpcPortraitProfile portrait : portraitSnapshot) {
            entries.add(portraitStringEntry(portrait, "display-name", "Display Name",
                    "Portrait profile display name.",
                    portraits, NpcPortraitProfile::displayName));
            entries.add(portraitStringEntry(portrait, "type", "Type",
                    "Portrait profile type.",
                    portraits, NpcPortraitProfile::type,
                    List.of("placeholder", "texture", "player_head")));
            entries.add(portraitStringEntry(portrait, "texture", "Texture",
                    "Configured portrait texture ID.",
                    portraits, NpcPortraitProfile::texture, false));
            entries.add(portraitStringEntry(portrait, "player-name", "Player Name",
                    "Player name used by player_head profiles.",
                    portraits, NpcPortraitProfile::playerName, false));
            entries.add(portraitStringEntry(portrait, "fallback-type", "Fallback Type",
                    "Fallback portrait profile type.",
                    portraits, NpcPortraitProfile::fallbackType,
                    List.of("placeholder", "texture")));
            entries.add(portraitStringEntry(portrait, "fallback-texture", "Fallback Texture",
                    "Fallback portrait texture ID.",
                    portraits, NpcPortraitProfile::fallbackTexture, false));
        }
        return entries;
    }

    private static List<ElarionConfigEntry<?>> dialogueEntries(
            Supplier<Collection<DialogueDefinition>> dialogues,
            List<DialogueDefinition> snapshot
    ) {
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry("dialogues.count", "Dialogue Count",
                "Number of currently loaded NPC dialogue graphs.",
                "dialogues/*.yml",
                snapshot.size(),
                () -> sortedDialogues(dialogues).size(),
                0,
                Integer.MAX_VALUE));
        entries.add(stringEntry("dialogues.ids", "Dialogue IDs",
                "Comma-separated dialogue IDs currently known to NPCs.",
                "dialogues/*.yml",
                ids(snapshot, DialogueDefinition::id),
                () -> ids(sortedDialogues(dialogues), DialogueDefinition::id),
                false));
        for (DialogueDefinition dialogue : snapshot) {
            entries.add(dialogueStringEntry(dialogue, "root", "Root Node",
                    "Root dialogue node ID.",
                    dialogues, DialogueDefinition::root));
            entries.add(dialogueIntEntry(dialogue, "nodes.count", "Node Count",
                    "Number of nodes in this dialogue graph.",
                    dialogues, value -> value.nodes().size(), 0, Integer.MAX_VALUE));
            entries.add(dialogueStringEntry(dialogue, "nodes.ids", "Node IDs",
                    "Comma-separated dialogue node IDs.",
                    dialogues, value -> keys(value.nodes().keySet()), false));
            entries.add(dialogueStringEntry(dialogue, "presentations", "Presentation Kinds",
                    "Distinct server-authored presentation kinds used by dialogue nodes.",
                    dialogues, NpcConfigDescriptors::presentationKinds, false));
            entries.add(dialogueIntEntry(dialogue, "options.count", "Option Count",
                    "Total number of dialogue options.",
                    dialogues, NpcConfigDescriptors::optionCount, 0, Integer.MAX_VALUE));
            entries.add(dialogueIntEntry(dialogue, "options.one-time-count", "One-Time Option Count",
                    "Number of options persisted as consumed after successful use.",
                    dialogues, NpcConfigDescriptors::oneTimeOptionCount, 0, Integer.MAX_VALUE));
            entries.add(dialogueIntEntry(dialogue, "actions.count", "Action Count",
                    "Total number of configured dialogue actions.",
                    dialogues, NpcConfigDescriptors::actionCount, 0, Integer.MAX_VALUE));
            entries.add(dialogueIntEntry(dialogue, "actions.history-worthy-count", "History-Worthy Action Count",
                    "Number of actions configured to emit meaningful structured NPC history outcomes.",
                    dialogues, NpcConfigDescriptors::historyWorthyActionCount, 0, Integer.MAX_VALUE));
            entries.add(dialogueStringEntry(dialogue, "actions.types", "Action Types",
                    "Action types referenced by this dialogue.",
                    dialogues, NpcConfigDescriptors::actionTypes, false));
            entries.add(dialogueIntEntry(dialogue, "conditions.count", "Condition Count",
                    "Total number of configured node, variant, and option conditions.",
                    dialogues, NpcConfigDescriptors::conditionCount, 0, Integer.MAX_VALUE));
            entries.add(dialogueStringEntry(dialogue, "conditions.types", "Condition Types",
                    "Condition types referenced by this dialogue.",
                    dialogues, NpcConfigDescriptors::conditionTypes, false));
            entries.add(dialogueIntEntry(dialogue, "variants.count", "Variant Count",
                    "Total number of conditional text variants.",
                    dialogues, NpcConfigDescriptors::variantCount, 0, Integer.MAX_VALUE));
            entries.add(dialogueIntEntry(dialogue, "prompts.count", "Prompt Count",
                    "Total number of configured option prompts.",
                    dialogues, NpcConfigDescriptors::promptCount, 0, Integer.MAX_VALUE));
        }
        return entries;
    }

    private static List<ElarionConfigEntry<?>> uiEntries(
            Supplier<NpcUiConfig> ui,
            NpcUiConfig snapshot
    ) {
        return List.of(
                intEntry("ui.panel-width", "Panel Width",
                        "Dialogue UI panel width.",
                        "ui.yml.panel-width",
                        snapshot.panelWidth(),
                        () -> safeUi(ui).panelWidth(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.min-panel-height", "Minimum Panel Height",
                        "Minimum dialogue UI panel height.",
                        "ui.yml.min-panel-height",
                        snapshot.minPanelHeight(),
                        () -> safeUi(ui).minPanelHeight(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.max-panel-height", "Maximum Panel Height",
                        "Maximum dialogue UI panel height.",
                        "ui.yml.max-panel-height",
                        snapshot.maxPanelHeight(),
                        () -> safeUi(ui).maxPanelHeight(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.minimum-ui-scale-percent", "Minimum UI Scale Percent",
                        "Minimum dialogue UI scale percentage.",
                        "ui.yml.minimum-ui-scale-percent",
                        snapshot.minimumUiScalePercent(),
                        () -> safeUi(ui).minimumUiScalePercent(),
                        25,
                        100),
                intEntry("ui.option-row-height", "Option Row Height",
                        "Dialogue option row height.",
                        "ui.yml.option-row-height",
                        snapshot.optionRowHeight(),
                        () -> safeUi(ui).optionRowHeight(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.visible-option-rows", "Visible Option Rows",
                        "Preferred number of visible option rows.",
                        "ui.yml.visible-option-rows",
                        snapshot.visibleOptionRows(),
                        () -> safeUi(ui).visibleOptionRows(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.scrollbar-width", "Scrollbar Width",
                        "Dialogue option scrollbar width.",
                        "ui.yml.scrollbar-width",
                        snapshot.scrollbarWidth(),
                        () -> safeUi(ui).scrollbarWidth(),
                        0,
                        Integer.MAX_VALUE),
                intEntry("ui.padding", "Padding",
                        "Dialogue UI panel padding.",
                        "ui.yml.padding",
                        snapshot.padding(),
                        () -> safeUi(ui).padding(),
                        0,
                        Integer.MAX_VALUE),
                intEntry("ui.button-height", "Button Height",
                        "Main dialogue button height.",
                        "ui.yml.button-height",
                        snapshot.buttonHeight(),
                        () -> safeUi(ui).buttonHeight(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.compact-button-height", "Compact Button Height",
                        "Compact dialogue button height.",
                        "ui.yml.compact-button-height",
                        snapshot.compactButtonHeight(),
                        () -> safeUi(ui).compactButtonHeight(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.button-gap", "Button Gap",
                        "Gap between dialogue buttons.",
                        "ui.yml.button-gap",
                        snapshot.buttonGap(),
                        () -> safeUi(ui).buttonGap(),
                        0,
                        Integer.MAX_VALUE),
                intEntry("ui.content-gap", "Content Gap",
                        "Gap between dialogue content regions.",
                        "ui.yml.content-gap",
                        snapshot.contentGap(),
                        () -> safeUi(ui).contentGap(),
                        0,
                        Integer.MAX_VALUE),
                intEntry("ui.npc-row-height", "NPC Row Height",
                        "NPC dialogue row height.",
                        "ui.yml.npc-row-height",
                        snapshot.npcRowHeight(),
                        () -> safeUi(ui).npcRowHeight(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.player-row-height", "Player Row Height",
                        "Player response row height.",
                        "ui.yml.player-row-height",
                        snapshot.playerRowHeight(),
                        () -> safeUi(ui).playerRowHeight(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.option-columns-wide", "Wide Option Columns",
                        "Number of option columns on wide layouts.",
                        "ui.yml.option-columns-wide",
                        snapshot.optionColumnsWide(),
                        () -> safeUi(ui).optionColumnsWide(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.portrait-size", "Portrait Size",
                        "NPC portrait size.",
                        "ui.yml.portrait-size",
                        snapshot.portraitSize(),
                        () -> safeUi(ui).portraitSize(),
                        1,
                        Integer.MAX_VALUE),
                intEntry("ui.player-portrait-size", "Player Portrait Size",
                        "Player portrait size.",
                        "ui.yml.player-portrait-size",
                        snapshot.playerPortraitSize(),
                        () -> safeUi(ui).playerPortraitSize(),
                        1,
                        Integer.MAX_VALUE),
                boolEntry("ui.show-portrait-reference", "Show Portrait Reference",
                        "Whether portrait reference/debug text may be shown.",
                        "ui.yml.show-portrait-reference",
                        snapshot.showPortraitReference(),
                        () -> safeUi(ui).showPortraitReference()),
                boolEntry("ui.show-relation-bar", "Show Relation Bar",
                        "Whether the dialogue UI shows relation hearts.",
                        "ui.yml.show-relation-bar",
                        snapshot.showRelationBar(),
                        () -> safeUi(ui).showRelationBar()),
                boolEntry("ui.show-action-feedback-in-gui", "Show Action Feedback In GUI",
                        "Whether action feedback is spoken in the dialogue GUI.",
                        "ui.yml.show-action-feedback-in-gui",
                        snapshot.showActionFeedbackInGui(),
                        () -> safeUi(ui).showActionFeedbackInGui()),
                boolEntry("ui.also-send-action-feedback-to-chat", "Also Send Feedback To Chat",
                        "Whether action feedback is also sent to chat.",
                        "ui.yml.also-send-action-feedback-to-chat",
                        snapshot.alsoSendActionFeedbackToChat(),
                        () -> safeUi(ui).alsoSendActionFeedbackToChat()),
                stringEntry("ui.default-interaction-range-blocks", "Default Interaction Range",
                        "Default NPC interaction range in blocks.",
                        "ui.yml.default-interaction-range-blocks",
                        Double.toString(snapshot.defaultInteractionRangeBlocks()),
                        () -> Double.toString(safeUi(ui).defaultInteractionRangeBlocks()),
                        false),
                boolEntry("ui.typing-enabled", "Typing Enabled",
                        "Whether dialogue text types out over time.",
                        "ui.yml.typing-enabled",
                        snapshot.typingEnabled(),
                        () -> safeUi(ui).typingEnabled()),
                intEntry("ui.typing-characters-per-second", "Typing Characters Per Second",
                        "Dialogue typing speed.",
                        "ui.yml.typing-characters-per-second",
                        snapshot.typingCharactersPerSecond(),
                        () -> safeUi(ui).typingCharactersPerSecond(),
                        1,
                        Integer.MAX_VALUE),
                boolEntry("ui.typing-click-completes", "Typing Click Completes",
                        "Whether click/keyboard input completes the current typing phase.",
                        "ui.yml.typing-click-completes",
                        snapshot.typingClickCompletes(),
                        () -> safeUi(ui).typingClickCompletes()),
                boolEntry("ui.typing-sound-enabled", "Typing Sound Enabled",
                        "Whether typing sound ticks are enabled.",
                        "ui.yml.typing-sound-enabled",
                        snapshot.typingSoundEnabled(),
                        () -> safeUi(ui).typingSoundEnabled()),
                intEntry("ui.typing-sound-interval-characters", "Typing Sound Interval",
                        "Characters between typing sounds.",
                        "ui.yml.typing-sound-interval-characters",
                        snapshot.typingSoundIntervalCharacters(),
                        () -> safeUi(ui).typingSoundIntervalCharacters(),
                        1,
                        Integer.MAX_VALUE));
    }

    private static ElarionConfigEntry<Boolean> npcBoolEntry(
            NpcDefinition npc,
            String field,
            String label,
            String description,
            Supplier<Collection<NpcDefinition>> npcs,
            Function<NpcDefinition, Boolean> value
    ) {
        return boolEntry(npcId(npc, field), npcLabel(npc, label), description,
                npcPath(npc, field), value.apply(npc),
                () -> value.apply(currentNpc(npcs, npc)));
    }

    private static ElarionConfigEntry<String> npcStringEntry(
            NpcDefinition npc,
            String field,
            String label,
            String description,
            Supplier<Collection<NpcDefinition>> npcs,
            Function<NpcDefinition, String> value
    ) {
        return npcStringEntry(npc, field, label, description, npcs, value, true);
    }

    private static ElarionConfigEntry<String> npcStringEntry(
            NpcDefinition npc,
            String field,
            String label,
            String description,
            Supplier<Collection<NpcDefinition>> npcs,
            Function<NpcDefinition, String> value,
            boolean nonBlank
    ) {
        return stringEntry(npcId(npc, field), npcLabel(npc, label), description,
                npcPath(npc, field), value.apply(npc),
                () -> value.apply(currentNpc(npcs, npc)), nonBlank);
    }

    private static ElarionConfigEntry<String> skinStringEntry(
            NpcSkinProfile skin,
            String field,
            String label,
            String description,
            Supplier<Collection<NpcSkinProfile>> skins,
            Function<NpcSkinProfile, String> value
    ) {
        return skinStringEntry(skin, field, label, description, skins, value, true);
    }

    private static ElarionConfigEntry<String> skinStringEntry(
            NpcSkinProfile skin,
            String field,
            String label,
            String description,
            Supplier<Collection<NpcSkinProfile>> skins,
            Function<NpcSkinProfile, String> value,
            boolean nonBlank
    ) {
        return stringEntry(skinId(skin, field), profileLabel(skin.id(), label), description,
                skinPath(skin, field), value.apply(skin),
                () -> value.apply(currentSkin(skins, skin)), nonBlank);
    }

    private static ElarionConfigEntry<String> skinStringEntry(
            NpcSkinProfile skin,
            String field,
            String label,
            String description,
            Supplier<Collection<NpcSkinProfile>> skins,
            Function<NpcSkinProfile, String> value,
            List<String> choices
    ) {
        return stringEntry(skinId(skin, field), profileLabel(skin.id(), label), description,
                skinPath(skin, field), value.apply(skin),
                () -> value.apply(currentSkin(skins, skin)), choices);
    }

    private static ElarionConfigEntry<String> portraitStringEntry(
            NpcPortraitProfile portrait,
            String field,
            String label,
            String description,
            Supplier<Collection<NpcPortraitProfile>> portraits,
            Function<NpcPortraitProfile, String> value
    ) {
        return portraitStringEntry(portrait, field, label, description, portraits, value, true);
    }

    private static ElarionConfigEntry<String> portraitStringEntry(
            NpcPortraitProfile portrait,
            String field,
            String label,
            String description,
            Supplier<Collection<NpcPortraitProfile>> portraits,
            Function<NpcPortraitProfile, String> value,
            boolean nonBlank
    ) {
        return stringEntry(portraitId(portrait, field), profileLabel(portrait.id(), label), description,
                portraitPath(portrait, field), value.apply(portrait),
                () -> value.apply(currentPortrait(portraits, portrait)), nonBlank);
    }

    private static ElarionConfigEntry<String> portraitStringEntry(
            NpcPortraitProfile portrait,
            String field,
            String label,
            String description,
            Supplier<Collection<NpcPortraitProfile>> portraits,
            Function<NpcPortraitProfile, String> value,
            List<String> choices
    ) {
        return stringEntry(portraitId(portrait, field), profileLabel(portrait.id(), label), description,
                portraitPath(portrait, field), value.apply(portrait),
                () -> value.apply(currentPortrait(portraits, portrait)), choices);
    }

    private static ElarionConfigEntry<Integer> dialogueIntEntry(
            DialogueDefinition dialogue,
            String field,
            String label,
            String description,
            Supplier<Collection<DialogueDefinition>> dialogues,
            Function<DialogueDefinition, Integer> value,
            int minimum,
            int maximum
    ) {
        return intEntry(dialogueId(dialogue, field), dialogueLabel(dialogue, label), description,
                dialoguePath(dialogue, field), value.apply(dialogue),
                () -> value.apply(currentDialogue(dialogues, dialogue)), minimum, maximum);
    }

    private static ElarionConfigEntry<String> dialogueStringEntry(
            DialogueDefinition dialogue,
            String field,
            String label,
            String description,
            Supplier<Collection<DialogueDefinition>> dialogues,
            Function<DialogueDefinition, String> value
    ) {
        return dialogueStringEntry(dialogue, field, label, description, dialogues, value, true);
    }

    private static ElarionConfigEntry<String> dialogueStringEntry(
            DialogueDefinition dialogue,
            String field,
            String label,
            String description,
            Supplier<Collection<DialogueDefinition>> dialogues,
            Function<DialogueDefinition, String> value,
            boolean nonBlank
    ) {
        return stringEntry(dialogueId(dialogue, field), dialogueLabel(dialogue, label), description,
                dialoguePath(dialogue, field), value.apply(dialogue),
                () -> value.apply(currentDialogue(dialogues, dialogue)), nonBlank);
    }

    private static ElarionConfigEntry<Boolean> boolEntry(
            String id,
            String label,
            String description,
            String path,
            boolean defaultValue,
            Supplier<Boolean> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.BOOLEAN, defaultValue, currentValue,
                ElarionConfigValidator.pass(), List.of("true", "false"), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id,
            String label,
            String description,
            String path,
            int defaultValue,
            Supplier<Integer> currentValue,
            int minimum,
            int maximum
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerRange(path, minimum, maximum), List.of(),
                Integer.toString(minimum), maximum == Integer.MAX_VALUE ? "" : Integer.toString(maximum),
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue
    ) {
        return stringEntry(id, label, description, path, defaultValue, currentValue, true);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(),
                List.of(), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            List<String> choices
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                ElarionConfigValidator.nonBlank(path), choices, "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static NpcDefinition currentNpc(Supplier<Collection<NpcDefinition>> npcs, NpcDefinition fallback) {
        for (NpcDefinition npc : sortedNpcs(npcs)) {
            if (npc.id().equals(fallback.id())) return npc;
        }
        return fallback;
    }

    private static NpcSkinProfile currentSkin(
            Supplier<Collection<NpcSkinProfile>> skins,
            NpcSkinProfile fallback
    ) {
        for (NpcSkinProfile skin : sortedSkins(skins)) {
            if (skin.id().equals(fallback.id())) return skin;
        }
        return fallback;
    }

    private static NpcPortraitProfile currentPortrait(
            Supplier<Collection<NpcPortraitProfile>> portraits,
            NpcPortraitProfile fallback
    ) {
        for (NpcPortraitProfile portrait : sortedPortraits(portraits)) {
            if (portrait.id().equals(fallback.id())) return portrait;
        }
        return fallback;
    }

    private static DialogueDefinition currentDialogue(
            Supplier<Collection<DialogueDefinition>> dialogues,
            DialogueDefinition fallback
    ) {
        for (DialogueDefinition dialogue : sortedDialogues(dialogues)) {
            if (dialogue.id().equals(fallback.id())) return dialogue;
        }
        return fallback;
    }

    private static NpcTradeCatalogDefinition currentTrade(
            Supplier<Collection<NpcTradeCatalogDefinition>> trades,
            NpcTradeCatalogDefinition fallback
    ) {
        for (NpcTradeCatalogDefinition trade : sortedTrades(trades)) {
            if (trade.id().equals(fallback.id())) return trade;
        }
        return fallback;
    }

    private static NpcTradeOfferDefinition currentOffer(
            Supplier<Collection<NpcTradeCatalogDefinition>> trades,
            NpcTradeCatalogDefinition catalog,
            NpcTradeOfferDefinition fallback
    ) {
        return currentTrade(trades, catalog).offers().stream()
                .filter(offer -> offer.id().equals(fallback.id()))
                .findFirst()
                .orElse(fallback);
    }

    private static NpcUiConfig safeUi(Supplier<NpcUiConfig> ui) {
        NpcUiConfig value = ui == null ? null : ui.get();
        return value == null ? NpcUiConfig.defaults() : value;
    }

    private static List<NpcDefinition> sortedNpcs(Supplier<Collection<NpcDefinition>> npcs) {
        Collection<NpcDefinition> value = npcs == null ? null : npcs.get();
        if (value == null) return List.of();
        return value.stream().sorted(Comparator.comparing(NpcDefinition::id)).toList();
    }

    private static List<NpcSkinProfile> sortedSkins(Supplier<Collection<NpcSkinProfile>> skins) {
        Collection<NpcSkinProfile> value = skins == null ? null : skins.get();
        if (value == null) return List.of();
        return value.stream().sorted(Comparator.comparing(NpcSkinProfile::id)).toList();
    }

    private static List<NpcPortraitProfile> sortedPortraits(
            Supplier<Collection<NpcPortraitProfile>> portraits
    ) {
        Collection<NpcPortraitProfile> value = portraits == null ? null : portraits.get();
        if (value == null) return List.of();
        return value.stream().sorted(Comparator.comparing(NpcPortraitProfile::id)).toList();
    }

    private static List<DialogueDefinition> sortedDialogues(
            Supplier<Collection<DialogueDefinition>> dialogues
    ) {
        Collection<DialogueDefinition> value = dialogues == null ? null : dialogues.get();
        if (value == null) return List.of();
        return value.stream().sorted(Comparator.comparing(DialogueDefinition::id)).toList();
    }

    private static List<NpcTradeCatalogDefinition> sortedTrades(
            Supplier<Collection<NpcTradeCatalogDefinition>> trades
    ) {
        Collection<NpcTradeCatalogDefinition> value = trades == null ? null : trades.get();
        if (value == null) return List.of();
        return value.stream().sorted(Comparator.comparing(NpcTradeCatalogDefinition::id)).toList();
    }

    private static String npcId(NpcDefinition npc, String field) {
        return "npcs." + key(npc.id()) + "." + field;
    }

    private static String npcPath(NpcDefinition npc, String field) {
        return "npcs.yml.npcs." + npc.id() + "." + field;
    }

    private static String npcLabel(NpcDefinition npc, String fieldLabel) {
        return npc.id() + " " + fieldLabel;
    }

    private static String skinId(NpcSkinProfile skin, String field) {
        return "skins." + key(skin.id()) + "." + field;
    }

    private static String skinPath(NpcSkinProfile skin, String field) {
        return "skins.yml.skins." + skin.id() + "." + field;
    }

    private static String portraitId(NpcPortraitProfile portrait, String field) {
        return "portraits." + key(portrait.id()) + "." + field;
    }

    private static String portraitPath(NpcPortraitProfile portrait, String field) {
        return "portraits.yml.portraits." + portrait.id() + "." + field;
    }

    private static String dialogueId(DialogueDefinition dialogue, String field) {
        return "dialogues." + key(dialogue.id()) + "." + field;
    }

    private static String dialoguePath(DialogueDefinition dialogue, String field) {
        return "dialogues/" + dialogue.id() + ".yml." + field;
    }

    private static String dialogueLabel(DialogueDefinition dialogue, String fieldLabel) {
        return dialogue.id() + " " + fieldLabel;
    }

    private static String tradeId(NpcTradeCatalogDefinition catalog, String field) {
        return "trades." + key(catalog.id()) + "." + field;
    }

    private static String tradePath(NpcTradeCatalogDefinition catalog, String field) {
        return "trades.yml.trades." + catalog.id() + "." + field;
    }

    private static String tradeLabel(NpcTradeCatalogDefinition catalog, String fieldLabel) {
        return catalog.id() + " " + fieldLabel;
    }

    private static String profileLabel(String profileId, String fieldLabel) {
        return profileId + " " + fieldLabel;
    }

    private static <T> String ids(List<T> values, Function<T, String> id) {
        return values.stream().map(id).reduce((left, right) -> left + ", " + right).orElse("");
    }

    private static String keys(Collection<String> values) {
        return values.stream().sorted().reduce((left, right) -> left + ", " + right).orElse("");
    }

    private static String key(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9_.:-]", "_");
        return normalized.isBlank() ? "unnamed" : normalized;
    }

    private static int optionCount(DialogueDefinition dialogue) {
        return dialogue.nodes().values().stream().mapToInt(node -> node.options().size()).sum();
    }

    private static int oneTimeOptionCount(DialogueDefinition dialogue) {
        return (int) dialogue.nodes().values().stream()
                .flatMap(node -> node.options().stream())
                .filter(DialogueOption::oneTime)
                .count();
    }

    private static String presentationKinds(DialogueDefinition dialogue) {
        return dialogue.nodes().values().stream()
                .map(node -> node.presentation().id())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private static int actionCount(DialogueDefinition dialogue) {
        return dialogue.nodes().values().stream()
                .flatMap(node -> node.options().stream())
                .mapToInt(option -> option.actions().size())
                .sum();
    }

    private static int historyWorthyActionCount(DialogueDefinition dialogue) {
        return (int) dialogue.nodes().values().stream()
                .flatMap(node -> node.options().stream())
                .flatMap(option -> option.actions().stream())
                .filter(DialogueAction::historyWorthy)
                .count();
    }

    private static String actionTypes(DialogueDefinition dialogue) {
        return dialogue.nodes().values().stream()
                .flatMap(node -> node.options().stream())
                .flatMap(option -> option.actions().stream())
                .map(DialogueAction::type)
                .filter(value -> !value.isBlank())
                .distinct()
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static int conditionCount(DialogueDefinition dialogue) {
        int total = 0;
        for (DialogueNode node : dialogue.nodes().values()) {
            total += node.conditions().size();
            for (var variant : node.variants()) total += variant.conditions().size();
            for (DialogueOption option : node.options()) total += option.conditions().size();
        }
        return total;
    }

    private static String conditionTypes(DialogueDefinition dialogue) {
        List<String> values = new ArrayList<>();
        for (DialogueNode node : dialogue.nodes().values()) {
            values.addAll(node.conditions().stream().map(DialogueCondition::type).toList());
            node.variants().forEach(variant ->
                    values.addAll(variant.conditions().stream().map(DialogueCondition::type).toList()));
            node.options().forEach(option ->
                    values.addAll(option.conditions().stream().map(DialogueCondition::type).toList()));
        }
        return values.stream()
                .filter(value -> !value.isBlank())
                .distinct()
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static int variantCount(DialogueDefinition dialogue) {
        return dialogue.nodes().values().stream().mapToInt(node -> node.variants().size()).sum();
    }

    private static int promptCount(DialogueDefinition dialogue) {
        return (int) dialogue.nodes().values().stream()
                .flatMap(node -> node.options().stream())
                .filter(option -> option.prompt().present())
                .count();
    }
}
