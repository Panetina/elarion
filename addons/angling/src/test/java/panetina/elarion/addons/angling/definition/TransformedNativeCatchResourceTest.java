package panetina.elarion.addons.angling.definition;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TransformedNativeCatchResourceTest {
    @Test
    void allTransformedNativeCatchesAreFabricNamespacedAndDecodable() throws Exception {
        Path root = resourcePath("data/elarion_angling/elarion_angling/fish");
        List<Path> definitions;
        try (var paths = Files.list(root)) {
            definitions = paths.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }

        assertEquals(148, definitions.size());
        List<String> failures = new ArrayList<>();
        for (Path definition : definitions) {
            String json = Files.readString(definition);
            assertFalse(json.contains("starcatcher"), definition + " retains the source namespace");
            assertFalse(json.contains("neoforge"), definition + " retains a NeoForge condition");
            var result = AnglingCatchDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
            result.error().ifPresent(error -> failures.add(definition.getFileName() + ": " + error.message()));
        }
        assertTrue(failures.isEmpty(), () -> "Transformed catch failures:\n" + String.join("\n", failures));
    }

    private static Path resourcePath(String name) throws URISyntaxException {
        URL resource = Objects.requireNonNull(
                TransformedNativeCatchResourceTest.class.getClassLoader().getResource(name),
                "Missing transformed Angling resource directory " + name);
        return Path.of(resource.toURI());
    }
}
