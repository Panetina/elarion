package panetina.elarion.core.integration.minecraft;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBridgeSigningTest {
    private static final String BODY = "{\"ok\":true}";
    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final String TIMESTAMP = "1784289600";
    private static final String NONCE = "abcdefghijklmnop";
    private static final String SIGNATURE = "7ad2f256d6362cb6f9bffd07407034626516e056e42f21a91438e73f40db877e";

    @Test
    void matchesWebsiteCanonicalSignature() {
        String message = MinecraftBridgeSigning.canonicalMessage(
                "RESPONSE", "production", BODY, TIMESTAMP, NONCE);
        assertTrue(MinecraftBridgeSigning.constantTimeEquals(
                SIGNATURE, MinecraftBridgeSigning.sign(message, SECRET)));
    }

    @Test
    void rejectsTamperingStaleResponsesAndReplay() {
        Clock clock = Clock.fixed(Instant.ofEpochSecond(Long.parseLong(TIMESTAMP)), ZoneOffset.UTC);
        MinecraftBridgeSigning.ResponseVerifier verifier = new MinecraftBridgeSigning.ResponseVerifier(clock);

        assertFalse(verifier.verify(BODY + " ", "production", SECRET,
                "production", TIMESTAMP, NONCE, SIGNATURE));
        assertFalse(verifier.verify(BODY, "production", SECRET,
                "production", "1784288999", "another_nonce_value", SIGNATURE));
        assertTrue(verifier.verify(BODY, "production", SECRET,
                "production", TIMESTAMP, NONCE, SIGNATURE));
        assertFalse(verifier.verify(BODY, "production", SECRET,
                "production", TIMESTAMP, NONCE, SIGNATURE));
    }
}
