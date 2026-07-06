package panetina.elarion.addons.quests.storage;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class QuestlineState {
    public String questId = "";
    public String scopeKey = "";
    public String stageId = "";
    public String endingId = "";
    public Map<String, String> variables = new LinkedHashMap<>();
    public Set<String> flags = new LinkedHashSet<>();
    public Set<String> evidence = new LinkedHashSet<>();
    public long updatedAt = 0L;

    public QuestlineState() {
    }

    public QuestlineState(String questId, String scopeKey, String stageId, long updatedAt) {
        this.questId = safe(questId);
        this.scopeKey = safe(scopeKey);
        this.stageId = safe(stageId);
        this.updatedAt = updatedAt;
    }

    public QuestlineState copy() {
        QuestlineState copy = new QuestlineState();
        copy.questId = safe(questId);
        copy.scopeKey = safe(scopeKey);
        copy.stageId = safe(stageId);
        copy.endingId = safe(endingId);
        copy.variables = variables == null ? new LinkedHashMap<>() : new LinkedHashMap<>(variables);
        copy.flags = flags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(flags);
        copy.evidence = evidence == null ? new LinkedHashSet<>() : new LinkedHashSet<>(evidence);
        copy.updatedAt = updatedAt;
        return copy;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
