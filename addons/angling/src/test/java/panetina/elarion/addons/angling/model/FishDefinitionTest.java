package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FishDefinitionTest {
    @Test
    void validPlaceholderDefinitionConstructsSuccessfully() {
        AnglingConditionId condition = AnglingConditionId.of("clear_weather");
        FishDefinition definition = new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of(condition));

        assertEquals(Identifier.of("elarion_angling", "placeholder_fish_001"), definition.id());
        assertEquals(List.of(condition), definition.conditions());
    }

    @Test
    void invalidNamespaceIsRejected() {
        assertThrows(FishDefinitionValidationException.class, () -> new FishDefinition(
                Identifier.of("elarion", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of()));
    }

    @Test
    void nonPlaceholderPathIsRejected() {
        assertThrows(FishDefinitionValidationException.class, () -> new FishDefinition(
                Identifier.of("elarion_angling", "final_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of()));
    }

    @Test
    void blankTranslationKeyIsRejected() {
        assertThrows(FishDefinitionValidationException.class, () -> new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                " ",
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of()));
    }

    @Test
    void invalidTranslationKeyPrefixIsRejected() {
        assertThrows(FishDefinitionValidationException.class, () -> new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "item.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of()));
    }

    @Test
    void missingRarityIsRejected() {
        assertThrows(FishDefinitionValidationException.class, () -> new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                null,
                10,
                List.of()));
    }

    @Test
    void nonpositiveWeightIsRejected() {
        assertThrows(FishDefinitionValidationException.class, () -> new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                0,
                List.of()));
    }

    @Test
    void invalidConditionIdIsRejected() {
        assertThrows(FishDefinitionValidationException.class, () -> AnglingConditionId.of("Invalid ID"));
        assertThrows(NullPointerException.class, () -> new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of((AnglingConditionId) null)));
    }

    @Test
    void duplicateAndExcessiveConditionsAreRejected() {
        AnglingConditionId condition = AnglingConditionId.of("placeholder_condition_001");
        assertThrows(FishDefinitionValidationException.class, () -> new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                1,
                List.of(condition, condition)));
        assertThrows(FishDefinitionValidationException.class, () -> new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                1,
                Collections.nCopies(FishDefinition.MAX_CONDITIONS + 1, condition)));
    }

    @Test
    void localConditionIdsDefaultToAnglingNamespace() {
        AnglingConditionId condition = AnglingConditionId.of("clear_weather");

        assertEquals(Identifier.of("elarion_angling", "clear_weather"), condition.value());
    }

    @Test
    void namespacedConditionIdsAreAccepted() {
        AnglingConditionId condition = AnglingConditionId.of("minecraft:overworld");

        assertEquals(Identifier.of("minecraft", "overworld"), condition.value());
    }

    @Test
    void conditionsAreImmutable() {
        FishDefinition definition = new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of(AnglingConditionId.of("clear_weather")));

        assertThrows(UnsupportedOperationException.class,
                () -> definition.conditions().add(AnglingConditionId.of("rain")));
        assertTrue(definition.conditions().contains(AnglingConditionId.of("clear_weather")));
    }
}
