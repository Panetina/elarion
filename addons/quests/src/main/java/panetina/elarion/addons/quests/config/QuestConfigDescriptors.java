package panetina.elarion.addons.quests.config;

import panetina.elarion.addons.quests.model.QuestDefinition;
import panetina.elarion.addons.quests.model.QuestVariableDefinition;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public final class QuestConfigDescriptors {
    private static final List<String> QUEST_SCOPES = List.of("realm", "global", "world", "player");

    private QuestConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<Collection<QuestDefinition>> definitions
    ) {
        registry.registerDomain(domain(definitions));
    }

    public static ElarionConfigDomain domain(Supplier<Collection<QuestDefinition>> definitions) {
        List<QuestDefinition> snapshot = sortedDefinitions(definitions);
        return new ElarionConfigDomain(
                "quests",
                "addons:quests",
                "Quests",
                "Data-driven quest package definitions and graph summaries.",
                List.of(
                        "config/elarion/addons/quests/questlines/*/*.yml",
                        "config/elarion/addons/quests/questlines/*.yml"),
                "/e quest reload",
                List.of(new ElarionConfigCategory(
                        "questlines",
                        "Questlines",
                        "Current loaded quest package metadata and graph summaries.",
                        questlineEntries(definitions, snapshot))));
    }

    private static List<ElarionConfigEntry<?>> questlineEntries(
            Supplier<Collection<QuestDefinition>> definitions,
            List<QuestDefinition> snapshot
    ) {
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        entries.add(intEntry(
                "questlines.count",
                "Questline Count",
                "Number of currently loaded quest packages.",
                "questlines/",
                snapshot.size(),
                () -> sortedDefinitions(definitions).size(),
                0));
        entries.add(stringEntry(
                "questlines.ids",
                "Questline IDs",
                "Comma-separated stable questline IDs currently known to Quests.",
                "questlines/",
                ids(snapshot),
                () -> ids(sortedDefinitions(definitions)),
                false));

        for (QuestDefinition quest : snapshot) {
            entries.add(questStringEntry(quest, "display-name", "Display Name",
                    "Public questline display name.", "quest.yml.display-name",
                    definitions, QuestDefinition::displayName, true));
            entries.add(questStringEntry(quest, "description", "Description",
                    "Public questline description.", "quest.yml.description",
                    definitions, QuestDefinition::description, false));
            entries.add(questStringEntry(quest, "scope", "Scope",
                    "Runtime scope used by this questline.", "quest.yml.scope",
                    definitions, QuestDefinition::scope, true, QUEST_SCOPES));
            entries.add(questStringEntry(quest, "root-stage", "Root Stage",
                    "Initial stage for new questline state.", "quest.yml.root-stage",
                    definitions, QuestDefinition::rootStage, false));
            entries.add(questStringEntry(quest, "version", "Version",
                    "Content version declared by the quest package.", "quest.yml.version",
                    definitions, QuestDefinition::version, false));
            entries.add(questStringEntry(quest, "tags", "Tags",
                    "Comma-separated questline tags.", "quest.yml.tags",
                    definitions, value -> join(value.tags()), false));

            entries.add(questIntEntry(quest, "actors.count", "Actor Count",
                    "Number of stable actor aliases.", "actors.yml.actors",
                    definitions, value -> value.actors().size()));
            entries.add(questStringEntry(quest, "actors.ids", "Actor IDs",
                    "Comma-separated actor aliases.", "actors.yml.actors",
                    definitions, value -> keys(value.actors().keySet()), false));
            entries.add(questIntEntry(quest, "actors.required-count", "Required Actor Count",
                    "Number of actors required by this package.", "actors.yml.actors",
                    definitions, QuestConfigDescriptors::requiredActorCount));

            entries.add(questIntEntry(quest, "variables.count", "Variable Count",
                    "Number of declared quest variables.", "variables.yml.variables",
                    definitions, value -> value.variables().size()));
            entries.add(questStringEntry(quest, "variables.ids", "Variable IDs",
                    "Comma-separated variable IDs.", "variables.yml.variables",
                    definitions, value -> keys(value.variables().keySet()), false));
            entries.add(questStringEntry(quest, "variables.types", "Variable Types",
                    "Variable IDs and normalized value types.", "variables.yml.variables",
                    definitions, QuestConfigDescriptors::variableTypes, false));
            entries.add(questStringEntry(quest, "variables.scopes", "Variable Scopes",
                    "Variable IDs and shared/player ownership scopes.", "variables.yml.variables",
                    definitions, QuestConfigDescriptors::variableScopes, false));

            entries.add(questIntEntry(quest, "stages.count", "Stage Count",
                    "Number of stages in the quest graph.", "stages.yml.stages",
                    definitions, value -> value.stages().size()));
            entries.add(questStringEntry(quest, "stages.ids", "Stage IDs",
                    "Comma-separated stage IDs.", "stages.yml.stages",
                    definitions, value -> keys(value.stages().keySet()), false));
            entries.add(questIntEntry(quest, "stages.edges", "Stage Edge Count",
                    "Number of configured next-stage graph edges.", "stages.yml.stages",
                    definitions, QuestConfigDescriptors::stageEdgeCount));

            entries.add(questIntEntry(quest, "evidence.count", "Evidence Count",
                    "Number of evidence definitions.", "evidence.yml.evidence",
                    definitions, value -> value.evidence().size()));
            entries.add(questStringEntry(quest, "evidence.ids", "Evidence IDs",
                    "Comma-separated evidence IDs.", "evidence.yml.evidence",
                    definitions, value -> keys(value.evidence().keySet()), false));

            entries.add(questIntEntry(quest, "endings.count", "Ending Count",
                    "Number of defined quest endings.", "endings.yml.endings",
                    definitions, value -> value.endings().size()));
            entries.add(questStringEntry(quest, "endings.ids", "Ending IDs",
                    "Comma-separated ending IDs.", "endings.yml.endings",
                    definitions, value -> keys(value.endings().keySet()), false));
            entries.add(questIntEntry(quest, "endings.shrine-projections", "Shrine Projection Count",
                    "Number of ending-owned Shrine display-name projections.", "endings.yml.endings",
                    definitions, QuestConfigDescriptors::shrineProjectionCount));

            entries.add(questIntEntry(quest, "conditions.count", "Condition Count",
                    "Number of reusable condition definitions.", "conditions.yml.conditions",
                    definitions, value -> value.conditions().size()));
            entries.add(questStringEntry(quest, "conditions.types", "Condition Types",
                    "Registered condition types referenced by this package.", "conditions.yml.conditions",
                    definitions, value -> value.conditions().values().stream()
                            .map(condition -> condition.type()).distinct().sorted().reduce(joiner()).orElse(""), false));

            entries.add(questIntEntry(quest, "consequences.count", "Consequence Count",
                    "Number of reusable scheduled consequence definitions.", "consequences.yml.consequences",
                    definitions, value -> value.consequences().size()));
            entries.add(questStringEntry(quest, "consequences.actions", "Consequence Actions",
                    "Registered action types referenced by scheduled consequences.",
                    "consequences.yml.consequences", definitions,
                    value -> value.consequences().values().stream()
                            .map(consequence -> consequence.action()).distinct().sorted().reduce(joiner()).orElse(""),
                    false));

            entries.add(questStringEntry(quest, "authoring.keys", "Authoring Keys",
                    "Editor-only authoring metadata keys.", "authoring.yml",
                    definitions, value -> keys(value.authoring().keySet()), false));
            entries.add(questStringEntry(quest, "metadata.keys", "Metadata Keys",
                    "Additional package metadata keys.", "quest.yml.metadata",
                    definitions, value -> keys(value.metadata().keySet()), false));
        }
        return entries;
    }

    private static ElarionConfigEntry<Integer> questIntEntry(
            QuestDefinition quest,
            String field,
            String label,
            String description,
            String path,
            Supplier<Collection<QuestDefinition>> definitions,
            Function<QuestDefinition, Integer> value
    ) {
        return intEntry(questId(quest, field), questLabel(quest, label), description,
                questPath(quest, path), value.apply(quest),
                () -> value.apply(currentQuest(definitions, quest)), 0);
    }

    private static ElarionConfigEntry<String> questStringEntry(
            QuestDefinition quest,
            String field,
            String label,
            String description,
            String path,
            Supplier<Collection<QuestDefinition>> definitions,
            Function<QuestDefinition, String> value,
            boolean nonBlank
    ) {
        return questStringEntry(quest, field, label, description, path, definitions, value, nonBlank, List.of());
    }

    private static ElarionConfigEntry<String> questStringEntry(
            QuestDefinition quest,
            String field,
            String label,
            String description,
            String path,
            Supplier<Collection<QuestDefinition>> definitions,
            Function<QuestDefinition, String> value,
            boolean nonBlank,
            List<String> choices
    ) {
        return stringEntry(questId(quest, field), questLabel(quest, label), description,
                questPath(quest, path), value.apply(quest),
                () -> value.apply(currentQuest(definitions, quest)), nonBlank, choices);
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id,
            String label,
            String description,
            String path,
            int defaultValue,
            Supplier<Integer> currentValue,
            int minimum
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerMinimum(path, minimum), List.of(),
                Integer.toString(minimum), "", true, false,
                ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            boolean nonBlank
    ) {
        return stringEntry(id, label, description, path, defaultValue, currentValue, nonBlank, List.of());
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            boolean nonBlank,
            List<String> choices
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(),
                choices, "", "", true, false,
                ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static QuestDefinition currentQuest(
            Supplier<Collection<QuestDefinition>> definitions,
            QuestDefinition fallback
    ) {
        return sortedDefinitions(definitions).stream()
                .filter(quest -> quest.id().equals(fallback.id()))
                .findFirst()
                .orElse(fallback);
    }

    private static List<QuestDefinition> sortedDefinitions(
            Supplier<Collection<QuestDefinition>> definitions
    ) {
        Collection<QuestDefinition> value = definitions == null ? null : definitions.get();
        if (value == null) return List.of();
        return value.stream().sorted(Comparator.comparing(QuestDefinition::id)).toList();
    }

    private static int requiredActorCount(QuestDefinition quest) {
        return (int) quest.actors().values().stream().filter(actor -> actor.required()).count();
    }

    private static int stageEdgeCount(QuestDefinition quest) {
        return quest.stages().values().stream().mapToInt(stage -> stage.next().size()).sum();
    }

    private static int shrineProjectionCount(QuestDefinition quest) {
        return quest.endings().values().stream().mapToInt(ending -> ending.shrineDisplayNames().size()).sum();
    }

    private static String variableTypes(QuestDefinition quest) {
        return quest.variables().values().stream()
                .sorted(Comparator.comparing(QuestVariableDefinition::id))
                .map(variable -> variable.id() + "=" + variable.type().name().toLowerCase(Locale.ROOT))
                .reduce(joiner()).orElse("");
    }

    private static String variableScopes(QuestDefinition quest) {
        return quest.variables().values().stream()
                .sorted(Comparator.comparing(QuestVariableDefinition::id))
                .map(variable -> variable.id() + "=" + variable.scope().name().toLowerCase(Locale.ROOT))
                .reduce(joiner()).orElse("");
    }

    private static String questId(QuestDefinition quest, String field) {
        return "questlines." + key(quest.id()) + "." + field;
    }

    private static String questPath(QuestDefinition quest, String path) {
        return "questlines/" + quest.id() + "/" + path;
    }

    private static String questLabel(QuestDefinition quest, String label) {
        return quest.id() + " " + label;
    }

    private static String ids(List<QuestDefinition> definitions) {
        return definitions.stream().map(QuestDefinition::id).reduce(joiner()).orElse("");
    }

    private static String keys(Collection<String> values) {
        return values.stream().sorted().reduce(joiner()).orElse("");
    }

    private static String join(Collection<String> values) {
        return values.stream().reduce(joiner()).orElse("");
    }

    private static java.util.function.BinaryOperator<String> joiner() {
        return (left, right) -> left + ", " + right;
    }

    private static String key(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9_.:-]", "_");
        return normalized.isBlank() ? "unnamed" : normalized;
    }
}
