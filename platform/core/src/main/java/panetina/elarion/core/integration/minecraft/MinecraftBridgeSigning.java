package panetina.elarion.core.integration.minecraft;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MinecraftBridgeSigning {
    static final long MAX_CLOCK_SKEW_SECONDS = 300;

    private MinecraftBridgeSigning() {
    }

    public static String canonicalMessage(
            String method,
            String pathAndQuery,
            String body,
            String timestamp,
            String nonce
    ) {
        return String.join("\n", method.toUpperCase(), pathAndQuery, sha256(body), timestamp, nonce);
    }

    public static String sign(String message, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable.", exception);
        }
    }

    public static String sha256(String body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(body.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    public static boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.US_ASCII), right.getBytes(StandardCharsets.US_ASCII));
    }

    public static final class ResponseVerifier {
        private static final int MAX_REMEMBERED_NONCES = 256;
        private final Clock clock;
        private final Map<String, Long> nonces = new LinkedHashMap<>();

        public ResponseVerifier(Clock clock) {
            this.clock = clock;
        }

        public synchronized boolean verify(
                String body,
                String expectedServerId,
                String secret,
                String serverId,
                String timestamp,
                String nonce,
                String signature
        ) {
            if (!expectedServerId.equals(serverId)
                    || !timestamp.matches("\\d{10}")
                    || !nonce.matches("[A-Za-z0-9_-]{16,128}")
                    || !signature.matches("[a-f0-9]{64}")) return false;
            long now = clock.instant().getEpochSecond();
            long signedAt;
            try {
                signedAt = Long.parseLong(timestamp);
            } catch (NumberFormatException exception) {
                return false;
            }
            if (Math.abs(now - signedAt) > MAX_CLOCK_SKEW_SECONDS || nonces.containsKey(nonce)) return false;
            String message = canonicalMessage("RESPONSE", expectedServerId, body, timestamp, nonce);
            if (!constantTimeEquals(signature, sign(message, secret))) return false;
            nonces.entrySet().removeIf(entry -> Math.abs(now - entry.getValue()) > MAX_CLOCK_SKEW_SECONDS);
            nonces.put(nonce, signedAt);
            while (nonces.size() > MAX_REMEMBERED_NONCES) {
                String oldest = nonces.keySet().iterator().next();
                nonces.remove(oldest);
            }
            return true;
        }
    }
}
