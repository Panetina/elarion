package panetina.elarion.core.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.TitleUnlockRule;
import panetina.elarion.core.model.ProgressionRegion;
import panetina.elarion.core.model.HistoryRecordingPolicy;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CoreConfigManager {
    private static final int CONFIG_VERSION = 1;
    private static final Set<String> FORMATTING_COLORS = Set.of(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red",
            "light_purple", "yellow", "white");
    private static final Map<String, String> DEFAULT_FILES = CoreConfigDefaultFiles.FILES;

    private final Logger logger;
    private final Yaml yaml = new Yaml();
    private final Path coreConfigDir;
    private Map<String, RealmDefinition> realms = Map.of();
    private Map<String, TitleDefinition> titles = Map.of();
    private Map<String, TitleUnlockRule> titleUnlockRules = Map.of();
    private Map<String, ProgressionRegion> progressionRegions = Map.of();
    private Map<String, List<RewardAction>> rewards = Map.of();
    private String defaultTitleId = "citizen";
    private boolean localChatEnabled = true;
    private int localChatRadius = 64;
    private boolean localChatSameWorldOnly = true;
    private boolean localChatAdminSpy = true;
    private String localChatFormat = "[Local] %player% \u00bb %message%";
    private int whisperChatRadius = 4;
    private String whisperChatFormat = "[Local] %player% whispers: %message%";
    private int yellChatRadius = 128;
    private int yellChatCooldownSeconds = 300;
    private String yellChatFormat = "[Local] %player% yells: %message%";
    private String realmChatFormat = "[Realm] %player% \u00bb %message%";
    private String allianceChatFormat = "[Alliance:%realm_short%] %player% \u00bb %message%";
    private boolean scopedJoinLeaveNotices = true;
    private String realmNoticeFormat = "%player% joined your Realm.";
    private String adminNoticeFormat = "%player% joined realm %realm%.";
    private boolean nicknamesEnabled = true;
    private int nicknameMaxLength = 32;
    private boolean nicknameUnique = true;
    private boolean nicknameReservePlayerUsernames = true;
    private Set<String> nicknameReservedNames = Set.of();
    private boolean nicknameProtectionEnabled = true;
    private boolean nicknameProtectRealmPresentation = true;
    private boolean nicknameProtectTitlePresentation = true;
    private boolean nicknameRejectContainingProtectedName = true;
    private HistoryRecordingPolicy historyRecordingPolicy = HistoryRecordingPolicy.defaults();
    private int historyQueryMaxMonths = 3;
    private int historyCommandLimitMax = 100;
    private boolean historyArchiveEnabled = true;
    private int historyArchiveMaxCompletedWeeks = 8;
    private Set<String> historyChronicleCategories = CoreConfigHistorySupport.DEFAULT_CHRONICLE_CATEGORIES;
    private int publicHistoryDefaultWeeks = 8;
    private int publicHistoryDefaultLimit = 50;
    private int publicHistoryMaxLimit = 200;

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
        new CoreConfigValidator(this::loadMap, CONFIG_VERSION, FORMATTING_COLORS).validateConfigs();

        CoreConfigParser parser = new CoreConfigParser(logger, yaml, coreConfigDir, defaultTitleId);
        Map<String, RealmDefinition> loadedRealms = parser.loadRealms();
        Map<String, TitleDefinition> loadedTitles = parser.loadTitles();
        Map<String, ProgressionRegion> loadedProgressionRegions = parser.loadProgressionRegions();
        Map<String, TitleUnlockRule> loadedTitleUnlockRules = parser.loadTitleUnlockRules();
        Map<String, List<RewardAction>> loadedRewards = parser.loadRewards();

        Map<String, Object> defaults = loadMap("citizens-defaults.yml");
        String loadedDefaultTitleId = string(map(defaults.get("defaults")).get("title"), "citizen");

        Map<String, Object> chat = loadMap("chat.yml");
        Map<String, Object> localChat = map(chat.get("local-chat"));
        boolean loadedLocalChatEnabled = bool(localChat.get("enabled"), true);
        int loadedLocalChatRadius = number(localChat.get("radius"), 64).intValue();
        boolean loadedLocalChatSameWorldOnly = bool(localChat.get("same-world-only"), true);
        boolean loadedLocalChatAdminSpy = bool(localChat.get("admin-spy"), true);
        String loadedLocalChatFormat = string(localChat.get("format"), localChatFormat);
        Map<String, Object> whisperChat = map(chat.get("whisper-chat"));
        int loadedWhisperChatRadius = number(whisperChat.get("radius"), 4).intValue();
        String loadedWhisperChatFormat = string(whisperChat.get("format"), whisperChatFormat);
        Map<String, Object> yellChat = map(chat.get("yell-chat"));
        int loadedYellChatRadius = number(yellChat.get("radius"), 128).intValue();
        int loadedYellChatCooldownSeconds =
                number(yellChat.get("cooldown-seconds"), 300).intValue();
        String loadedYellChatFormat = string(yellChat.get("format"), yellChatFormat);
        String loadedRealmChatFormat =
                string(map(chat.get("realm-chat")).get("format"), realmChatFormat);
        String loadedAllianceChatFormat =
                string(map(chat.get("alliance-chat")).get("format"), allianceChatFormat);
        Map<String, Object> notices = map(chat.get("notices"));
        boolean loadedScopedJoinLeaveNotices = bool(notices.get("scoped-join-leave"), true);
        String loadedRealmNoticeFormat = string(notices.get("realm-format"), realmNoticeFormat);
        String loadedAdminNoticeFormat = string(notices.get("admin-format"), adminNoticeFormat);

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
        CoreConfigHistorySupport.Settings loadedHistory = CoreConfigHistorySupport.load(
                loadMap("history.yml"), historyChronicleCategories);

        realms = loadedRealms;
        titles = loadedTitles;
        titleUnlockRules = loadedTitleUnlockRules;
        progressionRegions = loadedProgressionRegions;
        rewards = loadedRewards;
        defaultTitleId = loadedDefaultTitleId;
        localChatEnabled = loadedLocalChatEnabled;
        localChatRadius = loadedLocalChatRadius;
        localChatSameWorldOnly = loadedLocalChatSameWorldOnly;
        localChatAdminSpy = loadedLocalChatAdminSpy;
        localChatFormat = loadedLocalChatFormat;
        whisperChatRadius = loadedWhisperChatRadius;
        whisperChatFormat = loadedWhisperChatFormat;
        yellChatRadius = loadedYellChatRadius;
        yellChatCooldownSeconds = loadedYellChatCooldownSeconds;
        yellChatFormat = loadedYellChatFormat;
        realmChatFormat = loadedRealmChatFormat;
        allianceChatFormat = loadedAllianceChatFormat;
        scopedJoinLeaveNotices = loadedScopedJoinLeaveNotices;
        realmNoticeFormat = loadedRealmNoticeFormat;
        adminNoticeFormat = loadedAdminNoticeFormat;
        nicknamesEnabled = loadedNicknamesEnabled;
        nicknameMaxLength = loadedNicknameMaxLength;
        nicknameUnique = loadedNicknameUnique;
        nicknameReservePlayerUsernames = loadedNicknameReservePlayerUsernames;
        nicknameReservedNames = loadedNicknameReservedNames;
        nicknameProtectionEnabled = loadedNicknameProtectionEnabled;
        nicknameProtectRealmPresentation = loadedNicknameProtectRealmPresentation;
        nicknameProtectTitlePresentation = loadedNicknameProtectTitlePresentation;
        nicknameRejectContainingProtectedName = loadedNicknameRejectContainingProtectedName;
        historyRecordingPolicy = loadedHistory.recordingPolicy();
        historyQueryMaxMonths = loadedHistory.queryMaxMonths();
        historyCommandLimitMax = loadedHistory.commandLimitMax();
        historyArchiveEnabled = loadedHistory.archiveEnabled();
        historyArchiveMaxCompletedWeeks = loadedHistory.archiveMaxCompletedWeeks();
        historyChronicleCategories = loadedHistory.chronicleCategories();
        publicHistoryDefaultWeeks = loadedHistory.publicDefaultWeeks();
        publicHistoryDefaultLimit = loadedHistory.publicDefaultLimit();
        publicHistoryMaxLimit = loadedHistory.publicMaxLimit();
        logger.info("Loaded {} realms, {} titles, {} title progression rules, and {} reward definitions",
                realms.size(), titles.size(), titleUnlockRules.size(), rewards.size());
    }

    public Map<String, RealmDefinition> realms() { return realms; }
    public Map<String, TitleDefinition> titles() { return titles; }
    public Map<String, TitleUnlockRule> titleUnlockRules() { return titleUnlockRules; }
    public Map<String, ProgressionRegion> progressionRegions() { return progressionRegions; }
    public Map<String, List<RewardAction>> rewards() { return rewards; }
    public String defaultTitleId() { return defaultTitleId; }
    public boolean localChatEnabled() { return localChatEnabled; }
    public int localChatRadius() { return localChatRadius; }
    public boolean localChatSameWorldOnly() { return localChatSameWorldOnly; }
    public boolean localChatAdminSpy() { return localChatAdminSpy; }
    public String localChatFormat() { return localChatFormat; }
    public int whisperChatRadius() { return whisperChatRadius; }
    public String whisperChatFormat() { return whisperChatFormat; }
    public int yellChatRadius() { return yellChatRadius; }
    public int yellChatCooldownSeconds() { return yellChatCooldownSeconds; }
    public String yellChatFormat() { return yellChatFormat; }
    public String realmChatFormat() { return realmChatFormat; }
    public String allianceChatFormat() { return allianceChatFormat; }
    public boolean scopedJoinLeaveNotices() { return scopedJoinLeaveNotices; }
    public String realmNoticeFormat() { return realmNoticeFormat; }
    public String adminNoticeFormat() { return adminNoticeFormat; }
    public boolean nicknamesEnabled() { return nicknamesEnabled; }
    public int nicknameMaxLength() { return nicknameMaxLength; }
    public boolean nicknameUnique() { return nicknameUnique; }
    public boolean nicknameReservePlayerUsernames() { return nicknameReservePlayerUsernames; }
    public Set<String> nicknameReservedNames() { return nicknameReservedNames; }
    public boolean nicknameProtectionEnabled() { return nicknameProtectionEnabled; }
    public boolean nicknameProtectRealmPresentation() { return nicknameProtectRealmPresentation; }
    public boolean nicknameProtectTitlePresentation() { return nicknameProtectTitlePresentation; }
    public boolean nicknameRejectContainingProtectedName() { return nicknameRejectContainingProtectedName; }
    public HistoryRecordingPolicy historyRecordingPolicy() { return historyRecordingPolicy; }
    public int historyQueryMaxMonths() { return historyQueryMaxMonths; }
    public int historyCommandLimitMax() { return historyCommandLimitMax; }
    public boolean historyArchiveEnabled() { return historyArchiveEnabled; }
    public int historyArchiveMaxCompletedWeeks() { return historyArchiveMaxCompletedWeeks; }
    public Set<String> historyChronicleCategories() { return historyChronicleCategories; }
    public int publicHistoryDefaultWeeks() { return publicHistoryDefaultWeeks; }
    public int publicHistoryDefaultLimit() { return publicHistoryDefaultLimit; }
    public int publicHistoryMaxLimit() { return publicHistoryMaxLimit; }
    public Path coreConfigDir() { return coreConfigDir; }

    private void writeDefaults() {
        try {
            CoreConfigDefaults.write(coreConfigDir, DEFAULT_FILES);
            appendNicknamePolicyDefaults(coreConfigDir.resolve("identity.yml"));
            appendNicknameProtectionDefaults(coreConfigDir.resolve("identity.yml"));
            CoreConfigHistorySupport.appendDefaults(coreConfigDir.resolve("history.yml"));
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
        migrateChatConfig();
        migrateIdentityConfig();
        migrateHistoryConfig();
    }

    private void migrateChatConfig() {
        Path path = coreConfigDir.resolve("chat.yml");
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            String migrated = content.replace("[%realm_short%]", "[Realm]");
            if (!migrated.equals(content)) {
                Files.writeString(path, migrated, StandardCharsets.UTF_8);
                content = migrated;
            }
            StringBuilder addition = new StringBuilder();
            if (content.lines().noneMatch(line -> line.trim().equals("local-chat:"))) {
                addition.append("""

                        local-chat:
                          enabled: true
                          radius: 64
                          same-world-only: true
                          # Enables the OP-only /spy chat toggle. OPs do not spy
                          # automatically.
                          admin-spy: true
                          format: "[Local] %player% \u00bb %message%"
                        """);
            }
            if (content.lines().noneMatch(line -> line.trim().equals("whisper-chat:"))) {
                addition.append("""

                        whisper-chat:
                          command: "w"
                          radius: 4
                          format: "[Local] %player% whispers: %message%"
                        """);
            }
            if (content.lines().noneMatch(line -> line.trim().equals("yell-chat:"))) {
                addition.append("""

                        yell-chat:
                          command: "yell"
                          radius: 128
                          cooldown-seconds: 300
                          format: "[Local] %player% yells: %message%"
                        """);
            }
            if (content.lines().noneMatch(line -> line.trim().equals("notices:"))) {
                addition.append("""

                        notices:
                          scoped-join-leave: true
                          realm-format: "%player% joined your Realm."
                          admin-format: "%player% joined realm %realm%."
                        """);
            }
            if (content.lines().noneMatch(line -> line.trim().equals("alliance-chat:"))) {
                addition.append("""

                        alliance-chat:
                          command: "ac"
                          format: "[Alliance:%realm_short%] %player% \u00bb %message%"
                        """);
            }
            if (!addition.isEmpty()) {
                Files.writeString(path, addition.toString(), StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.APPEND);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to migrate " + path, exception);
        }
    }

    private void migrateIdentityConfig() {
        Path path = coreConfigDir.resolve("identity.yml");
        try {
            appendNicknamePolicyDefaults(path);
            appendNicknameProtectionDefaults(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to migrate " + path, exception);
        }
    }

    private void migrateHistoryConfig() {
        Path path = coreConfigDir.resolve("history.yml");
        try {
            CoreConfigHistorySupport.appendDefaults(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to migrate " + path, exception);
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

}
