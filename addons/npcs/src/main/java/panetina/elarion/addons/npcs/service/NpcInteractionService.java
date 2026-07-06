package panetina.elarion.addons.npcs.service;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.addons.npcs.model.DialogueAction;
import panetina.elarion.addons.npcs.model.DialogueCondition;
import panetina.elarion.addons.npcs.model.DialogueDefinition;
import panetina.elarion.addons.npcs.model.DialogueNode;
import panetina.elarion.addons.npcs.model.DialogueOption;
import panetina.elarion.addons.npcs.model.DialogueSession;
import panetina.elarion.addons.npcs.model.DialogueTextVariant;
import panetina.elarion.addons.npcs.model.DialogueView;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcUiConfig;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.network.NpcDialogueClosePayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOptionPayload;
import panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueSelectPayload;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.registry.ActionContext;
import panetina.elarion.core.registry.ConditionContext;
import panetina.elarion.core.registry.RegistryExecutionContext;
import panetina.elarion.core.registry.RegistryExecutionResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcInteractionService {
    private final ElarionApi api;
    private final NpcDefinitionService definitions;
    private final NpcPlacementService placements;
    private final Map<UUID, DialogueSession> sessions = new ConcurrentHashMap<>();

    public NpcInteractionService(ElarionApi api, NpcDefinitionService definitions, NpcPlacementService placements) {
        this.api = api;
        this.definitions = definitions;
        this.placements = placements;
    }

    public void open(ServerPlayerEntity player, UUID placedNpcId) {
        PlacedNpcRecord placed = placements.find(placedNpcId).orElse(null);
        if (placed == null || !near(player, placed)) {
            player.sendMessage(Text.literal("You are too far from that NPC."), false);
            return;
        }
        NpcDefinition npc = definitions.npc(placed.definitionId()).orElse(null);
        if (npc == null || !npc.enabled()) {
            player.sendMessage(Text.literal("That NPC is not available."), false);
            return;
        }
        if (!canInteract(player, npc)) {
            player.sendMessage(Text.literal("You cannot use this NPC yet."), false);
            return;
        }
        DialogueDefinition dialogue = definitions.dialogue(placed.dialogue(npc)).orElse(null);
        if (dialogue == null) {
            player.sendMessage(Text.literal("That NPC has no valid dialogue."), false);
            return;
        }
        openNode(player, placed, npc, dialogue, dialogue.root(), "", false, "", "", "");
    }

    public void select(ServerPlayerEntity player, NpcDialogueSelectPayload payload) {
        handleOption(player, payload.npcId(), payload.nodeId(), payload.optionId(), "");
    }

    public void submitPrompt(ServerPlayerEntity player, NpcDialoguePromptSubmitPayload payload) {
        handleOption(player, payload.npcId(), payload.nodeId(), payload.optionId(), payload.value());
    }

    private void handleOption(
            ServerPlayerEntity player,
            UUID npcId,
            String nodeId,
            String optionId,
            String promptValue
    ) {
        DialogueSession session = sessions.get(player.getUuid());
        if (session == null || !session.npcId().equals(npcId) || !session.nodeId().equals(nodeId)) {
            player.sendMessage(Text.literal("That dialogue is no longer active."), false);
            ServerPlayNetworking.send(player, NpcDialogueClosePayload.INSTANCE);
            return;
        }
        PlacedNpcRecord placed = placements.find(npcId).orElse(null);
        if (placed == null || !near(player, placed)) {
            sessions.remove(player.getUuid());
            ServerPlayNetworking.send(player, NpcDialogueClosePayload.INSTANCE);
            return;
        }
        NpcDefinition npc = definitions.npc(placed.definitionId()).orElse(null);
        DialogueDefinition dialogue = npc == null ? null : definitions.dialogue(session.dialogueId()).orElse(null);
        DialogueNode node = dialogue == null ? null : dialogue.nodes().get(nodeId);
        if (npc == null || dialogue == null || node == null) {
            sessions.remove(player.getUuid());
            ServerPlayNetworking.send(player, NpcDialogueClosePayload.INSTANCE);
            return;
        }
        if (!canInteract(player, npc)) {
            sessions.remove(player.getUuid());
            ServerPlayNetworking.send(player, NpcDialogueClosePayload.INSTANCE);
            return;
        }
        DialogueOption option = node.options().stream()
                .filter(candidate -> candidate.id().equals(optionId))
                .findFirst()
                .orElse(null);
        if (option == null || !visible(player, option.conditions(), placed)) {
            player.sendMessage(Text.literal("That option is not available."), false);
            return;
        }
        String promptError = validatePromptValue(option, promptValue);
        if (!promptError.isBlank()) {
            player.sendMessage(Text.literal(promptError), false);
            return;
        }
        if (!sessions.remove(player.getUuid(), session)) {
            return;
        }
        String feedback = "";
        boolean feedbackError = false;
        List<DialogueAction> actions = new ArrayList<>(option.actions());
        if (option.prompt().present()) {
            actions.add(new DialogueAction(option.prompt().action(), Map.of(), false));
        }
        for (DialogueAction action : actions) {
            RegistryExecutionResult result = api.registries().execute(
                    new ActionContext(context(player, placed), action.type(),
                            promptParameters(action.parameters(), promptValue)));
            if (!result.success()) {
                feedback = result.message();
                feedbackError = true;
                if (!definitions.ui().showActionFeedbackInGui() || definitions.ui().alsoSendActionFeedbackToChat()) {
                    player.sendMessage(Text.literal(feedback), false);
                }
                openNode(player, placed, npc, dialogue, node.id(), feedback, true,
                        option.playerText(), option.sound(), option.voice());
                return;
            }
            if (!result.message().isBlank()) {
                feedback = result.message();
                if (!definitions.ui().showActionFeedbackInGui() || definitions.ui().alsoSendActionFeedbackToChat()) {
                    player.sendMessage(Text.literal(feedback), false);
                }
            }
            result.serverTasks().forEach(Runnable::run);
        }
        String nextNode = option.next().isBlank() ? node.id() : option.next();
        openNode(player, placed, npc, dialogue, nextNode, feedback, feedbackError,
                option.playerText(), option.sound(), option.voice());
    }

    public void dismiss(ServerPlayerEntity player, UUID npcId) {
        DialogueSession session = sessions.get(player.getUuid());
        if (session != null && session.npcId().equals(npcId)) {
            sessions.remove(player.getUuid(), session);
        }
    }

    public void dismissPlayer(UUID playerId) {
        sessions.remove(playerId);
    }

    public void closeNpcSessions(UUID npcId) {
        List<UUID> affectedPlayers = sessions.values().stream()
                .filter(session -> session.npcId().equals(npcId))
                .map(DialogueSession::playerId)
                .toList();
        affectedPlayers.forEach(sessions::remove);
        placements.server().ifPresent(server -> affectedPlayers.forEach(playerId -> {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) ServerPlayNetworking.send(player, NpcDialogueClosePayload.INSTANCE);
        }));
    }

    private void openNode(
            ServerPlayerEntity player,
            PlacedNpcRecord placed,
            NpcDefinition npc,
            DialogueDefinition dialogue,
            String nodeId,
            String feedback,
            boolean feedbackError,
            String playerText,
            String playerSound,
            String playerVoice
    ) {
        DialogueNode node = dialogue.nodes().get(nodeId);
        if (node == null || !visible(player, node.conditions(), placed)) {
            player.sendMessage(Text.literal("That dialogue is not available."), false);
            return;
        }
        DialogueView view = view(player, placed, npc, dialogue.id(), node, feedback, feedbackError,
                playerText, playerSound, playerVoice);
        sessions.put(player.getUuid(), new DialogueSession(
                player.getUuid(), placed.id(), dialogue.id(), node.id(), System.currentTimeMillis()));
        ServerPlayNetworking.send(player, new NpcDialogueOpenPayload(
                view.npcId(),
                view.dialogueId(),
                view.nodeId(),
                view.npcName(),
                view.portrait(),
                view.portraitType(),
                view.portraitPlayerName(),
                view.portraitFallbackType(),
                view.portraitFallbackTexture(),
                view.playerText(),
                view.text(),
                view.npcSound(),
                view.npcVoice(),
                view.playerSound(),
                view.playerVoice(),
                view.feedback(),
                view.feedbackError(),
                view.currencyBalance() != null,
                view.currencyBalance() == null ? 0L : view.currencyBalance(),
                api.serverIdentity().currencyPlural(),
                view.relationLabel(),
                view.relationValue() == null ? 0 : view.relationValue(),
                definitions.ui().panelWidth(),
                definitions.ui().minPanelHeight(),
                definitions.ui().maxPanelHeight(),
                definitions.ui().minimumUiScalePercent(),
                definitions.ui().optionRowHeight(),
                definitions.ui().visibleOptionRows(),
                definitions.ui().scrollbarWidth(),
                definitions.ui().padding(),
                definitions.ui().buttonHeight(),
                definitions.ui().compactButtonHeight(),
                definitions.ui().buttonGap(),
                definitions.ui().contentGap(),
                definitions.ui().npcRowHeight(),
                definitions.ui().playerRowHeight(),
                definitions.ui().optionColumnsWide(),
                definitions.ui().portraitSize(),
                definitions.ui().playerPortraitSize(),
                definitions.ui().showPortraitReference(),
                definitions.ui().showRelationBar(),
                definitions.ui().typingEnabled(),
                definitions.ui().typingCharactersPerSecond(),
                definitions.ui().typingClickCompletes(),
                definitions.ui().typingSoundEnabled(),
                definitions.ui().typingSoundIntervalCharacters(),
                "npc",
                view.cards().stream()
                        .map(card -> new panetina.elarion.addons.npcs.network.NpcDialogueCardPayload(
                                card.id(), card.label(), card.icon(), card.count(), card.currencyAmount(), card.disabled()))
                        .toList(),
                view.options().stream()
                        .map(option -> new NpcDialogueOptionPayload(
                                option.id(), option.buttonText(), option.playerText(),
                                option.promptType(), option.promptQuestion(), option.promptMaxDigits()))
                        .toList()));
    }

    private DialogueView view(
            ServerPlayerEntity player,
            PlacedNpcRecord placed,
            NpcDefinition npc,
            String dialogueId,
            DialogueNode node,
            String feedback,
            boolean feedbackError,
            String playerText,
            String playerSound,
            String playerVoice
    ) {
        List<DialogueView.OptionView> options = new ArrayList<>();
        for (DialogueOption option : node.options()) {
            if (visible(player, option.conditions(), placed)) {
                options.add(new DialogueView.OptionView(
                        option.id(),
                        option.buttonText(),
                        option.playerText(),
                        option.prompt().type(),
                        option.prompt().question(),
                        option.prompt().maxDigits()));
            }
        }
        String portraitId = placed.portrait(npc);
        NpcPortraitProfile portraitProfile = definitions.portrait(portraitId).orElse(null);
        String portrait = portraitProfile == null ? "" : portraitProfile.texture();
        String portraitType = portraitProfile == null ? "placeholder" : portraitProfile.type();
        String portraitPlayerName = portraitProfile == null ? "" : portraitProfile.playerName();
        String portraitFallbackType = portraitProfile == null ? "placeholder" : portraitProfile.fallbackType();
        String portraitFallbackTexture = portraitProfile == null ? "" : portraitProfile.fallbackTexture();
        NpcUiConfig ui = definitions.ui();
        String displayedFeedback = ui.showActionFeedbackInGui() ? feedback : "";
        SelectedNodeText selected = selectedNodeText(player, node, placed);
        String npcResponse = responseText(selected.text(), displayedFeedback);
        return new DialogueView(
                placed.id(), dialogueId, node.id(), placed.displayName(npc), portrait, portraitType,
                portraitPlayerName, portraitFallbackType, portraitFallbackTexture,
                playerText == null ? "" : playerText,
                npcResponse,
                selected.sound(), selected.voice(),
                playerSound == null ? "" : playerSound,
                playerVoice == null ? "" : playerVoice,
                "", feedbackError,
                bankBalance(player), "Relation: Neutral", 0, List.of(), options);
    }

    private String validatePromptValue(DialogueOption option, String value) {
        if (!option.prompt().present()) return "";
        String raw = value == null ? "" : value;
        if (!"number".equals(option.prompt().type())) return "That prompt is not available.";
        if (raw.isBlank()) return "Enter an amount.";
        if (raw.length() > option.prompt().maxDigits()) return "That amount is too long.";
        for (int index = 0; index < raw.length(); index++) {
            if (!Character.isDigit(raw.charAt(index))) return "Only numbers are allowed.";
        }
        long amount;
        try {
            amount = Long.parseLong(raw);
        } catch (NumberFormatException exception) {
            return "Invalid amount.";
        }
        if (amount > Integer.MAX_VALUE) return "Amount is too large.";
        if (amount < option.prompt().minimumAmount()) return "Amount must be at least "
                + option.prompt().minimumAmount() + ".";
        if (option.prompt().maximumAmount() > 0L && amount > option.prompt().maximumAmount()) {
            return "Amount cannot be more than " + option.prompt().maximumAmount() + ".";
        }
        return "";
    }

    private static Map<String, String> promptParameters(Map<String, String> parameters, String promptValue) {
        if (promptValue == null || promptValue.isBlank()) return parameters;
        Map<String, String> copy = new LinkedHashMap<>(parameters);
        copy.put("amount", promptValue);
        return copy;
    }

    private Long bankBalance(ServerPlayerEntity player) {
        RegistryExecutionResult result = api.registries().execute(new ActionContext(
                context(player, null),
                "elarion:economy_bank_balance",
                Map.of()));
        if (!result.success()) return null;
        try {
            return Long.parseLong(result.message());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean visible(ServerPlayerEntity player, List<DialogueCondition> conditions, PlacedNpcRecord placed) {
        for (DialogueCondition condition : conditions) {
            RegistryExecutionResult result = api.registries().evaluate(
                    new ConditionContext(context(player, placed), condition.type(), condition.parameters()));
            if (!result.success()) return false;
        }
        return true;
    }

    private SelectedNodeText selectedNodeText(ServerPlayerEntity player, DialogueNode node, PlacedNpcRecord placed) {
        for (DialogueTextVariant variant : node.variants()) {
            if (!visible(player, variant.conditions(), placed)) continue;
            return new SelectedNodeText(
                    variant.text().isBlank() ? node.text() : variant.text(),
                    variant.sound().isBlank() ? node.sound() : variant.sound(),
                    variant.voice().isBlank() ? node.voice() : variant.voice());
        }
        return new SelectedNodeText(node.text(), node.sound(), node.voice());
    }

    private RegistryExecutionContext context(ServerPlayerEntity player, PlacedNpcRecord placed) {
        CitizenRecord citizen = api.citizens().getOrCreate(player);
        Map<String, String> metadata = new LinkedHashMap<>();
        if (placed != null) {
            metadata.put("npcId", placed.id().toString());
            metadata.put("npcDefinition", placed.definitionId());
        }
        return new RegistryExecutionContext(
                api,
                player.getServer(),
                player,
                player.getUuid(),
                citizen.realmId(),
                null,
                "",
                player.getWorld().getRegistryKey().getValue().toString(),
                "elarion_npcs",
                metadata);
    }

    static String responseText(String nodeText, String actionFeedback) {
        return actionFeedback == null || actionFeedback.isBlank() ? nodeText : actionFeedback;
    }

    private boolean near(ServerPlayerEntity player, PlacedNpcRecord placed) {
        return player.getWorld().getRegistryKey().getValue().toString().equals(placed.worldId())
                && player.squaredDistanceTo(placed.x(), placed.y(), placed.z()) <= interactionDistanceSquared(placed);
    }

    private boolean canInteract(ServerPlayerEntity player, NpcDefinition npc) {
        if (npc.requiredAbility().isBlank() || player.hasPermissionLevel(4)) return true;
        return api.abilities().has(api.citizens().getOrCreate(player), npc.requiredAbility());
    }

    private double interactionDistanceSquared(PlacedNpcRecord placed) {
        NpcDefinition npc = definitions.npc(placed.definitionId()).orElse(null);
        double range = npc == null || npc.interactionRangeBlocks() <= 0.0D
                ? definitions.ui().defaultInteractionRangeBlocks()
                : npc.interactionRangeBlocks();
        double bounded = Math.max(1.0D, Math.min(64.0D, range));
        return bounded * bounded;
    }

    private record SelectedNodeText(String text, String sound, String voice) {
    }
}
