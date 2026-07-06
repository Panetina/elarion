package panetina.elarion.addons.mounts.config;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.core.api.AddonConfigFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class MountCollectionTextConfig {
    private static final String DEFAULT_CONFIG = """
            mounts:
              airship:
                locked-row: "Realm vendor: {realm}"
                unlocked-row: "Ready to summon with R."
                locked-detail: "Buy from the future {realm} Realm vendor. Only {realm} members can buy it for now."
                unlocked-detail: "Unlocked from the {realm} Realm vendor. Set it active here, then press R to summon it."
              bee:
                locked-row: "Future collection reward."
                unlocked-row: "Ready to summon with R."
                locked-detail: "Future collection reward. This mount will unlock through progression, rewards, NPCs, or admin grants."
                unlocked-detail: "Unlocked as a collection reward. Set it active here, then press R to summon it."
              chinese_dragon:
                locked-row: "Future collection reward."
                unlocked-row: "Ready to summon with R."
                locked-detail: "Future collection reward. This mount will unlock through progression, rewards, NPCs, or admin grants."
                unlocked-detail: "Unlocked as a collection reward. Set it active here, then press R to summon it."
              ghast:
                locked-row: "Realm vendor: {realm}"
                unlocked-row: "Ready to summon with R."
                locked-detail: "Buy from the future {realm} Realm vendor. Only {realm} members can buy it for now."
                unlocked-detail: "Unlocked from the {realm} Realm vendor. Set it active here, then press R to summon it."
              hot_air_balloon:
                locked-row: "Realm vendor: {realm}"
                unlocked-row: "Ready to summon with R."
                locked-detail: "Buy from the future {realm} Realm vendor. Only {realm} members can buy it for now."
                unlocked-detail: "Unlocked from the {realm} Realm vendor. Set it active here, then press R to summon it."
              scifi_bike:
                locked-row: "Future collection reward."
                unlocked-row: "Ready to summon with R."
                locked-detail: "Future collection reward. This mount will unlock through progression, rewards, NPCs, or admin grants."
                unlocked-detail: "Unlocked as a collection reward. Set it active here, then press R to summon it."
              wyvern:
                locked-row: "Future collection reward."
                unlocked-row: "Ready to summon with R."
                locked-detail: "Future collection reward. This mount will unlock through progression, rewards, NPCs, or admin grants."
                unlocked-detail: "Unlocked as a collection reward. Set it active here, then press R to summon it."
            """;

    private final Map<String, Entry> entries;

    private MountCollectionTextConfig(Map<String, Entry> entries) {
        this.entries = Map.copyOf(entries);
    }

    public static MountCollectionTextConfig defaults() {
        return loadEntries(Map.of());
    }

    public static MountCollectionTextConfig load(Logger logger) {
        Path file = AddonConfigFiles.writeDefault("mounts", "collection.yml", DEFAULT_CONFIG);
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            if (!(loaded instanceof Map<?, ?> root)) return defaults();
            return loadEntries(map(root.get("mounts")));
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load mounts collection.yml; using defaults", exception);
            return defaults();
        }
    }

    public Entry entry(ElarionMountType type) {
        return entries.getOrDefault(type.id(), defaultEntry(type));
    }

    private static MountCollectionTextConfig loadEntries(Map<?, ?> root) {
        Map<String, Entry> result = new LinkedHashMap<>();
        for (ElarionMountType type : ElarionMountType.values()) {
            Map<?, ?> map = map(root.get(type.id()));
            Entry fallback = defaultEntry(type);
            result.put(type.id(), new Entry(
                    string(map, "locked-row", fallback.lockedRow()),
                    string(map, "unlocked-row", fallback.unlockedRow()),
                    string(map, "locked-detail", fallback.lockedDetail()),
                    string(map, "unlocked-detail", fallback.unlockedDetail())
            ));
        }
        return new MountCollectionTextConfig(result);
    }

    private static Entry defaultEntry(ElarionMountType type) {
        boolean realm = type == ElarionMountType.AIRSHIP
                || type == ElarionMountType.GHAST
                || type == ElarionMountType.HOT_AIR_BALLOON;
        return realm
                ? new Entry(
                "Realm vendor: {realm}",
                "Ready to summon with R.",
                "Buy from the future {realm} Realm vendor. Only {realm} members can buy it for now.",
                "Unlocked from the {realm} Realm vendor. Set it active here, then press R to summon it.")
                : new Entry(
                "Future collection reward.",
                "Ready to summon with R.",
                "Future collection reward. This mount will unlock through progression, rewards, NPCs, or admin grants.",
                "Unlocked as a collection reward. Set it active here, then press R to summon it.");
    }

    private static Map<?, ?> map(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }

    private static String string(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        return value instanceof String string && !string.isBlank() ? string.trim() : fallback;
    }

    public record Entry(String lockedRow, String unlockedRow, String lockedDetail, String unlockedDetail) {
        public String row(boolean unlocked, String realmId) {
            return apply(unlocked ? unlockedRow : lockedRow, realmId);
        }

        public String detail(boolean unlocked, String realmId) {
            return apply(unlocked ? unlockedDetail : lockedDetail, realmId);
        }

        private static String apply(String text, String realmId) {
            String realm = realmId == null || realmId.isBlank()
                    ? "Realm"
                    : realmId.toUpperCase(Locale.ROOT);
            return (text == null ? "" : text).replace("{realm}", realm);
        }
    }
}
