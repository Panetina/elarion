package panetina.elarion.addons.quests.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class QuestStorageTest {
    @TempDir
    Path temp;

    @Test
    void stateRoundTripsSharedQuestlineState() {
        QuestRuntimeState state = new QuestRuntimeState();
        QuestlineState line = new QuestlineState(
                "generic_foundation", "realm:realm1", "complete", 123L);
        line.variables.put("shared_truth", "3");
        line.evidence.add("found_note");
        state.questlines.put("generic_foundation::realm:realm1", line);

        QuestStorage storage = new QuestStorage(LoggerFactory.getLogger("test"));
        storage.save(temp, state);
        QuestRuntimeState loaded = storage.load(temp);

        QuestlineState restored = loaded.questlines.get("generic_foundation::realm:realm1");
        assertEquals("complete", restored.stageId);
        assertEquals("3", restored.variables.get("shared_truth"));
        assertTrue(restored.evidence.contains("found_note"));
    }

    @Test
    void stateRoundTripsActorBindings() {
        UUID npcId = UUID.randomUUID();
        QuestRuntimeState state = new QuestRuntimeState();
        QuestActorBindingScope bindings = new QuestActorBindingScope(
                "generic_foundation", "realm:realm1", 123L);
        bindings.actors.put("guide", new QuestActorBindingRecord(
                "guide", npcId, "generic_guide_1", "generic_guide", 123L));
        state.actorBindings.put("generic_foundation::realm:realm1", bindings);

        QuestStorage storage = new QuestStorage(LoggerFactory.getLogger("test"));
        storage.save(temp, state);
        QuestRuntimeState loaded = storage.load(temp);

        QuestActorBindingRecord restored = loaded.actorBindings
                .get("generic_foundation::realm:realm1")
                .actors
                .get("guide");
        assertEquals(npcId, restored.placedNpcId);
        assertEquals("generic_guide_1", restored.handle);
        assertEquals("generic_guide", restored.definitionId);
    }
}
