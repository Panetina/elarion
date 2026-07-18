package panetina.elarion.addons.npcs.service;

import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.registry.ActionContext;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.ConditionContext;
import panetina.elarion.core.registry.ConditionType;
import panetina.elarion.core.registry.RegistryExecutionResult;

import java.util.Map;
import java.util.UUID;

public final class NpcStoryRegistryHandlers {
    public static final String SET_STORY_FLAG = "elarion_npcs:set_story_flag";
    public static final String CLEAR_STORY_FLAG = "elarion_npcs:clear_story_flag";
    public static final String STORY_FLAG_SET = "elarion_npcs:story_flag_set";
    public static final String SET_ENDING = "elarion_npcs:set_ending";
    public static final String ENDING_IS = "elarion_npcs:ending_is";
    public static final String SET_REENTRY_NODE = "elarion_npcs:set_reentry_node";
    private static final String OWNER = "elarion_npcs";

    private NpcStoryRegistryHandlers() {
    }

    public static void register(ElarionApi api, NpcStoryStateService stories) {
        registerAction(api, SET_STORY_FLAG, "Sets a durable story flag for the current player and placed NPC.",
                context -> setFlag(stories, context, true));
        registerAction(api, CLEAR_STORY_FLAG, "Clears a durable story flag for the current player and placed NPC.",
                context -> setFlag(stories, context, false));
        registerCondition(api, STORY_FLAG_SET, "Checks a durable story flag for the current player and placed NPC.",
                context -> storyFlagSet(stories, context));
        registerAction(api, SET_ENDING, "Locks the current placed NPC's story ending for a player.",
                context -> setEnding(stories, context));
        registerCondition(api, ENDING_IS, "Checks the current placed NPC's story ending for a player.",
                context -> endingIs(stories, context));
        registerAction(api, SET_REENTRY_NODE, "Sets the dialogue node used when this player reopens the placed NPC.",
                context -> setReentryNode(stories, context));
    }

    static RegistryExecutionResult setFlag(NpcStoryStateService stories, ActionContext context, boolean enabled) {
        StoryContext story = storyContext(context.execution().actorId(), context.parameters(),
                context.execution().metadata());
        if (!story.valid()) return RegistryExecutionResult.failure(story.error());
        stories.setFlag(story.playerId(), story.npcId(), story.value(), enabled);
        return RegistryExecutionResult.ok();
    }

    static RegistryExecutionResult storyFlagSet(NpcStoryStateService stories, ConditionContext context) {
        StoryContext story = storyContext(context.execution().actorId(), context.parameters(),
                context.execution().metadata());
        if (!story.valid()) return RegistryExecutionResult.failure(story.error());
        return stories.hasFlag(story.playerId(), story.npcId(), story.value())
                ? RegistryExecutionResult.ok()
                : RegistryExecutionResult.failure("NPC story flag is not set");
    }

    static RegistryExecutionResult setEnding(NpcStoryStateService stories, ActionContext context) {
        StoryContext story = storyContext(context.execution().actorId(), context.parameters(),
                context.execution().metadata(), "ending", "id");
        if (!story.valid()) return RegistryExecutionResult.failure(story.error());
        stories.setEnding(story.playerId(), story.npcId(), story.value());
        return RegistryExecutionResult.ok();
    }

    static RegistryExecutionResult endingIs(NpcStoryStateService stories, ConditionContext context) {
        StoryContext story = storyContext(context.execution().actorId(), context.parameters(),
                context.execution().metadata(), "ending", "id");
        if (!story.valid()) return RegistryExecutionResult.failure(story.error());
        return stories.state(story.playerId(), story.npcId()).endingId().equals(story.value())
                ? RegistryExecutionResult.ok()
                : RegistryExecutionResult.failure("NPC ending does not match");
    }

    static RegistryExecutionResult setReentryNode(NpcStoryStateService stories, ActionContext context) {
        StoryContext story = storyContext(context.execution().actorId(), context.parameters(),
                context.execution().metadata(), "node", "nodeId");
        if (!story.valid()) return RegistryExecutionResult.failure(story.error());
        stories.setReentryNode(story.playerId(), story.npcId(), story.value());
        return RegistryExecutionResult.ok();
    }

    private static StoryContext storyContext(UUID actorId, Map<String, String> parameters,
                                             Map<String, String> metadata) {
        return storyContext(actorId, parameters, metadata, "flag", "id");
    }

    private static StoryContext storyContext(UUID actorId, Map<String, String> parameters,
                                             Map<String, String> metadata, String primary, String secondary) {
        String rawPlayer = parameters.getOrDefault("player", parameters.getOrDefault("playerId", "")).trim();
        UUID playerId = rawPlayer.isBlank() ? actorId : uuid(rawPlayer);
        String rawNpc = parameters.getOrDefault("npc", parameters.getOrDefault("npcId", "")).trim();
        if (rawNpc.isBlank()) rawNpc = metadata.getOrDefault("npcId", "");
        UUID npcId = uuid(rawNpc);
        String value = parameters.getOrDefault(primary, parameters.getOrDefault(secondary, "")).trim();
        if (playerId == null) return new StoryContext(null, npcId, value, "missing story player");
        if (npcId == null) return new StoryContext(playerId, null, value, "missing story NPC");
        if (value.isBlank()) return new StoryContext(playerId, npcId, value, "missing story " + primary);
        return new StoryContext(playerId, npcId, value, "");
    }

    private static UUID uuid(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void registerAction(ElarionApi api, String id, String description,
                                       panetina.elarion.core.registry.ActionHandler handler) {
        api.registries().actions().register(new ActionType(id, OWNER, description));
        api.registries().registerActionHandler(id, handler);
    }

    private static void registerCondition(ElarionApi api, String id, String description,
                                          panetina.elarion.core.registry.ConditionHandler handler) {
        api.registries().conditions().register(new ConditionType(id, OWNER, description));
        api.registries().registerConditionHandler(id, handler);
    }

    private record StoryContext(UUID playerId, UUID npcId, String value, String error) {
        boolean valid() {
            return error.isBlank();
        }
    }
}
