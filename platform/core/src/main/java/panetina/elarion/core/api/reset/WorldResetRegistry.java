package panetina.elarion.core.api.reset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WorldResetRegistry {
    private final List<WorldResetHandler> handlers = new ArrayList<>();
    private WorldResetOperator operator;

    public synchronized void register(WorldResetHandler handler) {
        if (handler == null || handler.id() == null || handler.id().isBlank()) {
            throw new IllegalArgumentException("World reset handler ID is required.");
        }
        if (handlers.stream().anyMatch(existing -> existing.id().equals(handler.id()))) {
            throw new IllegalStateException("World reset handler already registered: " + handler.id());
        }
        handlers.add(handler);
        handlers.sort(Comparator.comparing(WorldResetHandler::id));
    }

    public synchronized List<WorldResetHandler> handlers() {
        return List.copyOf(handlers);
    }

    public synchronized void setOperator(WorldResetOperator operator) {
        this.operator = operator;
    }

    public synchronized WorldResetOperator operator() {
        return operator;
    }
}
