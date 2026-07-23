package panetina.elarion.addons.angling.restriction;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.definition.AnglingCatchDefinition;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AnglingRestrictionParityTest {
    @Test
    void everyNativeRestrictionCompilesIntoTypedRuntimeState() throws Exception {
        var compilers = AnglingRestrictionCompilers.create();
        assertEquals(16, compilers.registeredIds().size());

        Path root = resourcePath("data/elarion_angling/elarion_angling/fish");
        List<Path> definitions;
        try (var paths = Files.list(root)) {
            definitions = paths.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
        int compiledCount = 0;
        for (Path path : definitions) {
            AnglingCatchDefinition definition = AnglingCatchDefinition.CODEC.parse(
                    JsonOps.INSTANCE, JsonParser.parseString(Files.readString(path))).getOrThrow();
            Identifier definitionId = Identifier.of("elarion_angling",
                    path.getFileName().toString().replaceFirst("\\.json$", ""));
            compiledCount += compilers.compileAll(definitionId, definition.restrictions()).size();
        }
        assertEquals(148, definitions.size());
        assertEquals(640, compiledCount, "Frozen native restriction count changed");
    }

    private static Path resourcePath(String name) throws URISyntaxException {
        URL resource = Objects.requireNonNull(
                AnglingRestrictionParityTest.class.getClassLoader().getResource(name),
                "Missing transformed Angling resource directory " + name);
        return Path.of(resource.toURI());
    }
}
