package panetina.elarion.core.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class TitleProgressRecord {
    private final UUID uuid;
    private final Map<String, Long> progressTicks = new LinkedHashMap<>();

    public TitleProgressRecord(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() { return uuid; }
    public Map<String, Long> progressTicks() { return progressTicks; }

    public long addTicks(String ruleId, long ticks) {
        long value = progressTicks.getOrDefault(ruleId, 0L) + Math.max(0, ticks);
        progressTicks.put(ruleId, value);
        return value;
    }

    public void reset(String ruleId) {
        progressTicks.remove(ruleId);
    }
}
