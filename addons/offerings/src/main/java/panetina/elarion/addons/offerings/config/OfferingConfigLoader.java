package panetina.elarion.addons.offerings.config;

import net.minecraft.util.Identifier;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.offerings.model.OfferingMilestone;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingRequirement;
import panetina.elarion.addons.offerings.model.OfferingScope;
import panetina.elarion.addons.offerings.model.OfferingPresentation;
import panetina.elarion.addons.offerings.model.OfferingUiConfig;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionApi;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class OfferingConfigLoader {
    private OfferingConfigLoader() {
    }

    public static Map<String, OfferingProjectDefinition> load(ElarionApi api) {
        Path root = ensureDefaults();
        Path projects = root.resolve("projects");
        Map<String, OfferingProjectDefinition> definitions = new LinkedHashMap<>();
        Yaml yaml = new Yaml();
        try (var files = Files.list(projects)) {
            for (Path file : files.filter(path -> path.getFileName().toString().endsWith(".yml")).sorted().toList()) {
                OfferingProjectDefinition definition = readProject(api, yaml, file);
                OfferingConfigValidator.validate(definition, file,
                        id -> api.registries().milestoneEvents().contains(id),
                        id -> api.registries().actions().contains(id));
                if (definitions.put(definition.id(), definition) != null) {
                    throw new OfferingConfigException("Duplicate offering project id " + definition.id());
                }
            }
        } catch (IOException exception) {
            throw new OfferingConfigException("Failed to read offering projects", exception);
        }
        if (definitions.isEmpty()) throw new OfferingConfigException("No offering project definitions loaded.");
        return Map.copyOf(definitions);
    }

    @SuppressWarnings("unchecked")
    public static OfferingUiConfig loadUi(ElarionApi api) {
        Path root = ensureDefaults();
        Yaml yaml = new Yaml();
        Path file = root.resolve("ui.yml");
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = yaml.load(reader);
            if (!(loaded instanceof Map<?, ?> raw)) throw new OfferingConfigException(file + " must be a mapping.");
            Map<String, Object> map = (Map<String, Object>) raw;
            OfferingUiConfig defaults = OfferingUiConfig.defaults();
            OfferingUiConfig config = new OfferingUiConfig(
                    string(map, "theme-variant", defaults.themeVariant()),
                    integer(map, "logical-width", defaults.logicalWidth()),
                    integer(map, "logical-height", defaults.logicalHeight()),
                    integer(map, "minimum-scale-percent", defaults.minimumScalePercent()),
                    integer(map, "summary-width", defaults.summaryWidth()),
                    integer(map, "tab-height", defaults.tabHeight()),
                    integer(map, "row-height", defaults.rowHeight()),
                    integer(map, "icon-size", defaults.iconSize()),
                    integer(map, "close-button-width", defaults.closeButtonWidth()),
                    api.serverIdentity().replace(string(map, "rewards-placeholder", defaults.rewardsPlaceholder())),
                    api.serverIdentity().replace(string(map, "history-placeholder", defaults.historyPlaceholder())),
                    api.serverIdentity().replace(string(map, "contribution-placeholder",
                            defaults.contributionPlaceholder())),
                    api.serverIdentity().replace(string(map, "event-title", defaults.eventTitle())),
                    api.serverIdentity().replace(string(map, "event-body", defaults.eventBody())),
                    api.serverIdentity().replace(string(map, "event-locked-body", defaults.eventLockedBody())));
            validateUi(api, config, file);
            return config;
        } catch (IOException exception) {
            throw new OfferingConfigException("Failed to read " + file, exception);
        }
    }

    private static Path ensureDefaults() {
        Path society = AddonConfigFiles.writeDefault("offerings", "society.yml", OfferingConfigDefaults.SOCIETY);
        Path root = society.getParent();
        AddonConfigFiles.writeDefault("offerings", "ui.yml", OfferingConfigDefaults.UI);
        Path projects = root.resolve("projects");
        try {
            Files.createDirectories(projects);
            Path councilHall = projects.resolve("council_hall.yml");
            if (Files.notExists(councilHall)) {
                Files.writeString(councilHall, OfferingConfigDefaults.COUNCIL_HALL, StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new OfferingConfigException("Failed to create offering project defaults", exception);
        }
        return root;
    }

    @SuppressWarnings("unchecked")
    private static OfferingProjectDefinition readProject(ElarionApi api, Yaml yaml, Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = yaml.load(reader);
            if (!(loaded instanceof Map<?, ?> raw)) {
                throw new OfferingConfigException(file + " must contain a YAML mapping.");
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            String id = string(map, "id");
            return new OfferingProjectDefinition(
                    id,
                    api.serverIdentity().replace(string(map, "display-name")),
                    api.serverIdentity().replace(string(map, "description")),
                    bool(map, "enabled", true),
                    OfferingScope.parse(string(map, "scope")),
                    bool(map, "repeatable", false),
                    bool(map, "allow-multiple-instances", false),
                    requirements(map.get("requirements")),
                    milestones(map.get("milestones")),
                    presentation(api, map.get("presentation")),
                    levels(api, map.get("levels"))
            );
        } catch (IOException exception) {
            throw new OfferingConfigException("Failed to read " + file, exception);
        } catch (RuntimeException exception) {
            if (exception instanceof OfferingConfigException config) throw config;
            throw new OfferingConfigException("Invalid offering project " + file + ": " + exception.getMessage(),
                    exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static OfferingPresentation presentation(ElarionApi api, Object raw) {
        Map<String, Object> map = raw instanceof Map<?, ?> value ? (Map<String, Object>) value : Map.of();
        OfferingPresentation defaults = OfferingPresentation.defaults();
        return new OfferingPresentation(
                api.serverIdentity().replace(string(map, "level-text", defaults.levelText())),
                string(map, "icon", defaults.icon()));
    }

    @SuppressWarnings("unchecked")
    private static List<panetina.elarion.addons.offerings.model.OfferingProjectLevel> levels(
            ElarionApi api,
            Object value
    ) {
        if (!(value instanceof List<?> list)) return List.of();
        List<panetina.elarion.addons.offerings.model.OfferingProjectLevel> result = new ArrayList<>();
        for (Object entry : list) {
            if (!(entry instanceof Map<?, ?> raw)) continue;
            Map<String, Object> map = (Map<String, Object>) raw;
            result.add(new panetina.elarion.addons.offerings.model.OfferingProjectLevel(
                    string(map, "id"),
                    api.serverIdentity().replace(string(map, "display-name")),
                    api.serverIdentity().replace(string(map, "description")),
                    requirements(map.get("requirements")),
                    milestones(map.get("milestones")),
                    presentation(api, map.get("presentation"))));
        }
        return List.copyOf(result);
    }

    private static void validateUi(ElarionApi api, OfferingUiConfig config, Path file) {
        List<String> errors = new ArrayList<>();
        if (!api.uiThemes().current().variants().containsKey(config.themeVariant())) {
            errors.add("unknown theme variant " + config.themeVariant());
        }
        if (config.logicalWidth() < 360 || config.logicalWidth() > 960) errors.add("logical-width out of range");
        if (config.logicalHeight() < 260 || config.logicalHeight() > 720) errors.add("logical-height out of range");
        if (config.minimumScalePercent() < 25 || config.minimumScalePercent() > 100) {
            errors.add("minimum-scale-percent out of range");
        }
        if (config.summaryWidth() < 100 || config.summaryWidth() > config.logicalWidth() / 2) {
            errors.add("summary-width out of range");
        }
        if (config.rowHeight() < 16 || config.rowHeight() > 64) errors.add("row-height out of range");
        if (config.iconSize() < 16 || config.iconSize() > 96) errors.add("icon-size out of range");
        if (!errors.isEmpty()) throw new OfferingConfigException(file + ": " + String.join("; ", errors));
    }

    private static int integer(Map<?, ?> map, String key, int fallback) {
        Object value = map.get(key);
        if (value instanceof Number number) return number.intValue();
        if (value == null) return fallback;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String string(Map<?, ?> map, String key, String fallback) {
        String value = string(map, key);
        return value.isBlank() ? fallback : value;
    }

    @SuppressWarnings("unchecked")
    private static List<OfferingRequirement> requirements(Object value) {
        if (!(value instanceof Map<?, ?> raw)) return List.of();
        Map<String, Object> map = (Map<String, Object>) raw;
        List<OfferingRequirement> result = new ArrayList<>();
        Object items = map.get("items");
        if (items instanceof List<?> list) {
            for (Object entry : list) {
                if (!(entry instanceof Map<?, ?> itemRaw)) continue;
                Map<String, Object> item = (Map<String, Object>) itemRaw;
                String id = string(item, item.containsKey("tag") ? "tag" : "id");
                String prefix = item.containsKey("tag") ? "#" : "";
                result.add(new OfferingRequirement("items", prefix + id, longValue(item.get("count"), 1)));
            }
        }
        Object currency = map.get("currency");
        if (currency instanceof Map<?, ?> currencyRaw) {
            result.add(new OfferingRequirement(
                    "currency", "", longValue(currencyRaw.get("amount"), 1)));
        } else if (currency instanceof Number number) {
            result.add(new OfferingRequirement("currency", "", number.longValue()));
        }
        Object events = map.get("events");
        if (events instanceof List<?> list) {
            for (Object entry : list) {
                if (!(entry instanceof Map<?, ?> eventRaw)) continue;
                Map<String, Object> event = (Map<String, Object>) eventRaw;
                result.add(new OfferingRequirement("events", string(event, "id"), longValue(event.get("count"), 1)));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<OfferingMilestone> milestones(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        List<OfferingMilestone> result = new ArrayList<>();
        for (Object entry : list) {
            if (!(entry instanceof Map<?, ?> raw)) continue;
            Map<String, Object> map = (Map<String, Object>) raw;
            Map<String, String> params = new LinkedHashMap<>();
            Object parameters = map.get("parameters");
            if (parameters instanceof Map<?, ?> paramMap) {
                for (Map.Entry<?, ?> param : paramMap.entrySet()) {
                    params.put(String.valueOf(param.getKey()), String.valueOf(param.getValue()));
                }
            }
            result.add(new OfferingMilestone(
                    string(map, "id"),
                    string(map, "type"),
                    params));
        }
        return result;
    }

    private static String string(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static boolean bool(Map<?, ?> map, String key, boolean fallback) {
        Object value = map.get(key);
        if (value == null) return fallback;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static long longValue(Object value, long fallback) {
        if (value instanceof Number number) return number.longValue();
        if (value == null) return fallback;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
