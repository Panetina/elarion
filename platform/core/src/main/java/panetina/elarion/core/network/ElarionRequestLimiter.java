package panetina.elarion.core.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Bounded fixed-window request limiter for server-authoritative client requests.
 */
public final class ElarionRequestLimiter {
    private final Map<RequestKey, Window> windows = new HashMap<>();

    public synchronized boolean allow(
            UUID playerId,
            String channel,
            long nowMillis,
            int maximumRequests,
            long windowMillis
    ) {
        Objects.requireNonNull(playerId, "playerId");
        String normalizedChannel = normalizeChannel(channel);
        if (normalizedChannel.isBlank()) throw new IllegalArgumentException("Request channel cannot be blank");
        if (maximumRequests <= 0) throw new IllegalArgumentException("Maximum requests must be positive");
        if (windowMillis <= 0L) throw new IllegalArgumentException("Window duration must be positive");

        RequestKey key = new RequestKey(playerId, normalizedChannel);
        Window window = windows.get(key);
        if (window == null || nowMillis < window.startedAtMillis
                || nowMillis - window.startedAtMillis >= windowMillis) {
            windows.put(key, new Window(nowMillis, 1));
            return true;
        }
        if (window.requestCount >= maximumRequests) return false;
        window.requestCount++;
        return true;
    }

    public synchronized void clear(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        Iterator<RequestKey> iterator = windows.keySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().playerId.equals(playerId)) iterator.remove();
        }
    }

    synchronized int trackedWindowCount() {
        return windows.size();
    }

    private static String normalizeChannel(String channel) {
        return channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT);
    }

    private record RequestKey(UUID playerId, String channel) {
    }

    private static final class Window {
        private final long startedAtMillis;
        private int requestCount;

        private Window(long startedAtMillis, int requestCount) {
            this.startedAtMillis = startedAtMillis;
            this.requestCount = requestCount;
        }
    }
}
