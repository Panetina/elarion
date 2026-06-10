package panetina.elarion.addons.economy.config;

import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.economy.model.EconomyGovernorMode;
import panetina.elarion.core.api.AddonConfigFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record EconomyConfig(
        int configVersion,
        long snapshotIntervalMillis,
        boolean forceJournalWrites,
        int queryMaxMonths,
        int queryMaxLimit,
        EconomyGovernorMode governorMode,
        int governorWindowDays,
        int governorMaxTransactions
) {
    private static final String DEFAULT_CONFIG = """
            config-version: 1

            persistence:
              # Balances stay in memory; compact snapshots are written periodically.
              snapshot-interval-seconds: 300

              # Keep true for crash-safe money movements. Each transaction journal
              # append is forced to disk before balances change.
              force-journal-writes: true

            queries:
              max-months: 12
              max-limit: 100

            governor:
              # Development begins with observation only. No automatic adjustments.
              mode: "MONITOR_ONLY"
              window-days: 7
              max-transactions: 10000
            """;

    public static EconomyConfig load() {
        Path path = AddonConfigFiles.writeDefault("economy", "economy.yml", DEFAULT_CONFIG);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            Map<?, ?> root = loaded instanceof Map<?, ?> map ? map : Map.of();
            List<String> errors = new ArrayList<>();
            int version = integer(root.get("config-version"), 1);
            Map<?, ?> persistence = child(root, "persistence");
            Map<?, ?> queries = child(root, "queries");
            Map<?, ?> governor = child(root, "governor");
            long snapshotSeconds = number(persistence.get("snapshot-interval-seconds"), 300L);
            boolean forceWrites = bool(persistence.get("force-journal-writes"), true);
            int maxMonths = integer(queries.get("max-months"), 12);
            int maxLimit = integer(queries.get("max-limit"), 100);
            EconomyGovernorMode mode = mode(governor.get("mode"), errors);
            int windowDays = integer(governor.get("window-days"), 7);
            int maxTransactions = integer(governor.get("max-transactions"), 10_000);

            if (version != 1) errors.add("config-version: expected 1");
            if (snapshotSeconds < 10) errors.add("persistence.snapshot-interval-seconds: minimum is 10");
            if (maxMonths < 1 || maxMonths > 120) errors.add("queries.max-months: expected 1..120");
            if (maxLimit < 1 || maxLimit > 1000) errors.add("queries.max-limit: expected 1..1000");
            if (windowDays < 1 || windowDays > 365) errors.add("governor.window-days: expected 1..365");
            if (maxTransactions < 100 || maxTransactions > 1_000_000) {
                errors.add("governor.max-transactions: expected 100..1000000");
            }
            if (!errors.isEmpty()) {
                throw new IllegalStateException("Invalid Economy config " + path + ": "
                        + String.join("; ", errors));
            }
            return new EconomyConfig(version, snapshotSeconds * 1000L, forceWrites,
                    maxMonths, maxLimit, mode, windowDays, maxTransactions);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load Economy config " + path, exception);
        }
    }

    private static Map<?, ?> child(Map<?, ?> root, String key) {
        return root.get(key) instanceof Map<?, ?> map ? map : Map.of();
    }

    private static long number(Object value, long fallback) {
        return value instanceof Number number ? number.longValue() : fallback;
    }

    private static int integer(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static boolean bool(Object value, boolean fallback) {
        return value instanceof Boolean bool ? bool : fallback;
    }

    private static EconomyGovernorMode mode(Object value, List<String> errors) {
        try {
            return EconomyGovernorMode.valueOf(String.valueOf(
                    value == null ? "MONITOR_ONLY" : value).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            errors.add("governor.mode: expected OFF, MONITOR_ONLY, SUGGEST_ONLY, AUTO_LIMITED, or AUTO_FULL");
            return EconomyGovernorMode.MONITOR_ONLY;
        }
    }
}
