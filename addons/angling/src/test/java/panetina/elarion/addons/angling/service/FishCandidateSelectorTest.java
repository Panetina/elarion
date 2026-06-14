package panetina.elarion.addons.angling.service;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.condition.AnglingConditionContext;
import panetina.elarion.addons.angling.condition.AnglingConditionRegistry;
import panetina.elarion.addons.angling.model.AnglingConditionId;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinition;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;
import panetina.elarion.addons.angling.resource.FishDefinitionRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FishCandidateSelectorTest {
    private static final AnglingConditionId MATCH =
            AnglingConditionId.of("placeholder_match");
    private static final AnglingConditionId REJECT =
            AnglingConditionId.of("placeholder_reject");

    @Test
    void filtersFailClosedAndPreservesDefinitionOrder() {
        FishDefinition first = fish("placeholder_fish_001", 1, MATCH);
        FishDefinition rejected = fish("placeholder_fish_002", 1, REJECT);
        FishDefinition unknown = fish(
                "placeholder_fish_003",
                1,
                AnglingConditionId.of("placeholder_unknown"));
        FishDefinition unconditional = fish("placeholder_fish_004", 1);
        AnglingConditionRegistry conditions = new AnglingConditionRegistry();
        conditions.register(MATCH, (definition, context) -> true);
        conditions.register(REJECT, (definition, context) -> false);
        FishCandidateSelector selector = selector(
                List.of(first, rejected, unknown, unconditional),
                conditions);

        List<FishDefinition> eligible = selector.eligible(context());

        assertEquals(List.of(first, unconditional), eligible);
        assertThrows(UnsupportedOperationException.class, eligible::clear);
    }

    @Test
    void weightedSelectionUsesStableOrderedIntervalsAndNegativeRolls() {
        FishDefinition first = fish("placeholder_fish_001", 2);
        FishDefinition second = fish("placeholder_fish_002", 3);
        FishCandidateSelector selector = selector(
                List.of(first, second),
                new AnglingConditionRegistry());

        assertEquals(first, selector.select(context(), 0).orElseThrow());
        assertEquals(first, selector.select(context(), 1).orElseThrow());
        assertEquals(second, selector.select(context(), 2).orElseThrow());
        assertEquals(second, selector.select(context(), 4).orElseThrow());
        assertEquals(second, selector.select(context(), -1).orElseThrow());
        assertEquals(first, selector.select(context(), 5).orElseThrow());
    }

    @Test
    void noEligibleDefinitionsReturnsEmpty() {
        AnglingConditionRegistry conditions = new AnglingConditionRegistry();
        conditions.register(REJECT, (definition, context) -> false);
        FishCandidateSelector selector = selector(
                List.of(fish("placeholder_fish_001", 1, REJECT)),
                conditions);

        assertTrue(selector.select(context(), 0).isEmpty());
    }

    @Test
    void eachCallUsesTheCurrentAtomicDefinitionSnapshot() {
        FishDefinition first = fish("placeholder_fish_001", 1);
        FishDefinition second = fish("placeholder_fish_002", 1);
        FishDefinitionRepository definitions = new FishDefinitionRepository();
        definitions.publish(new FishDefinitionIndex(List.of(first)));
        FishCandidateSelector selector =
                new FishCandidateSelector(definitions, new AnglingConditionRegistry());

        assertEquals(first, selector.select(context(), 0).orElseThrow());
        definitions.publish(new FishDefinitionIndex(List.of(second)));
        assertEquals(second, selector.select(context(), 0).orElseThrow());
    }

    private static FishCandidateSelector selector(
            List<FishDefinition> definitions,
            AnglingConditionRegistry conditions
    ) {
        FishDefinitionRepository repository = new FishDefinitionRepository();
        repository.publish(new FishDefinitionIndex(definitions));
        return new FishCandidateSelector(repository, conditions);
    }

    private static FishDefinition fish(
            String path,
            int weight,
            AnglingConditionId... conditions
    ) {
        return new FishDefinition(
                Identifier.of("elarion_angling", path),
                "fish.elarion_angling." + path,
                AnglingRarity.PLACEHOLDER_COMMON,
                weight,
                List.of(conditions));
    }

    private static AnglingConditionContext context() {
        return new AnglingConditionContext(
                UUID.randomUUID(),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                Identifier.of("minecraft", "water"),
                null,
                64,
                6_000,
                false,
                false);
    }
}
