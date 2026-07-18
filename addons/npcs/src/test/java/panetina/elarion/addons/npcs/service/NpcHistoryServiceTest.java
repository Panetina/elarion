package panetina.elarion.addons.npcs.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcHistoryServiceTest {
    @Test
    void createsStructuredOutcomeWithPersistedVariant() {
        UUID eventId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        PlacedNpcRecord placed = new PlacedNpcRecord(
                npcId, "tavin", "tavin", UUID.randomUUID(), "minecraft:overworld",
                0, 64, 0, 0, 0, "", "", "", "", playerId, 1L);
        NpcDefinition npc = new NpcDefinition(
                "tavin", "Tavin", "", "skin", "portrait", "dialogue",
                List.of(), "", 6.0D, true);

        var event = NpcHistoryService.storyOutcomeEvent(eventId, 123L, playerId, "Mara", "realm1",
                placed, npc, "dialogue", "root", "pledge", "an honest alliance");

        assertEquals("npc", event.category());
        assertEquals("story-outcome", event.type());
        assertEquals("Mara", event.metadata().get("actor"));
        assertEquals("Tavin", event.metadata().get("npc"));
        assertEquals("an honest alliance", event.metadata().get("outcome"));
        assertTrue(event.metadata().get("chronicle.variant").startsWith("npc.story-outcome."));
    }
}
