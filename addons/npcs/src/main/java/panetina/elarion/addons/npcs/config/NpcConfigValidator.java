package panetina.elarion.addons.npcs.config;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.npcs.model.DialogueAction;
import panetina.elarion.addons.npcs.model.DialogueCondition;
import panetina.elarion.addons.npcs.model.DialogueDefinition;
import panetina.elarion.addons.npcs.model.DialogueNode;
import panetina.elarion.addons.npcs.model.DialogueOption;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcPresentationKind;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcTradeCatalogDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;
import panetina.elarion.addons.npcs.service.NpcTaxJurisdictionResolver;
import panetina.elarion.addons.npcs.service.NpcStoryRegistryHandlers;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

final class NpcConfigValidator {
    private NpcConfigValidator() {
    }

    static void validate(
            Map<String, NpcDefinition> npcs,
            Map<String, NpcSkinProfile> skins,
            Map<String, NpcPortraitProfile> portraits,
            Map<String, DialogueDefinition> dialogues,
            Map<String, NpcTradeCatalogDefinition> trades,
            Predicate<String> knownCondition,
            Predicate<String> knownAction,
            List<String> errors
    ) {
        for (NpcDefinition npc : npcs.values()) {
            if (npc.enabled() && !skins.containsKey(npc.skin())) errors.add("npc " + npc.id()
                    + ": unknown skin " + npc.skin());
            if (npc.enabled() && !portraits.containsKey(npc.portrait())) errors.add("npc " + npc.id()
                    + ": unknown portrait " + npc.portrait());
            if (npc.enabled() && !dialogues.containsKey(npc.dialogue())) errors.add("npc " + npc.id()
                    + ": unknown dialogue " + npc.dialogue());
            if (!npc.tradeCatalog().isBlank() && !trades.containsKey(npc.tradeCatalog())) {
                errors.add("npc " + npc.id() + ": unknown trade catalog " + npc.tradeCatalog());
            }
            if (!NpcTaxJurisdictionResolver.validPolicy(npc.taxJurisdiction())) {
                errors.add("npc " + npc.id() + ": invalid tax-jurisdiction " + npc.taxJurisdiction()
                        + "; expected auto, realm:<id>, or world:<namespaced-id>");
            }
            if (!npc.requiredAbility().isBlank() && !npc.requiredAbility().contains(".")) {
                errors.add("npc " + npc.id()
                        + ": required-ability must be namespaced, for example elarion.bank.use");
            }
            if (npc.interactionRangeBlocks() < 0.0D) {
                errors.add("npc " + npc.id() + ": interaction-range-blocks cannot be negative");
            }
            npc.tags().stream()
                    .filter(tag -> tag == null || tag.isBlank())
                    .findAny()
                    .ifPresent(tag -> errors.add("npc " + npc.id() + ": tags cannot be blank"));
        }
        validateTrades(trades, errors);
        skins.values().forEach(skin -> validateProfileType("skin " + skin.id(), skin.type(),
                List.of("placeholder", "texture", "player_body", "default"), errors));
        portraits.values().forEach(portrait -> validateProfileType("portrait " + portrait.id(), portrait.type(),
                List.of("placeholder", "texture", "player_head"), errors));
        for (DialogueDefinition dialogue : dialogues.values()) {
            if (!dialogue.nodes().containsKey(dialogue.root())) errors.add("dialogue " + dialogue.id()
                    + ": missing root node " + dialogue.root());
            validateDialogueGraph(dialogue, errors);
            for (DialogueNode node : dialogue.nodes().values()) {
                validateConditions("dialogue " + dialogue.id() + " node " + node.id(),
                        node.conditions(), knownCondition, errors);
                validateNodeIdentity(dialogue, node, errors);
                validateVariantIds(dialogue, node, errors);
                validateOptionIds(dialogue, node, errors);
                node.variants().forEach(variant -> validateConditions(
                        "dialogue " + dialogue.id() + " node " + node.id() + " variant " + variant.id(),
                        variant.conditions(), knownCondition, errors));
                for (DialogueOption option : node.options()) {
                    if (!option.next().isBlank() && !dialogue.nodes().containsKey(option.next())) {
                        errors.add("dialogue " + dialogue.id() + " option " + option.id()
                                + ": invalid next node " + option.next());
                    }
                    validateConditions("dialogue " + dialogue.id() + " option " + option.id(),
                            option.conditions(), knownCondition, errors);
                    for (DialogueAction action : option.actions()) {
                        if (!knownAction.test(action.type())) errors.add("dialogue "
                                + dialogue.id() + " option " + option.id() + ": unknown action " + action.type());
                        validateStoryAction(dialogue, option, action, errors);
                        if (action.historyWorthy()
                                && action.parameters().getOrDefault("history-outcome", "").isBlank()) {
                            errors.add("dialogue " + dialogue.id() + " option " + option.id()
                                    + ": history-worthy action requires history-outcome");
                        }
                    }
                    validatePresentationOption(dialogue.id(), node, option, errors);
                    validatePrompt("dialogue " + dialogue.id() + " option " + option.id(),
                            option.prompt(), knownAction, errors);
                }
            }
        }
    }

