package panetina.elarion.core.api.reset;

import java.util.Map;

public record WorldResetResult(Map<String, Long> changed) {
    public WorldResetResult {
        changed = changed == null ? Map.of() : Map.copyOf(changed);
    }

    public static WorldResetResult of(String key, long value) {
        return new WorldResetResult(Map.of(key, value));
    }
}
