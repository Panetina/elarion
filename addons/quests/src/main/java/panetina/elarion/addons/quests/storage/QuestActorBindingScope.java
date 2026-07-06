package panetina.elarion.addons.quests.storage;

import java.util.LinkedHashMap;
import java.util.Map;

public final class QuestActorBindingScope {
    public String questId = "";
    public String scopeKey = "";
    public Map<String, QuestActorBindingRecord> actors = new LinkedHashMap<>();
    public long updatedAt = 0L;

    public QuestActorBindingScope() {
    }

    public QuestActorBindingScope(String questId, String scopeKey, long updatedAt) {
        this.questId = safe(questId);
        this.scopeKey = safe(scopeKey);
        this.updatedAt = updatedAt;
    }

    public QuestActorBindingScope copy() {
        QuestActorBindingScope copy = new QuestActorBindingScope(questId, scopeKey, updatedAt);
        copy.actors = new LinkedHashMap<>();
        if (actors != null) {
            actors.forEach((key, value) -> copy.actors.put(key,
                    value == null ? new QuestActorBindingRecord() : value.copy()));
        }
        return copy;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
