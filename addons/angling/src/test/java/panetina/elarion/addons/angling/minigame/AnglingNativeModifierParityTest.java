package panetina.elarion.addons.angling.minigame;

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

final class AnglingNativeModifierParityTest {
    @Test
    void everyNativeModifierCompilesIntoTypedConfiguration() throws Exception {
        var compilers = AnglingNativeModifierCompilers.create();
        assertEquals(9, compilers.registeredIds().size());

        Path root = resourcePath("data/elarion_angling/elarion_angling/fish");
        List<Path> definitions;
        try (var paths = Files.list(root)) {
            definitions = paths.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
        int compiled = 0;
        for (Path path : definitions) {
            AnglingCatchDefinition definition = AnglingCatchDefinition.CODEC.parse(
                    JsonOps.INSTANCE, JsonParser.parseString(Files.readString(path))).getOrThrow();
            Identifier definitionId = Identifier.of("elarion_angling",
                    path.getFileName().toString().replaceFirst("\\.json$", ""));
            compiled += compilers.compileAll(definitionId, definition.difficulty().modifiers()).size();
            for (var sweetspot : definition.difficulty().sweetspots()) {
                compiled += compilers.compileAll(definitionId, sweetspot.modifiers()).size();
            }
        }
        assertEquals(68, compiled, "Frozen native modifier-node count changed");
    }

    private static Path resourcePath(String name) throws URISyntaxException {
        URL resource = Objects.requireNonNull(
                AnglingNativeModifierParityTest.class.getClassLoader().getResource(name),
                "Missing transformed Angling resource directory " + name);
        return Path.of(resource.toURI());
    }
}
