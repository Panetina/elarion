package panetina.elarion.addons.npcs.config;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.addons.npcs.model.DialogueAction;
import panetina.elarion.addons.npcs.model.DialogueCondition;
import panetina.elarion.addons.npcs.model.DialogueDefinition;
import panetina.elarion.addons.npcs.model.DialogueNode;
import panetina.elarion.addons.npcs.model.DialogueOption;
import panetina.elarion.addons.npcs.model.DialoguePrompt;
import panetina.elarion.addons.npcs.model.DialogueTextVariant;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.NpcUiConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class NpcConfigLoader {
    private final Logger logger;
    private final Path rootOverride;
    private Path root;
    private ElarionApi api;

    public NpcConfigLoader(Logger logger) {
        this(logger, null);
    }

    public NpcConfigLoader(Logger logger, Path rootOverride) {
        this.logger = logger;
        this.rootOverride = rootOverride;
    }

    public NpcConfig load(ElarionApi api) {
        this.api = api;
        this.root = rootOverride == null ? defaultRoot() : rootOverride;
        writeDefaults();
        List<String> errors = new ArrayList<>();
        Map<String, NpcSkinProfile> skins = loadSkins(errors);
        Map<String, NpcPortraitProfile> portraits = loadPortraits(errors);
        Map<String, DialogueDefinition> dialogues = loadDialogues(errors);
        Map<String, NpcDefinition> npcs = loadNpcs(errors);
        NpcUiConfig ui = loadUi(errors);
        NpcConfigValidator.validate(
                npcs, skins, portraits, dialogues,
                id -> api.registries().conditions().contains(id),
                id -> api.registries().actions().contains(id),
                errors);
        if (!errors.isEmpty()) throw new NpcConfigException(errors);
        logger.info("Loaded {} NPC definitions, {} skin profiles, {} portrait profiles, and {} dialogues",
                npcs.size(), skins.size(), portraits.size(), dialogues.size());
        return new NpcConfig(npcs, skins, portraits, dialogues, ui);
    }

    public Path root() {
        return root;
    }

    private Path defaultRoot() {
        return AddonConfigFiles.writeDefault("npcs", "npcs.yml", NpcConfigDefaults.NPCS).getParent();
    }

    private void writeDefaults() {
        try {
            Files.createDirectories(root.resolve("dialogues"));
            writeIfMissing(root.resolve("npcs.yml"), NpcConfigDefaults.NPCS);
            writeIfMissing(root.resolve("skins.yml"), NpcConfigDefaults.SKINS);
            writeIfMissing(root.resolve("portraits.yml"), NpcConfigDefaults.PORTRAITS);
            writeIfMissing(root.resolve("ui.yml"), NpcConfigDefaults.UI);
            writeIfMissing(root.resolve("dialogues").resolve("worldheart_banker.yml"),
                    NpcConfigDefaults.BANKER_DIALOGUE);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create NPC config defaults", exception);
        }
    }

    private static void writeIfMissing(Path path, String content) throws IOException {
        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        }
    }

    private Map<String, NpcDefinition> loadNpcs(List<String> errors) {
        Map<String, Object> rootMap = map(readYaml(root.resolve("npcs.yml"), errors), "npcs.yml", errors);
        Map<String, NpcDefinition> result = new LinkedHashMap<>();
        Map<String, Object> npcs = map(rootMap.get("npcs"), "npcs.yml npcs", errors);
        for (Map.Entry<String, Object> entry : npcs.entrySet()) {
            Map<String, Object> value = map(entry.getValue(), "npc " + entry.getKey(), errors);
            result.put(entry.getKey(), new NpcDefinition(
                    entry.getKey(),
                    text(value, "display-name", entry.getKey()),
                    text(value, "description", ""),
                    string(value, "skin", ""),
                    string(value, "portrait", ""),
                    string(value, "dialogue", ""),
                    list(value.get("tags")).stream().map(String::valueOf).toList(),
                    string(value, "required-ability", ""),
                    decimal(value, "interaction-range-blocks", 0.0D),
                    bool(value, "enabled", true)));
        }
        return result;
    }

    private Map<String, NpcSkinProfile> loadSkins(List<String> errors) {
        Map<String, Object> rootMap = map(readYaml(root.resolve("skins.yml"), errors), "skins.yml", errors);
        Map<String, NpcSkinProfile> result = new LinkedHashMap<>();
        Map<String, Object> skins = map(rootMap.get("skins"), "skins.yml skins", errors);
        for (Map.Entry<String, Object> entry : skins.entrySet()) {
            Map<String, Object> value = map(entry.getValue(), "skin " + entry.getKey(), errors);
            result.put(entry.getKey(), new NpcSkinProfile(
                    entry.getKey(),
                    text(value, "display-name", entry.getKey()),
                    string(value, "type", "placeholder").toLowerCase(Locale.ROOT),
                    string(value, "texture", ""),
                    string(value, "player-name", ""),
                    string(value, "fallback-type", "placeholder").toLowerCase(Locale.ROOT),
                    string(value, "fallback-texture", ""),
                    string(value, "adapter", "")));
        }
        return result;
    }

    private Map<String, NpcPortraitProfile> loadPortraits(List<String> errors) {
        Map<String, Object> rootMap = map(readYaml(root.resolve("portraits.yml"), errors), "portraits.yml", errors);
        Map<String, NpcPortraitProfile> result = new LinkedHashMap<>();
        Map<String, Object> portraits = map(rootMap.get("portraits"), "portraits.yml portraits", errors);
        for (Map.Entry<String, Object> entry : portraits.entrySet()) {
            Map<String, Object> value = map(entry.getValue(), "portrait " + entry.getKey(), errors);
            result.put(entry.getKey(), new NpcPortraitProfile(
                    entry.getKey(),
                    text(value, "display-name", entry.getKey()),
                    string(value, "type", "placeholder").toLowerCase(Locale.ROOT),
                    string(value, "texture", ""),
                    string(value, "player-name", ""),
                    string(value, "fallback-type", "placeholder").toLowerCase(Locale.ROOT),
                    string(value, "fallback-texture", "")));
        }
        return result;
    }

    private Map<String, DialogueDefinition> loadDialogues(List<String> errors) {
        Map<String, DialogueDefinition> result = new LinkedHashMap<>();
        Path directory = root.resolve("dialogues");
        if (Files.notExists(directory)) return result;
        try (var stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".yml")
                            || path.getFileName().toString().endsWith(".yaml"))
                    .sorted()
                    .forEach(path -> {
                        Map<String, Object> value = map(readYaml(path, errors), path.toString(), errors);
                        String id = string(value, "id", dialogueId(directory, path));
                        Map<String, DialogueNode> nodes = new LinkedHashMap<>();
                        map(value.get("nodes"), "dialogue " + id + " nodes", errors)
                                .forEach((nodeId, rawNode) -> {
                                    Map<String, Object> node = map(rawNode, "dialogue node " + nodeId, errors);
                                    nodes.put(nodeId, new DialogueNode(
                                            nodeId,
                                            text(node, "text", ""),
                                            string(node, "sound", ""),
                                            string(node, "voice", ""),
                                            conditions(node.get("conditions"), errors),
                                            variants(node.get("variants"), errors),
                                            options(node.get("options"), errors)));
                                });
                        result.put(id, new DialogueDefinition(id, string(value, "root", ""), nodes));
                    });
        } catch (IOException exception) {
            errors.add("Failed to list NPC dialogue configs: " + exception.getMessage());
        }
        return result;
    }

    static String dialogueId(Path root, Path path) {
        Path relative = root.relativize(path);
        String id = stripExtension(relative.toString());
        return id.replace('\\', '/');
    }

    private NpcUiConfig loadUi(List<String> errors) {
        NpcUiConfig defaults = NpcUiConfig.defaults();
        Map<String, Object> rootMap = map(readYaml(root.resolve("ui.yml"), errors), "ui.yml", errors);
        return new NpcUiConfig(
                integer(rootMap, "panel-width", defaults.panelWidth()),
                integer(rootMap, "min-panel-height", defaults.minPanelHeight()),
                integer(rootMap, "max-panel-height", defaults.maxPanelHeight()),
                integer(rootMap, "minimum-ui-scale-percent", defaults.minimumUiScalePercent()),
                integer(rootMap, "option-row-height", defaults.optionRowHeight()),
                integer(rootMap, "visible-option-rows", defaults.visibleOptionRows()),
                integer(rootMap, "scrollbar-width", defaults.scrollbarWidth()),
                integer(rootMap, "padding", defaults.padding()),
                integer(rootMap, "button-height", defaults.buttonHeight()),
                integer(rootMap, "compact-button-height", defaults.compactButtonHeight()),
                integer(rootMap, "button-gap", defaults.buttonGap()),
                integer(rootMap, "content-gap", defaults.contentGap()),
                integer(rootMap, "npc-row-height", defaults.npcRowHeight()),
                integer(rootMap, "player-row-height", defaults.playerRowHeight()),
                integer(rootMap, "option-columns-wide", defaults.optionColumnsWide()),
                integer(rootMap, "portrait-size", defaults.portraitSize()),
                integer(rootMap, "player-portrait-size", defaults.playerPortraitSize()),
                bool(rootMap, "show-portrait-reference", defaults.showPortraitReference()),
                bool(rootMap, "show-relation-bar", defaults.showRelationBar()),
                bool(rootMap, "show-action-feedback-in-gui", defaults.showActionFeedbackInGui()),
                bool(rootMap, "also-send-action-feedback-to-chat", defaults.alsoSendActionFeedbackToChat()),
                decimal(rootMap, "default-interaction-range-blocks", defaults.defaultInteractionRangeBlocks()),
                bool(rootMap, "typing-enabled", defaults.typingEnabled()),
                integer(rootMap, "typing-characters-per-second", defaults.typingCharactersPerSecond()),
                bool(rootMap, "typing-click-completes", defaults.typingClickCompletes()),
                bool(rootMap, "typing-sound-enabled", defaults.typingSoundEnabled()),
                integer(rootMap, "typing-sound-interval-characters", defaults.typingSoundIntervalCharacters()));
    }

    private List<DialogueOption> options(Object raw, List<String> errors) {
        List<DialogueOption> result = new ArrayList<>();
        int index = 0;
        for (Object object : list(raw)) {
            Map<String, Object> option = map(object, "dialogue option", errors);
            String id = string(option, "id", "option_" + index++);
            String text = string(option, "text", id);
            result.add(new DialogueOption(
                    id,
                    text(option, "button-text", text),
                    text(option, "player-text", text),
                    string(option, "sound", ""),
                    string(option, "voice", ""),
                    string(option, "next", ""),
                    conditions(option.get("conditions"), errors),
                    actions(option.get("actions"), errors),
                    prompt(option.get("prompt"), errors),
                    bool(option, "close", false)));
        }
        return result;
    }

    private List<DialogueTextVariant> variants(Object raw, List<String> errors) {
        List<DialogueTextVariant> result = new ArrayList<>();
        int index = 0;
        for (Object object : list(raw)) {
            Map<String, Object> variant = map(object, "dialogue text variant", errors);
            String id = string(variant, "id", "variant_" + index++);
            result.add(new DialogueTextVariant(
                    id,
                    text(variant, "text", ""),
                    string(variant, "sound", ""),
                    string(variant, "voice", ""),
                    conditions(variant.get("conditions"), errors)));
        }
        return result;
    }

    private DialoguePrompt prompt(Object raw, List<String> errors) {
        if (raw == null) return DialoguePrompt.NONE;
        Map<String, Object> value = map(raw, "dialogue prompt", errors);
        return new DialoguePrompt(
                string(value, "type", ""),
                text(value, "question", ""),
                string(value, "action", ""),
                integer(value, "max-digits", 10),
                longValue(value, "min-amount", 1L),
                longValue(value, "max-amount", 0L));
    }

    private List<DialogueCondition> conditions(Object raw, List<String> errors) {
        List<DialogueCondition> result = new ArrayList<>();
        for (Object object : list(raw)) {
            Map<String, Object> value = map(object, "dialogue condition", errors);
            result.add(new DialogueCondition(string(value, "type", ""), stringMap(value)));
        }
        return result;
    }

    private List<DialogueAction> actions(Object raw, List<String> errors) {
        List<DialogueAction> result = new ArrayList<>();
        for (Object object : list(raw)) {
            Map<String, Object> value = map(object, "dialogue action", errors);
            result.add(new DialogueAction(
                    string(value, "type", ""),
                    stringMap(value),
                    bool(value, "history-worthy", false)));
        }
        return result;
    }

    private Object readYaml(Path path, List<String> errors) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return new Yaml(new SafeConstructor(new LoaderOptions())).load(content);
        } catch (IOException | RuntimeException exception) {
            errors.add("Failed to read " + path + ": " + exception.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value, String owner, List<String> errors) {
        if (value == null) return Map.of();
        if (value instanceof Map<?, ?> raw) {
            Map<String, Object> result = new LinkedHashMap<>();
            raw.forEach((key, child) -> result.put(String.valueOf(key), child));
            return result;
        }
        errors.add(owner + ": expected object");
        return Map.of();
    }

    private static List<?> list(Object value) {
        return value instanceof List<?> list ? list : List.of();
    }

    private static String string(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private String text(Map<String, Object> map, String key, String fallback) {
        return api.serverIdentity().replace(string(map, key, fallback));
    }

    private static boolean bool(Map<String, Object> map, String key, boolean fallback) {
        Object value = map.get(key);
        if (value instanceof Boolean bool) return bool;
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value).toLowerCase(Locale.ROOT));
    }

    private static int integer(Map<String, Object> map, String key, int fallback) {
        Object value = map.get(key);
        if (value instanceof Number number) return number.intValue();
        if (value == null) return fallback;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static long longValue(Map<String, Object> map, String key, long fallback) {
        Object value = map.get(key);
        if (value instanceof Number number) return number.longValue();
        if (value == null) return fallback;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static double decimal(Map<String, Object> map, String key, double fallback) {
        Object value = map.get(key);
        if (value instanceof Number number) return number.doubleValue();
        if (value == null) return fallback;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static int color(Map<String, Object> map, String key, int fallback) {
        Object value = map.get(key);
        if (value == null) return fallback;
        String text = String.valueOf(value).trim();
        try {
            return text.startsWith("0x") || text.startsWith("0X")
                    ? (int) Long.parseLong(text.substring(2), 16)
                    : Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static Map<String, String> stringMap(Map<String, Object> map) {
        Map<String, String> result = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            if (!"conditions".equals(key) && !"actions".equals(key) && !"history-worthy".equals(key)
                    && value != null) {
                result.put(key, String.valueOf(value));
            }
        });
        return result;
    }

    private static String stripExtension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? name : name.substring(0, index);
    }
}
