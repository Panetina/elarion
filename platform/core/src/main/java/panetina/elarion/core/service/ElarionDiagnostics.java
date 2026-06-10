package panetina.elarion.core.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class ElarionDiagnostics {
    private static final ConcurrentHashMap<String, Supplier<Map<String, String>>> PROVIDERS =
            new ConcurrentHashMap<>();

    private ElarionDiagnostics() {
    }

    public static void register(String id, Supplier<Map<String, String>> provider) {
        if (id == null || id.isBlank() || provider == null) {
            return;
        }
        PROVIDERS.put(id, provider);
    }

    public static Map<String, String> snapshot(String id) {
        Supplier<Map<String, String>> provider = PROVIDERS.get(id);
        if (provider == null) {
            return Map.of();
        }
        Map<String, String> snapshot = provider.get();
        return snapshot == null ? Map.of() : new LinkedHashMap<>(snapshot);
    }
}
