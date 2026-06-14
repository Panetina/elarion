package panetina.elarion.addons.angling.condition;

import panetina.elarion.addons.angling.model.FishDefinition;

@FunctionalInterface
public interface AnglingConditionEvaluator {
    boolean test(FishDefinition definition, AnglingConditionContext context);
}
