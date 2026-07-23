package panetina.elarion.core.api.reset;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PlayerResetRegistry {
    private final List<PlayerResetHandler> handlers = new CopyOnWriteArrayList<>();

    public void register(PlayerResetHandler handler) {
        if (handler == null || normalize(handler.id()).isBlank()) {
            throw new IllegalArgumentException("Player reset handler ID is required.");
        }
        String id = normalize(handler.id());
        if (handlers.stream().anyMatch(existing -> normalize(existing.id()).equals(id))) {
            throw new IllegalStateException("Player reset handler already registered: " + id);
        }
        handlers.add(handler);
        handlers.sort(Comparator.comparing(value -> normalize(value.id())));
    }

    public List<PlayerResetHandler> handlers() {
        return List.copyOf(handlers);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
