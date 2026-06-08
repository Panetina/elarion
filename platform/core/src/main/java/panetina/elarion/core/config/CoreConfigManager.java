package panetina.elarion.core.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.core.model.CommunityDefinition;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.model.SpawnPoint;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.VisibilityScope;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CoreConfigManager {
    private static final Map<String, String> DEFAULT_FILES = Map.ofEntries(
            Map.entry("communities.yml", """
                    # Supported community colors:
                    # black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple
                    # gold, gray, dark_gray, blue, green, aqua, red
                    # light_purple, yellow, white
                    #
                    # Invalid color names fall back to white.

                    communities:
                      oak:
                        display-name: "Kingdom of Oak"
                        short-name: "OAK"
                        prefix: "[OAK]"
                        color: "green"
                        visibility-scope: "COMMUNITY"
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
                    abilities:
                      elarion.newspaper.publish:
                        description: "Publish and manage newspapers."
                      elarion.portal.foreign_access:
                        description: "Use portals belonging to another community."
                    """),
            Map.entry("identity.yml", """
                    nickname:
                      enabled: true
                      max-length: 32
                    title:
                      render-under-username: true
                    """),
            Map.entry("chat.yml", """
                    community-chat:
                      command: "cc"
                      format: "[%community_short%] %player% \u00bb %message%"
                    """),
            Map.entry("visibility.yml", """
                    defaults:
                      scope: "COMMUNITY"
                      operators-visible: true
                    """),
            Map.entry("rewards.yml", """
                    rewards:
                      welcome:
                        actions:
                          - type: "message"
                            text: "Welcome to Elarion."
                    """),
            Map.entry("commands.yml", """
                    commands:
                      admin-root: "e"
                      admin-permission-level: 4
                      community-chat-root: "cc"
                    """),
            Map.entry("citizens-defaults.yml", """
                    defaults:
                      status: "ACTIVE"
                      title: "citizen"
                      flags: []
                    """)
    );

    private final Logger logger;
    private final Yaml yaml = new Yaml();
    private final Path coreConfigDir = FabricLoader.getInstance().getConfigDir().resolve("elarion/core");
    private Map<String, CommunityDefinition> communities = Map.of();
    private Map<String, TitleDefinition> titles = Map.of();
    private Map<String, List<RewardAction>> rewards = Map.of();
    private String defaultTitleId = "citizen";
    private String communityChatFormat = "[%community_short%] %player% \u00bb %message%";
    private boolean nicknamesEnabled = true;
    private int nicknameMaxLength = 32;

    public CoreConfigManager(Logger logger) {
        this.logger = logger;
    }

    public void load() {
        writeDefaults();
        communities = loadCommunities();
        titles = loadTitles();
        rewards = loadRewards();

        Map<String, Object> defaults = loadMap("citizens-defaults.yml");
        defaultTitleId = string(map(defaults.get("defaults")).get("title"), "citizen");

        Map<String, Object> chat = loadMap("chat.yml");
        communityChatFormat = string(map(chat.get("community-chat")).get("format"), communityChatFormat);

        Map<String, Object> identity = loadMap("identity.yml");
        Map<String, Object> nickname = map(identity.get("nickname"));
        nicknamesEnabled = bool(nickname.get("enabled"), true);
        nicknameMaxLength = number(nickname.get("max-length"), 32).intValue();
        logger.info("Loaded {} communities, {} titles, and {} reward definitions",
                communities.size(), titles.size(), rewards.size());
    }

    public Map<String, CommunityDefinition> communities() { return communities; }
    public Map<String, TitleDefinition> titles() { return titles; }
    public Map<String, List<RewardAction>> rewards() { return rewards; }
    public String defaultTitleId() { return defaultTitleId; }
    public String communityChatFormat() { return communityChatFormat; }
    public boolean nicknamesEnabled() { return nicknamesEnabled; }
    public int nicknameMaxLength() { return nicknameMaxLength; }
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
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create Elarion configuration", exception);
        }
    }

    private Map<String, CommunityDefinition> loadCommunities() {
        Map<String, CommunityDefinition> result = new LinkedHashMap<>();
        Map<String, Object> root = loadMap("communities.yml");
        for (Map.Entry<String, Object> entry : map(root.get("communities")).entrySet()) {
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
            result.put(id, new CommunityDefinition(
                    id,
                    string(data.get("display-name"), id),
                    string(data.get("short-name"), id.toUpperCase(Locale.ROOT)),
                    string(data.get("prefix"), ""),
                    string(data.get("color"), "white"),
                    spawnPoint,
                    enumValue(VisibilityScope.class, data.get("visibility-scope"), VisibilityScope.COMMUNITY),
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
