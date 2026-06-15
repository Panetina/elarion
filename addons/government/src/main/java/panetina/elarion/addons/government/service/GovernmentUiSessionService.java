package panetina.elarion.addons.government.service;

import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GovernmentUiSessionService {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private final double rangeSquared;

    public GovernmentUiSessionService(long ttlMillis, double rangeSquared) {
        this.ttlMillis = ttlMillis;
        this.rangeSquared = rangeSquared;
    }

    public Session create(
            UUID playerId,
            String blockType,
            String realmId,
            String worldId,
            BlockPos pos,
            long now
    ) {
        cleanup(now);
        String id = UUID.randomUUID().toString();
        Session session = new Session(
                id,
                playerId,
                safe(blockType),
                safe(realmId),
                safe(worldId),
                pos.toImmutable(),
                now + ttlMillis);
        sessions.put(id, session);
        return session;
    }

    public Optional<Session> validate(
            UUID playerId,
            String sessionId,
            String realmId,
            String blockType,
            String worldId,
            double playerX,
            double playerY,
            double playerZ,
            long now
    ) {
        cleanup(now);
        if (playerId == null || sessionId == null || sessionId.isBlank()) return Optional.empty();
        Session session = sessions.get(sessionId);
        if (session == null || session.expiresAt() < now) return Optional.empty();
        if (!session.playerId().equals(playerId)) return Optional.empty();
        if (!session.realmId().equals(safe(realmId))) return Optional.empty();
        if (!session.blockType().equals(safe(blockType))) return Optional.empty();
        if (!session.worldId().equals(safe(worldId))) return Optional.empty();
        if (distanceSquared(session.pos(), playerX, playerY, playerZ) > rangeSquared) return Optional.empty();
        return Optional.of(session);
    }

    public void cleanup(long now) {
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt() < now);
    }

    int size() {
        return sessions.size();
    }

    private static double distanceSquared(BlockPos pos, double x, double y, double z) {
        double dx = pos.getX() + 0.5D - x;
        double dy = pos.getY() + 0.5D - y;
        double dz = pos.getZ() + 0.5D - z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public record Session(
            String id,
            UUID playerId,
            String blockType,
            String realmId,
            String worldId,
            BlockPos pos,
            long expiresAt
    ) {
    }
}
