package panetina.elarion.core.integration.minecraft;

import org.slf4j.Logger;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Mode;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Projection;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Visibility;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class MinecraftProjectionPublisher {
    private final Logger logger;
    private final boolean enabled;
    private final MinecraftProjectionOutboxStorage storage;
    private Path root;
    private MinecraftProjectionOutboxStorage.State state = MinecraftProjectionOutboxStorage.State.empty();
    /**
     * Publication can run from gameplay callbacks.  Persistence belongs to the
     * bridge worker, never the caller's server tick.
     */
    private boolean dirty;

    public MinecraftProjectionPublisher(Logger logger, boolean enabled) {
        this.logger = logger;
        this.enabled = enabled;
        this.storage = new MinecraftProjectionOutboxStorage(logger);
    }

    public synchronized void bind(Path elarionRoot) {
        root = elarionRoot;
        state = storage.load(elarionRoot);
        dirty = false;
    }

    public synchronized void unbind() {
        root = null;
        dirty = false;
    }

    /** Controlled shutdown is the only synchronous persistence fallback. */
    public synchronized void persistForShutdown() {
        persistDirty();
    }

    public boolean publishState(String kind, String entityId, String realmId, Visibility visibility,
                                Map<String, String> payload) {
        return publish(Mode.STATE, kind, entityId, realmId, visibility, payload);
    }

    public boolean publishEvent(String kind, String entityId, String realmId, Visibility visibility,
                                Map<String, String> payload) {
        return publish(Mode.EVENT, kind, entityId, realmId, visibility, payload);
    }

    public boolean publishMapMarker(WebsiteMapMarker marker) {
        if (marker == null) return false;
        return publishState(marker.projectionKind(), marker.id(), marker.realmId(),
                marker.visibility(), marker.payload());
    }

    private synchronized boolean publish(Mode mode, String kind, String entityId, String realmId,
                                         Visibility visibility, Map<String, String> payload) {
        if (!enabled || root == null) return false;
        try {
            Projection value = new Projection(1, mode, kind, entityId, realmId, visibility,
                    1, System.currentTimeMillis(), payload);
            state = state.enqueue(value);
            dirty = true;
            return true;
        } catch (RuntimeException exception) {
            logger.warn("Minecraft projection was not queued: {}", safeMessage(exception));
            return false;
        }
    }

    synchronized void flush(MinecraftBridgeClient client) throws IOException, InterruptedException {
        persistDirty();
        List<Projection> batch = state.batch(100);
        if (batch.isEmpty()) return;
        long acceptedThrough = client.publishProjections(batch);
        if (acceptedThrough < batch.getFirst().sequence()) return;
        state = state.acknowledged(acceptedThrough);
        storage.save(root, state);
        dirty = false;
    }

    public synchronized int pendingCount() {
        return state.pending().size();
    }

    /** Called by the dedicated bridge worker before network delivery. */
    private void persistDirty() {
        if (!dirty || root == null) return;
        storage.save(root, state);
        dirty = false;
    }

    private static String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) return throwable.getClass().getSimpleName();
        return message.substring(0, Math.min(160, message.length()));
    }
}
