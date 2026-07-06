package panetina.elarion.addons.quests.model;

public record QuestVariableDefinition(
        String id,
        QuestVariableScope scope,
        QuestVariableType type,
        String defaultValue
) {
    public QuestVariableDefinition {
        id = id == null ? "" : id;
        scope = scope == null ? QuestVariableScope.SHARED : scope;
        type = type == null ? QuestVariableType.STRING : type;
        defaultValue = type.normalize(defaultValue);
    }
}
