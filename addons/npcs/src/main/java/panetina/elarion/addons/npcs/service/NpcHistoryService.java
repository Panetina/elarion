package panetina.elarion.addons.npcs.service;

import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.NpcChronicleText;
import panetina.elarion.addons.npcs.model.DialogueAction;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.model.PublicHistoryEntry;
import panetina.elarion.core.service.ChronicleRendererRegistry;
import panetina.elarion.core.service.ChronicleVariantSelector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class NpcHistoryService {
    private static final ChronicleVariantSelector SELECTOR = new ChronicleVariantSelector();
    private final Logger logger;
    private final ElarionApi api;

    public NpcHistoryService(Logger logger, ElarionApi api) {
        this.logger = logger;
        this.api = api;
    }

    public void recordOutcome(ServerPlayerEntity player, PlacedNpcRecord placed, NpcDefinition npc,
                              String dialogueId, String nodeId, String optionId, DialogueAction action) {
        if (!action.historyWorthy()) return;
        String outcome = action.parameters().getOrDefault("history-outcome", "").trim();
        if (outcome.isBlank()) return;
        String realmId = api.citizens().getOrCreate(player).realmId();
        HistoryEvent event = storyOutcomeEvent(UUID.randomUUID(), System.currentTimeMillis(),
                player.getUuid(), player.getDisplayName().getString(), realmId, placed, npc,
                dialogueId, nodeId, optionId, outcome);
        try {
            api.history().record(event);
        } catch (RuntimeException exception) {
            logger.error("Failed to record NPC story outcome {} for {}", outcome, placed.id(), exception);
        }
    }

    static HistoryEvent storyOutcomeEvent(UUID eventId, long timestamp, UUID actorId, String actorName,
                                          String realmId, PlacedNpcRecord placed, NpcDefinition npc,
                                          String dialogueId, String nodeId, String optionId, String outcome) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("actor", actorName);
        metadata.put("npc", npc.displayName());
        metadata.put("outcome", outcome);
        metadata.put("npcDefinition", npc.id());
        metadata.put("dialogue", dialogueId);
        metadata.put("node", nodeId);
        metadata.put("option", optionId);
        PublicHistoryEntry projection = new PublicHistoryEntry(eventId, timestamp, "live-index", "npc",
                "story-outcome", actorId, "npc", placed.id().toString(), realmId, metadata, "");
        metadata.put(ChronicleRendererRegistry.VARIANT_METADATA_KEY,
                SELECTOR.selectVariantId(projection, NpcChronicleText.storyOutcomeFamily()));
        return new HistoryEvent(eventId, timestamp, "npc", "story-outcome",
                actorId, "npc", placed.id().toString(), realmId, metadata, "");
    }
}
