package panetina.elarion.core.config;

import panetina.elarion.core.model.TitleAcquisitionMode;
import panetina.elarion.core.model.TitleOwnershipMode;
import panetina.elarion.core.model.VisibilityScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class CoreConfigValidator {
    private final Function<String, Map<String, Object>> loader;
    private final int CONFIG_VERSION;
    private final Set<String> FORMATTING_COLORS;

    CoreConfigValidator(
            Function<String, Map<String, Object>> loader,
            int configVersion,
            Set<String> formattingColors
    ) {
        this.loader = loader;
        this.CONFIG_VERSION = configVersion;
        this.FORMATTING_COLORS = formattingColors;
    }

    private Map<String, Object> loadMap(String fileName) {
        return loader.apply(fileName);
    }

    void validateConfigs() {
        List<String> errors = new ArrayList<>();
        validateRealms(loadMap("realms.yml"), errors);
        validateTitles(loadMap("titles.yml"), errors);
        validateTitleProgression(loadMap("title-progression.yml"), loadMap("titles.yml"), errors);
        validateRewards(loadMap("rewards.yml"), errors);
        validateIdentity(loadMap("identity.yml"), errors);
        validateSimpleFiles(errors);
        CoreConfigReferenceValidator.validate(this::loadMap, errors);
        if (!errors.isEmpty()) throw new ConfigValidationException(errors);
    }

    private void validateRealms(Map<String, Object> root, List<String> errors) {
        checkKeys("realms.yml", root, Set.of("config-version", "realms"), errors);
        checkVersion("realms.yml", root, errors);
        Map<String, Object> definitions = requiredMap("realms.yml.realms", root.get("realms"), errors);
        if (definitions.isEmpty()) errors.add("realms.yml.realms: at least one realm is required");
        definitions.forEach((id, raw) -> {
            String path = "realms.yml.realms." + id;
            Map<String, Object> data = requiredMap(path, raw, errors);
            checkKeys(path, data, Set.of("display-name", "short-name", "prefix", "color",
                    "visibility-scope", "spawn", "flags"), errors);
            requireString(path + ".display-name", data.get("display-name"), false, errors);
            requireString(path + ".short-name", data.get("short-name"), false, errors);
            requireString(path + ".prefix", data.get("prefix"), true, errors);
            String color = requireString(path + ".color", data.get("color"), false, errors);
            if (color != null && !FORMATTING_COLORS.contains(color.toLowerCase(Locale.ROOT))) {
                errors.add(path + ".color: unsupported color '" + color + "'");
            }
            requireEnum(path + ".visibility-scope", data.get("visibility-scope"), VisibilityScope.class, errors);
            requireStringCollection(path + ".flags", data.get("flags"), errors);
            Map<String, Object> spawn = requiredMap(path + ".spawn", data.get("spawn"), errors);
            checkKeys(path + ".spawn", spawn, Set.of("world", "x", "y", "z", "yaw", "pitch"), errors);
            requireString(path + ".spawn.world", spawn.get("world"), false, errors);
            for (String coordinate : List.of("x", "y", "z", "yaw", "pitch")) {
                requireNumber(path + ".spawn." + coordinate, spawn.get(coordinate), errors);
            }
        });
    }

    private void validateTitles(Map<String, Object> root, List<String> errors) {
        checkKeys("titles.yml", root, Set.of("config-version", "titles"), errors);
        checkVersion("titles.yml", root, errors);
        Map<String, Object> definitions = requiredMap("titles.yml.titles", root.get("titles"), errors);
        definitions.forEach((id, raw) -> {
            String path = "titles.yml.titles." + id;
            Map<String, Object> data = requiredMap(path, raw, errors);
            checkKeys(path, data, Set.of("description", "display-name", "prefix", "suffix", "priority",
                    "visible-under-username", "acquisition-mode", "ownership-mode",
                    "hidden-from-discovery", "abilities", "active-effects"), errors);
            if (data.containsKey("description")) {
                requireString(path + ".description", data.get("description"), true, errors);
            }
            requireString(path + ".display-name", data.get("display-name"), false, errors);
            requireString(path + ".prefix", data.get("prefix"), true, errors);
            requireString(path + ".suffix", data.get("suffix"), true, errors);
            requireNumber(path + ".priority", data.get("priority"), errors);
            requireBoolean(path + ".visible-under-username", data.get("visible-under-username"), errors);
            if (data.containsKey("acquisition-mode")) {
                requireEnum(path + ".acquisition-mode", data.get("acquisition-mode"),
                        TitleAcquisitionMode.class, errors);
            }
            if (data.containsKey("ownership-mode")) {
                requireEnum(path + ".ownership-mode", data.get("ownership-mode"),
                        TitleOwnershipMode.class, errors);
            }
            if (data.containsKey("hidden-from-discovery")) {
                requireBoolean(path + ".hidden-from-discovery", data.get("hidden-from-discovery"), errors);
            }
            requireStringCollection(path + ".abilities", data.get("abilities"), errors);
            if (data.containsKey("active-effects")) {
                requireActionCollection(path + ".active-effects", data.get("active-effects"), errors);
            }
        });
    }

    private void validateTitleProgression(Map<String, Object> root, Map<String, Object> titlesRoot, List<String> errors) {
        checkKeys("title-progression.yml", root, Set.of("config-version", "regions", "rules"), errors);
        checkVersion("title-progression.yml", root, errors);
        Map<String, Object> titleDefinitions = map(titlesRoot.get("titles"));
        Set<String> titleIds = new LinkedHashSet<>();
        titleDefinitions.keySet().forEach(id -> titleIds.add(normalizeId(id)));
        Set<String> regionIds = new LinkedHashSet<>();
        Map<String, Object> regions = map(root.get("regions"));
        regions.forEach((id, raw) -> {
            String normalized = normalizeId(id);
            regionIds.add(normalized);
            String path = "title-progression.yml.regions." + id;
            Map<String, Object> data = requiredMap(path, raw, errors);
            checkKeys(path, data, Set.of("world", "min-x", "min-y", "min-z", "max-x", "max-y", "max-z"), errors);
            requireString(path + ".world", data.get("world"), false, errors);
            for (String key : List.of("min-x", "min-y", "min-z", "max-x", "max-y", "max-z")) {
                requireNumber(path + "." + key, data.get(key), errors);
            }
        });
        Map<String, Object> rules = requiredMap("title-progression.yml.rules", root.get("rules"), errors);
        rules.forEach((id, raw) -> {
            String path = "title-progression.yml.rules." + id;
            Map<String, Object> data = requiredMap(path, raw, errors);
            checkKeys(path, data, Set.of("title", "trigger", "stat-key", "threshold", "amount",
                    "entities", "blocks", "items", "recipes", "worlds", "dimensions", "biomes", "regions",
                    "metadata", "continuous"), errors);
            String title = requireString(path + ".title", data.get("title"), false, errors);
            if (title != null && !titleIds.contains(normalizeId(title))) {
                errors.add(path + ".title: unknown title '" + title + "'");
            }
            requireString(path + ".trigger", data.get("trigger"), false, errors);
            if (data.containsKey("stat-key")) requireString(path + ".stat-key", data.get("stat-key"), false, errors);
            if (data.containsKey("threshold")) requireNumber(path + ".threshold", data.get("threshold"), errors);
            if (data.containsKey("amount")) requireNumber(path + ".amount", data.get("amount"), errors);
            for (String key : List.of("entities", "blocks", "items", "recipes", "worlds", "dimensions", "biomes", "regions")) {
                if (data.containsKey(key)) requireStringCollection(path + "." + key, data.get(key), errors);
            }
            for (String key : List.of("entities", "blocks", "items", "recipes")) {
                for (String matcher : stringSet(data.get(key))) {
                    validateRegistryMatcher(path + "." + key, matcher, errors);
                }
            }
            for (String region : stringSet(data.get("regions"))) {
                if (!regionIds.contains(normalizeId(region))) {
                    errors.add(path + ".regions: unknown region '" + region + "'");
                }
            }
            if (data.containsKey("metadata")) requiredStringMap(path + ".metadata", data.get("metadata"), errors);
            if (data.containsKey("continuous")) {
                Map<String, Object> continuous = requiredMap(path + ".continuous", data.get("continuous"), errors);
                checkKeys(path + ".continuous", continuous, Set.of("duration", "duration-unit",
                        "sample-interval-ticks", "reset-on-failure", "required-status-effects",
                        "allowed-status-effects", "required-metadata"), errors);
                requireNumber(path + ".continuous.duration", continuous.get("duration"), errors);
                requireString(path + ".continuous.duration-unit", continuous.get("duration-unit"), false, errors);
                requireNumber(path + ".continuous.sample-interval-ticks",
                        continuous.get("sample-interval-ticks"), errors);
                requireBoolean(path + ".continuous.reset-on-failure", continuous.get("reset-on-failure"), errors);
                for (String key : List.of("required-status-effects", "allowed-status-effects", "required-metadata")) {
                    if (continuous.containsKey(key)) {
                        requireStringCollection(path + ".continuous." + key, continuous.get(key), errors);
                    }
                }
            }
        });
    }

    private void validateRewards(Map<String, Object> root, List<String> errors) {
        checkKeys("rewards.yml", root, Set.of("config-version", "rewards"), errors);
        checkVersion("rewards.yml", root, errors);
        Map<String, Object> definitions = requiredMap("rewards.yml.rewards", root.get("rewards"), errors);
        definitions.forEach((id, raw) -> {
            String path = "rewards.yml.rewards." + id;
            Map<String, Object> reward = requiredMap(path, raw, errors);
            checkKeys(path, reward, Set.of("actions"), errors);
            Object actions = reward.get("actions");
            if (!(actions instanceof Collection<?> collection)) {
                errors.add(path + ".actions: expected a list");
                return;
            }
            int index = 0;
            for (Object action : collection) {
                Map<String, Object> data = requiredMap(path + ".actions[" + index + "]", action, errors);
                requireString(path + ".actions[" + index + "].type", data.get("type"), false, errors);
                index++;
            }
        });
    }

    private void validateIdentity(Map<String, Object> root, List<String> errors) {
        checkKeys("identity.yml", root, Set.of("config-version", "nickname", "nickname-policy",
                "nickname-protection", "title"), errors);
        checkVersion("identity.yml", root, errors);
        Map<String, Object> nickname = requiredMap("identity.yml.nickname", root.get("nickname"), errors);
        checkKeys("identity.yml.nickname", nickname, Set.of("enabled", "max-length"), errors);
        requireBoolean("identity.yml.nickname.enabled", nickname.get("enabled"), errors);
        Number maxLength = requireNumber("identity.yml.nickname.max-length", nickname.get("max-length"), errors);
        if (maxLength != null && maxLength.intValue() < 1) {
            errors.add("identity.yml.nickname.max-length: must be at least 1");
        }
        Map<String, Object> policy = requiredMap(
                "identity.yml.nickname-policy", root.get("nickname-policy"), errors);
        checkKeys("identity.yml.nickname-policy", policy,
                Set.of("unique", "reserve-player-usernames", "reserved-names"), errors);
        requireBoolean("identity.yml.nickname-policy.unique", policy.get("unique"), errors);
        requireBoolean("identity.yml.nickname-policy.reserve-player-usernames",
                policy.get("reserve-player-usernames"), errors);
        requireStringCollection("identity.yml.nickname-policy.reserved-names",
                policy.get("reserved-names"), errors);
        Map<String, Object> protection = requiredMap(
                "identity.yml.nickname-protection", root.get("nickname-protection"), errors);
        checkKeys("identity.yml.nickname-protection", protection, Set.of("enabled",
                "protect-realm-presentation", "protect-title-presentation",
                "reject-containing-protected-name"), errors);
        for (String key : protection.keySet()) {
            requireBoolean("identity.yml.nickname-protection." + key, protection.get(key), errors);
        }
        Map<String, Object> title = requiredMap("identity.yml.title", root.get("title"), errors);
        checkKeys("identity.yml.title", title, Set.of("render-under-username"), errors);
        requireBoolean("identity.yml.title.render-under-username",
                title.get("render-under-username"), errors);
    }

    private void validateSimpleFiles(List<String> errors) {
        Map<String, Object> defaults = loadMap("citizens-defaults.yml");
        checkKeys("citizens-defaults.yml", defaults, Set.of("config-version", "defaults"), errors);
        checkVersion("citizens-defaults.yml", defaults, errors);
        Map<String, Object> defaultValues =
                requiredMap("citizens-defaults.yml.defaults", defaults.get("defaults"), errors);
        checkKeys("citizens-defaults.yml.defaults", defaultValues,
                Set.of("status", "title", "flags"), errors);
        requireString("citizens-defaults.yml.defaults.status", defaultValues.get("status"), false, errors);
        requireEnum("citizens-defaults.yml.defaults.status",
                defaultValues.get("status"), panetina.elarion.core.model.CitizenStatus.class, errors);
        requireString("citizens-defaults.yml.defaults.title", defaultValues.get("title"), false, errors);
        requireStringCollection("citizens-defaults.yml.defaults.flags", defaultValues.get("flags"), errors);

        Map<String, Object> chat = loadMap("chat.yml");
        checkKeys("chat.yml", chat, Set.of("config-version", "local-chat", "whisper-chat",
                "yell-chat", "realm-chat", "alliance-chat", "notices"), errors);
        checkVersion("chat.yml", chat, errors);
        Map<String, Object> localChat =
                requiredMap("chat.yml.local-chat", chat.get("local-chat"), errors);
        checkKeys("chat.yml.local-chat", localChat, Set.of("enabled", "radius",
                "same-world-only", "admin-spy", "format"), errors);
        requireBoolean("chat.yml.local-chat.enabled", localChat.get("enabled"), errors);
        Number radius = requireNumber("chat.yml.local-chat.radius", localChat.get("radius"), errors);
        if (radius != null && radius.intValue() < 1) {
            errors.add("chat.yml.local-chat.radius: must be at least 1");
        }
        requireBoolean("chat.yml.local-chat.same-world-only", localChat.get("same-world-only"), errors);
        requireBoolean("chat.yml.local-chat.admin-spy", localChat.get("admin-spy"), errors);
        requireString("chat.yml.local-chat.format", localChat.get("format"), false, errors);
        Map<String, Object> whisperChat =
                requiredMap("chat.yml.whisper-chat", chat.get("whisper-chat"), errors);
        checkKeys("chat.yml.whisper-chat", whisperChat, Set.of("command", "radius", "format"), errors);
        requireString("chat.yml.whisper-chat.command", whisperChat.get("command"), false, errors);
        Number whisperRadius =
                requireNumber("chat.yml.whisper-chat.radius", whisperChat.get("radius"), errors);
        if (whisperRadius != null && whisperRadius.intValue() < 1) {
            errors.add("chat.yml.whisper-chat.radius: must be at least 1");
        }
        requireString("chat.yml.whisper-chat.format", whisperChat.get("format"), false, errors);
        Map<String, Object> yellChat =
                requiredMap("chat.yml.yell-chat", chat.get("yell-chat"), errors);
        checkKeys("chat.yml.yell-chat", yellChat,
                Set.of("command", "radius", "cooldown-seconds", "format"), errors);
        requireString("chat.yml.yell-chat.command", yellChat.get("command"), false, errors);
        Number yellRadius = requireNumber("chat.yml.yell-chat.radius", yellChat.get("radius"), errors);
        if (yellRadius != null && yellRadius.intValue() < 1) {
            errors.add("chat.yml.yell-chat.radius: must be at least 1");
        }
        Number yellCooldown = requireNumber(
                "chat.yml.yell-chat.cooldown-seconds", yellChat.get("cooldown-seconds"), errors);
        if (yellCooldown != null && yellCooldown.intValue() < 0) {
            errors.add("chat.yml.yell-chat.cooldown-seconds: must not be negative");
        }
        requireString("chat.yml.yell-chat.format", yellChat.get("format"), false, errors);
        Map<String, Object> realmChat =
                requiredMap("chat.yml.realm-chat", chat.get("realm-chat"), errors);
        checkKeys("chat.yml.realm-chat", realmChat, Set.of("command", "format"), errors);
        requireString("chat.yml.realm-chat.command", realmChat.get("command"), false, errors);
        requireString("chat.yml.realm-chat.format", realmChat.get("format"), false, errors);
        Map<String, Object> allianceChat =
                requiredMap("chat.yml.alliance-chat", chat.get("alliance-chat"), errors);
        checkKeys("chat.yml.alliance-chat", allianceChat, Set.of("command", "format"), errors);
        requireString("chat.yml.alliance-chat.command", allianceChat.get("command"), false, errors);
        requireString("chat.yml.alliance-chat.format", allianceChat.get("format"), false, errors);
        Map<String, Object> notices = requiredMap("chat.yml.notices", chat.get("notices"), errors);
        checkKeys("chat.yml.notices", notices, Set.of("scoped-join-leave", "realm-format", "admin-format"), errors);
        requireBoolean("chat.yml.notices.scoped-join-leave", notices.get("scoped-join-leave"), errors);
        requireString("chat.yml.notices.realm-format", notices.get("realm-format"), false, errors);
        requireString("chat.yml.notices.admin-format", notices.get("admin-format"), false, errors);

        Map<String, Object> abilities = loadMap("abilities.yml");
        checkKeys("abilities.yml", abilities, Set.of("config-version", "abilities"), errors);
        checkVersion("abilities.yml", abilities, errors);
        requiredMap("abilities.yml.abilities", abilities.get("abilities"), errors)
                .forEach((id, raw) -> {
                    String path = "abilities.yml.abilities." + id;
                    Map<String, Object> value = requiredMap(path, raw, errors);
                    checkKeys(path, value, Set.of("description"), errors);
                    requireString(path + ".description", value.get("description"), false, errors);
                });

        Map<String, Object> commands = loadMap("commands.yml");
        checkKeys("commands.yml", commands, Set.of("config-version", "commands"), errors);
        checkVersion("commands.yml", commands, errors);
        Map<String, Object> commandValues =
                requiredMap("commands.yml.commands", commands.get("commands"), errors);
        checkKeys("commands.yml.commands", commandValues,
                Set.of("admin-root", "admin-permission-level", "realm-chat-root"), errors);
        requireString("commands.yml.commands.admin-root", commandValues.get("admin-root"), false, errors);
        Number permission = requireNumber("commands.yml.commands.admin-permission-level",
                commandValues.get("admin-permission-level"), errors);
        if (permission != null && (permission.intValue() < 0 || permission.intValue() > 4)) {
            errors.add("commands.yml.commands.admin-permission-level: must be between 0 and 4");
        }
        requireString("commands.yml.commands.realm-chat-root",
                commandValues.get("realm-chat-root"), false, errors);

        Map<String, Object> visibility = loadMap("visibility.yml");
        checkKeys("visibility.yml", visibility, Set.of("config-version", "defaults"), errors);
        checkVersion("visibility.yml", visibility, errors);
        Map<String, Object> visibilityDefaults =
                requiredMap("visibility.yml.defaults", visibility.get("defaults"), errors);
        checkKeys("visibility.yml.defaults", visibilityDefaults,
                Set.of("scope", "operators-visible"), errors);
        requireEnum("visibility.yml.defaults.scope",
                visibilityDefaults.get("scope"), VisibilityScope.class, errors);
        requireBoolean("visibility.yml.defaults.operators-visible",
                visibilityDefaults.get("operators-visible"), errors);

        CoreConfigHistorySupport.validate(loadMap("history.yml"), CONFIG_VERSION, errors);
    }

    private void checkVersion(String file, Map<String, Object> root, List<String> errors) {
        Number version = requireNumber(file + ".config-version", root.get("config-version"), errors);
        if (version != null && version.intValue() != CONFIG_VERSION) {
            errors.add(file + ".config-version: expected " + CONFIG_VERSION + " but found " + version);
        }
    }

    private static void checkKeys(
            String path, Map<String, Object> values, Set<String> allowed, List<String> errors
    ) {
        values.keySet().stream()
                .filter(key -> !allowed.contains(key))
                .forEach(key -> errors.add(path + "." + key + ": unknown field"));
    }

    private static Map<String, Object> requiredMap(String path, Object value, List<String> errors) {
        if (value instanceof Map<?, ?>) return map(value);
        errors.add(path + ": expected a mapping");
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> result ? (Map<String, Object>) result : Map.of();
    }

    private static String normalizeId(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private static Set<String> stringSet(Object value) {
        if (!(value instanceof Collection<?> collection)) return Set.of();
        Set<String> result = new LinkedHashSet<>();
        collection.forEach(item -> result.add(String.valueOf(item)));
        return result;
    }

    private static String requireString(
            String path, Object value, boolean allowBlank, List<String> errors
    ) {
        if (!(value instanceof String text)) {
            errors.add(path + ": expected a string");
            return null;
        }
        if (!allowBlank && text.isBlank()) errors.add(path + ": must not be blank");
        return text;
    }

    private static Number requireNumber(String path, Object value, List<String> errors) {
        if (value instanceof Number number) return number;
        errors.add(path + ": expected a number");
        return null;
    }

    private static void requireBoolean(String path, Object value, List<String> errors) {
        if (!(value instanceof Boolean)) errors.add(path + ": expected true or false");
    }

    private static void requireStringCollection(String path, Object value, List<String> errors) {
        if (!(value instanceof Collection<?> collection)) {
            errors.add(path + ": expected a list of strings");
            return;
        }
        if (collection.stream().anyMatch(item -> !(item instanceof String))) {
            errors.add(path + ": every list item must be a string");
        }
    }

    private static void requireActionCollection(String path, Object value, List<String> errors) {
        if (!(value instanceof Collection<?> collection)) {
            errors.add(path + ": expected a list of mappings");
            return;
        }
        int index = 0;
        for (Object item : collection) {
            Map<String, Object> data = requiredMap(path + "[" + index + "]", item, errors);
            requireString(path + "[" + index + "].type", data.get("type"), false, errors);
            index++;
        }
    }

    private static void requiredStringMap(String path, Object value, List<String> errors) {
        Map<String, Object> data = requiredMap(path, value, errors);
        data.forEach((key, raw) -> {
            if (!(raw instanceof String)) errors.add(path + "." + key + ": expected a string");
        });
    }

    private static void validateRegistryMatcher(String path, String value, List<String> errors) {
        String raw = value == null ? "" : value.trim();
        if (raw.startsWith("#")) raw = raw.substring(1);
        try {
            net.minecraft.util.Identifier.of(raw);
        } catch (RuntimeException exception) {
            errors.add(path + ": invalid registry ID or tag '" + value + "'");
        }
    }

    private static <T extends Enum<T>> void requireEnum(
            String path, Object value, Class<T> type, List<String> errors
    ) {
        if (!(value instanceof String text)) {
            errors.add(path + ": expected one of " + List.of(type.getEnumConstants()));
            return;
        }
        try {
            Enum.valueOf(type, text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            errors.add(path + ": unknown value '" + text + "', expected one of "
                    + List.of(type.getEnumConstants()));
        }
    }


}
