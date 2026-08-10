package panetina.elarion.core.registry;

import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.model.RealmRelationship;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ElarionRegistries {
    private final ElarionRegistry<ConditionType> conditions = new ElarionRegistry<>("condition");
    private final ElarionRegistry<ActionType> actions = new ElarionRegistry<>("action");
    private final ElarionRegistry<RequirementType> requirements = new ElarionRegistry<>("requirement");
    private final ElarionRegistry<MilestoneEventType> milestoneEvents = new ElarionRegistry<>("milestone event");
    private final PlayerContextActionRegistry playerContextActions = new PlayerContextActionRegistry();
    private final Map<String, ConditionHandler> conditionHandlers = new ConcurrentHashMap<>();
    private final Map<String, ActionHandler> actionHandlers = new ConcurrentHashMap<>();
    private final Map<String, RequirementHandler> requirementHandlers = new ConcurrentHashMap<>();
    private final Map<String, MilestoneEventHandler> milestoneHandlers = new ConcurrentHashMap<>();

    public ElarionRegistries() {
        registerBuiltIns();
    }

    public ElarionRegistry<ConditionType> conditions() {
        return conditions;
    }

    public ElarionRegistry<ActionType> actions() {
        return actions;
    }

    public ElarionRegistry<RequirementType> requirements() {
        return requirements;
    }

    public ElarionRegistry<MilestoneEventType> milestoneEvents() {
        return milestoneEvents;
    }

    public PlayerContextActionRegistry playerContextActions() {
        return playerContextActions;
    }

    public void registerConditionHandler(String id, ConditionHandler handler) {
        conditions.requireKnown(id, "condition");
        conditionHandlers.put(id, handler);
    }

    public void registerActionHandler(String id, ActionHandler handler) {
        actions.requireKnown(id, "action");
        actionHandlers.put(id, handler);
    }

    public void registerRequirementHandler(String id, RequirementHandler handler) {
        requirements.requireKnown(id, "requirement");
        requirementHandlers.put(id, handler);
    }

    public void registerMilestoneHandler(String id, MilestoneEventHandler handler) {
        milestoneEvents.requireKnown(id, "milestone event");
        milestoneHandlers.put(id, handler);
    }

    public RegistryExecutionResult evaluate(ConditionContext context) {
        ConditionHandler handler = conditionHandlers.get(context.conditionId());
        if (handler == null) {
            return RegistryExecutionResult.failure("Unknown condition handler: " + context.conditionId());
        }
        return handler.evaluate(context);
    }

    public RegistryExecutionResult execute(ActionContext context) {
        ActionHandler handler = actionHandlers.get(context.actionId());
        if (handler == null) {
            return RegistryExecutionResult.failure("Unknown action handler: " + context.actionId());
        }
        return handler.execute(context);
    }

    public RegistryExecutionResult evaluate(RequirementContext context) {
        RequirementHandler handler = requirementHandlers.get(context.requirementId());
        if (handler == null) {
            return RegistryExecutionResult.failure("Unknown requirement handler: " + context.requirementId());
        }
        return handler.evaluate(context);
    }

    public RegistryExecutionResult execute(MilestoneContext context) {
        MilestoneEventHandler handler = milestoneHandlers.get(context.milestoneId());
        if (handler == null) {
            return RegistryExecutionResult.failure("Unknown milestone handler: " + context.milestoneId());
        }
        return handler.execute(context);
    }

    private void registerBuiltIns() {
        for (String id : new String[]{
                "has_realm", "has_title", "has_ability", "relationship_is", "is_leader", "has_realm_flag",
                "has_history_event"
        }) {
            conditions.register(new ConditionType(id, "elarion_core", "Built-in Elarion condition."));
        }
        for (String id : new String[]{"run_reward", "emit_history", "close"}) {
            actions.register(new ActionType(id, "elarion_core", "Built-in Elarion action."));
        }
        for (String id : new String[]{"items", "kills", "title_unlocked", "ability_present", "realm_flag"}) {
            requirements.register(new RequirementType(id, "elarion_core", "Built-in Elarion requirement."));
        }
        for (String id : new String[]{
                "elarion:grant_title", "elarion:grant_ability", "elarion:emit_history", "elarion:run_reward"
        }) {
            milestoneEvents.register(new MilestoneEventType(id, "elarion_core", "Built-in Elarion milestone event."));
        }
        registerBuiltInHandlers();
    }

    private void registerBuiltInHandlers() {
        registerConditionHandler("has_realm", context -> {
            String expected = context.parameters().getOrDefault("realm", context.parameters().getOrDefault("id", ""));
            String actual = context.execution().actorRealmId();
            return result(!expected.isBlank() && expected.equalsIgnoreCase(actual), "realm did not match");
        });
        registerConditionHandler("has_title", context -> {
            if (context.execution().actorId() == null) return RegistryExecutionResult.failure("missing actor");
            String title = context.parameters().getOrDefault("title", context.parameters().getOrDefault("id", ""));
            CitizenRecord citizen = context.execution().api().citizens().find(context.execution().actorId()).orElse(null);
            return result(citizen != null && citizen.hasUnlockedTitle(title), "title not unlocked");
        });
        registerConditionHandler("has_ability", context -> {
            if (context.execution().actorId() == null) return RegistryExecutionResult.failure("missing actor");
            String ability = context.parameters().getOrDefault("ability", context.parameters().getOrDefault("id", ""));
            CitizenRecord citizen = context.execution().api().citizens().find(context.execution().actorId()).orElse(null);
            return result(citizen != null && context.execution().api().abilities().has(citizen, ability),
                    "ability missing");
        });
        registerConditionHandler("relationship_is", context -> {
            String first = context.parameters().getOrDefault("first", context.execution().actorRealmId());
            String second = context.parameters().getOrDefault("second", context.execution().targetRealmId());
            String expected = context.parameters().getOrDefault("relationship", "");
            if (first.isBlank() || second.isBlank() || expected.isBlank()) {
                return RegistryExecutionResult.failure("relationship parameters missing");
            }
            RealmRelationship relationship = context.execution().api().governance().relationship(first, second);
            return result(relationship.name().equalsIgnoreCase(expected), "relationship did not match");
        });
        registerConditionHandler("is_leader", context -> {
            if (context.execution().actorId() == null) return RegistryExecutionResult.failure("missing actor");
            String realm = context.parameters().getOrDefault("realm", context.execution().actorRealmId());
            boolean leader = !realm.isBlank()
                    && context.execution().api().governance().leader(realm)
                    .filter(context.execution().actorId()::equals)
                    .isPresent();
            return result(leader, "actor is not leader");
        });
        registerConditionHandler("has_realm_flag", context -> {
            String realm = context.parameters().getOrDefault("realm", context.execution().actorRealmId());
            String flag = context.parameters().getOrDefault("flag", context.parameters().getOrDefault("id", ""));
            boolean has = context.execution().api().realms().find(realm)
                    .map(definition -> definition.flags().contains(flag))
                    .orElse(false);
            return result(has, "realm flag missing");
        });
        registerConditionHandler("has_history_event", context ->
                RegistryExecutionResult.failure("history event condition requires addon-specific indexing"));

        registerActionHandler("close", context -> RegistryExecutionResult.ok());
        registerActionHandler("run_reward", context -> {
            if (context.execution().actor() == null) return RegistryExecutionResult.failure("missing actor player");
            String reward = context.parameters().getOrDefault("reward", context.parameters().getOrDefault("id", ""));
            return result(context.execution().api().rewards().executeReward(reward, context.execution().actor()),
                    "reward failed");
        });
        registerActionHandler("emit_history", context -> {
            if (context.execution().server() == null) return RegistryExecutionResult.failure("missing server");
            context.execution().api().history().record(HistoryEvent.create(
                    context.parameters().getOrDefault("category", "general"),
                    context.parameters().getOrDefault("type", "event"),
                    context.execution().actorId(),
                    context.parameters().getOrDefault("subject-type", ""),
                    context.parameters().getOrDefault("subject-id", ""),
                    context.parameters().getOrDefault("realm", context.execution().actorRealmId()),
                    context.parameters()));
            return RegistryExecutionResult.ok();
        });

        registerRequirementHandler("items", context ->
                RegistryExecutionResult.failure("item requirements need an inventory-consuming context"));
        registerRequirementHandler("kills", context ->
                RegistryExecutionResult.failure("kill requirements need a stats context"));
        registerRequirementHandler("title_unlocked", context -> evaluate(new ConditionContext(
                context.execution(), "has_title", context.parameters())));
        registerRequirementHandler("ability_present", context -> evaluate(new ConditionContext(
                context.execution(), "has_ability", context.parameters())));
        registerRequirementHandler("realm_flag", context -> evaluate(new ConditionContext(
                context.execution(), "has_realm_flag", context.parameters())));

        registerMilestoneHandler("elarion:grant_title", context -> {
            if (context.execution().actor() == null) return RegistryExecutionResult.failure("missing actor player");
            var result = context.execution().api().titles().grant(
                    context.execution().actor(),
                    context.parameters().get("title"),
                    context.execution().actorId(),
                    "milestone:" + context.milestoneId());
            return new RegistryExecutionResult(result.success(), result.message(), java.util.List.of());
        });
        registerMilestoneHandler("elarion:grant_ability", context -> {
            if (context.execution().actor() == null) return RegistryExecutionResult.failure("missing actor player");
            String ability = context.parameters().get("ability");
            if (ability == null || ability.isBlank()) return RegistryExecutionResult.failure("missing ability");
            context.execution().api().citizens().update(context.execution().actor(), "milestone-ability",
                    citizen -> context.execution().api().abilities().grant(citizen, ability));
            return RegistryExecutionResult.ok("Granted ability " + ability);
        });
        registerMilestoneHandler("elarion:emit_history", context -> execute(new ActionContext(
                context.execution(), "emit_history", context.parameters())));
        registerMilestoneHandler("elarion:run_reward", context -> execute(new ActionContext(
                context.execution(), "run_reward", context.parameters())));
    }

    private static RegistryExecutionResult result(boolean success, String failure) {
        return success ? RegistryExecutionResult.ok() : RegistryExecutionResult.failure(failure);
    }
}
