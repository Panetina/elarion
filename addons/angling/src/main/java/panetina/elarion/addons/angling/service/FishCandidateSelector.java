package panetina.elarion.addons.angling.service;

import panetina.elarion.addons.angling.condition.AnglingConditionContext;
import panetina.elarion.addons.angling.condition.AnglingConditionRegistry;
import panetina.elarion.addons.angling.model.FishDefinition;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;
import panetina.elarion.addons.angling.resource.FishDefinitionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class FishCandidateSelector {
    private final FishDefinitionRepository definitions;
    private final AnglingConditionRegistry conditions;

    public FishCandidateSelector(
            FishDefinitionRepository definitions,
            AnglingConditionRegistry conditions
    ) {
        this.definitions = Objects.requireNonNull(definitions, "definitions");
        this.conditions = Objects.requireNonNull(conditions, "conditions");
    }

    public List<FishDefinition> eligible(AnglingConditionContext context) {
        Objects.requireNonNull(context, "context");
        FishDefinitionIndex snapshot = definitions.current();
        List<FishDefinition> eligible = new ArrayList<>();
        for (FishDefinition definition : snapshot.all()) {
            if (conditions.matches(definition, context)) {
                eligible.add(definition);
            }
        }
        return List.copyOf(eligible);
    }

    public Optional<FishDefinition> select(AnglingConditionContext context, long roll) {
        List<FishDefinition> eligible = eligible(context);
        if (eligible.isEmpty()) return Optional.empty();

        long totalWeight = 0;
        for (FishDefinition definition : eligible) {
            totalWeight = Math.addExact(totalWeight, definition.weight());
        }
        long target = Math.floorMod(roll, totalWeight);
        long cumulative = 0;
        for (FishDefinition definition : eligible) {
            cumulative = Math.addExact(cumulative, definition.weight());
            if (target < cumulative) return Optional.of(definition);
        }
        throw new IllegalStateException("Weighted fish selection did not resolve a candidate");
    }
}
