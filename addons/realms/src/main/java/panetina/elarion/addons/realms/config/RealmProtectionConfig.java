package panetina.elarion.addons.realms.config;

import org.yaml.snakeyaml.Yaml;
import panetina.elarion.core.api.AddonConfigFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record RealmProtectionConfig(
        Set<String> sharedWorldIds,
        boolean operatorBypass,
        boolean protectExplosionBlocks,
        long feedbackCooldownMillis,
        Set<String> extraAllyInteractableBlocks,
        Set<String> extraContainerBlocks
) {
    private static final String DEFAULT_CONFIG = """
            config-version: 1

            # PvP and explosion block damage are disabled in these shared worlds.
            shared-world-ids:
              - "elarion:lobby"
              - "elarion:worldheart"

            # Keep false by default so OP testing follows normal gameplay rules.
            # Enable only for temporary admin repair.
            operator-bypass: false
            protect-explosion-blocks: true
            feedback-cooldown-millis: 1000

            # Doors, trapdoors, fence gates, buttons, and levers are recognized automatically.
            extra-ally-interactable-blocks: []

            # Inventory block entities are recognized automatically.
            extra-container-blocks: []
            """;

    public static RealmProtectionConfig defaults() {
        return new RealmProtectionConfig(
                Set.of("elarion:lobby", "elarion:worldheart"),
                false,
                true,
                1000L,
                Set.of(),
                Set.of());
    }

    public static RealmProtectionConfig load() {
        Path path = AddonConfigFiles.writeDefault("realms", "protection.yml", DEFAULT_CONFIG);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            Map<?, ?> root = loaded instanceof Map<?, ?> map ? map : Map.of();
            RealmProtectionConfig defaults = defaults();
            return new RealmProtectionConfig(
                    strings(root.get("shared-world-ids"), defaults.sharedWorldIds()),
                    bool(root.get("operator-bypass"), defaults.operatorBypass()),
                    bool(root.get("protect-explosion-blocks"), defaults.protectExplosionBlocks()),
                    number(root.get("feedback-cooldown-millis"), defaults.feedbackCooldownMillis()),
                    strings(root.get("extra-ally-interactable-blocks"), defaults.extraAllyInteractableBlocks()),
                    strings(root.get("extra-container-blocks"), defaults.extraContainerBlocks())
            );
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to load Realm protection config " + path, exception);
        }
    }

    private static Set<String> strings(Object value, Set<String> fallback) {
        if (!(value instanceof List<?> list)) return fallback;
        Set<String> result = new LinkedHashSet<>();
        for (Object item : list) {
            if (item != null && !item.toString().isBlank()) result.add(item.toString().trim());
        }
        return Set.copyOf(result);
    }

    private static boolean bool(Object value, boolean fallback) {
        return value instanceof Boolean bool ? bool : fallback;
    }

    private static long number(Object value, long fallback) {
        return value instanceof Number number ? Math.max(0L, number.longValue()) : fallback;
    }
}
