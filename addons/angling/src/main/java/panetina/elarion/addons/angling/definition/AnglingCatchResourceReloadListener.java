package panetina.elarion.addons.angling.definition;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Parses all definition files before publishing one complete compiled snapshot. */
final class AnglingCatchResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final Identifier LISTENER_ID = Identifier.of(ElarionAnglingAddon.MOD_ID, "catch_definitions");
    private static final String DIRECTORY = "elarion_angling/fish";

    private final AnglingCatchSnapshotRepository repository;

    AnglingCatchResourceReloadListener(AnglingCatchSnapshotRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.findResources(DIRECTORY,
                id -> id.getPath().endsWith(".json"));
        List<Map.Entry<Identifier, Resource>> sorted = resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .toList();
        Map<Identifier, AnglingCatchDefinition> parsed = new LinkedHashMap<>();
        List<String> failures = new ArrayList<>();
        for (var entry : sorted) {
            Identifier resourceId = entry.getKey();
            Identifier definitionId;
            try {
                definitionId = definitionId(resourceId);
            } catch (IllegalArgumentException exception) {
                failures.add(resourceId + ": " + exception.getMessage());
                continue;
            }
            try (Reader reader = entry.getValue().getReader()) {
                var result = AnglingCatchDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
                result.result().ifPresentOrElse(
                        definition -> parsed.put(definitionId, definition),
                        () -> failures.add(resourceId + ": "
                                + result.error().map(com.mojang.serialization.DataResult.Error::message)
                                .orElse("definition codec rejected resource"))
                );
            } catch (IOException | RuntimeException exception) {
                failures.add(resourceId + ": " + exception.getMessage());
            }
        }
        if (!failures.isEmpty()) {
            throw new IllegalStateException("Elarion Angling catch reload rejected " + failures.size()
                    + " resource(s):\n" + String.join("\n", failures));
        }
        repository.compileAndPublish(parsed);
    }

    private static Identifier definitionId(Identifier resourceId) {
        String path = resourceId.getPath();
        String prefix = DIRECTORY + "/";
        if (!path.startsWith(prefix) || !path.endsWith(".json")) {
            throw new IllegalArgumentException("resource is outside the catch-definition directory");
        }
        String definitionPath = path.substring(prefix.length(), path.length() - ".json".length());
        if (definitionPath.isBlank() || definitionPath.contains("..")) {
            throw new IllegalArgumentException("invalid catch-definition path");
        }
        return Identifier.of(resourceId.getNamespace(), definitionPath);
    }
}
