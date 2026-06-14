package panetina.elarion.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AddonInitializationOrder {
    private AddonInitializationOrder() {
    }

    static List<String> sort(Map<String, Set<String>> dependencies) {
        List<String> result = new ArrayList<>(dependencies.size());
        Map<String, Visit> visits = new HashMap<>();
        dependencies.keySet().stream().sorted().forEach(id ->
                visit(id, dependencies, visits, result, new ArrayList<>()));
        return List.copyOf(result);
    }

    private static void visit(
            String id,
            Map<String, Set<String>> dependencies,
            Map<String, Visit> visits,
            List<String> result,
            List<String> path
    ) {
        Visit state = visits.get(id);
        if (state == Visit.DONE) return;
        if (state == Visit.ACTIVE) {
            path.add(id);
            throw new IllegalStateException("Circular Elarion addon dependency: " + String.join(" -> ", path));
        }
        visits.put(id, Visit.ACTIVE);
        path.add(id);
        dependencies.getOrDefault(id, Set.of()).stream()
                .filter(dependencies::containsKey)
                .sorted()
                .forEach(dependency -> visit(dependency, dependencies, visits, result, new ArrayList<>(path)));
        visits.put(id, Visit.DONE);
        result.add(id);
    }

    static Set<String> dependenciesOf(
            net.fabricmc.loader.api.entrypoint.EntrypointContainer<?> container,
            Set<String> addonProviders
    ) {
        Set<String> result = new HashSet<>();
        container.getProvider().getMetadata().getDependencies().forEach(dependency -> {
            if (dependency.getKind() == net.fabricmc.loader.api.metadata.ModDependency.Kind.DEPENDS
                    && addonProviders.contains(dependency.getModId())) {
                result.add(dependency.getModId());
            }
        });
        return Set.copyOf(result);
    }

    private enum Visit {
        ACTIVE,
        DONE
    }
}
