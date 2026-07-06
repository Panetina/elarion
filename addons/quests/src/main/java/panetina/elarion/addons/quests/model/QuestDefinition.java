package panetina.elarion.addons.quests.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record QuestDefinition(
        String id,
        String displayName,
        String description,
        String scope,
        String rootStage,
        String version,
        List<String> tags,
        Map<String, QuestActorDefinition> actors,
        Map<String, QuestVariableDefinition> variables,
        Map<String, QuestStageDefinition> stages,
        Map<String, QuestEvidenceDefinition> evidence,
        Map<String, QuestEndingDefinition> endings,
        Map<String, QuestConditionDefinition> conditions,
        Map<String, QuestConsequenceDefinition> consequences,
        Map<String, String> authoring,
        Map<String, String> metadata
) {
    public QuestDefinition(
            String id,
            String displayName,
            String description,
            String scope,
            String rootStage,
            Map<String, QuestActorDefinition> actors,
            Map<String, QuestVariableDefinition> variables,
            Map<String, QuestStageDefinition> stages,
            Map<String, QuestEvidenceDefinition> evidence,
            Map<String, QuestEndingDefinition> endings,
            Map<String, String> metadata
    ) {
        this(id, displayName, description, scope, rootStage, "", List.of(), actors, variables, stages, evidence,
                endings, Map.of(), Map.of(), Map.of(), metadata);
    }

    public QuestDefinition {
        id = id == null ? "" : id;
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        description = description == null ? "" : description;
        scope = scope == null || scope.isBlank() ? "realm" : scope;
        rootStage = rootStage == null ? "" : rootStage;
        version = version == null ? "" : version;
        tags = tags == null ? List.of() : tags.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
        actors = actors == null ? Map.of() : new LinkedHashMap<>(actors);
        variables = variables == null ? Map.of() : new LinkedHashMap<>(variables);
        stages = stages == null ? Map.of() : new LinkedHashMap<>(stages);
        evidence = evidence == null ? Map.of() : new LinkedHashMap<>(evidence);
        endings = endings == null ? Map.of() : new LinkedHashMap<>(endings);
        conditions = conditions == null ? Map.of() : new LinkedHashMap<>(conditions);
        consequences = consequences == null ? Map.of() : new LinkedHashMap<>(consequences);
        authoring = authoring == null ? Map.of() : new LinkedHashMap<>(authoring);
        metadata = metadata == null ? Map.of() : new LinkedHashMap<>(metadata);
    }

    public String defaultStage() {
        if (!rootStage.isBlank()) return rootStage;
        return stages.keySet().stream().findFirst().orElse("");
    }
}
