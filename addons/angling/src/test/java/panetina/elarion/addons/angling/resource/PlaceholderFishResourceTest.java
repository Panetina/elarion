package panetina.elarion.addons.angling.resource;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.loader.FishDefinitionLoader;
import panetina.elarion.addons.angling.model.AnglingConditionId;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlaceholderFishResourceTest {
    @Test
    void placeholderFishResourceLoadsThroughPureLoader() throws IOException {
        String json = readResource("/data/elarion_angling/angling/fish/placeholder_fish_001.json");

        FishDefinitionIndex index = new FishDefinitionLoader().load(Map.of("angling/fish/placeholder_fish_001", json));

        assertEquals(1, index.all().size());
        var definition = index.all().getFirst();
        assertEquals(Identifier.of("elarion_angling", "placeholder_fish_001"), definition.id());
        assertEquals(AnglingRarity.PLACEHOLDER_COMMON, definition.rarity());
        assertEquals(10, definition.weight());
        assertEquals(definition, index.byCondition(AnglingConditionId.of("placeholder_condition_001")).getFirst());
    }

    @Test
    void playerFacingFeedbackRemainsExplicitPlaceholderCopy() throws IOException {
        String json = readResource("/assets/elarion_angling/lang/en_us.json");

        assertTrue(json.contains(
                "\"feedback.elarion_angling.catch_accepted\": \"[REPLACE: feedback.catch.accepted]\""));
        assertTrue(json.contains(
                "\"feedback.elarion_angling.catch_unavailable\": \"[REPLACE: feedback.catch.unavailable]\""));
    }

    private static String readResource(String path) throws IOException {
        try (var stream = PlaceholderFishResourceTest.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Missing test resource: " + path);
            }
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                var writer = new java.io.StringWriter();
                reader.transferTo(writer);
                return writer.toString();
            }
        }
    }
}