    static void validate(
            Map<String, NpcDefinition> npcs,
            Map<String, NpcSkinProfile> skins,
            Map<String, NpcPortraitProfile> portraits,
            Map<String, DialogueDefinition> dialogues,
            Predicate<String> knownCondition,
            Predicate<String> knownAction,
            List<String> errors
    ) {
        validate(npcs, skins, portraits, dialogues, Map.of(), knownCondition, knownAction, errors);
    }

    private static void validateDialogueGraph(
            DialogueDefinition dialogue,
            List<String> errors
    ) {
        if (dialogue.root().isBlank() || !dialogue.nodes().containsKey(dialogue.root())) {
            return;
        }

        Set<String> reachable = new LinkedHashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(dialogue.root());
        while (!queue.isEmpty()) {
            String nodeId = queue.removeFirst();
            if (!reachable.add(nodeId)) continue;
            DialogueNode node = dialogue.nodes().get(nodeId);
            if (node == null) continue;
            for (DialogueOption option : node.options()) {
                if (option.next().isBlank()) continue;
                if (dialogue.nodes().containsKey(option.next()) && !reachable.contains(option.next())) {
                    queue.add(option.next());
                }
            }
        }

        for (String nodeId : dialogue.nodes().keySet()) {
            if (!reachable.contains(nodeId)) {
                errors.add("dialogue " + dialogue.id() + " node " + nodeId
                        + ": unreachable from root " + dialogue.root());
            }
        }

        for (DialogueNode node : dialogue.nodes().values()) {
            validateServiceNodeExit(dialogue, node, errors);
        }
    }

    private static void validateStoryAction(
            DialogueDefinition dialogue,
            DialogueOption option,
            DialogueAction action,
            List<String> errors
    ) {
        String owner = "dialogue " + dialogue.id() + " option " + option.id();
        if (NpcStoryRegistryHandlers.SET_STORY_FLAG.equals(action.type())
                || NpcStoryRegistryHandlers.CLEAR_STORY_FLAG.equals(action.type())) {
            if (action.parameters().getOrDefault("flag", action.parameters().getOrDefault("id", "")).isBlank()) {
                errors.add(owner + ": story flag action requires flag");
            }
        } else if (NpcStoryRegistryHandlers.SET_ENDING.equals(action.type())) {
            if (action.parameters().getOrDefault("ending", action.parameters().getOrDefault("id", "")).isBlank()) {
                errors.add(owner + ": ending action requires ending");
            }
        } else if (NpcStoryRegistryHandlers.SET_REENTRY_NODE.equals(action.type())) {
            String node = action.parameters().getOrDefault("node",
                    action.parameters().getOrDefault("nodeId", ""));
            if (node.isBlank()) errors.add(owner + ": re-entry action requires node");
            else if (!dialogue.nodes().containsKey(node)) errors.add(owner + ": invalid re-entry node " + node);
        }
    }

    private static void validateNodeIdentity(
            DialogueDefinition dialogue,
            DialogueNode node,
            List<String> errors
    ) {
        if (node.id().isBlank()) {
            errors.add("dialogue " + dialogue.id() + ": node id cannot be blank");
        }
    }

    private static void validateOptionIds(
            DialogueDefinition dialogue,
            DialogueNode node,
            List<String> errors
    ) {
        Set<String> ids = new HashSet<>();
        for (DialogueOption option : node.options()) {
            if (option.id() == null || option.id().isBlank()) {
                errors.add("dialogue " + dialogue.id() + " node " + node.id()
                        + ": option id cannot be blank");
            } else if (!ids.add(option.id())) {
                errors.add("dialogue " + dialogue.id() + " node " + node.id()
                        + " option " + option.id() + ": duplicate option id");
            }
        }
    }

