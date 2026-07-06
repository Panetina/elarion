package panetina.elarion.addons.underworld.config;

import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.util.List;
import java.util.function.Supplier;

public final class UnderworldConfigDescriptors {
    private UnderworldConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<UnderworldConfig> config
    ) {
        registry.registerDomain(domain(config));
    }

    public static ElarionConfigDomain domain(Supplier<UnderworldConfig> config) {
        UnderworldConfig defaults = UnderworldConfig.defaults();
        return new ElarionConfigDomain(
                "underworld",
                "addons:underworld",
                "Underworld",
                "Underworld sessions, corpses, PvP loot, combat tags, and Soul Fractures.",
                List.of("config/elarion/addons/underworld/underworld.yml"),
                "/e death reload",
                List.of(
                        new ElarionConfigCategory(
                                "underworld",
                                "Underworld",
                                "World routing, spawn, timer, restriction, and world-filter settings.",
                                List.of(
                                        boolEntry("enabled", "Enabled", "Enables the Underworld death loop.",
                                                "underworld.enabled", defaults.enabled(),
                                                () -> current(config).enabled()),
                                        stringEntry("world-id", "World ID", "Managed world used for dead souls.",
                                                "underworld.world-id", defaults.worldId(),
                                                () -> current(config).worldId(), true),
                                        decimalEntry("spawn-x", "Spawn X", "Underworld soul spawn X coordinate.",
                                                "underworld.spawn-x", defaults.spawnX(),
                                                () -> current(config).spawnX(), null, null),
                                        decimalEntry("spawn-y", "Spawn Y", "Underworld soul spawn Y coordinate.",
                                                "underworld.spawn-y", defaults.spawnY(),
                                                () -> current(config).spawnY(), null, null),
                                        decimalEntry("spawn-z", "Spawn Z", "Underworld soul spawn Z coordinate.",
                                                "underworld.spawn-z", defaults.spawnZ(),
                                                () -> current(config).spawnZ(), null, null),
                                        intEntry("pve-timer-minutes", "PvE Timer", "Minutes served after a PvE death.",
                                                "underworld.pve-timer-minutes", defaults.pveTimerMinutes(),
                                                () -> current(config).pveTimerMinutes(), 0),
                                        intEntry("pvp-timer-minutes", "PvP Timer", "Minutes served after a PvP death.",
                                                "underworld.pvp-timer-minutes", defaults.pvpTimerMinutes(),
                                                () -> current(config).pvpTimerMinutes(), 0),
                                        intEntry("authority-timer-minutes", "Authority Timer",
                                                "Minutes served when authority-specific death timing applies.",
                                                "underworld.authority-timer-minutes", defaults.authorityTimerMinutes(),
                                                () -> current(config).authorityTimerMinutes(), 0),
                                        intEntry("extra-minutes-per-underworld-death", "Repeat Death Penalty",
                                                "Additional minutes added for each death while already in the Underworld.",
                                                "underworld.extra-minutes-per-underworld-death",
                                                defaults.extraMinutesPerUnderworldDeath(),
                                                () -> current(config).extraMinutesPerUnderworldDeath(), 0),
                                        boolEntry("pause-timer-on-logout", "Pause Timer On Logout",
                                                "Pauses an active return timer while its player is offline.",
                                                "underworld.pause-timer-on-logout", defaults.pauseTimerOnLogout(),
                                                () -> current(config).pauseTimerOnLogout()),
                                        boolEntry("disable-chat", "Disable Chat",
                                                "Blocks chat while the soul is in the Underworld.",
                                                "underworld.disable-chat", defaults.disableChat(),
                                                () -> current(config).disableChat()),
                                        boolEntry("disable-portals", "Disable Portals",
                                                "Blocks portal travel while the soul is in the Underworld.",
                                                "underworld.disable-portals", defaults.disablePortals(),
                                                () -> current(config).disablePortals()),
                                        boolEntry("hide-nameplates", "Hide Nameplates",
                                                "Hides applicable player identity presentation while dead.",
                                                "underworld.hide-nameplates", defaults.hideNameplates(),
                                                () -> current(config).hideNameplates()),
                                        listEntry("enabled-worlds", "Enabled Worlds",
                                                "World IDs eligible for death capture; * enables all worlds.",
                                                "underworld.enabled-worlds", defaults.enabledWorlds(),
                                                () -> current(config).enabledWorlds()),
                                        listEntry("excluded-worlds", "Excluded Worlds",
                                                "World IDs excluded from normal death capture.",
                                                "underworld.excluded-worlds", defaults.excludedWorlds(),
                                                () -> current(config).excludedWorlds()))),
                        new ElarionConfigCategory(
                                "corpse",
                                "Corpse",
                                "Corpse lifecycle settings.",
                                List.of(intEntry("expires-minutes", "Expiry Minutes",
                                        "Minutes a public corpse remains before vault transfer and tomb removal.",
                                        "corpse.expires-minutes", defaults.corpseExpiresMinutes(),
                                        () -> current(config).corpseExpiresMinutes(), 0))),
                        new ElarionConfigCategory(
                                "pvp-loot",
                                "PvP Loot",
                                "PvP corpse loot selection, exclusivity, and exclusions.",
                                List.of(
                                        boolEntry("enabled", "Enabled", "Enables configured PvP corpse loot.",
                                                "pvp-loot.enabled", defaults.pvpLootEnabled(),
                                                () -> current(config).pvpLootEnabled()),
                                        boolEntry("armor-drops", "Armor Drops",
                                                "Allows equipped armor to participate in PvP loot.",
                                                "pvp-loot.armor-drops", defaults.armorDrops(),
                                                () -> current(config).armorDrops()),
                                        intEntry("random-item-min", "Random Item Minimum",
                                                "Minimum number of random eligible item stacks selected.",
                                                "pvp-loot.random-item-min", defaults.randomItemMin(),
                                                () -> current(config).randomItemMin(), 0),
                                        intEntry("random-item-max", "Random Item Maximum",
                                                "Maximum number of random eligible item stacks selected.",
                                                "pvp-loot.random-item-max", defaults.randomItemMax(),
                                                () -> current(config).randomItemMax(), 0),
                                        decimalEntry("physical-currency-percent", "Physical Currency Percent",
                                                "Fraction of eligible physical currency selected for PvP loot.",
                                                "pvp-loot.physical-currency-percent",
                                                defaults.physicalCurrencyPercent(),
                                                () -> current(config).physicalCurrencyPercent(), 0.0D, 1.0D),
                                        intEntry("killer-exclusive-seconds", "Killer Exclusive Seconds",
                                                "Seconds only the killer may access PvP loot.",
                                                "pvp-loot.killer-exclusive-seconds",
                                                defaults.killerExclusiveSeconds(),
                                                () -> current(config).killerExclusiveSeconds(), 0),
                                        boolEntry("allow-other-players-after-killer-window", "Allow Other Players",
                                                "Allows non-killers to loot after the killer-exclusive window.",
                                                "pvp-loot.allow-other-players-after-killer-window",
                                                defaults.allowOtherPlayersAfterKillerWindow(),
                                                () -> current(config).allowOtherPlayersAfterKillerWindow()),
                                        boolEntry("include-offhand-random-loot", "Include Offhand",
                                                "Allows the offhand stack to participate in random PvP loot.",
                                                "pvp-loot.include-offhand-random-loot",
                                                defaults.includeOffhandRandomLoot(),
                                                () -> current(config).includeOffhandRandomLoot()),
                                        listEntry("excluded-item-ids", "Excluded Item IDs",
                                                "Exact item IDs excluded from PvP loot.",
                                                "pvp-loot.excluded-item-ids", defaults.excludedItemIds(),
                                                () -> current(config).excludedItemIds()),
                                        listEntry("excluded-item-tags", "Excluded Item Tags",
                                                "Item tags excluded from PvP loot.",
                                                "pvp-loot.excluded-item-tags", defaults.excludedItemTags(),
                                                () -> current(config).excludedItemTags()),
                                        listEntry("physical-currency-item-ids", "Currency Item IDs",
                                                "Exact item IDs treated as physical currency.",
                                                "pvp-loot.physical-currency-item-ids",
                                                defaults.physicalCurrencyItemIds(),
                                                () -> current(config).physicalCurrencyItemIds()),
                                        listEntry("physical-currency-tags", "Currency Item Tags",
                                                "Item tags treated as physical currency.",
                                                "pvp-loot.physical-currency-tags",
                                                defaults.physicalCurrencyTags(),
                                                () -> current(config).physicalCurrencyTags()))),
                        new ElarionConfigCategory(
                                "combat-tag",
                                "Combat Tag",
                                "Combat-tag enablement and duration.",
                                List.of(
                                        boolEntry("enabled", "Enabled", "Enables combat tagging.",
                                                "combat-tag.enabled", defaults.combatTagEnabled(),
                                                () -> current(config).combatTagEnabled()),
                                        intEntry("duration-seconds", "Duration Seconds",
                                                "Seconds a combat tag remains active.",
                                                "combat-tag.duration-seconds",
                                                defaults.combatTagDurationSeconds(),
                                                () -> current(config).combatTagDurationSeconds(), 0))),
                        new ElarionConfigCategory(
                                "soul",
                                "Soul Fractures",
                                "Soul Fracture and True Death thresholds.",
                                List.of(
                                        boolEntry("enabled", "Enabled", "Enables Soul Fracture tracking.",
                                                "soul.enabled", defaults.soulEnabled(),
                                                () -> current(config).soulEnabled()),
                                        intEntry("max-fractures", "Maximum Fractures",
                                                "Fracture threshold used by the True Death policy.",
                                                "soul.max-fractures", defaults.maxFractures(),
                                                () -> current(config).maxFractures(), 0),
                                        boolEntry("true-death-at-max-fractures", "True Death At Maximum",
                                                "Triggers Core Character Lifecycle at the fracture threshold.",
                                                "soul.true-death-at-max-fractures",
                                                defaults.trueDeathAtMaxFractures(),
                                                () -> current(config).trueDeathAtMaxFractures())))));
    }

    private static ElarionConfigEntry<Boolean> boolEntry(
            String id, String label, String description, String path,
            boolean defaultValue, Supplier<Boolean> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.BOOLEAN, defaultValue, currentValue,
                ElarionConfigValidator.pass(), List.of("true", "false"), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id, String label, String description, String path,
            int defaultValue, Supplier<Integer> currentValue, int minimum
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerMinimum(path, minimum), List.of(),
                Integer.toString(minimum), "", true, false,
                ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id, String label, String description, String path,
            String defaultValue, Supplier<String> currentValue, boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(),
                List.of(), "", "", true, false,
                ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> listEntry(
            String id, String label, String description, String path,
            List<String> defaultValue, Supplier<List<String>> currentValue
    ) {
        return stringEntry(id, label, description, path,
                joined(defaultValue), () -> joined(currentValue.get()), false);
    }

    private static ElarionConfigEntry<String> decimalEntry(
            String id, String label, String description, String path,
            double defaultValue, Supplier<Double> currentValue, Double minimum, Double maximum
    ) {
        Supplier<String> displayed = () -> Double.toString(currentValue.get());
        ElarionConfigValidator<String> validator = value -> {
            try {
                double parsed = Double.parseDouble(value);
                if (!Double.isFinite(parsed)) return List.of(path + ": must be finite");
                if (minimum != null && parsed < minimum) {
                    return List.of(path + ": must be at least " + minimum);
                }
                if (maximum != null && parsed > maximum) {
                    return List.of(path + ": must be at most " + maximum);
                }
                return List.of();
            } catch (NumberFormatException exception) {
                return List.of(path + ": must be a decimal number");
            }
        };
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING,
                Double.toString(defaultValue), displayed, validator, List.of(),
                minimum == null ? "" : minimum.toString(),
                maximum == null ? "" : maximum.toString(),
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static UnderworldConfig current(Supplier<UnderworldConfig> config) {
        UnderworldConfig value = config == null ? null : config.get();
        return value == null ? UnderworldConfig.defaults() : value;
    }

    private static String joined(List<String> values) {
        return values == null ? "" : String.join(", ", values);
    }
}
