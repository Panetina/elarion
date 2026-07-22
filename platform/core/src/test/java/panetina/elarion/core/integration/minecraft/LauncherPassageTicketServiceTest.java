package panetina.elarion.core.integration.minecraft;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LauncherPassageTicketServiceTest {
    private static final String SECRET = "this-is-a-bridge-secret-with-at-least-thirty-two-characters";
    private static final UUID PLAYER = UUID.fromString("5eda14b6-711c-4729-ab92-0e9b3ffcb7c1");

    @Test
    void issuesBoundedServerSignedReceiptForTheJoiningPlayer() {
        Clock clock = Clock.fixed(Instant.ofEpochSecond(1_900_000_000L), ZoneOffset.UTC);
        String ticket = new LauncherPassageTicketService(config(true), clock).issue(PLAYER);

        String[] parts = ticket.split("\\.", -1);
        assertEquals(2, parts.length);
        assertEquals(MinecraftBridgeSigning.sign("PASSAGE_RECEIPT\n" + parts[0], SECRET), parts[1]);
        assertEquals("1\nlauncher-passage\nelarion-primary\n" + PLAYER + "\n1900604800",
                new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8));
    }

    @Test
    void disabledBridgeCannotIssueAReceipt() {
        assertTrue(new LauncherPassageTicketService(config(false)).issue(PLAYER).isEmpty());
    }

    @Test
    void signatureDoesNotValidateIfReceiptClaimsChange() {
        String ticket = new LauncherPassageTicketService(config(true)).issue(PLAYER);
        String[] parts = ticket.split("\\.", -1);
        String altered = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "1\nlauncher-passage\nelarion-primary\n00000000-0000-0000-0000-000000000000\n1900604800"
                        .getBytes(StandardCharsets.UTF_8));
        assertFalse(MinecraftBridgeSigning.constantTimeEquals(
                parts[1], MinecraftBridgeSigning.sign("PASSAGE_RECEIPT\n" + altered, SECRET)));
    }

    private static MinecraftBridgeConfig config(boolean enabled) {
        return new MinecraftBridgeConfig(enabled, URI.create("https://staging.ashesofelarion.com"),
                "elarion-primary", SECRET, 30);
    }
}
