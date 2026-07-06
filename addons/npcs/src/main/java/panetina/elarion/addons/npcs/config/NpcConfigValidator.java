package panetina.elarion.addons.npcs.config;

import panetina.elarion.addons.npcs.model.DialogueAction;
import panetina.elarion.addons.npcs.model.DialogueCondition;
import panetina.elarion.addons.npcs.model.DialogueDefinition;
import panetina.elarion.addons.npcs.model.DialogueNode;
import panetina.elarion.addons.npcs.model.DialogueOption;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

final class NpcConfigValidator {
    private NpcConfigValidator() {
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
        for (NpcDefinition npc : npcs.values()) {
            if (npc.enabled() && !skins.containsKey(npc.skin())) errors.add("npc " + npc.id()
                    + ": unknown skin " + npc.skin());
            if (npc.enabled() && !portraits.containsKey(npc.portrait())) errors.add("npc " + npc.id()
                    + ": unknown portrait " + npc.portrait());
            if (npc.enabled() && !dialogues.containsKey(npc.dialogue())) errors.add("npc " + npc.id()
                    + ": unknown dialogue " + npc.dialogue());
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
        skins.values().forEach(skin -> validateProfileType("skin " + skin.id(), skin.type(),
                List.of("placeholder", "texture", "player_body", "default"), errors));
        portraits.values().forEach(portrait -> validateProfileType("portrait " + portrait.id(), portrait.type(),
                List.of("placeholder", "texture", "player_head"), errors));
        for (DialogueDefinition dialogue : dialogues.values()) {
            if (!dialogue.nodes().containsKey(dialogue.root())) errors.add("dialogue " + dialogue.id()
                    + ": missing root node " + dialogue.root());
            for (DialogueNode node : dialogue.nodes().values()) {
                validateConditions("dialogue " + dialogue.id() + " node " + node.id(),
                        node.conditions(), knownCondition, errors);
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
                    }
                    validatePrompt("dialogue " + dialogue.id() + " option " + option.id(),
                            option.prompt(), knownAction, errors);
                }
            }
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
