package panetina.elarion.core.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.model.SpawnPoint;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.VisibilityScope;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CoreConfigManager {
    private static final int CONFIG_VERSION = 1;
    private static final Set<String> FORMATTING_COLORS = Set.of(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red",
            "light_purple", "yellow", "white");
    private static final Map<String, String> DEFAULT_FILES = Map.ofEntries(
            Map.entry("realms.yml", """
                    config-version: 1

                    # Supported realm colors:
                    # black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple
                    # gold, gray, dark_gray, blue, green, aqua, red
                    # light_purple, yellow, white
                    #
                    # Invalid color names fall back to white.

                    realms:
                      oak:
                        display-name: "Kingdom of Oak"
                        short-name: "OAK"
                        prefix: "[OAK]"
                        color: "green"
                        visibility-scope: "REALM"
                        spawn:
                          world: "minecraft:overworld"
                          x: 0
                          y: 64
                          z: 0
                          yaw: 0
                          pitch: 0
                        flags: []
                    """),
            Map.entry("titles.yml", """
                    config-version: 1

                    titles:
                      citizen:
                        display-name: "Citizen"
                        prefix: ""
                        suffix: ""
                        priority: 0
                        visible-under-username: true
                        abilities: []
                      news_reporter:
                        display-name: "News Reporter"
                        prefix: ""
                        suffix: ""
                        priority: 20
                        visible-under-username: true
                        abilities:
                          - "elarion.newspaper.publish"
                      diplomat:
                        display-name: "Diplomat"
                        prefix: ""
                        suffix: ""
                        priority: 30
                        visible-under-username: true
                        abilities:
                          - "elarion.portal.foreign_access"
                    """),
            Map.entry("abilities.yml", """
                    config-version: 1

                    abilities:
                      elarion.newspaper.publish:
                        description: "Publish and manage newspapers."
                      elarion.portal.foreign_access:
                        description: "Use portals belonging to another realm."
                    """),
            Map.entry("identity.yml", """
                    config-version: 1

                    nickname:
                      enabled: true
                      max-length: 32
                    nickname-policy:
                      # Comparison always ignores capitalization, whitespace, and
                      # common separators. Submitted nicknames may contain only
                      # letters, spaces, apostrophes, and hyphens. Every name
                      # segment is title-cased.
                      unique: true
                      reserve-player-usernames: true
                      reserved-names:
                        - "admin"
                        - "administrator"
                        - "server"
                        - "system"
                        - "console"
                        - "operator"
                        - "moderator"
                        - "elarion"
                    nickname-protection:
                      enabled: true
                      protect-realm-presentation: true
                      protect-title-presentation: true
                      reject-containing-protected-name: true
                    title:
                      render-under-username: true
                    """),
            Map.entry("chat.yml", """
                    config-version: 1

                    realm-chat:
                      command: "rc"
                      format: "[%realm_short%] %player% \u00bb %message%"
                    """),
            Map.entry("visibility.yml", """
                    config-version: 1

                    defaults:
                      scope: "REALM"
                      operators-visible: true
                    """),
            Map.entry("rewards.yml", """
                    config-version: 1

                    rewards:
                      welcome:
                        actions:
                          - type: "message"
                            text: "Welcome to Elarion."
                    """),
            Map.entry("commands.yml", """
                    config-version: 1

                    commands:
                      admin-root: "e"
                      admin-permission-level: 4
                      realm-chat-root: "rc"
                    """),
            Map.entry("citizens-defaults.yml", """
                    config-version: 1

                    defaults:
                      status: "ACTIVE"
                      title: "citizen"
                      flags: []
                    """)
    );

    private final Logger logger;
    private final Yaml yaml = new Yaml();
    private final Path coreConfigDir;
    private Map<String, RealmDefinition> realms = Map.of();
    private Map<String, TitleDefinition> titles = Map.of();
    private Map<String, List<RewardAction>> rewards = Map.of();
    private String defaultTitleId = "citizen";
    private String realmChatFormat = "[%realm_short%] %player% \u00bb %message%";
    private boolean nicknamesEnabled = true;
    private int nicknameMaxLength = 32;
    private boolean nicknameUnique = true;
    private boolean nicknameReservePlayerUsernames = true;
    private Set<String> nicknameReservedNames = Set.of();
    private boolean nicknameProtectionEnabled = true;
    private boolean nicknameProtectRealmPresentation = true;
    private boolean nicknameProtectTitlePresentation = true;
    private boolean nicknameRejectContainingProtectedName = true;

    public CoreConfigManager(Logger logger) {
        this(logger, FabricLoader.getInstance().getConfigDir().resolve("elarion/core"));
    }

    public CoreConfigManager(Logger logger, Path coreConfigDir) {
        this.logger = logger;
        this.coreConfigDir = coreConfigDir;
    }

    public void load() {
        writeDefaults();
        migrateConfigs();
        validateConfigs();

        Map<String, RealmDefinition> loadedRealms = loadRealms();
        Map<String, TitleDefinition> loadedTitles = loadTitles();
        Map<String, List<RewardAction>> loadedRewards = loadRewards();

        Map<String, Object> defaults = loadMap("citizens-defaults.yml");
        String loadedDefaultTitleId = string(map(defaults.get("defaults")).get("title"), "citizen");

        Map<String, Object> chat = loadMap("chat.yml");
        String loadedRealmChatFormat =
                string(map(chat.get("realm-chat")).get("format"), realmChatFormat);

        Map<String, Object> identity = loadMap("identity.yml");
        Map<String, Object> nickname = map(identity.get("nickname"));
        boolean loadedNicknamesEnabled = bool(nickname.get("enabled"), true);
        int loadedNicknameMaxLength = number(nickname.get("max-length"), 32).intValue();
        Map<String, Object> nicknamePolicy = map(identity.get("nickname-policy"));
        boolean loadedNicknameUnique = bool(nicknamePolicy.get("unique"), true);
        boolean loadedNicknameReservePlayerUsernames =
                bool(nicknamePolicy.get("reserve-player-usernames"), true);
        Set<String> loadedNicknameReservedNames = stringSet(nicknamePolicy.get("reserved-names"));
        Map<String, Object> nicknameProtection = map(identity.get("nickname-protection"));
        boolean loadedNicknameProtectionEnabled = bool(nicknameProtection.get("enabled"), true);
        boolean loadedNicknameProtectRealmPresentation =
                bool(nicknameProtection.get("protect-realm-presentation"), true);
        boolean loadedNicknameProtectTitlePresentation =
                bool(nicknameProtection.get("protect-title-presentation"), true);
        boolean loadedNicknameRejectContainingProtectedName =
                bool(nicknameProtection.get("reject-containing-protected-name"), true);

        realms = loadedRealms;
        titles = loadedTitles;
        rewards = loadedRewards;
        defaultTitleId = loadedDefaultTitleId;
        realmChatFormat = loadedRealmChatFormat;
        nicknamesEnabled = loadedNicknamesEnabled;
        nicknameMaxLength = loadedNicknameMaxLength;
        nicknameUnique = loadedNicknameUnique;
        nicknameReservePlayerUsernames = loadedNicknameReservePlayerUsernames;
        nicknameReservedNames = loadedNicknameReservedNames;
        nicknameProtectionEnabled = loadedNicknameProtectionEnabled;
        nicknameProtectRealmPresentation = loadedNicknameProtectRealmPresentation;
        nicknameProtectTitlePresentation = loadedNicknameProtectTitlePresentation;
        nicknameRejectContainingProtectedName = loadedNicknameRejectContainingProtectedName;
        logger.info("Loaded {} realms, {} titles, and {} reward definitions",
                realms.size(), titles.size(), rewards.size());
    }

    public Map<String, RealmDefinition> realms() { return realms; }
    public Map<String, TitleDefinition> titles() { return titles; }
    public Map<String, List<RewardAction>> rewards() { return rewards; }
    public String defaultTitleId() { return defaultTitleId; }
    public String realmChatFormat() { return realmChatFormat; }
    public boolean nicknamesEnabled() { return nicknamesEnabled; }
    public int nicknameMaxLength() { return nicknameMaxLength; }
    public boolean nicknameUnique() { return nicknameUnique; }
    public boolean nicknameReservePlayerUsernames() { return nicknameReservePlayerUsernames; }
    public Set<String> nicknameReservedNames() { return nicknameReservedNames; }
    public boolean nicknameProtectionEnabled() { return nicknameProtectionEnabled; }
    public boolean nicknameProtectRealmPresentation() { return nicknameProtectRealmPresentation; }
    public boolean nicknameProtectTitlePresentation() { return nicknameProtectTitlePresentation; }
    public boolean nicknameRejectContainingProtectedName() { return nicknameRejectContainingProtectedName; }
    public Path coreConfigDir() { return coreConfigDir; }

    private void writeDefaults() {
        try {
            Files.createDirectories(coreConfigDir);
            for (Map.Entry<String, String> entry : DEFAULT_FILES.entrySet()) {
                Path path = coreConfigDir.resolve(entry.getKey());
                if (Files.notExists(path)) {
                    Files.writeString(path, entry.getValue(), StandardCharsets.UTF_8);
                }
            }
            appendNicknamePolicyDefaults(coreConfigDir.resolve("identity.yml"));
            appendNicknameProtectionDefaults(coreConfigDir.resolve("identity.yml"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create Elarion configuration", exception);
        }
    }

    private void migrateConfigs() {
        for (String fileName : DEFAULT_FILES.keySet()) {
            Path path = coreConfigDir.resolve(fileName);
            Map<String, Object> root = loadMap(fileName);
            int version = number(root.get("config-version"), 0).intValue();
            if (version > CONFIG_VERSION) {
                throw new ConfigValidationException(List.of(
                        fileName + ".config-version: version " + version
                                + " is newer than supported version " + CONFIG_VERSION));
            }
            if (version == CONFIG_VERSION) continue;
            try {
                Path backup = path.resolveSibling(path.getFileName() + ".bak-v" + version);
                if (Files.notExists(backup)) {
                    Files.copy(path, backup, StandardCopyOption.COPY_ATTRIBUTES);
                }
                Files.writeString(path, System.lineSeparator() + "config-version: " + CONFIG_VERSION
                        + System.lineSeparator(), StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.APPEND);
                logger.info("Migrated {} from config version {} to {}", fileName, version, CONFIG_VERSION);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to migrate " + path, exception);
            }
        }
    }

    private void validateConfigs() {
        List<String> errors = new ArrayList<>();
        validateRealms(loadMap("realms.yml"), errors);
        validateTitles(loadMap("titles.yml"), errors);
        validateRewards(loadMap("rewards.yml"), errors);
        validateIdentity(loadMap("identity.yml"), errors);
        validateSimpleFiles(errors);
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
            checkKeys(path, data, Set.of("display-name", "prefix", "suffix", "priority",
                    "visible-under-username", "abilities"), errors);
            requireString(path + ".display-name", data.get("display-name"), false, errors);
            requireString(path + ".prefix", data.get("prefix"), true, errors);
            requireString(path + ".suffix", data.get("suffix"), true, errors);
            requireNumber(path + ".priority", data.get("priority"), errors);
            requireBoolean(path + ".visible-under-username", data.get("visible-under-username"), errors);
            requireStringCollection(path + ".abilities", data.get("abilities"), errors);
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
        checkKeys("chat.yml", chat, Set.of("config-version", "realm-chat"), errors);
        checkVersion("chat.yml", chat, errors);
        Map<String, Object> realmChat =
                requiredMap("chat.yml.realm-chat", chat.get("realm-chat"), errors);
        checkKeys("chat.yml.realm-chat", realmChat, Set.of("command", "format"), errors);
        requireString("chat.yml.realm-chat.command", realmChat.get("command"), false, errors);
        requireString("chat.yml.realm-chat.format", realmChat.get("format"), false, errors);

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

    private static void appendNicknamePolicyDefaults(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        if (content.lines().anyMatch(line -> line.trim().equals("nickname-policy:"))) return;

        Files.writeString(path, """

                nickname-policy:
                  # Comparison always ignores capitalization, whitespace, and
                  # common separators. Submitted nicknames may contain only
                  # letters, spaces, apostrophes, and hyphens. Every name
                  # segment is title-cased.
                  unique: true
                  reserve-player-usernames: true
                  reserved-names:
                    - "admin"
                    - "administrator"
                    - "server"
                    - "system"
                    - "console"
                    - "operator"
                    - "moderator"
                    - "elarion"
                """, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
    }

    private static void appendNicknameProtectionDefaults(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        if (content.lines().anyMatch(line -> line.trim().equals("nickname-protection:"))) return;

        Files.writeString(path, """

                nickname-protection:
                  # Prevent impersonation of staff, system messages, realms,
                  # and official titles. Common lookalike Unicode letters are
                  # compared as their Latin equivalents.
                  enabled: true
                  protect-realm-presentation: true
                  protect-title-presentation: true
                  reject-containing-protected-name: true
                """, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
    }

    private Map<String, RealmDefinition> loadRealms() {
        Map<String, RealmDefinition> result = new LinkedHashMap<>();
        Map<String, Object> root = loadMap("realms.yml");
        for (Map.Entry<String, Object> entry : map(root.get("realms")).entrySet()) {
            String id = normalizeId(entry.getKey());
            Map<String, Object> data = map(entry.getValue());
            Map<String, Object> spawn = map(data.get("spawn"));
            SpawnPoint spawnPoint = new SpawnPoint(
                    string(spawn.get("world"), "minecraft:overworld"),
                    number(spawn.get("x"), 0).doubleValue(),
                    number(spawn.get("y"), 64).doubleValue(),
                    number(spawn.get("z"), 0).doubleValue(),
                    number(spawn.get("yaw"), 0).floatValue(),
                    number(spawn.get("pitch"), 0).floatValue()
            );
            result.put(id, new RealmDefinition(
                    id,
                    string(data.get("display-name"), id),
                    string(data.get("short-name"), id.toUpperCase(Locale.ROOT)),
                    string(data.get("prefix"), ""),
                    string(data.get("color"), "white"),
                    spawnPoint,
                    enumValue(VisibilityScope.class, data.get("visibility-scope"), VisibilityScope.REALM),
                    stringSet(data.get("flags"))
            ));
        }
        return Map.copyOf(result);
    }

    private Map<String, TitleDefinition> loadTitles() {
        Map<String, TitleDefinition> result = new LinkedHashMap<>();
        Map<String, Object> root = loadMap("titles.yml");
        for (Map.Entry<String, Object> entry : map(root.get("titles")).entrySet()) {
            String id = normalizeId(entry.getKey());
            Map<String, Object> data = map(entry.getValue());
            result.put(id, new TitleDefinition(
                    id,
                    string(data.get("display-name"), id),
                    string(data.get("prefix"), ""),
                    string(data.get("suffix"), ""),
                    number(data.get("priority"), 0).intValue(),
                    bool(data.get("visible-under-username"), true),
                    stringSet(data.get("abilities"))
            ));
        }
        return Map.copyOf(result);
    }

    private Map<String, List<RewardAction>> loadRewards() {
        Map<String, List<RewardAction>> result = new LinkedHashMap<>();
        Map<String, Object> root = loadMap("rewards.yml");
        for (Map.Entry<String, Object> entry : map(root.get("rewards")).entrySet()) {
            List<RewardAction> actions = new ArrayList<>();
            Object rawActions = map(entry.getValue()).get("actions");
            if (rawActions instanceof Collection<?> collection) {
                for (Object item : collection) {
                    Map<String, Object> raw = map(item);
                    String type = string(raw.get("type"), "");
                    Map<String, String> parameters = new LinkedHashMap<>();
                    raw.forEach((key, value) -> {
                        if (!key.equals("type")) parameters.put(key, String.valueOf(value));
                    });
                    if (!type.isBlank()) actions.add(new RewardAction(type, parameters));
                }
            }
            result.put(normalizeId(entry.getKey()), List.copyOf(actions));
        }
        return Map.copyOf(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadMap(String fileName) {
        Path path = coreConfigDir.resolve(fileName);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object loaded = yaml.load(reader);
            return loaded instanceof Map<?, ?> value ? (Map<String, Object>) value : Map.of();
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load {}", path, exception);
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> result ? (Map<String, Object>) result : Map.of();
    }

    private static String normalizeId(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private static String string(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private static Number number(Object value, Number fallback) {
        return value instanceof Number number ? number : fallback;
    }

    private static boolean bool(Object value, boolean fallback) {
        return value instanceof Boolean bool ? bool : fallback;
    }

    private static Set<String> stringSet(Object value) {
        if (!(value instanceof Collection<?> collection)) return Set.of();
        Set<String> result = new LinkedHashSet<>();
        collection.forEach(item -> result.add(String.valueOf(item)));
        return result;
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, Object value, T fallback) {
        if (value == null) return fallback;
        try {
            return Enum.valueOf(type, String.valueOf(value).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
