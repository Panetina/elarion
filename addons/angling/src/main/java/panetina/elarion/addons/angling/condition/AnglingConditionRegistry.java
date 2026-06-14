package panetina.elarion.addons.angling.condition;

import panetina.elarion.addons.angling.model.AnglingConditionId;
import panetina.elarion.addons.angling.model.FishDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AnglingConditionRegistry {
    public static final int MAX_EVALUATORS = 256;

    private final Map<AnglingConditionId, AnglingConditionEvaluator> evaluators =
            new LinkedHashMap<>();

    public synchronized void register(
            AnglingConditionId id,
            AnglingConditionEvaluator evaluator
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(evaluator, "evaluator");
        if (evaluators.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate Angling condition evaluator: " + id.value());
        }
        if (evaluators.size() >= MAX_EVALUATORS) {
            throw new IllegalStateException(
                    "Angling condition registry is limited to " + MAX_EVALUATORS + " evaluators");
        }
        evaluators.put(id, evaluator);
    }

    public boolean matches(
            FishDefinition definition,
            AnglingConditionContext context
    ) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(context, "context");
        for (AnglingConditionId conditionId : definition.conditions()) {
            AnglingConditionEvaluator evaluator = evaluator(conditionId);
            if (evaluator == null || !evaluator.test(definition, context)) {
                return false;
            }
        }
        return true;
    }

    public synchronized Set<AnglingConditionId> registeredIds() {
        return Set.copyOf(evaluators.keySet());
    }

    private synchronized AnglingConditionEvaluator evaluator(AnglingConditionId id) {
        return evaluators.get(id);
    }
}
