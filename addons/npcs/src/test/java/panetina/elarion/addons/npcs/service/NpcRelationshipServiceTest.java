package panetina.elarion.addons.npcs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.npcs.storage.NpcRelationshipStorage;
import panetina.elarion.core.registry.ActionContext;
import panetina.elarion.core.registry.ConditionContext;
import panetina.elarion.core.registry.RegistryExecutionContext;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcRelationshipServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void setAndAddClampRelationshipScore() {
        NpcRelationshipService service = service();
        UUID playerId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();

        service.set(playerId, npcId, 12_000);
        assertEquals(NpcRelationshipService.MAX_SCORE, service.score(playerId, npcId));

        service.add(playerId, npcId, -25_000);
        assertEquals(NpcRelationshipService.MIN_SCORE, service.score(playerId, npcId));
    }

    @Test
    void registryActionsAndConditionUseCurrentNpcMetadata() {
        NpcRelationshipService service = service();
        UUID playerId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        RegistryExecutionContext execution = new RegistryExecutionContext(
                null, null, null, playerId, "", null, "", "", "elarion_npcs",
                Map.of("npcId", npcId.toString()));

        var set = NpcRelationshipRegistryHandlers.setRelationship(
                service,
                new ActionContext(execution, NpcRelationshipRegistryHandlers.SET_RELATIONSHIP,
                        Map.of("value", "20")));
        var add = NpcRelationshipRegistryHandlers.addRelationship(
                service,
                new ActionContext(execution, NpcRelationshipRegistryHandlers.ADD_RELATIONSHIP,
                        Map.of("amount", "7")));
        var pass = NpcRelationshipRegistryHandlers.relationshipAtLeast(
                service,
                new ConditionContext(execution, NpcRelationshipRegistryHandlers.RELATIONSHIP_AT_LEAST,
                        Map.of("minimum", "25")));
        var fail = NpcRelationshipRegistryHandlers.relationshipAtLeast(
                service,
                new ConditionContext(execution, NpcRelationshipRegistryHandlers.RELATIONSHIP_AT_LEAST,
                        Map.of("minimum", "30")));

        assertTrue(set.success(), set.message());
        assertTrue(add.success(), add.message());
        assertEquals(27, service.score(playerId, npcId));
        assertTrue(pass.success(), pass.message());
        assertTrue(!fail.success());
    }

    @Test
    void registryActionRejectsMissingNpcContext() {
        NpcRelationshipService service = service();
        RegistryExecutionContext execution = new RegistryExecutionContext(
                null, null, null, UUID.randomUUID(), "", null, "", "", "elarion_npcs", Map.of());

        var result = NpcRelationshipRegistryHandlers.addRelationship(
                service,
                new ActionContext(execution, NpcRelationshipRegistryHandlers.ADD_RELATIONSHIP,
                        Map.of("amount", "1")));

        assertTrue(!result.success());
    }

    @Test
    void summaryIsIncrementalAndDoesNotDoubleCountUpdates() {
        UUID playerId = UUID.randomUUID();
        UUID firstNpc = UUID.randomUUID();
        UUID secondNpc = UUID.randomUUID();
        NpcRelationshipService service = service(npcId -> npcId.equals(firstNpc) ? "realm:realm1" : "worldheart");

        service.set(playerId, firstNpc, 1_000);
        service.set(playerId, secondNpc, 3_000);
        assertEquals(1, service.factionSummaries(playerId).get("realm:realm1").contactCount());
        assertEquals(1_000, service.factionSummaries(playerId).get("realm:realm1").totalScore());
        assertEquals(3_000, service.factionSummaries(playerId).get("worldheart").totalScore());

        service.set(playerId, firstNpc, 5_000);
        assertEquals(1, service.factionSummaries(playerId).get("realm:realm1").contactCount());
        assertEquals(5_000, service.factionSummaries(playerId).get("realm:realm1").totalScore());
        assertTrue(service.factionSummaries(UUID.randomUUID()).isEmpty());
    }

    @Test
    void factionStandingAndRegistryRequirementReuseIncrementalSummary() {
        UUID playerId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        NpcRelationshipService service = service(ignored -> "worldheart");
        service.set(playerId, npcId, 122);
        RegistryExecutionContext execution = new RegistryExecutionContext(
                null, null, null, playerId, "", null, "", "", "elarion_npcs", Map.of());

        assertEquals("liked", service.faction(playerId, "worldheart").standingId());
        assertEquals(2, service.faction(playerId, "worldheart").progress());
        assertTrue(service.meetsStanding(playerId, "worldheart", "liked"));
        assertTrue(!service.meetsStanding(playerId, "worldheart", "loved"));

        var byStanding = NpcRelationshipRegistryHandlers.factionReputationAtLeast(
                service,
                new ConditionContext(execution, NpcRelationshipRegistryHandlers.FACTION_REPUTATION_AT_LEAST,
                        Map.of("faction", "worldheart", "standing", "liked")));
        var byScore = NpcRelationshipRegistryHandlers.factionReputationAtLeast(
                service,
                new ConditionContext(execution, NpcRelationshipRegistryHandlers.FACTION_REPUTATION_AT_LEAST,
                        Map.of("faction", "worldheart", "minimum", "200")));

        assertTrue(byStanding.success(), byStanding.message());
        assertTrue(!byScore.success());
    }

    private NpcRelationshipService service() {
        return service(ignored -> "unaffiliated");
    }

    private NpcRelationshipService service(java.util.function.Function<UUID, String> factionResolver) {
        NpcRelationshipService service = new NpcRelationshipService(
                LoggerFactory.getLogger("npc-relationship-service-test"),
                new NpcRelationshipStorage(LoggerFactory.getLogger("npc-relationship-storage-test"), tempDir),
                factionResolver);
        service.bind(null);
        return service;
    }
}
