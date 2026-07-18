package panetina.elarion.core.integration.minecraft;

import panetina.elarion.core.integration.minecraft.MinecraftBridgeProtocol.Acknowledgement;
import panetina.elarion.core.integration.minecraft.MinecraftBridgeProtocol.Command;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

public final class MinecraftBridgeClient {
    private static final String CHANGES_PATH = "/api/internal/minecraft/whitelist/changes";
    private static final String ACK_PATH = "/api/internal/minecraft/whitelist/ack";
    private static final String PROJECTIONS_PATH = "/api/internal/minecraft/projections";
    private static final int MAX_RESPONSE_BYTES = 512 * 1024;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MinecraftBridgeConfig config;
    private final HttpClient http;
    private final Clock clock;
    private final MinecraftBridgeSigning.ResponseVerifier responseVerifier;

    public MinecraftBridgeClient(MinecraftBridgeConfig config) {
        this(config, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build(), Clock.systemUTC());
    }

    MinecraftBridgeClient(MinecraftBridgeConfig config, HttpClient http, Clock clock) {
        this.config = config;
        this.http = http;
        this.clock = clock;
        this.responseVerifier = new MinecraftBridgeSigning.ResponseVerifier(clock);
    }

    public List<Command> fetchChanges(long after) throws IOException, InterruptedException {
        String pathAndQuery = CHANGES_PATH + "?after=" + after + "&limit=100";
        String response = send("GET", pathAndQuery, "");
        return MinecraftBridgeProtocol.parseChanges(response, after);
    }

    public void acknowledge(List<Acknowledgement> acknowledgements) throws IOException, InterruptedException {
        String body = MinecraftBridgeProtocol.acknowledgementBody(acknowledgements);
        String response = send("POST", ACK_PATH, body);
        MinecraftBridgeProtocol.requireSuccess(response);
    }

    public long publishProjections(List<MinecraftProjectionProtocol.Projection> projections)
            throws IOException, InterruptedException {
        String response = send("POST", PROJECTIONS_PATH, MinecraftProjectionProtocol.batchBody(projections));
        return MinecraftProjectionProtocol.parseAcceptedThrough(response);
    }

    private String send(String method, String pathAndQuery, String body)
            throws IOException, InterruptedException {
        String timestamp = Long.toString(clock.instant().getEpochSecond());
        String nonce = nonce();
        String canonical = MinecraftBridgeSigning.canonicalMessage(method, pathAndQuery, body, timestamp, nonce);
        HttpRequest.BodyPublisher publisher = body.isEmpty()
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder(resolve(pathAndQuery))
                .timeout(Duration.ofSeconds(20))
                .method(method, publisher)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Elarion-Server", config.serverId())
                .header("X-Elarion-Timestamp", timestamp)
                .header("X-Elarion-Nonce", nonce)
                .header("X-Elarion-Signature", MinecraftBridgeSigning.sign(canonical, config.secret()))
                .build();
        HttpResponse<java.io.InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            response.body().close();
            throw new IOException("Minecraft bridge returned HTTP " + response.statusCode() + '.');
        }
        long declaredLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
        if (declaredLength > MAX_RESPONSE_BYTES) {
            response.body().close();
            throw new IOException("Minecraft bridge response exceeded the size limit.");
        }
        byte[] responseBytes;
        try (var stream = response.body()) {
            responseBytes = stream.readNBytes(MAX_RESPONSE_BYTES + 1);
        }
        if (responseBytes.length > MAX_RESPONSE_BYTES) {
            throw new IOException("Minecraft bridge response exceeded the size limit.");
        }
        String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
        boolean verified = responseVerifier.verify(
                responseBody, config.serverId(), config.secret(),
                response.headers().firstValue("X-Elarion-Server").orElse(""),
                response.headers().firstValue("X-Elarion-Timestamp").orElse(""),
                response.headers().firstValue("X-Elarion-Nonce").orElse(""),
                response.headers().firstValue("X-Elarion-Signature").orElse(""));
        if (!verified) throw new IOException("Minecraft bridge response signature was invalid or replayed.");
        return responseBody;
    }

    private URI resolve(String pathAndQuery) {
        return config.baseUri().resolve(pathAndQuery);
    }

    private static String nonce() {
        byte[] value = new byte[24];
        RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
