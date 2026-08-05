package panetina.elarion.addons.quests.service;

import panetina.elarion.addons.quests.config.QuestConfigLoader;
import panetina.elarion.addons.quests.model.QuestDefinition;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class QuestDefinitionService {
    private final QuestConfigLoader loader;
    private final AtomicReference<Map<String, QuestDefinition>> definitions = new AtomicReference<>(Map.of());
    private final List<Runnable> reloadListeners = new CopyOnWriteArrayList<>();

    public QuestDefinitionService(QuestConfigLoader loader) {
        this.loader = loader;
    }

    public void load() {
        definitions.set(loader.load());
        reloadListeners.forEach(Runnable::run);
    }

    /** Registers a lightweight projection invalidation hook; definitions remain owned here. */
    public void onReload(Runnable listener) {
        if (listener != null) reloadListeners.add(listener);
    }

    public Collection<QuestDefinition> all() {
        return definitions.get().values().stream()
                .sorted(Comparator.comparing(QuestDefinition::id))
                .toList();
    }

    public Optional<QuestDefinition> find(String id) {
        return Optional.ofNullable(definitions.get().get(id == null ? "" : id));
    }

    public QuestDefinition require(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown questline " + id));
    }
}
