package panetina.elarion.core.client;

import net.minecraft.util.Formatting;
import panetina.elarion.core.network.IdentitySyncPayload;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientIdentityCache {
    private static final Map<UUID, ClientIdentity> IDENTITIES = new ConcurrentHashMap<>();

    private ClientIdentityCache() {}

    public static void update(IdentitySyncPayload payload) {
        Formatting color = Formatting.byName(payload.color());
        IDENTITIES.put(payload.uuid(), new ClientIdentity(
                payload.uuid(),
                payload.username(),
                payload.nickname(),
                payload.prefix(),
                payload.suffix(),
                payload.title(),
                payload.leaderLabel(),
                color == null ? Formatting.WHITE : color,
                payload.realmName(),
                payload.realmId(),
                payload.tabVisible(),
                payload.visible()));
    }

    public static Optional<ClientIdentity> find(UUID uuid) {
        return Optional.ofNullable(IDENTITIES.get(uuid));
    }

    public static Collection<ClientIdentity> all() {
        return IDENTITIES.values();
    }

    public static boolean isKnownHidden(UUID uuid) {
        ClientIdentity identity = IDENTITIES.get(uuid);
        return identity != null && !identity.visible();
    }

    public static boolean isKnownTabHidden(UUID uuid) {
        ClientIdentity identity = IDENTITIES.get(uuid);
        return identity != null && !identity.tabVisible();
    }

    public static boolean shouldHideTabEntry(UUID uuid) {
        ClientIdentity identity = IDENTITIES.get(uuid);
        if (identity != null) return !identity.tabVisible();
        return false;
    }

    public static void clear() {
        IDENTITIES.clear();
    }
}
