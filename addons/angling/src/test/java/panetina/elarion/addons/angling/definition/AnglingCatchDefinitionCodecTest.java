package panetina.elarion.addons.angling.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingCatchDefinitionCodecTest {
    private static final String FROZEN_SHAPE = """
            {
              "base_chance": 8,
              "catch_info": {
                "fish_bucket": {"count": 1, "id": "elarion_angling:starcaught_bucket"},
                "item": {"count": 1, "id": "elarion_angling:aloe_bream"}
              },
              "difficulty": {
                "decay": 1.0,
                "hp": 100,
                "missPenalty": 10,
                "modifiers": [{"type": "elarion_angling:teleport", "translation_override": ""}],
                "speed": 11,
                "sweetspots": [{
                  "color_as_int": -16711936,
                  "hitbox_size_in_pixels": 15,
                  "is_flip": false,
                  "moving_rate": 0.0,
                  "reward": 20,
                  "sweetspot_type": "elarion_angling:normal",
                  "texture_path": "elarion_angling:textures/gui/minigame/spots/thin.png",
                  "vanishing_rate": 0.0
                }]
              },
              "has_guide_entry": true,
              "rarity": "rare",
              "restrictions": [{
                "type": "elarion_angling:fluid",
                "fluids": ["minecraft:water"]
              }],
              "size_and_weight": {
                "average_size_cm": 36.0,
                "average_weight_grams": 2000.0,
                "deviation_size_cm": 12.0,
                "deviation_weight_grams": 1000.0,
                "golden_chance": 0.02
              },
              "skips_minigame": false,
              "textures": "elarion_angling:textures/gui/minigame/surface.png"
            }
            """;

    @Test
    void frozenCatchShapeDecodesAndRoundTrips() {
        JsonElement source = JsonParser.parseString(FROZEN_SHAPE);
        AnglingCatchDefinition definition = AnglingCatchDefinition.CODEC.parse(JsonOps.INSTANCE, source).getOrThrow();

        assertEquals(AnglingCatchDefinition.CURRENT_SCHEMA_VERSION, definition.schemaVersion());
        assertEquals("elarion_angling:aloe_bream", definition.catchInfo().item().id().toString());
        assertEquals(AnglingRarity.RARE, definition.rarity());
        assertEquals("elarion_angling:fluid", definition.restrictions().getFirst().type().toString());
        assertEquals("elarion_angling:teleport", definition.difficulty().modifiers().getFirst().type().toString());
        assertEquals(1, definition.difficulty().sweetspots().size());

        JsonElement encoded = AnglingCatchDefinition.CODEC.encodeStart(JsonOps.INSTANCE, definition).getOrThrow();
        AnglingCatchDefinition decoded = AnglingCatchDefinition.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(definition, decoded);
    }

    @Test
    void definitionCollectionsAreImmutable() {
        AnglingCatchDefinition definition = AnglingCatchDefinition.CODEC.parse(
                JsonOps.INSTANCE, JsonParser.parseString(FROZEN_SHAPE)).getOrThrow();
        assertThrows(UnsupportedOperationException.class, () -> definition.restrictions().clear());
        assertThrows(UnsupportedOperationException.class, () -> definition.difficulty().modifiers().clear());
    }

    @Test
    void typedNodesAreBoundedAndMustDeclareTheirType() {
        assertFalse(AnglingTypedNode.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"fluids\":[\"minecraft:water\"]}")).result().isPresent());

        String oversized = "{\"type\":\"elarion_angling:test\",\"value\":\""
                + "x".repeat(AnglingTypedNode.MAX_JSON_CHARACTERS) + "\"}";
        assertFalse(AnglingTypedNode.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString(oversized)).result().isPresent());
    }

    @Test
    void invalidDistributionIsRejectedByCodec() {
        JsonElement invalid = JsonParser.parseString(FROZEN_SHAPE);
        invalid.getAsJsonObject().getAsJsonObject("size_and_weight").addProperty("golden_chance", 2.0F);
        assertTrue(AnglingCatchDefinition.CODEC.parse(JsonOps.INSTANCE, invalid).error().isPresent());
    }
}
