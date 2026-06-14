package panetina.elarion.core.storage;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class DirtyTracker {
    private final Set<UUID> dirty = ConcurrentHashMap.newKeySet();

    public void mark(UUID uuid) {
        if (uuid != null) dirty.add(uuid);
    }

    public boolean remove(UUID uuid) {
        return dirty.remove(uuid);
    }

    public void clear() {
        dirty.clear();
    }

    public boolean contains(UUID uuid) {
        return dirty.contains(uuid);
    }

    public Set<UUID> snapshot() {
        return Set.copyOf(dirty);
    }

    public void flush(Consumer<UUID> saver) {
        for (UUID uuid : snapshot()) {
            saver.accept(uuid);
        }
    }
}
