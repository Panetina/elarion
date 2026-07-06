package panetina.elarion.addons.optimization;

import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;
import panetina.elarion.core.service.ElarionTaskConfig;
import panetina.elarion.core.service.ElarionTaskService;

import java.util.List;
import java.util.function.Supplier;

public final class PerformanceConfigDescriptors {
    private PerformanceConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<ElarionTaskService.Snapshot> snapshot
    ) {
        registry.registerDomain(domain(snapshot));
    }

    public static ElarionConfigDomain domain(Supplier<ElarionTaskService.Snapshot> snapshot) {
        ElarionTaskConfig.Settings defaults = ElarionTaskConfig.Settings.defaults(false);
        ElarionTaskService.Budget budget = defaults.budget();
        ElarionTaskConfig.Monitoring monitoring = defaults.monitoring();
        return new ElarionConfigDomain(
                "optimization",
                "platform:core",
                "Performance",
                "Core task budgets and performance monitoring thresholds surfaced by Optimization.",
                List.of("config/elarion/addons/optimization/performance.yml"),
                "",
                List.of(
                        new ElarionConfigCategory(
                                "host",
                                "Host",
                                "Configured host profile and CPU-sharing risk metadata.",
                                List.of(
                                        stringEntry("hardware-profile", "Hardware Profile",
                                                "Operator-authored host profile label.",
                                                "hardware-profile", defaults.hardwareProfile(),
                                                () -> current(snapshot).hardwareProfile()),
                                        stringEntry("cpu-sharing-risk", "CPU Sharing Risk",
                                                "Operator-authored CPU sharing risk label.",
                                                "cpu-sharing-risk", defaults.cpuSharingRisk(),
                                                () -> current(snapshot).cpuSharingRisk()))),
                        new ElarionConfigCategory(
                                "task-budgets",
                                "Task Budgets",
                                "Core IO, compute, and server-thread apply limits.",
                                List.of(
                                        intEntry("io-workers", "IO Workers",
                                                "Number of bounded IO worker threads.",
                                                "task-budgets.io-workers", budget.ioWorkers(),
                                                () -> current(snapshot).ioWorkers(), 1),
                                        intEntry("compute-workers", "Compute Workers",
                                                "Number of bounded compute worker threads.",
                                                "task-budgets.compute-workers", budget.computeWorkers(),
                                                () -> current(snapshot).computeWorkers(), 1),
                                        intEntry("max-server-applies-per-tick", "Maximum Server Applies",
                                                "Maximum queued server-thread tasks applied per tick.",
                                                "task-budgets.max-server-applies-per-tick",
                                                budget.maxServerAppliesPerTick(),
                                                () -> current(snapshot).maxServerAppliesPerTick(), 1),
                                        intEntry("max-queued-server-tasks", "Maximum Queued Server Tasks",
                                                "Maximum bounded server-thread queue size.",
                                                "task-budgets.max-queued-server-tasks",
                                                budget.maxQueuedServerTasks(),
                                                () -> current(snapshot).maxQueuedServerTasks(), 1),
                                        decimalEntry("max-server-apply-millis", "Maximum Server Apply Time",
                                                "Maximum server-thread queue apply budget in milliseconds.",
                                                "task-budgets.max-server-apply-millis",
                                                budget.maxServerApplyNanos() / 1_000_000.0D,
                                                () -> current(snapshot).maxServerApplyMillis(), 0.0D))),
                        new ElarionConfigCategory(
                                "monitoring",
                                "Monitoring",
                                "Warning thresholds, sample cadence, and headroom classification.",
                                List.of(
                                        decimalEntry("tick-warning-millis", "Tick Warning",
                                                "Tick duration warning threshold in milliseconds.",
                                                "monitoring.tick-warning-millis",
                                                monitoring.tickWarningMillis(),
                                                () -> current(snapshot).tickWarningMillis(), 0.0D),
                                        intEntry("queue-warning-threshold", "Queue Warning Threshold",
                                                "Queued server-task count considered pressure.",
                                                "monitoring.queue-warning-threshold",
                                                monitoring.queueWarningThreshold(),
                                                () -> current(snapshot).queueWarningThreshold(), 1),
                                        decimalEntry("slow-operation-warning-millis", "Slow Operation Warning",
                                                "Operation duration considered slow in milliseconds.",
                                                "monitoring.slow-operation-warning-millis",
                                                monitoring.slowOperationWarningMillis(),
                                                () -> current(snapshot).slowOperationWarningMillis(), 0.0D),
                                        intEntry("sample-interval-seconds", "Sample Interval",
                                                "Seconds between bounded performance samples.",
                                                "monitoring.sample-interval-seconds",
                                                monitoring.sampleIntervalSeconds(),
                                                () -> current(snapshot).sampleIntervalSeconds(), 1),
                                        boolEntry("enable-world-samples", "World Samples",
                                                "Enables bounded per-world diagnostics sampling.",
                                                "monitoring.enable-world-samples",
                                                monitoring.worldSamplesEnabled(),
                                                () -> current(snapshot).worldSamplesEnabled()),
                                        boolEntry("enable-realm-samples", "Realm Samples",
                                                "Enables bounded per-Realm diagnostics sampling.",
                                                "monitoring.enable-realm-samples",
                                                monitoring.realmSamplesEnabled(),
                                                () -> current(snapshot).realmSamplesEnabled()),
                                        decimalEntry("headroom-warm-mspt", "Warm Headroom",
                                                "MSPT threshold for warm headroom classification.",
                                                "monitoring.headroom-warm-mspt",
                                                monitoring.headroomWarmMillis(),
                                                () -> current(snapshot).headroomWarmMillis(), 0.0D),
                                        decimalEntry("headroom-pressure-mspt", "Pressure Headroom",
                                                "MSPT threshold for pressure classification.",
                                                "monitoring.headroom-pressure-mspt",
                                                monitoring.headroomPressureMillis(),
                                                () -> current(snapshot).headroomPressureMillis(), 0.0D),
                                        decimalEntry("headroom-overloaded-mspt", "Overloaded Headroom",
                                                "MSPT threshold for overloaded classification.",
                                                "monitoring.headroom-overloaded-mspt",
                                                monitoring.headroomOverloadedMillis(),
                                                () -> current(snapshot).headroomOverloadedMillis(), 0.0D)))));
    }

    private static ElarionConfigEntry<Boolean> boolEntry(
            String id, String label, String description, String path,
            boolean defaultValue, Supplier<Boolean> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.BOOLEAN, defaultValue, currentValue,
                ElarionConfigValidator.pass(), List.of("true", "false"), "", "",
                false, true, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id, String label, String description, String path,
            int defaultValue, Supplier<Integer> currentValue, int minimum
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerMinimum(path, minimum), List.of(),
                Integer.toString(minimum), "", false, true,
                ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id, String label, String description, String path,
            String defaultValue, Supplier<String> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                ElarionConfigValidator.nonBlank(path), List.of(), "", "",
                false, true, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> decimalEntry(
            String id, String label, String description, String path,
            double defaultValue, Supplier<Double> currentValue, double minimum
    ) {
        Supplier<String> displayed = () -> Double.toString(currentValue.get());
        ElarionConfigValidator<String> validator = value -> {
            try {
                double parsed = Double.parseDouble(value);
                return Double.isFinite(parsed) && parsed > minimum
                        ? List.of()
                        : List.of(path + ": must be greater than " + minimum);
            } catch (NumberFormatException exception) {
                return List.of(path + ": must be a decimal number");
            }
        };
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING,
                Double.toString(defaultValue), displayed, validator, List.of(),
                Double.toString(minimum), "", false, true,
                ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionTaskService.Snapshot current(
            Supplier<ElarionTaskService.Snapshot> snapshot
    ) {
        ElarionTaskService.Snapshot value = snapshot == null ? null : snapshot.get();
        if (value == null) {
            throw new IllegalStateException("Performance config snapshot is unavailable");
        }
        return value;
    }
}
