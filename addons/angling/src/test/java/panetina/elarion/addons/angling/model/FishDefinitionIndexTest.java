package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FishDefinitionIndexTest {
    @Test
    void duplicateIdsFailIndexConstruction() {
        FishDefinition first = fish("placeholder_fish_001", AnglingRarity.PLACEHOLDER_COMMON, "clear_weather");
        FishDefinition second = fish("placeholder_fish_001", AnglingRarity.PLACEHOLDER_RARE, "rain");

        assertThrows(FishDefinitionValidationException.class, () -> new FishDefinitionIndex(List.of(first, second)));
    }

    @Test
    void lookupsPreserveInputOrderAndUsePrecomputedGroups() {
        FishDefinition first = fish("placeholder_fish_001", AnglingRarity.PLACEHOLDER_COMMON, "clear_weather");
        FishDefinition second = fish("placeholder_fish_002", AnglingRarity.PLACEHOLDER_COMMON, "rain");
        FishDefinition third = fish("placeholder_fish_003", AnglingRarity.PLACEHOLDER_RARE, "rain");

        FishDefinitionIndex index = new FishDefinitionIndex(List.of(first, second, third));

        assertEquals(List.of(first, second, third), index.all());
        assertEquals(first, index.get(first.id()).orElseThrow());
        assertEquals(List.of(first, second), index.byRarity(AnglingRarity.PLACEHOLDER_COMMON));
        assertEquals(List.of(second, third), index.byCondition(AnglingConditionId.of("rain")));
    }

    @Test
    void collectionResultsAreImmutable() {
        FishDefinition first = fish("placeholder_fish_001", AnglingRarity.PLACEHOLDER_COMMON, "clear_weather");
        FishDefinitionIndex index = new FishDefinitionIndex(List.of(first));

        assertThrows(UnsupportedOperationException.class, () -> index.all().add(first));
        assertThrows(UnsupportedOperationException.class, () -> index.byRarity(AnglingRarity.PLACEHOLDER_COMMON).add(first));
        assertThrows(UnsupportedOperationException.class, () -> index.byCondition(AnglingConditionId.of("clear_weather")).add(first));
    }

    @Test
    void missingLookupReturnsEmpty() {
        FishDefinitionIndex index = new FishDefinitionIndex(List.of(
                fish("placeholder_fish_001", AnglingRarity.PLACEHOLDER_COMMON, "clear_weather")));

        assertTrue(index.get(Identifier.of("elarion_angling", "placeholder_missing")).isEmpty());
        assertEquals(List.of(), index.byRarity(AnglingRarity.PLACEHOLDER_EPIC));
        assertEquals(List.of(), index.byCondition(AnglingConditionId.of("night")));
    }

    @Test
    void definitionCollectionAboveSelectionBoundIsRejected() {
        FishDefinition definition =
                fish("placeholder_fish_001", AnglingRarity.PLACEHOLDER_COMMON, "clear_weather");

        assertThrows(FishDefinitionValidationException.class, () ->
                new FishDefinitionIndex(Collections.nCopies(
                        FishDefinitionIndex.MAX_DEFINITIONS + 1,
                        definition)));
    }

    private static FishDefinition fish(String path, AnglingRarity rarity, String condition) {
        return new FishDefinition(
                Identifier.of("elarion_angling", path),
                "fish.elarion_angling." + path,
                rarity,
                10,
                List.of(AnglingConditionId.of(condition)));
    }
}
