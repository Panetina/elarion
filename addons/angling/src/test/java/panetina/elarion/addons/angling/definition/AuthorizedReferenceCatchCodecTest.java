package panetina.elarion.addons.angling.definition;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Local parity evidence; skipped only when the ignored USB/reference copy is absent. */
final class AuthorizedReferenceCatchCodecTest {
    private static final Path REFERENCE = locateReference();

    @Test
    void everyFrozenCatchDefinitionDecodesThroughFabricSchema() throws IOException {
        Assumptions.assumeTrue(Files.isDirectory(REFERENCE),
                "Authorized local reference is not present on this machine");

        List<Path> definitions;
        try (var paths = Files.walk(REFERENCE)) {
            definitions = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> path.toString().replace('\\', '/').contains("/starcatcher/fish/"))
                    .filter(AuthorizedReferenceCatchCodecTest::containsCatchInfo)
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }

        assertEquals(463, definitions.size(), "Frozen catch-definition count changed");
        List<String> failures = new ArrayList<>();
        for (Path definition : definitions) {
            var result = AnglingCatchDefinition.CODEC.parse(
                    JsonOps.INSTANCE, JsonParser.parseString(Files.readString(definition)));
            result.error().ifPresent(error -> failures.add(
                    REFERENCE.relativize(definition).toString() + ": " + error.message()));
        }
        assertTrue(failures.isEmpty(), () -> "Catch codec parity failures:\n" + String.join("\n", failures));
    }

    private static boolean containsCatchInfo(Path path) {
        try {
            return Files.readString(path).contains("\"catch_info\"");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read authorized catch definition " + path, exception);
        }
    }

    private static Path locateReference() {
        Path moduleRelative = Path.of(
                "reference", "upstream-starcatcher-neoforge-1.21.1", "src", "generated", "resources", "data");
        if (Files.isDirectory(moduleRelative)) {
            return moduleRelative;
        }
        return Path.of("addons", "angling").resolve(moduleRelative);
    }
}