    private static void validateVariantIds(
            DialogueDefinition dialogue,
            DialogueNode node,
            List<String> errors
    ) {
        Set<String> ids = new HashSet<>();
        for (var variant : node.variants()) {
            if (variant.id().isBlank()) {
                errors.add("dialogue " + dialogue.id() + " node " + node.id()
                        + ": variant id cannot be blank");
            } else if (!ids.add(variant.id())) {
                errors.add("dialogue " + dialogue.id() + " node " + node.id()
                        + " variant " + variant.id() + ": duplicate variant id");
            }
        }
    }

    private static void validateServiceNodeExit(
            DialogueDefinition dialogue,
            DialogueNode node,
            List<String> errors
    ) {
        if (node.presentation() == NpcPresentationKind.DIALOGUE) return;
        boolean hasExit = node.options().stream().anyMatch(option ->
                option.close() || (!option.next().isBlank() && !option.next().equals(node.id())));
        if (!hasExit) {
            errors.add("dialogue " + dialogue.id() + " node " + node.id()
                    + ": " + node.presentation().id() + " presentation should provide an exit option");
        }
    }

    private static void validateTrades(
            Map<String, NpcTradeCatalogDefinition> trades,
            List<String> errors
    ) {
        for (NpcTradeCatalogDefinition catalog : trades.values()) {
            if (catalog.offers().size() > 128) {
                errors.add("trade catalog " + catalog.id() + ": cannot contain more than 128 offers");
            }
            java.util.Set<String> ids = new java.util.HashSet<>();
            Map<String, NpcTradeOfferDefinition> offersById = new java.util.LinkedHashMap<>();
            for (NpcTradeOfferDefinition offer : catalog.offers()) {
                String owner = "trade catalog " + catalog.id() + " offer " + offer.id();
                if (offer.id().isBlank()) errors.add(owner + ": id cannot be blank");
                else if (!ids.add(offer.id())) errors.add(owner + ": duplicate offer id");
                else offersById.put(offer.id(), offer);
                if (!"buy".equals(offer.direction()) && !"sell".equals(offer.direction())) {
                    errors.add(owner + ": direction must be buy or sell");
                }
                if ("sell".equals(offer.direction())) {
                    validateSellOffer(owner, offer, errors);
                }
                if (offer.label().isBlank()) errors.add(owner + ": label cannot be blank");
                if (Identifier.tryParse(offer.itemId()) == null) errors.add(owner + ": invalid item id");
                if (offer.count() < 1 || offer.count() > 64) errors.add(owner + ": count must be between 1 and 64");
                if (offer.customModelData() < 0) errors.add(owner + ": custom-model-data cannot be negative");
                if (!offer.priceKey().isBlank() && !offer.priceKey().matches("[a-z0-9_.-]+")) {
                    errors.add(owner + ": price-key must use lowercase letters, numbers, dots, hyphens, or underscores");
                }
                if (offer.price() < 1L) errors.add(owner + ": price must be positive");
                if (offer.stockLimit() < 0) errors.add(owner + ": stock-limit cannot be negative");
                if (offer.stockLimit() > 100_000) errors.add(owner + ": stock-limit cannot exceed 100000");
                if (offer.restockAmount() < 0) errors.add(owner + ": restock-amount cannot be negative");
                if (offer.restockAmount() > 100_000) errors.add(owner + ": restock-amount cannot exceed 100000");
                if (offer.stockLimit() > 0 && offer.restockAmount() > offer.stockLimit()) {
                    errors.add(owner + ": restock-amount cannot exceed stock-limit");
                }
                if (offer.restockIntervalSeconds() < 0L) {
                    errors.add(owner + ": restock-interval-seconds cannot be negative");
                }
                if (offer.restockIntervalSeconds() > 604_800L) {
                    errors.add(owner + ": restock-interval-seconds cannot exceed 604800");
                }
                if (offer.lore().size() > 8) errors.add(owner + ": lore cannot exceed 8 lines");
                offer.enchantments().forEach(enchantment -> {
                    if (Identifier.tryParse(enchantment.id()) == null) errors.add(owner + ": invalid enchantment id");
                    if (enchantment.level() < 1 || enchantment.level() > 255) {
                        errors.add(owner + ": enchantment level must be between 1 and 255");
                    }
                });
            }
            for (NpcTradeOfferDefinition offer : catalog.offers()) {
                if (!"sell".equals(offer.direction()) || !"placed_npc".equals(offer.stockDestination())) continue;
                String owner = "trade catalog " + catalog.id() + " offer " + offer.id();
                if (offer.destinationOfferId().isBlank()) {
                    errors.add(owner + ": destination-offer is required when stock-destination is placed_npc");
                    continue;
                }
                NpcTradeOfferDefinition destination = offersById.get(offer.destinationOfferId());
                if (destination == null) {
                    errors.add(owner + ": unknown destination-offer " + offer.destinationOfferId());
                } else if (!"buy".equals(destination.direction())) {
                    errors.add(owner + ": destination-offer must point to a buy offer");
                }
            }
        }
    }

