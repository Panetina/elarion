package panetina.elarion.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaBiomeTagResourceTest {
    @Test
    void suppliesWorldgenCompatibilityTags() throws IOException {
        assertTagContains("is_birch_forest.json", "minecraft:birch_forest", "minecraft:old_growth_birch_forest");
        assertTagContains("is_mangrove.json", "minecraft:mangrove_swamp");
    }

    private static void assertTagContains(String fileName, String... biomeIds) throws IOException {
        String path = "data/minecraft/tags/worldgen/biome/" + fileName;
        try (InputStream stream = VanillaBiomeTagResourceTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, "Missing biome tag resource " + path);
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            for (String biomeId : biomeIds) {
                assertTrue(json.contains('"' + biomeId + '"'), "Missing " + biomeId + " from " + path);
            }
        }
    }
}
