package panetina.elarion.addons.quests.storage;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class QuestPlayerState {
    public String questId = "";
    public String scopeKey = "";
    public UUID playerId;
    public Map<String, String> variables = new LinkedHashMap<>();
    public Set<String> flags = new LinkedHashSet<>();
    public Set<String> evidenceSeen = new LinkedHashSet<>();
    public long updatedAt = 0L;

    public QuestPlayerState() {
    }

    public QuestPlayerState(String questId, String scopeKey, UUID playerId, long updatedAt) {
        this.questId = safe(questId);
        this.scopeKey = safe(scopeKey);
        this.playerId = playerId;
        this.updatedAt = updatedAt;
    }

    public QuestPlayerState copy() {
        QuestPlayerState copy = new QuestPlayerState();
        copy.questId = safe(questId);
        copy.scopeKey = safe(scopeKey);
        copy.playerId = playerId;
        copy.variables = variables == null ? new LinkedHashMap<>() : new LinkedHashMap<>(variables);
        copy.flags = flags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(flags);
        copy.evidenceSeen = evidenceSeen == null ? new LinkedHashSet<>() : new LinkedHashSet<>(evidenceSeen);
        copy.updatedAt = updatedAt;
        return copy;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
