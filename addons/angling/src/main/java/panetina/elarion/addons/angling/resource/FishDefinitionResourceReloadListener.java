package panetina.elarion.addons.angling.resource;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class FishDefinitionResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = Identifier.of("elarion_angling", "fish_definitions");
    private static final String RESOURCE_PREFIX = "angling/fish/";
    private static final String RESOURCE_ROOT = "angling/fish";
    private static final String JSON_SUFFIX = ".json";

    private final FishDefinitionRepository repository;

    public FishDefinitionResourceReloadListener(FishDefinitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        repository.reload(readDocuments(manager));
    }

    private Map<String, String> readDocuments(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.findResources(RESOURCE_ROOT, FishDefinitionResourceReloadListener::isDefinitionResource);
        Map<Identifier, String> rawDocuments = new LinkedHashMap<>();
        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .forEach(entry -> rawDocuments.put(entry.getKey(), readResource(entry.getKey(), entry.getValue())));
        return toLoaderDocuments(rawDocuments);
    }

    static boolean isDefinitionResource(Identifier id) {
        return "elarion_angling".equals(id.getNamespace())
                && id.getPath().startsWith(RESOURCE_PREFIX)
                && id.getPath().endsWith(JSON_SUFFIX);
    }

    static String documentId(Identifier id) {
        String path = id.getPath();
        return path.substring(0, path.length() - JSON_SUFFIX.length());
    }

    static Map<String, String> toLoaderDocuments(Map<Identifier, String> rawDocuments) {
        Map<String, String> documents = new LinkedHashMap<>();
        rawDocuments.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .forEach(entry -> documents.put(documentId(entry.getKey()), entry.getValue()));
        return Collections.unmodifiableMap(documents);
    }

    private String readResource(Identifier id, Resource resource) {
        try (BufferedReader reader = resource.getReader()) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to read fish definition resource " + id, exception);
        }
    }
}
