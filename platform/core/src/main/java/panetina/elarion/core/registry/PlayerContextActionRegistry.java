package panetina.elarion.core.registry;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core-owned extension point for small, target-player actions. Availability and
 * execution are both server-side; a client snapshot is presentation only.
 */
public final class PlayerContextActionRegistry {
    public record Action(String id, String label, Handler handler) {
        public Action {
            id = id == null ? "" : id.trim();
            label = label == null ? "" : label.trim();
            if (!id.matches("[a-z0-9_.:-]{3,96}") || label.isBlank() || label.length() > 64 || handler == null) {
                throw new IllegalArgumentException("Invalid player context action.");
            }
        }
    }

    public interface Handler {
        boolean available(ServerPlayerEntity actor, ServerPlayerEntity target);
        RegistryExecutionResult execute(ServerPlayerEntity actor, ServerPlayerEntity target);
    }

    private final Map<String, Action> actions = new LinkedHashMap<>();

    public synchronized void register(Action action) {
        if (actions.putIfAbsent(action.id(), action) != null) {
            throw new IllegalStateException("Player context action is already registered: " + action.id());
        }
    }

    public synchronized List<Action> available(ServerPlayerEntity actor, ServerPlayerEntity target) {
        if (!validTarget(actor, target)) return List.of();
        return actions.values().stream()
                .filter(action -> action.handler().available(actor, target))
                .sorted(Comparator.comparing(Action::id))
                .limit(8)
                .toList();
    }

    public synchronized RegistryExecutionResult execute(String id, ServerPlayerEntity actor, ServerPlayerEntity target) {
        Action action = actions.get(id == null ? "" : id.trim());
        if (action == null || !validTarget(actor, target) || !action.handler().available(actor, target)) {
            return RegistryExecutionResult.failure("That action is no longer available.");
        }
        return action.handler().execute(actor, target);
    }

    private static boolean validTarget(ServerPlayerEntity actor, ServerPlayerEntity target) {
        return actor != null && target != null && !actor.getUuid().equals(target.getUuid())
                && actor.squaredDistanceTo(target) <= 64.0D;
    }
}
