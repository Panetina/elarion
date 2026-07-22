package panetina.elarion.core.integration.minecraft;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.UUID;

/**
 * Issues a short-lived, server-signed bearer receipt for the launcher passage
 * read model. The receipt contains no game state and is verified by the
 * website with the bridge secret; Core remains the canonical state owner.
 */
public final class LauncherPassageTicketService {
    static final String AUDIENCE = "launcher-passage";
    static final long LIFETIME_SECONDS = 7L * 24L * 60L * 60L;

    private final MinecraftBridgeConfig bridge;
    private final Clock clock;

    public LauncherPassageTicketService(MinecraftBridgeConfig bridge) {
        this(bridge, Clock.systemUTC());
    }

    LauncherPassageTicketService(MinecraftBridgeConfig bridge, Clock clock) {
        this.bridge = bridge;
        this.clock = clock;
    }

    public String issue(UUID playerId) {
        if (!bridge.enabled()) return "";
        long expiresAt = clock.instant().getEpochSecond() + LIFETIME_SECONDS;
        String claims = String.join("\n", "1", AUDIENCE, bridge.serverId(), playerId.toString(), Long.toString(expiresAt));
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(claims.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + MinecraftBridgeSigning.sign("PASSAGE_RECEIPT\n" + encoded, bridge.secret());
    }
}
