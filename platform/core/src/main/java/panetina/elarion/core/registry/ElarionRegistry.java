package panetina.elarion.core.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ElarionRegistry<T extends ElarionRegistry.Entry> {
    private final String name;
    private final Map<String, T> entries = new ConcurrentHashMap<>();

    public ElarionRegistry(String name) {
        this.name = name;
    }

    public void register(T entry) {
        if (entry == null || entry.id() == null || entry.id().isBlank()) {
            throw new IllegalArgumentException(name + " registry entry id cannot be blank");
        }
        T previous = entries.putIfAbsent(entry.id(), entry);
        if (previous != null) {
            throw new IllegalArgumentException(name + " registry already has entry " + entry.id());
        }
    }

    public Optional<T> get(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    public boolean contains(String id) {
        return entries.containsKey(id);
    }

    public Collection<T> entries() {
        return List.copyOf(entries.values());
    }

    public void requireKnown(String id, String field) {
        if (!contains(id)) {
            throw new IllegalArgumentException(field + ": unknown " + name + " id " + id);
        }
    }

    public interface Entry {
        String id();
        String owner();
        String description();
    }
}
