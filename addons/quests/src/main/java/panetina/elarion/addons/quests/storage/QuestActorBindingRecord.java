package panetina.elarion.addons.quests.storage;

import java.util.UUID;

public final class QuestActorBindingRecord {
    public String actor = "";
    public UUID placedNpcId;
    public String handle = "";
    public String definitionId = "";
    public long boundAt = 0L;

    public QuestActorBindingRecord() {
    }

    public QuestActorBindingRecord(
            String actor,
            UUID placedNpcId,
            String handle,
            String definitionId,
            long boundAt
    ) {
        this.actor = safe(actor);
        this.placedNpcId = placedNpcId;
        this.handle = safe(handle);
        this.definitionId = safe(definitionId);
        this.boundAt = boundAt;
    }

    public QuestActorBindingRecord copy() {
        return new QuestActorBindingRecord(actor, placedNpcId, handle, definitionId, boundAt);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
