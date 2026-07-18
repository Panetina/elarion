package panetina.elarion.addons.npcs.service;

import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.registry.ActionContext;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.ConditionContext;
import panetina.elarion.core.registry.ConditionType;
import panetina.elarion.core.registry.RegistryExecutionResult;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class NpcRelationshipRegistryHandlers {
    public static final String SET_RELATIONSHIP = "elarion_npcs:set_relationship";
    public static final String ADD_RELATIONSHIP = "elarion_npcs:add_relationship";
    public static final String RELATIONSHIP_AT_LEAST = "elarion_npcs:relationship_at_least";
    public static final String FACTION_REPUTATION_AT_LEAST = "elarion_npcs:faction_reputation_at_least";
    private static final String OWNER = "elarion_npcs";

    private NpcRelationshipRegistryHandlers() {
    }

    public static void register(ElarionApi api, NpcRelationshipService relationships) {
        registerAction(api, SET_RELATIONSHIP, "Sets a player's relationship score with the current placed NPC.",
                context -> setRelationship(relationships, context));
        registerAction(api, ADD_RELATIONSHIP, "Adds to a player's relationship score with the current placed NPC.",
                context -> addRelationship(relationships, context));
        registerCondition(api, RELATIONSHIP_AT_LEAST,
                "Checks whether a player's relationship score with the current placed NPC is at least a value.",
                context -> relationshipAtLeast(relationships, context));
        registerCondition(api, FACTION_REPUTATION_AT_LEAST,
                "Checks a player's bounded faction reputation by minimum score or standing.",
                context -> factionReputationAtLeast(relationships, context));
    }

    static RegistryExecutionResult factionReputationAtLeast(
            NpcRelationshipService relationships,
            ConditionContext context
    ) {
        UUID playerId = context.execution().actorId();
        String faction = context.parameters().getOrDefault("faction", "").trim();
        if (playerId == null) return RegistryExecutionResult.failure("missing reputation player");
        if (faction.isBlank()) return RegistryExecutionResult.failure("missing reputation faction");
        Integer minimum = integer(context.parameters(), "minimum", "value");
        boolean accepted;
        if (minimum != null) {
            accepted = relationships.meets(playerId, faction, minimum);
        } else {
            String standing = context.parameters().getOrDefault("standing", "");
            if (standing.isBlank()) return RegistryExecutionResult.failure("missing reputation requirement");
            accepted = relationships.meetsStanding(playerId, faction, standing);
        }
        return accepted ? RegistryExecutionResult.ok()
                : RegistryExecutionResult.failure("faction reputation below requirement");
    }

    static RegistryExecutionResult setRelationship(NpcRelationshipService relationships, ActionContext context) {
        UUID playerId = playerId(context);
        UUID npcId = npcId(context.parameters(), context.execution().metadata());
        if (playerId == null) return RegistryExecutionResult.failure("missing relationship player");
        if (npcId == null) return RegistryExecutionResult.failure("missing relationship NPC");
        Integer value = integer(context.parameters(), "value", "score");
        if (value == null) return RegistryExecutionResult.failure("missing relationship score");
        relationships.set(playerId, npcId, value);
        return RegistryExecutionResult.ok();
    }

    static RegistryExecutionResult addRelationship(NpcRelationshipService relationships, ActionContext context) {
        UUID playerId = playerId(context);
        UUID npcId = npcId(context.parameters(), context.execution().metadata());
        if (playerId == null) return RegistryExecutionResult.failure("missing relationship player");
        if (npcId == null) return RegistryExecutionResult.failure("missing relationship NPC");
        Integer amount = integer(context.parameters(), "amount", "delta");
        if (amount == null) return RegistryExecutionResult.failure("missing relationship amount");
        relationships.add(playerId, npcId, amount);
        return RegistryExecutionResult.ok();
    }

    static RegistryExecutionResult relationshipAtLeast(
            NpcRelationshipService relationships,
            ConditionContext context
    ) {
        UUID playerId = context.execution().actorId();
        UUID npcId = npcId(context.parameters(), context.execution().metadata());
        if (playerId == null) return RegistryExecutionResult.failure("missing relationship player");
        if (npcId == null) return RegistryExecutionResult.failure("missing relationship NPC");
        Integer minimum = integer(context.parameters(), "minimum", "value");
        if (minimum == null) return RegistryExecutionResult.failure("missing relationship minimum");
        int score = relationships.score(playerId, npcId);
        return score >= minimum
                ? RegistryExecutionResult.ok()
                : RegistryExecutionResult.failure("NPC relationship below minimum");
    }

    private static UUID playerId(ActionContext context) {
        String raw = context.parameters().getOrDefault("player", context.parameters().getOrDefault("playerId", ""));
        if (!raw.isBlank()) return uuid(raw);
        return context.execution().actorId();
    }

    private static UUID npcId(Map<String, String> parameters, Map<String, String> metadata) {
        String raw = parameters.getOrDefault("npc", parameters.getOrDefault("npcId", ""));
        if (raw.isBlank()) raw = metadata.getOrDefault("npcId", "");
        return uuid(raw);
    }

    private static UUID uuid(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static Integer integer(Map<String, String> parameters, String primary, String secondary) {
        String raw = parameters.getOrDefault(primary, parameters.getOrDefault(secondary, ""));
        try {
            return raw.isBlank() ? null : Integer.parseInt(raw.trim().toLowerCase(Locale.ROOT));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static void registerAction(
            ElarionApi api,
            String id,
            String description,
            panetina.elarion.core.registry.ActionHandler handler
    ) {
        api.registries().actions().register(new ActionType(id, OWNER, description));
        api.registries().registerActionHandler(id, handler);
    }

    private static void registerCondition(
            ElarionApi api,
            String id,
            String description,
            panetina.elarion.core.registry.ConditionHandler handler
    ) {
        api.registries().conditions().register(new ConditionType(id, OWNER, description));
        api.registries().registerConditionHandler(id, handler);
    }
}
