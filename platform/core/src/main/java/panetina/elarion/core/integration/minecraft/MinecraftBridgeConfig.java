package panetina.elarion.core.integration.minecraft;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public record MinecraftBridgeConfig(
        boolean enabled,
        URI baseUri,
        String serverId,
        String secret,
        int pollSeconds
) {
    private static final Pattern SERVER_ID = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private static final Set<String> ALLOWED_KEYS = Set.of(
            "version", "enabled", "base-url", "server-id", "secret", "poll-seconds");
    private static final String DEFAULT_FILE = """
            version: 1
            enabled: false
            base-url: "https://ashesofelarion.com"
            server-id: "production"
            secret: ""
            poll-seconds: 30
            """;

    public static MinecraftBridgeConfig load(Logger logger) {
        return load(FabricLoader.getInstance().getConfigDir()
                .resolve("elarion/core/minecraft-bridge.yml"), logger, System.getenv());
    }

    static MinecraftBridgeConfig load(Path file, Logger logger, Map<String, String> environment) {
        createDefault(file, logger);
        Map<String, Object> values = read(file);
        Set<String> unknownKeys = new java.util.LinkedHashSet<>(values.keySet());
        unknownKeys.removeAll(ALLOWED_KEYS);
        if (!unknownKeys.isEmpty()) {
            throw new IllegalStateException("Unknown Minecraft bridge configuration keys: " + unknownKeys);
        }
        if (configuredVersion(values.get("version")) != 1) {
            throw new IllegalStateException("Unsupported Minecraft bridge configuration version.");
        }
        boolean enabled = booleanValue(environment, "ELARION_MINECRAFT_BRIDGE_ENABLED",
                values.get("enabled"), false);
        String baseUrl = stringValue(environment, "ELARION_MINECRAFT_BRIDGE_URL",
                values.get("base-url"), "https://ashesofelarion.com");
        String serverId = stringValue(environment, "ELARION_MINECRAFT_SERVER_ID",
                values.get("server-id"), "production");
        String secret = stringValue(environment, "ELARION_MINECRAFT_BRIDGE_SECRET",
                values.get("secret"), "");
        int pollSeconds = integerValue(environment, "ELARION_MINECRAFT_BRIDGE_POLL_SECONDS",
                values.get("poll-seconds"), 30);

        URI baseUri = validateBaseUri(baseUrl);
        if (!SERVER_ID.matcher(serverId).matches()) {
            throw new IllegalStateException("Minecraft bridge server-id must contain only letters, digits, '_' or '-'.");
        }
        if (pollSeconds < 30 || pollSeconds > 3600) {
            throw new IllegalStateException("Minecraft bridge poll-seconds must be between 30 and 3600.");
        }
        if (enabled && secret.length() < 32) {
            throw new IllegalStateException("Minecraft bridge secret must contain at least 32 characters when enabled.");
        }
        return new MinecraftBridgeConfig(enabled, baseUri, serverId, secret, pollSeconds);
    }

    private static URI validateBaseUri(String value) {
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Minecraft bridge base-url is invalid.", exception);
        }
        boolean localHttp = "http".equalsIgnoreCase(uri.getScheme())
                && ("localhost".equalsIgnoreCase(uri.getHost()) || "127.0.0.1".equals(uri.getHost()));
        if (!("https".equalsIgnoreCase(uri.getScheme()) || localHttp)
                || uri.getHost() == null || uri.getUserInfo() != null || uri.getQuery() != null
                || uri.getFragment() != null) {
            throw new IllegalStateException("Minecraft bridge base-url must be HTTPS (or localhost HTTP) without credentials, query, or fragment.");
        }
        String path = uri.getPath();
        if (path != null && !path.isBlank() && !"/".equals(path)) {
            throw new IllegalStateException("Minecraft bridge base-url must not include a path.");
        }
        return URI.create(uri.getScheme() + "://" + uri.getAuthority());
    }

    private static Map<String, Object> read(Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object parsed = new Yaml().load(reader);
            if (!(parsed instanceof Map<?, ?> raw)) return Map.of();
            Map<String, Object> values = new LinkedHashMap<>();
            raw.forEach((key, value) -> values.put(String.valueOf(key), value));
            return values;
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Failed to read Minecraft bridge configuration.", exception);
        }
    }

    private static void createDefault(Path file, Logger logger) {
        if (Files.exists(file)) return;
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(temporary, DEFAULT_FILE, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, file);
            }
            logger.info("Created disabled Minecraft bridge configuration at {}", file);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create Minecraft bridge configuration.", exception);
        }
    }

    private static String stringValue(Map<String, String> environment, String name, Object configured, String fallback) {
        String override = environment.get(name);
        if (override != null && !override.isBlank()) return override.trim();
        return configured == null ? fallback : String.valueOf(configured).trim();
    }

    private static boolean booleanValue(
            Map<String, String> environment, String name, Object configured, boolean fallback
    ) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) value = configured == null ? String.valueOf(fallback) : String.valueOf(configured);
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new IllegalStateException(name + " must be true or false.");
    }

    private static int integerValue(Map<String, String> environment, String name, Object configured, int fallback) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) value = configured == null ? String.valueOf(fallback) : String.valueOf(configured);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(name + " must be an integer.", exception);
        }
    }

    private static int configuredVersion(Object configured) {
        if (configured == null) return 1;
        try {
            return Integer.parseInt(String.valueOf(configured));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Minecraft bridge configuration version must be an integer.", exception);
        }
    }
}