    private static void validateSellOffer(
            String owner,
            NpcTradeOfferDefinition offer,
            List<String> errors
    ) {
        if (!List.of("exact_item", "exact_stack").contains(offer.sellMatch())) {
            errors.add(owner + ": sell-match must be exact_item or exact_stack");
        }
        if (!List.of("vanilla_only", "exact_components").contains(offer.componentPolicy())) {
            errors.add(owner + ": component-policy must be vanilla_only or exact_components");
        }
        if (offer.maxQuantity() < 1 || offer.maxQuantity() > 64) {
            errors.add(owner + ": max-quantity must be between 1 and 64");
        }
        if (!List.of("none", "placed_npc").contains(offer.stockDestination())) {
            errors.add(owner + ": stock-destination must be none or placed_npc");
        }
    }

    private static void validatePresentationOption(
            String dialogueId,
            DialogueNode node,
            DialogueOption option,
            List<String> errors
    ) {
        if (node.presentation() != NpcPresentationKind.TRADE) return;
        if (!option.actions().isEmpty()) {
            errors.add("dialogue " + dialogueId + " node " + node.id() + " option " + option.id()
                    + ": trade presentation options cannot execute actions; use the dedicated trade request path");
        }
        if (option.prompt().present()) {
            errors.add("dialogue " + dialogueId + " node " + node.id() + " option " + option.id()
                    + ": trade presentation options cannot use prompts; use the dedicated trade request path");
        }
    }

    private static void validatePrompt(
            String owner,
            panetina.elarion.addons.npcs.model.DialoguePrompt prompt,
            Predicate<String> knownAction,
            List<String> errors
    ) {
        if (prompt == null || !prompt.present()) return;
        if (!"number".equals(prompt.type())) {
            errors.add(owner + ": unknown prompt type " + prompt.type() + " (allowed: number)");
        }
        if (prompt.question().isBlank()) {
            errors.add(owner + ": prompt question cannot be blank");
        }
        if (prompt.action().isBlank() || !knownAction.test(prompt.action())) {
            errors.add(owner + ": unknown prompt action " + prompt.action());
        }
        if (prompt.maxDigits() < 1 || prompt.maxDigits() > 10) {
            errors.add(owner + ": prompt max-digits must be between 1 and 10");
        }
        if (prompt.minimumAmount() < 0L) {
            errors.add(owner + ": prompt min-amount cannot be negative");
        }
        if (prompt.maximumAmount() < 0L) {
            errors.add(owner + ": prompt max-amount cannot be negative");
        }
        if (prompt.maximumAmount() > 0L && prompt.minimumAmount() > prompt.maximumAmount()) {
            errors.add(owner + ": prompt min-amount cannot be greater than max-amount");
        }
    }

    private static void validateProfileType(String owner, String type, List<String> allowed, List<String> errors) {
        if (type == null || !allowed.contains(type)) {
            errors.add(owner + ": unknown type " + type + " (allowed: " + String.join(", ", allowed) + ")");
        }
    }

    private static void validateConditions(
            String owner,
            List<DialogueCondition> conditions,
            Predicate<String> knownCondition,
            List<String> errors
    ) {
        for (DialogueCondition condition : conditions) {
            if (!knownCondition.test(condition.type())) {
                errors.add(owner + ": unknown condition " + condition.type());
            }
        }
    }
}
