package panetina.elarion.addons.underworld.client;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UnderworldBanishmentStatus {
    private static final Set<UUID> BANISHED_PLAYERS = ConcurrentHashMap.newKeySet();

    private UnderworldBanishmentStatus() {
    }

    public static void update(UUID playerId, boolean banished) {
        if (playerId == null) return;
        if (banished) BANISHED_PLAYERS.add(playerId);
        else BANISHED_PLAYERS.remove(playerId);
    }

    public static boolean isBanished(UUID playerId) {
        return playerId != null && BANISHED_PLAYERS.contains(playerId);
    }

    public static void clear() {
        BANISHED_PLAYERS.clear();
    }
}
