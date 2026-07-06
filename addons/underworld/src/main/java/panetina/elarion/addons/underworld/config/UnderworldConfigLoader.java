package panetina.elarion.addons.underworld.config;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.core.api.AddonConfigFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class UnderworldConfigLoader {
    private static final String DEFAULT_CONFIG = """
            underworld:
              enabled: true
              world-id: "elarion:underworld"
              spawn-x: 0.5
              spawn-y: 80.0
              spawn-z: 0.5
              pve-timer-minutes: 5
              pvp-timer-minutes: 10
              authority-timer-minutes: 15
              extra-minutes-per-underworld-death: 10
              pause-timer-on-logout: true
              disable-chat: true
              disable-portals: true
              hide-nameplates: true
              enabled-worlds: ["*"]
              excluded-worlds: ["elarion:underworld"]
            corpse:
              expires-minutes: 120
            pvp-loot:
              enabled: true
              armor-drops: true
              random-item-min: 3
              random-item-max: 5
              physical-currency-percent: 0.25
              killer-exclusive-seconds: 300
              allow-other-players-after-killer-window: false
              include-offhand-random-loot: true
              excluded-item-ids: []
              excluded-item-tags:
                - "elarion:soulbound"
                - "elarion:quest_items"
                - "elarion:authority_items"
                - "elarion:no_pvp_loot"
              physical-currency-item-ids:
                - "elarion:currency"
              physical-currency-tags:
                - "elarion:physical_currency"
            combat-tag:
              enabled: true
              duration-seconds: 30
            soul:
              enabled: true
              max-fractures: 3
              true-death-at-max-fractures: true
            """;

    private UnderworldConfigLoader() {
    }

    public static UnderworldConfig load(Logger logger) {
        Path file = AddonConfigFiles.writeDefault("underworld", "underworld.yml", DEFAULT_CONFIG);
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            if (!(loaded instanceof Map<?, ?> root)) return UnderworldConfig.defaults();
            UnderworldConfig defaults = UnderworldConfig.defaults();
            Map<?, ?> underworld = map(root.get("underworld"));
            Map<?, ?> corpse = map(root.get("corpse"));
            Map<?, ?> pvp = map(root.get("pvp-loot"));
            Map<?, ?> combat = map(root.get("combat-tag"));
            Map<?, ?> soul = map(root.get("soul"));
            return new UnderworldConfig(
                    bool(underworld, "enabled", defaults.enabled()),
                    string(underworld, "world-id", defaults.worldId()),
                    dbl(underworld, "spawn-x", defaults.spawnX()),
                    dbl(underworld, "spawn-y", defaults.spawnY()),
                    dbl(underworld, "spawn-z", defaults.spawnZ()),
                    integer(underworld, "pve-timer-minutes", defaults.pveTimerMinutes()),
                    integer(underworld, "pvp-timer-minutes", defaults.pvpTimerMinutes()),
                    integer(underworld, "authority-timer-minutes", defaults.authorityTimerMinutes()),
                    integer(underworld, "extra-minutes-per-underworld-death", defaults.extraMinutesPerUnderworldDeath()),
                    bool(underworld, "pause-timer-on-logout", defaults.pauseTimerOnLogout()),
                    bool(underworld, "disable-chat", defaults.disableChat()),
                    bool(underworld, "disable-portals", defaults.disablePortals()),
                    bool(underworld, "hide-nameplates", defaults.hideNameplates()),
                    integer(corpse, "expires-minutes", defaults.corpseExpiresMinutes()),
                    bool(pvp, "enabled", defaults.pvpLootEnabled()),
                    bool(pvp, "armor-drops", defaults.armorDrops()),
                    integer(pvp, "random-item-min", defaults.randomItemMin()),
                    integer(pvp, "random-item-max", defaults.randomItemMax()),
                    dbl(pvp, "physical-currency-percent", defaults.physicalCurrencyPercent()),
                    integer(pvp, "killer-exclusive-seconds", defaults.killerExclusiveSeconds()),
                    bool(pvp, "allow-other-players-after-killer-window", defaults.allowOtherPlayersAfterKillerWindow()),
                    bool(pvp, "include-offhand-random-loot", defaults.includeOffhandRandomLoot()),
                    strings(pvp, "excluded-item-ids", defaults.excludedItemIds()),
                    strings(pvp, "excluded-item-tags", defaults.excludedItemTags()),
                    strings(pvp, "physical-currency-item-ids", defaults.physicalCurrencyItemIds()),
                    strings(pvp, "physical-currency-tags", defaults.physicalCurrencyTags()),
                    bool(combat, "enabled", defaults.combatTagEnabled()),
                    integer(combat, "duration-seconds", defaults.combatTagDurationSeconds()),
                    bool(soul, "enabled", defaults.soulEnabled()),
                    integer(soul, "max-fractures", defaults.maxFractures()),
                    bool(soul, "true-death-at-max-fractures", defaults.trueDeathAtMaxFractures()),
                    strings(underworld, "enabled-worlds", defaults.enabledWorlds()),
                    strings(underworld, "excluded-worlds", defaults.excludedWorlds())
            );
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load underworld.yml; using defaults", exception);
            return UnderworldConfig.defaults();
        }
    }

    private static Map<?, ?> map(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }

    private static boolean bool(Map<?, ?> map, String key, boolean fallback) {
        Object value = map.get(key);
        return value instanceof Boolean bool ? bool : fallback;
    }

    private static int integer(Map<?, ?> map, String key, int fallback) {
        Object value = map.get(key);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static double dbl(Map<?, ?> map, String key, double fallback) {
        Object value = map.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    private static String string(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        return value instanceof String string && !string.isBlank() ? string.trim() : fallback;
    }

    private static List<String> strings(Map<?, ?> map, String key, List<String> fallback) {
        Object value = map.get(key);
        if (!(value instanceof List<?> list)) return fallback;
        return list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
    }
}
