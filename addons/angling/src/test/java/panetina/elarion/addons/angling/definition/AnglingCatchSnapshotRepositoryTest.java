package panetina.elarion.addons.angling.definition;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.compile.AnglingDefinitionCompileException;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AnglingCatchSnapshotRepositoryTest {
    @Test
    void completeNativeSnapshotPublishesWithBoundedIndexes() throws Exception {
        Map<Identifier, AnglingCatchDefinition> definitions = loadDefinitions();
        AnglingCatchSnapshotRepository repository = new AnglingCatchSnapshotRepository();
        AnglingCatchSnapshot snapshot = repository.compileAndPublish(definitions);

        assertEquals(1, snapshot.revision());
        assertEquals(148, snapshot.size());
        assertEquals(148, snapshot.byType(AnglingCatchType.FISH).size()
                + snapshot.byType(AnglingCatchType.EXTRA).size()
                + snapshot.byType(AnglingCatchType.SECRET).size()
                + snapshot.byType(AnglingCatchType.TROPHY).size());
        assertEquals(148, java.util.Arrays.stream(AnglingRarity.values())
                .mapToInt(rarity -> snapshot.byRarity(rarity).size()).sum());

        int restrictions = snapshot.all().values().stream()
                .mapToInt(value -> value.definition().restrictions().size()).sum();
        int modifiers = snapshot.all().values().stream()
                .mapToInt(value -> value.definition().minigameModifiers().size()
                        + value.definition().sweetspots().stream()
                        .mapToInt(spot -> spot.onHitModifiers().size()).sum()).sum();
        int sweetspots = snapshot.all().values().stream()
                .mapToInt(value -> value.definition().sweetspots().size()).sum();
        assertEquals(640, restrictions);
        assertEquals(68, modifiers);
        assertEquals(352, sweetspots);
    }

    @Test
    void failedCompilationLeavesLastValidSnapshotUntouched() throws Exception {
        AnglingCatchSnapshotRepository repository = new AnglingCatchSnapshotRepository();
        AnglingCatchSnapshot valid = repository.compileAndPublish(loadDefinitions());
        AnglingCatchSnapshot.NativeCatch first = valid.all().values().iterator().next();
        AnglingCatchDefinition source = first.definition().source();
        Identifier unknownType = Identifier.of("elarion_angling", "unknown_modifier");
        AnglingDifficultyDefinition invalidDifficulty = new AnglingDifficultyDefinition(
                source.difficulty().hp(), source.difficulty().speed(), source.difficulty().missPenalty(),
                source.difficulty().decay(),
                List.of(new AnglingTypedNode(unknownType,
                        "{\"type\":\"elarion_angling:unknown_modifier\"}")),
                source.difficulty().sweetspots());
        AnglingCatchDefinition invalid = new AnglingCatchDefinition(
                source.schemaVersion(), source.catchInfo(), source.baseChance(), source.sizeAndWeight(),
                source.rarity(), source.restrictions(), invalidDifficulty, source.skipsMinigame(),
                source.hasGuideEntry(), source.texture());

        assertThrows(AnglingDefinitionCompileException.class,
                () -> repository.compileAndPublish(Map.of(first.id(), invalid)));
        assertSame(valid, repository.current());
        assertEquals(1, repository.current().revision());
    }

    private static Map<Identifier, AnglingCatchDefinition> loadDefinitions() throws Exception {
        Path root = resourcePath("data/elarion_angling/elarion_angling/fish");
        List<Path> paths;
        try (var stream = Files.list(root)) {
            paths = stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
        Map<Identifier, AnglingCatchDefinition> definitions = new LinkedHashMap<>();
        for (Path path : paths) {
            Identifier id = Identifier.of("elarion_angling",
                    path.getFileName().toString().replaceFirst("\\.json$", ""));
            definitions.put(id, AnglingCatchDefinition.CODEC.parse(
                    JsonOps.INSTANCE, JsonParser.parseString(Files.readString(path))).getOrThrow());
        }
        return definitions;
    }

    private static Path resourcePath(String name) throws URISyntaxException {
        URL resource = Objects.requireNonNull(
                AnglingCatchSnapshotRepositoryTest.class.getClassLoader().getResource(name),
                "Missing transformed Angling resource directory " + name);
        return Path.of(resource.toURI());
    }
}
