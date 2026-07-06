package panetina.elarion.addons.quests.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import panetina.elarion.addons.quests.model.QuestActorDefinition;
import panetina.elarion.addons.quests.model.QuestConditionDefinition;
import panetina.elarion.addons.quests.model.QuestConsequenceDefinition;
import panetina.elarion.addons.quests.model.QuestDefinition;
import panetina.elarion.addons.quests.model.QuestEndingDefinition;
import panetina.elarion.addons.quests.model.QuestEvidenceDefinition;
import panetina.elarion.addons.quests.model.QuestStageDefinition;
import panetina.elarion.addons.quests.model.QuestVariableDefinition;
import panetina.elarion.addons.quests.model.QuestVariableScope;
import panetina.elarion.addons.quests.model.QuestVariableType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public final class QuestConfigLoader {
    private final Logger logger;
    private final Path rootOverride;
    private final Predicate<String> conditionExists;
    private final Predicate<String> actionExists;
    private Path root;

    public QuestConfigLoader(Logger logger) {
        this(logger, null);
    }

    public QuestConfigLoader(Logger logger, Path rootOverride) {
        this(logger, rootOverride, id -> true, id -> true);
    }

    public QuestConfigLoader(
            Logger logger,
            Path rootOverride,
            Predicate<String> conditionExists,
            Predicate<String> actionExists
    ) {
        this.logger = logger;
        this.rootOverride = rootOverride;
        this.conditionExists = conditionExists == null ? id -> true : conditionExists;
        this.actionExists = actionExists == null ? id -> true : actionExists;
    }

    public Map<String, QuestDefinition> load() {
        this.root = rootOverride == null ? defaultRoot() : rootOverride;
        writeDefaults();
        List<String> errors = new ArrayList<>();
        Map<String, QuestDefinition> result = new LinkedHashMap<>();
        Path directory = root.resolve("questlines");
        if (Files.notExists(directory)) return result;
        try (var stream = Files.list(directory)) {
            stream.sorted().forEach(path -> {
                QuestDefinition definition = null;
                if (Files.isDirectory(path)) {
                    definition = packageQuest(path, errors);
                } else if (isYaml(path)) {
                    definition = legacyQuest(path, errors);
                }
                if (definition != null) {
                    QuestDefinition previous = result.put(definition.id(), definition);
                    if (previous != null) {
                        errors.add("duplicate quest id " + definition.id());
                    }
                }
            });
        } catch (IOException exception) {
            errors.add("Failed to list questline configs: " + exception.getMessage());
        }
        validate(result, errors);
        if (!errors.isEmpty()) throw new QuestConfigException(errors);
        logger.info("Loaded {} questline definitions", result.size());
        return Map.copyOf(result);
    }

    public Path root() {
        return root;
    }

    private Path defaultRoot() {
        return FabricLoader.getInstance().getConfigDir().resolve("elarion/addons/quests");
    }

    private void writeDefaults() {
        try {
            Files.createDirectories(root.resolve("questlines"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create quest config directory", exception);
        }
    }

    private QuestDefinition legacyQuest(Path path, List<String> errors) {
        Map<String, Object> value = map(readYaml(path, errors), path.toString(), errors);
        if (value.isEmpty()) return null;
        String id = string(value, "id", stripExtension(path.getFileName().toString()));
        return questFromSections(
                id,
                value,
                childSection(value, "actors"),
                childSection(value, "variables"),
                childSection(value, "stages"),
                childSection(value, "evidence"),
                childSection(value, "endings"),
                childSection(value, "conditions"),
                childSection(value, "consequences"),
                childSection(value, "authoring"),
                errors);
    }

    private QuestDefinition packageQuest(Path directory, List<String> errors) {
        Map<String, Object> quest = readOptionalMap(directory.resolve("quest.yml"), "quest package " + directory.getFileName(), errors);
        if (quest.isEmpty()) {
            errors.add("quest package " + directory.getFileName() + ": missing quest.yml");
            return null;
        }
        String id = string(quest, "id", directory.getFileName().toString());
        return questFromSections(
                id,
                quest,
                section(readOptionalMap(directory.resolve("actors.yml"), "quest " + id + " actors.yml", errors), "actors"),
                section(readOptionalMap(directory.resolve("variables.yml"), "quest " + id + " variables.yml", errors), "variables"),
                section(readOptionalMap(directory.resolve("stages.yml"), "quest " + id + " stages.yml", errors), "stages"),
                section(readOptionalMap(directory.resolve("evidence.yml"), "quest " + id + " evidence.yml", errors), "evidence"),
                section(readOptionalMap(directory.resolve("endings.yml"), "quest " + id + " endings.yml", errors), "endings"),
                section(readOptionalMap(directory.resolve("conditions.yml"), "quest " + id + " conditions.yml", errors), "conditions"),
                section(readOptionalMap(directory.resolve("consequences.yml"), "quest " + id + " consequences.yml", errors), "consequences"),
                readOptionalMap(directory.resolve("authoring.yml"), "quest " + id + " authoring.yml", errors),
                errors);
    }

    private QuestDefinition questFromSections(
            String id,
            Map<String, Object> quest,
            Map<String, Object> actors,
            Map<String, Object> variables,
            Map<String, Object> stages,
            Map<String, Object> evidence,
            Map<String, Object> endings,
            Map<String, Object> conditions,
            Map<String, Object> consequences,
            Map<String, Object> authoring,
            List<String> errors
    ) {
        return new QuestDefinition(
                id,
                string(quest, "display-name", id),
                string(quest, "description", ""),
                string(quest, "scope", "realm").toLowerCase(Locale.ROOT),
                string(quest, "root-stage", ""),
                string(quest, "version", ""),
                list(quest.get("tags")).stream().map(String::valueOf).toList(),
                actors(actors, errors),
                variables(variables, errors),
                stages(stages, errors),
                evidence(evidence, errors),
                endings(endings, errors),
                conditions(conditions, errors),
                consequences(consequences, errors),
                stringMap(authoring),
                stringMap(map(quest.get("metadata"), "quest " + id + " metadata", errors)));
    }

    private Map<String, QuestActorDefinition> actors(Map<String, Object> raw, List<String> errors) {
        Map<String, QuestActorDefinition> result = new LinkedHashMap<>();
        raw.forEach((id, object) -> {
            Map<String, Object> value = map(object, "quest actor " + id, errors);
            List<String> allowed = list(value.get("allowed-npcs")).stream().map(String::valueOf).toList();
            if (allowed.isEmpty()) {
                allowed = list(value.get("npc-definitions")).stream().map(String::valueOf).toList();
            }
            String npc = string(value, "npc", "");
            result.put(id, new QuestActorDefinition(
                    id,
                    npc,
                    string(value, "display-name", id),
                    string(value, "role", ""),
                    allowed.isEmpty() && !npc.isBlank() ? List.of(npc) : allowed,
                    bool(value, "required", true)));
        });
        return result;
    }

    private Map<String, QuestVariableDefinition> variables(Map<String, Object> raw, List<String> errors) {
        Map<String, QuestVariableDefinition> result = new LinkedHashMap<>();
        raw.forEach((id, object) -> {
            Map<String, Object> value = map(object, "quest variable " + id, errors);
            try {
                QuestVariableType type = QuestVariableType.parse(string(value, "type", "string"));
                result.put(id, new QuestVariableDefinition(
                        id,
                        QuestVariableScope.parse(string(value, "scope", "shared")),
                        type,
                        string(value, "default", defaultValue(type))));
            } catch (IllegalArgumentException exception) {
                errors.add("quest variable " + id + ": " + exception.getMessage());
            }
        });
        return result;
    }

    private Map<String, QuestStageDefinition> stages(Map<String, Object> raw, List<String> errors) {
        Map<String, QuestStageDefinition> result = new LinkedHashMap<>();
        raw.forEach((id, object) -> {
            Map<String, Object> value = map(object, "quest stage " + id, errors);
            result.put(id, new QuestStageDefinition(
                    id,
                    string(value, "display-name", id),
                    string(value, "description", ""),
                    string(value, "objective", ""),
                    list(value.get("next")).stream().map(String::valueOf).toList(),
                    stringMap(map(value.get("metadata"), "quest stage " + id + " metadata", errors))));
        });
        return result;
    }

    private Map<String, QuestEvidenceDefinition> evidence(Map<String, Object> raw, List<String> errors) {
        Map<String, QuestEvidenceDefinition> result = new LinkedHashMap<>();
        raw.forEach((id, object) -> {
            Map<String, Object> value = map(object, "quest evidence " + id, errors);
            result.put(id, new QuestEvidenceDefinition(
                    id,
                    string(value, "display-name", id),
                    string(value, "description", ""),
                    string(value, "icon", "")));
        });
        return result;
    }

    private Map<String, QuestEndingDefinition> endings(Map<String, Object> raw, List<String> errors) {
        Map<String, QuestEndingDefinition> result = new LinkedHashMap<>();
        raw.forEach((id, object) -> {
            Map<String, Object> value = map(object, "quest ending " + id, errors);
            result.put(id, new QuestEndingDefinition(
                    id,
                    string(value, "display-name", id),
                    string(value, "description", ""),
                    stringMap(map(value.get("shrine-display-names"), "quest ending " + id + " shrine-display-names", errors))));
        });
        return result;
    }

    private Map<String, QuestConditionDefinition> conditions(Map<String, Object> raw, List<String> errors) {
        Map<String, QuestConditionDefinition> result = new LinkedHashMap<>();
        raw.forEach((id, object) -> {
            Map<String, Object> value = map(object, "quest condition " + id, errors);
            result.put(id, new QuestConditionDefinition(
                    id,
                    string(value, "type", ""),
                    parameterMap(value, "type")));
        });
        return result;
    }

    private Map<String, QuestConsequenceDefinition> consequences(Map<String, Object> raw, List<String> errors) {
        Map<String, QuestConsequenceDefinition> result = new LinkedHashMap<>();
        raw.forEach((id, object) -> {
            Map<String, Object> value = map(object, "quest consequence " + id, errors);
            result.put(id, new QuestConsequenceDefinition(
                    id,
                    string(value, "action", ""),
                    parameterMap(value, "action")));
        });
        return result;
    }

    private void validate(Map<String, QuestDefinition> definitions, List<String> errors) {
        for (QuestDefinition definition : definitions.values()) {
            String owner = "quest " + definition.id();
            if (definition.id().isBlank()) errors.add(owner + ": id cannot be blank");
            if (definition.stages().isEmpty()) errors.add(owner + ": at least one stage is required");
            if (!definition.rootStage().isBlank() && !definition.stages().containsKey(definition.rootStage())) {
                errors.add(owner + ": root-stage " + definition.rootStage() + " is not defined");
            }
            if (!List.of("realm", "global", "world", "player").contains(definition.scope())) {
                errors.add(owner + ": scope must be one of realm, global, world, player");
            }
            definition.stages().values().forEach(stage -> stage.next().stream()
                    .filter(next -> !definition.stages().containsKey(next))
                    .forEach(next -> errors.add(owner + ": stage " + stage.id() + " references unknown next stage " + next)));
            definition.actors().values().stream()
                    .filter(actor -> actor.id().isBlank())
                    .findAny()
                    .ifPresent(actor -> errors.add(owner + ": actor id cannot be blank"));
            definition.variables().values().stream()
                    .filter(variable -> variable.id().isBlank())
                    .findAny()
                    .ifPresent(variable -> errors.add(owner + ": variable id cannot be blank"));
            definition.endings().values().stream()
                    .filter(ending -> ending.id().isBlank())
                    .findAny()
                    .ifPresent(ending -> errors.add(owner + ": ending id cannot be blank"));
            validateConditions(definition, owner, errors);
            validateConsequences(definition, owner, errors);
        }
    }

    private void validateConditions(QuestDefinition definition, String owner, List<String> errors) {
        for (QuestConditionDefinition condition : definition.conditions().values()) {
            if (condition.id().isBlank()) errors.add(owner + ": condition id cannot be blank");
            if (condition.type().isBlank()) {
                errors.add(owner + ": condition " + condition.id() + " type cannot be blank");
            } else if (!conditionExists.test(condition.type())) {
                errors.add(owner + ": condition " + condition.id() + " unknown condition type " + condition.type());
            }
            String variable = condition.parameters().getOrDefault("variable", condition.parameters().getOrDefault("id", ""));
            if ((condition.type().endsWith("variable_equals") || condition.type().endsWith("variable_at_least"))
                    && !definition.variables().containsKey(variable)) {
                errors.add(owner + ": condition " + condition.id() + " references unknown variable " + variable);
            }
            String evidence = condition.parameters().getOrDefault("evidence", condition.parameters().getOrDefault("id", ""));
            if (condition.type().endsWith("has_evidence") && !definition.evidence().containsKey(evidence)) {
                errors.add(owner + ": condition " + condition.id() + " references unknown evidence " + evidence);
            }
            String ending = condition.parameters().getOrDefault("ending", condition.parameters().getOrDefault("id", ""));
            if (condition.type().endsWith("ending_is") && !definition.endings().containsKey(ending)) {
                errors.add(owner + ": condition " + condition.id() + " references unknown ending " + ending);
            }
            String stage = condition.parameters().getOrDefault("stage", "");
            if (condition.type().endsWith("stage_is") && !definition.stages().containsKey(stage)) {
                errors.add(owner + ": condition " + condition.id() + " references unknown stage " + stage);
            }
        }
    }

    private void validateConsequences(QuestDefinition definition, String owner, List<String> errors) {
        for (QuestConsequenceDefinition consequence : definition.consequences().values()) {
            if (consequence.id().isBlank()) errors.add(owner + ": consequence id cannot be blank");
            if (consequence.action().isBlank()) {
                errors.add(owner + ": consequence " + consequence.id() + " action cannot be blank");
            } else if (!actionExists.test(consequence.action())) {
                errors.add(owner + ": consequence " + consequence.id() + " unknown action " + consequence.action());
            }
        }
    }

    private Map<String, Object> readOptionalMap(Path path, String owner, List<String> errors) {
        if (Files.notExists(path)) return Map.of();
        return map(readYaml(path, errors), owner, errors);
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

    private static Map<String, Object> section(Map<String, Object> value, String key) {
        Object child = value.get(key);
        if (child instanceof Map<?, ?> raw) {
            Map<String, Object> result = new LinkedHashMap<>();
            raw.forEach((childKey, childValue) -> result.put(String.valueOf(childKey), childValue));
            return result;
        }
        return value;
    }

    private static Map<String, Object> childSection(Map<String, Object> value, String key) {
        Object child = value.get(key);
        if (child instanceof Map<?, ?> raw) {
            Map<String, Object> result = new LinkedHashMap<>();
            raw.forEach((childKey, childValue) -> result.put(String.valueOf(childKey), childValue));
            return result;
        }
        return Map.of();
    }

    private static List<?> list(Object value) {
        return value instanceof List<?> list ? list : List.of();
    }

    private static String string(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static boolean bool(Map<String, Object> map, String key, boolean fallback) {
        Object value = map.get(key);
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }

    private static Map<String, String> stringMap(Map<String, Object> map) {
        Map<String, String> result = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            if (value != null) result.put(key, String.valueOf(value));
        });
        return result;
    }

    private static Map<String, String> parameterMap(Map<String, Object> map, String reservedKey) {
        Map<String, Object> parameters = map(map.get("parameters"), "parameters", new ArrayList<>());
        if (!parameters.isEmpty()) return stringMap(parameters);
        Map<String, String> result = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            if (!"id".equals(key) && !reservedKey.equals(key) && value != null) {
                result.put(key, String.valueOf(value));
            }
        });
        return result;
    }

    private static boolean isYaml(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".yml") || name.endsWith(".yaml");
    }

    private static String defaultValue(QuestVariableType type) {
        return switch (type) {
            case BOOLEAN -> "false";
            case INTEGER -> "0";
            case STRING -> "";
        };
    }

    private static String stripExtension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? name : name.substring(0, index);
    }
}
