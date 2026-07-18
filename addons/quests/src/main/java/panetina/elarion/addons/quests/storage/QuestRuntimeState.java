package panetina.elarion.addons.quests.storage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuestRuntimeState {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public int schemaVersion = CURRENT_SCHEMA_VERSION;
    public Map<String, QuestlineState> questlines = new LinkedHashMap<>();
    public Map<String, QuestPlayerState> players = new LinkedHashMap<>();
    public Map<String, QuestActorBindingScope> actorBindings = new LinkedHashMap<>();
    public List<QuestScheduledConsequence> scheduled = new ArrayList<>();

    public QuestRuntimeState copy() {
        QuestRuntimeState copy = new QuestRuntimeState();
        copy.schemaVersion = schemaVersion;
        copy.questlines = new LinkedHashMap<>();
        if (questlines != null) {
            questlines.forEach((key, value) -> copy.questlines.put(key, value == null ? new QuestlineState() : value.copy()));
        }
        copy.players = new LinkedHashMap<>();
        if (players != null) {
            players.forEach((key, value) -> copy.players.put(key, value == null ? new QuestPlayerState() : value.copy()));
        }
        copy.actorBindings = new LinkedHashMap<>();
        if (actorBindings != null) {
            actorBindings.forEach((key, value) -> copy.actorBindings.put(key,
                    value == null ? new QuestActorBindingScope() : value.copy()));
        }
        copy.scheduled = new ArrayList<>();
        if (scheduled != null) {
            scheduled.stream()
                    .filter(value -> value != null)
                    .map(QuestScheduledConsequence::copy)
                    .forEach(copy.scheduled::add);
        }
        return copy;
    }
}
