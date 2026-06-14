package panetina.elarion.addons.angling.loader;

import com.google.gson.JsonPrimitive;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.model.AnglingConditionId;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FishDefinitionLoaderTest {
    private final FishDefinitionLoader loader = new FishDefinitionLoader();

    @Test
    void validPlaceholderJsonProducesIndexWithOneDefinition() {
        FishDefinitionIndex index = loader.load(Map.of(
                "placeholder_fish_001", json("placeholder_fish_001", "PLACEHOLDER_COMMON", 10, "\"clear_weather\"")));

        assertEquals(1, index.all().size());
        assertEquals(Identifier.of("elarion_angling", "placeholder_fish_001"), index.all().getFirst().id());
        assertEquals(AnglingRarity.PLACEHOLDER_COMMON, index.all().getFirst().rarity());
        assertEquals(1, index.byCondition(AnglingConditionId.of("clear_weather")).size());
    }

    @Test
    void missingOptionalConditionsBecomesEmptyImmutableList() {
        FishDefinitionIndex index = loader.load(Map.of("placeholder_fish_001", """
                {
                  "id": "elarion_angling:placeholder_fish_001",
                  "translation_key": "fish.elarion_angling.placeholder_fish_001",
                  "rarity": "PLACEHOLDER_COMMON",
                  "weight": 10
                }
                """));

        assertTrue(index.all().getFirst().conditions().isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> index.all().getFirst().conditions().add(AnglingConditionId.of("rain")));
    }

    @Test
    void invalidJsonRootFailsWithDocumentId() {
        FishDefinitionParseException exception = assertThrows(
                FishDefinitionParseException.class,
                () -> loader.loadElements(Map.of("bad_root", new JsonPrimitive("not-object"))));

        assertEquals("bad_root", exception.documentId());
        assertEquals("$", exception.fieldPath());
        assertTrue(exception.getMessage().contains("root must be an object"));
    }

    @Test
    void missingRequiredFieldFailsWithFieldPath() {
        FishDefinitionParseException exception = assertThrows(
                FishDefinitionParseException.class,
                () -> loader.load(Map.of("missing_id", """
                        {
                          "translation_key": "fish.elarion_angling.placeholder_fish_001",
                          "rarity": "PLACEHOLDER_COMMON",
                          "weight": 10
                        }
                        """)));

        assertEquals("missing_id", exception.documentId());
        assertEquals("id", exception.fieldPath());
    }

    @Test
    void unknownRarityFailsWithFieldPath() {
        FishDefinitionParseException exception = assertThrows(
                FishDefinitionParseException.class,
                () -> loader.load(Map.of("bad_rarity", json("placeholder_fish_001", "COMMON", 10, "\"clear_weather\""))));

        assertEquals("bad_rarity", exception.documentId());
        assertEquals("rarity", exception.fieldPath());
    }

    @Test
    void invalidConditionEntryFailsWithFieldPath() {
        FishDefinitionParseException exception = assertThrows(
                FishDefinitionParseException.class,
                () -> loader.load(Map.of("bad_condition", json("placeholder_fish_001", "PLACEHOLDER_COMMON", 10, "15"))));

        assertEquals("bad_condition", exception.documentId());
        assertEquals("conditions[0]", exception.fieldPath());
    }

    @Test
    void duplicateFishIdsAcrossDocumentsFailIndexConstruction() {
        Map<String, String> documents = new LinkedHashMap<>();
        documents.put("first", json("placeholder_fish_001", "PLACEHOLDER_COMMON", 10, "\"clear_weather\""));
        documents.put("second", json("placeholder_fish_001", "PLACEHOLDER_RARE", 20, "\"rain\""));

        FishDefinitionParseException exception = assertThrows(
                FishDefinitionParseException.class,
                () -> loader.load(documents));

        assertEquals("<index>", exception.documentId());
        assertEquals("$", exception.fieldPath());
        assertTrue(exception.getMessage().contains("Duplicate fish ID"));
    }

    @Test
    void invalidFishIdNamespaceFailsThroughModelValidation() {
        FishDefinitionParseException exception = assertThrows(
                FishDefinitionParseException.class,
                () -> loader.load(Map.of("bad_namespace", """
                        {
                          "id": "elarion:placeholder_fish_001",
                          "translation_key": "fish.elarion_angling.placeholder_fish_001",
                          "rarity": "PLACEHOLDER_COMMON",
                          "weight": 10,
                          "conditions": []
                        }
                        """)));

        assertEquals("bad_namespace", exception.documentId());
        assertEquals("$", exception.fieldPath());
        assertTrue(exception.getMessage().contains("namespace"));
    }

    @Test
    void successfulMultiDocumentLoadPreservesDeterministicInputOrder() {
        Map<String, String> documents = new LinkedHashMap<>();
        documents.put("first", json("placeholder_fish_001", "PLACEHOLDER_COMMON", 10, "\"clear_weather\""));
        documents.put("second", json("placeholder_fish_002", "PLACEHOLDER_RARE", 20, "\"rain\""));

        FishDefinitionIndex index = loader.load(documents);

        assertEquals(Identifier.of("elarion_angling", "placeholder_fish_001"), index.all().get(0).id());
        assertEquals(Identifier.of("elarion_angling", "placeholder_fish_002"), index.all().get(1).id());
    }

    private static String json(String path, String rarity, int weight, String conditionEntry) {
        return """
                {
                  "id": "elarion_angling:%s",
                  "translation_key": "fish.elarion_angling.%s",
                  "rarity": "%s",
                  "weight": %d,
                  "conditions": [
                    %s
                  ]
                }
                """.formatted(path, path, rarity, weight, conditionEntry);
    }
}
