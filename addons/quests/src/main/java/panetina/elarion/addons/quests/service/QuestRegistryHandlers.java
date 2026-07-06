package panetina.elarion.addons.quests.service;

import panetina.elarion.addons.quests.model.QuestDefinition;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.ConditionContext;
import panetina.elarion.core.registry.ConditionType;
import panetina.elarion.core.registry.RegistryExecutionResult;

import java.util.Locale;

public final class QuestRegistryHandlers {
    private static final String OWNER = "elarion_quests";

    private QuestRegistryHandlers() {
    }

    public static void register(ElarionApi api, QuestDefinitionService definitions, QuestStateService states) {
        registerAction(api, "elarion_quests:start", "Starts a questline scope.", states::start);
        registerAction(api, "elarion_quests:set_stage", "Sets the current shared quest stage.", states::setStage);
        registerAction(api, "elarion_quests:set_flag", "Sets or clears a shared/player quest flag.", states::setFlag);
        registerAction(api, "elarion_quests:set_variable", "Sets a shared/player quest variable.", states::setVariable);
        registerAction(api, "elarion_quests:add_variable", "Adds to an integer shared/player quest variable.", states::addVariable);
        registerAction(api, "elarion_quests:collect_evidence", "Records quest evidence.", states::collectEvidence);
        registerAction(api, "elarion_quests:lock_ending", "Locks a quest ending.", states::lockEnding);
        registerAction(api, "elarion_quests:schedule_consequence", "Queues a delayed quest action.", states::scheduleConsequence);
        registerAction(api, "elarion_quests:notify", "Publishes a quest notification.", states::notify);
        registerAction(api, "elarion_quests:set_shrine_display", "Overrides an Offering shrine instance display name.",
                states::setShrineDisplay);

        registerCondition(api, "elarion_quests:stage_is", "Checks the current shared quest stage.",
                context -> {
                    QuestDefinition quest = definition(definitions, context);
                    String scopeKey = states.resolveScopeKeyForRegistry(quest, context.execution(), context.parameters());
                    String stage = context.parameters().getOrDefault("stage", "");
                    return result(states.stageIs(quest.id(), scopeKey, stage), "quest stage did not match");
                });
        registerCondition(api, "elarion_quests:has_flag", "Checks a shared/player quest flag.",
                context -> {
                    QuestDefinition quest = definition(definitions, context);
                    String scopeKey = states.resolveScopeKeyForRegistry(quest, context.execution(), context.parameters());
                    String flag = context.parameters().getOrDefault("flag", context.parameters().getOrDefault("id", ""));
                    boolean playerTarget = isPlayerTarget(context);
                    return result(states.hasFlag(quest.id(), scopeKey, context.execution().actorId(), flag, playerTarget),
                            "quest flag missing");
                });
        registerCondition(api, "elarion_quests:has_evidence", "Checks collected shared quest evidence.",
                context -> {
                    QuestDefinition quest = definition(definitions, context);
                    String scopeKey = states.resolveScopeKeyForRegistry(quest, context.execution(), context.parameters());
                    String evidence = context.parameters().getOrDefault("evidence", context.parameters().getOrDefault("id", ""));
                    return result(states.hasEvidence(quest.id(), scopeKey, evidence), "quest evidence missing");
                });
        registerCondition(api, "elarion_quests:variable_equals", "Checks a quest variable value.",
                context -> {
                    QuestDefinition quest = definition(definitions, context);
                    String scopeKey = states.resolveScopeKeyForRegistry(quest, context.execution(), context.parameters());
                    String variable = context.parameters().getOrDefault("variable", context.parameters().getOrDefault("id", ""));
                    String expected = context.parameters().getOrDefault("value", "");
                    return result(states.variableEquals(quest, scopeKey, context.execution().actorId(), variable, expected),
                            "quest variable did not match");
                });
        registerCondition(api, "elarion_quests:variable_at_least", "Checks an integer quest variable minimum.",
                context -> {
                    QuestDefinition quest = definition(definitions, context);
                    String scopeKey = states.resolveScopeKeyForRegistry(quest, context.execution(), context.parameters());
                    String variable = context.parameters().getOrDefault("variable", context.parameters().getOrDefault("id", ""));
                    int minimum = integer(context.parameters().getOrDefault("minimum",
                            context.parameters().getOrDefault("value", "0")));
                    return result(states.variableAtLeast(quest, scopeKey, context.execution().actorId(), variable, minimum),
                            "quest variable below minimum");
                });
        registerCondition(api, "elarion_quests:ending_is", "Checks the locked quest ending.",
                context -> {
                    QuestDefinition quest = definition(definitions, context);
                    String scopeKey = states.resolveScopeKeyForRegistry(quest, context.execution(), context.parameters());
                    String ending = context.parameters().getOrDefault("ending", context.parameters().getOrDefault("id", ""));
                    return result(states.endingIs(quest.id(), scopeKey, ending), "quest ending did not match");
                });
    }

    private static QuestDefinition definition(QuestDefinitionService definitions, ConditionContext context) {
        return definitions.require(context.parameters().getOrDefault("quest", ""));
    }

    private static boolean isPlayerTarget(ConditionContext context) {
        String raw = context.parameters().getOrDefault("state-scope",
                context.parameters().getOrDefault("target", ""));
        return raw.toLowerCase(Locale.ROOT).equals("player")
                || raw.toLowerCase(Locale.ROOT).equals("personal");
    }

    private static int integer(String raw) {
        try {
            return Integer.parseInt(raw == null ? "0" : raw);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static RegistryExecutionResult result(boolean success, String failure) {
        return success ? RegistryExecutionResult.ok() : RegistryExecutionResult.failure(failure);
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
