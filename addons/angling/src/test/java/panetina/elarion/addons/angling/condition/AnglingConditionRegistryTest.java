package panetina.elarion.addons.angling.condition;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.model.AnglingConditionId;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinition;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingConditionRegistryTest {
    private static final AnglingConditionId CONDITION =
            AnglingConditionId.of("placeholder_condition_001");

    @Test
    void allConditionsMustBeKnownAndMatch() {
        AnglingConditionRegistry registry = new AnglingConditionRegistry();
        FishDefinition definition = fish(CONDITION);

        assertFalse(registry.matches(definition, context()));

        registry.register(CONDITION, (candidate, context) -> context.raining());
        assertTrue(registry.matches(definition, context()));
    }

    @Test
    void duplicateRegistrationFailsAndIdsAreImmutable() {
        AnglingConditionRegistry registry = new AnglingConditionRegistry();
        registry.register(CONDITION, (candidate, context) -> true);

        assertThrows(IllegalArgumentException.class, () ->
                registry.register(CONDITION, (candidate, context) -> false));
        assertThrows(UnsupportedOperationException.class, () ->
                registry.registeredIds().clear());
    }

    @Test
    void definitionsWithoutConditionsMatch() {
        assertTrue(new AnglingConditionRegistry().matches(fish(), context()));
    }

    @Test
    void evaluatorCountIsBounded() {
        AnglingConditionRegistry registry = new AnglingConditionRegistry();
        for (int index = 0; index < AnglingConditionRegistry.MAX_EVALUATORS; index++) {
            registry.register(
                    AnglingConditionId.of("placeholder_condition_" + index),
                    (candidate, context) -> true);
        }

        assertThrows(IllegalStateException.class, () -> registry.register(
                AnglingConditionId.of("placeholder_condition_overflow"),
                (candidate, context) -> true));
    }

    private static FishDefinition fish(AnglingConditionId... conditions) {
        return new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                1,
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
                true,
                false);
    }
}
