package panetina.elarion.addons.npcs.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.npcs.model.DialogueNode;
import panetina.elarion.addons.npcs.model.DialogueOption;
import panetina.elarion.addons.npcs.model.DialoguePrompt;
import panetina.elarion.addons.npcs.model.NpcPresentationKind;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcBankPresentationMigrationTest {
    @Test
    void generatedServiceNpcDefaultsUseCurrentTextureAssets() {
        String generatedDefaults = String.join("\n",
                NpcConfigDefaults.NPCS,
                NpcConfigDefaults.SKINS,
                NpcConfigDefaults.PORTRAITS,
                NpcConfigDefaults.BANKER_DIALOGUE,
                NpcConfigDefaults.TRADER_DIALOGUE);

        assertFalse(generatedDefaults.contains("dunk_banker"),
                "Generated NPC defaults must not reference the deleted legacy banker texture.");
        assertTrue(generatedDefaults.contains("elarion:textures/entity/npc/worldheart_banker.png"));
        assertTrue(generatedDefaults.contains("elarion:textures/entity/npc/worldheart_trader.png"));
    }

    @Test
    void generatedBankerDialogueUsesDedicatedBankPresentation() {
        assertTrue(NpcConfigDefaults.BANKER_DIALOGUE.contains("presentation: bank"));
        assertTrue(NpcConfigDefaults.BANKER_DIALOGUE.contains("presentation-role: \"open_bank\""));
        assertTrue(NpcConfigDefaults.BANKER_DIALOGUE.contains("button-text: \"Open Bank\""));
        assertTrue(NpcConfigDefaults.BANKER_DIALOGUE.contains("button-text: \"Back to Conversation\""));
    }

    @Test
    void generatedGuildmasterDialogueIsValidYaml() {
        Object parsed = new Yaml().load(NpcConfigDefaults.GUILDMASTER_DIALOGUE);
        assertTrue(parsed instanceof Map<?, ?>);
    }

    @Test
    void shippedBankPromptsRemainValidWhenEconomyProviderIsAbsent() {
        assertTrue(NpcConfigLoader.optionalProviderAction(
                "elarion:economy_deposit_currency_amount"));
        assertTrue(NpcConfigLoader.optionalProviderAction(
                "elarion:economy_withdraw_currency_amount"));
        assertFalse(NpcConfigLoader.optionalProviderAction("elarion:economy_unknown"));
        assertFalse(NpcConfigLoader.optionalProviderAction(null));
    }

    @Test
    void projectsLegacyPromptOptionsIntoDedicatedBankNode() {
        DialogueOption deposit = option("deposit", "elarion:economy_deposit_currency_amount");
        DialogueOption withdraw = option("withdraw", "elarion:economy_withdraw_currency_amount");
        DialogueOption lore = new DialogueOption(
                "lore", "Lore", "Lore", "", "", "currency",
                List.of(), List.of(), DialoguePrompt.NONE, false);
        DialogueNode intro = new DialogueNode(
                "intro", "Welcome", "", "", List.of(), List.of(deposit, withdraw, lore));
        DialogueNode currency = new DialogueNode(
                "currency", "Currency", "", "", List.of(), List.of());

        Map<String, DialogueNode> migrated = NpcConfigLoader.migrateLegacyBankPresentation(
                "intro", new LinkedHashMap<>(Map.of("intro", intro, "currency", currency)));

        assertEquals(List.of("open_bank", "lore"),
                migrated.get("intro").options().stream().map(DialogueOption::id).toList());
        DialogueNode bank = migrated.get("bank_service");
        assertEquals(NpcPresentationKind.BANK, bank.presentation());
        assertEquals(List.of("deposit", "withdraw", "back"),
                bank.options().stream().map(DialogueOption::presentationRole).toList());
    }

    @Test
    void assignsDefaultCatalogToLegacyWorldheartTraderDefinitions() {
        assertEquals("worldheart_trader", NpcConfigLoader.defaultTradeCatalog("worldheart_trader"));
        assertEquals("", NpcConfigLoader.defaultTradeCatalog("other_trader"));
    }

    @Test
    void bridgesLegacyWorldheartCobblestoneBuybackDestination() {
        List<NpcTradeOfferDefinition> bridged = NpcConfigLoader.bridgeLegacyTradeDestinations(
                "worldheart_trader",
                List.of(cobblestoneBuy(), legacyCobblestoneBuyback()));

        assertEquals("cobblestone", bridged.get(1).destinationOfferId());
    }

    @Test
    void doesNotGuessCustomBuybackDestination() {
        List<NpcTradeOfferDefinition> custom = NpcConfigLoader.bridgeLegacyTradeDestinations(
                "custom_trader",
                List.of(cobblestoneBuy(), legacyCobblestoneBuyback()));

        assertEquals("", custom.get(1).destinationOfferId());
    }

    private static DialogueOption option(String id, String action) {
        return new DialogueOption(
                id, id, id, "", "", "intro", List.of(), List.of(),
                new DialoguePrompt("number", "Amount?", action, 10, 1, 0), false);
    }

    private static NpcTradeOfferDefinition cobblestoneBuy() {
        return new NpcTradeOfferDefinition(
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
                true);
    }

    private static NpcTradeOfferDefinition legacyCobblestoneBuyback() {
        return new NpcTradeOfferDefinition(
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
                "placed_npc");
    }
}
