package panetina.elarion.addons.guilds.config;

import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.guilds.model.GuildConfig;
import panetina.elarion.core.api.AddonConfigFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class GuildConfigLoader {
    private GuildConfigLoader() {
    }

    public static GuildConfig load() {
        Path file = AddonConfigFiles.writeDefault("guilds", "guilds.yml", GuildConfigDefaults.GUILDS);
        try (Reader reader = java.nio.file.Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            if (!(loaded instanceof Map<?, ?> raw)) {
                throw new GuildConfigException("guilds.yml must contain a YAML mapping.");
            }
            GuildConfig config = parse(raw);
            validate(config);
            return config;
        } catch (IOException exception) {
            throw new GuildConfigException("Failed to load guilds.yml", exception);
        } catch (RuntimeException exception) {
            if (exception instanceof GuildConfigException config) throw config;
            throw new GuildConfigException("Invalid guilds.yml: " + exception.getMessage(), exception);
        }
    }

    private static GuildConfig parse(Map<?, ?> root) {
        Map<?, ?> creation = map(root.get("creation"));
        Map<?, ?> tags = map(root.get("tags"));
        Map<?, ?> identity = map(root.get("identity"));
        Map<?, ?> invitations = map(root.get("invitations"));
        return new GuildConfig(
                bool(root, "enabled", true),
                number(creation, "fee", 25L),
                (int) number(tags, "min-length", 2L),
                (int) number(tags, "max-length", 6L),
                (int) number(identity, "max-name-length", 48L),
                string(identity, "id-pattern", "[a-z0-9_-]{3,32}"),
                string(tags, "pattern", "[A-Z0-9]{2,6}"),
                stringSet(tags.get("blocked")),
                java.time.Duration.ofDays(number(invitations, "lifetime-days", 7L)).toMillis()
        );
    }

    private static void validate(GuildConfig config) {
        if (config.creationFee() < 0L) throw new GuildConfigException("creation.fee must not be negative.");
        if (config.minTagLength() > config.maxTagLength()) {
            throw new GuildConfigException("tags.min-length must not exceed tags.max-length.");
        }
        compile("identity.id-pattern", config.idPattern());
        compile("tags.pattern", config.tagPattern());
    }

    private static void compile(String path, String pattern) {
        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException exception) {
            throw new GuildConfigException(path + " is not a valid regex: " + exception.getMessage());
        }
    }

    private static Map<?, ?> map(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }

    private static boolean bool(Map<?, ?> map, String key, boolean fallback) {
        Object value = map.get(key);
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }

    private static long number(Map<?, ?> map, String key, long fallback) {
        Object value = map.get(key);
        if (value instanceof Number number) return number.longValue();
        if (value == null) return fallback;
        return Long.parseLong(String.valueOf(value));
    }

    private static String string(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value).trim();
    }

    private static Set<String> stringSet(Object value) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (value instanceof Iterable<?> list) {
            for (Object item : list) {
                if (item != null && !String.valueOf(item).isBlank()) {
                    result.add(String.valueOf(item).trim().toUpperCase(java.util.Locale.ROOT));
                }
            }
        }
        return Set.copyOf(result);
    }
}
