package panetina.elarion.addons.npcs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.storage.NpcStoryStateStorage;
import panetina.elarion.core.registry.ActionContext;
import panetina.elarion.core.registry.ConditionContext;
import panetina.elarion.core.registry.RegistryExecutionContext;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcStoryStateServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void persistsFlagsChoicesEndingAndReentryAcrossBind() {
        UUID playerId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        NpcStoryStateService first = service();
        first.setFlag(playerId, npcId, "trusted", true);
        first.markChoiceUsed(playerId, npcId, "dialogue/root/pledge");
        first.setEnding(playerId, npcId, "allied");
        first.setReentryNode(playerId, npcId, "returning");

        NpcStoryStateService reloaded = service();
        var state = reloaded.state(playerId, npcId);
        assertTrue(state.flags().contains("trusted"));
        assertTrue(state.usedChoices().contains("dialogue/root/pledge"));
        assertEquals("allied", state.endingId());
        assertEquals("returning", state.reentryNodeId());
    }

    @Test
    void registryHandlersUseCurrentPlacedNpc() {
        NpcStoryStateService service = service();
        UUID playerId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        RegistryExecutionContext execution = new RegistryExecutionContext(
                null, null, null, playerId, "", null, "", "", "elarion_npcs",
                Map.of("npcId", npcId.toString()));

        assertTrue(NpcStoryRegistryHandlers.setFlag(service,
                new ActionContext(execution, NpcStoryRegistryHandlers.SET_STORY_FLAG,
                        Map.of("flag", "trusted")), true).success());
        assertTrue(NpcStoryRegistryHandlers.storyFlagSet(service,
                new ConditionContext(execution, NpcStoryRegistryHandlers.STORY_FLAG_SET,
                        Map.of("flag", "trusted"))).success());
        assertTrue(NpcStoryRegistryHandlers.setEnding(service,
                new ActionContext(execution, NpcStoryRegistryHandlers.SET_ENDING,
                        Map.of("ending", "allied"))).success());
        assertTrue(NpcStoryRegistryHandlers.endingIs(service,
                new ConditionContext(execution, NpcStoryRegistryHandlers.ENDING_IS,
                        Map.of("ending", "allied"))).success());
        assertTrue(NpcStoryRegistryHandlers.setReentryNode(service,
                new ActionContext(execution, NpcStoryRegistryHandlers.SET_REENTRY_NODE,
                        Map.of("node", "returning"))).success());
        assertEquals("returning", service.state(playerId, npcId).reentryNodeId());

        assertTrue(NpcStoryRegistryHandlers.setFlag(service,
                new ActionContext(execution, NpcStoryRegistryHandlers.CLEAR_STORY_FLAG,
                        Map.of("flag", "trusted")), false).success());
        assertFalse(service.hasFlag(playerId, npcId, "trusted"));
    }

    @Test
    void choiceKeysAreStableAndScoped() {
        assertEquals("mara/root/pledge", NpcStoryStateService.choiceKey("mara", "root", "pledge"));
    }

    private NpcStoryStateService service() {
        NpcStoryStateService service = new NpcStoryStateService(
                LoggerFactory.getLogger("npc-story-service-test"),
                new NpcStoryStateStorage(LoggerFactory.getLogger("npc-story-storage-test"), tempDir));
        service.bind(null);
        return service;
    }
}
