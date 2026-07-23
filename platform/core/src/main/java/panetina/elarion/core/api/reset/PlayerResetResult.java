package panetina.elarion.core.api.reset;

import java.util.Map;

public record PlayerResetResult(Map<String, Long> changed) {
    public PlayerResetResult {
        changed = changed == null ? Map.of() : Map.copyOf(changed);
    }

    public static PlayerResetResult of(String key, long value) {
        return new PlayerResetResult(Map.of(key, value));
    }
}
