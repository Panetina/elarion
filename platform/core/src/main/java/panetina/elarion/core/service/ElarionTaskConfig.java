package panetina.elarion.core.service;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ElarionTaskConfig {
    public static final String DEFAULT_CONFIG = """
            config-version: 1

            # Unknown online host. CPU sharing is likely, so defaults stay conservative.
            hardware-profile: "unknown_online_host"
            cpu-sharing-risk: "likely"

            task-budgets:
              io-workers: 1
              compute-workers: 2
              max-server-applies-per-tick: 256
              max-server-apply-millis: 2.0
              max-queued-server-tasks: 4096

            monitoring:
              tick-warning-millis: 45.0
              queue-warning-threshold: 2048
              slow-operation-warning-millis: 50.0
              sample-interval-seconds: 30
              enable-world-samples: true
              enable-realm-samples: true
              headroom-warm-mspt: 35.0
              headroom-pressure-mspt: 45.0
              headroom-overloaded-mspt: 50.0

            compatibility:
              bobby: "optional-client-cache-only"
              distant-horizons: "optional-visual-lod"
            """;

    private ElarionTaskConfig() {
    }

    public static ElarionTaskService.Budget load(Logger logger) {
        return loadSettings(logger).budget();
    }

    public static Settings loadSettings(Logger logger) {
        Path file = FabricLoader.getInstance().getConfigDir()
                .resolve("elarion/addons/optimization/performance.yml");
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.writeString(file, DEFAULT_CONFIG, StandardCharsets.UTF_8);
            }
            Map<String, Object> root = loadMap(file);
            Map<String, Object> budgets = map(root.get("task-budgets"));
            Map<String, Object> monitoring = map(root.get("monitoring"));
            List<String> warnings = new ArrayList<>();
            ElarionTaskService.Budget budget = new ElarionTaskService.Budget(
                    positiveInt(budgets.get("io-workers"), 1, "task-budgets.io-workers", warnings),
                    positiveInt(budgets.get("compute-workers"), 2, "task-budgets.compute-workers", warnings),
                    positiveInt(budgets.get("max-queued-server-tasks"), 4096,
                            "task-budgets.max-queued-server-tasks", warnings),
                    positiveInt(budgets.get("max-server-applies-per-tick"), 256,
                            "task-budgets.max-server-applies-per-tick", warnings),
                    Math.max(1L, Math.round(number(
                            budgets.get("max-server-apply-millis"), 2.0D,
                            "task-budgets.max-server-apply-millis", warnings).doubleValue() * 1_000_000.0D))
            );
            Monitoring monitoringSettings = new Monitoring(
                    Math.max(1L, Math.round(number(
                            monitoring.get("tick-warning-millis"), 45.0D,
                            "monitoring.tick-warning-millis", warnings).doubleValue() * 1_000_000.0D)),
                    positiveInt(monitoring.get("queue-warning-threshold"), 2048,
                            "monitoring.queue-warning-threshold", warnings),
                    Math.max(1L, Math.round(number(
                            monitoring.get("slow-operation-warning-millis"), 50.0D,
                            "monitoring.slow-operation-warning-millis", warnings).doubleValue() * 1_000_000.0D)),
                    positiveInt(monitoring.get("sample-interval-seconds"), 30,
                            "monitoring.sample-interval-seconds", warnings),
                    bool(monitoring.get("enable-world-samples"), true,
                            "monitoring.enable-world-samples", warnings),
                    bool(monitoring.get("enable-realm-samples"), true,
                            "monitoring.enable-realm-samples", warnings),
                    Math.max(1L, Math.round(number(
                            monitoring.get("headroom-warm-mspt"), 35.0D,
                            "monitoring.headroom-warm-mspt", warnings).doubleValue() * 1_000_000.0D)),
                    Math.max(1L, Math.round(number(
                            monitoring.get("headroom-pressure-mspt"), 45.0D,
                            "monitoring.headroom-pressure-mspt", warnings).doubleValue() * 1_000_000.0D)),
                    Math.max(1L, Math.round(number(
                            monitoring.get("headroom-overloaded-mspt"), 50.0D,
                            "monitoring.headroom-overloaded-mspt", warnings).doubleValue() * 1_000_000.0D))
            );
            warnings.forEach(warning -> logger.warn("Elarion performance config: {}", warning));
            return new Settings(
                    string(root.get("hardware-profile"), "unknown_online_host",
                            "hardware-profile", warnings),
                    string(root.get("cpu-sharing-risk"), "likely", "cpu-sharing-risk", warnings),
                    budget,
                    monitoringSettings,
                    false,
                    List.copyOf(warnings)
            );
        } catch (IOException | RuntimeException exception) {
            logger.warn("Using default Elarion performance settings because performance config could not be loaded", exception);
            return Settings.defaults(true);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadMap(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            return loaded instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private static Number number(Object value, Number fallback, String path, List<String> warnings) {
        if (value instanceof Number number) {
            return number.doubleValue() > 0.0D ? number : warn(path, value, fallback, warnings);
        }
        return warn(path, value, fallback, warnings);
    }

    private static int positiveInt(Object value, int fallback, String path, List<String> warnings) {
        Number number = number(value, fallback, path, warnings);
        int parsed = number.intValue();
        if (parsed <= 0) {
            warn(path, value, fallback, warnings);
            return fallback;
        }
        return parsed;
    }

    private static boolean bool(Object value, boolean fallback, String path, List<String> warnings) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        warn(path, value, fallback, warnings);
        return fallback;
    }

    private static String string(Object value, String fallback, String path, List<String> warnings) {
        if (value instanceof String string && !string.isBlank()) {
            return string;
        }
        warn(path, value, fallback, warnings);
        return fallback;
    }

    private static Number warn(String path, Object value, Number fallback, List<String> warnings) {
        warnings.add(path + " expected positive number but got " + printable(value)
                + "; using " + fallback);
        return fallback;
    }

    private static void warn(String path, Object value, Object fallback, List<String> warnings) {
        warnings.add(path + " expected " + fallback.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT)
                + " but got " + printable(value) + "; using " + fallback);
    }

    private static String printable(Object value) {
        return value == null ? "missing" : String.valueOf(value);
    }

    public record Settings(
            String hardwareProfile,
            String cpuSharingRisk,
            ElarionTaskService.Budget budget,
            Monitoring monitoring,
            boolean usingFallback,
            List<String> validationWarnings
    ) {
        public static Settings defaults(boolean usingFallback) {
            return new Settings(
                    "unknown_online_host",
                    "likely",
                    ElarionTaskService.Budget.defaults(),
                    Monitoring.defaults(),
                    usingFallback,
                    List.of()
            );
        }
    }

    public record Monitoring(
            long tickWarningNanos,
            int queueWarningThreshold,
            long slowOperationWarningNanos,
            int sampleIntervalSeconds,
            boolean worldSamplesEnabled,
            boolean realmSamplesEnabled,
            long headroomWarmNanos,
            long headroomPressureNanos,
            long headroomOverloadedNanos
    ) {
        public static Monitoring defaults() {
            return new Monitoring(
                    TimeUnit.MILLISECONDS.toNanos(45),
                    2048,
                    TimeUnit.MILLISECONDS.toNanos(50),
                    30,
                    true,
                    true,
                    TimeUnit.MILLISECONDS.toNanos(35),
                    TimeUnit.MILLISECONDS.toNanos(45),
                    TimeUnit.MILLISECONDS.toNanos(50)
            );
        }

        public double tickWarningMillis() {
            return tickWarningNanos / 1_000_000.0D;
        }

        public double slowOperationWarningMillis() {
            return slowOperationWarningNanos / 1_000_000.0D;
        }

        public double headroomWarmMillis() {
            return headroomWarmNanos / 1_000_000.0D;
        }

        public double headroomPressureMillis() {
            return headroomPressureNanos / 1_000_000.0D;
        }

        public double headroomOverloadedMillis() {
            return headroomOverloadedNanos / 1_000_000.0D;
        }
    }
}
