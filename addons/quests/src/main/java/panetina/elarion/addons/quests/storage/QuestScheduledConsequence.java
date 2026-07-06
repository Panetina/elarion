package panetina.elarion.addons.quests.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class QuestScheduledConsequence {
    public String id = "";
    public String questId = "";
    public String scopeKey = "";
    public UUID playerId;
    public long dueAt = 0L;
    public String action = "";
    public Map<String, String> parameters = new LinkedHashMap<>();

    public QuestScheduledConsequence() {
    }

    public QuestScheduledConsequence(
            String id,
            String questId,
            String scopeKey,
            UUID playerId,
            long dueAt,
            String action,
            Map<String, String> parameters
    ) {
        this.id = safe(id);
        this.questId = safe(questId);
        this.scopeKey = safe(scopeKey);
        this.playerId = playerId;
        this.dueAt = dueAt;
        this.action = safe(action);
        this.parameters = parameters == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parameters);
    }

    public QuestScheduledConsequence copy() {
        return new QuestScheduledConsequence(id, questId, scopeKey, playerId, dueAt, action, parameters);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
