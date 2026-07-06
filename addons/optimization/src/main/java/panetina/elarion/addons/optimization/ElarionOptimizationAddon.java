package panetina.elarion.addons.optimization;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.command.CommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.command.CommandOutput;
import panetina.elarion.core.service.ElarionDiagnostics;
import panetina.elarion.core.service.ElarionPerformanceMonitor;
import panetina.elarion.core.service.ElarionTaskService;
import panetina.elarion.core.service.ElarionTaskConfig;

import java.time.Instant;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public final class ElarionOptimizationAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_optimization");

    @Override
    public void initialize(ElarionApi api) {
        AddonConfigFiles.writeDefault("optimization", "performance.yml", ElarionTaskConfig.DEFAULT_CONFIG);
        PerformanceConfigDescriptors.register(api.system().configs(), api.system().tasks()::snapshot);
        api.system().abilities().register("elarion.optimization.admin");
        PerformanceSampler sampler = new PerformanceSampler(api);
        api.system().commands().registerAdminSubcommand(() -> perfCommand(api, sampler));
        api.system().commands().registerHelpDescription("/e perf status", "Show Elarion performance queue status.");
        api.system().commands().registerHelpDescription("/e perf queues", "Show Elarion task queue family counters.");
        api.system().commands().registerHelpDescription("/e perf config", "Show loaded Elarion performance settings.");
        api.system().commands().registerHelpDescription("/e perf worlds", "Show sampled Elarion world performance diagnostics.");
        api.system().commands().registerHelpDescription("/e perf realms", "Show sampled Elarion Realm performance diagnostics.");
        api.system().commands().registerHelpDescription("/e perf realm <realm>", "Show sampled diagnostics for one Realm.");
        api.system().commands().registerHelpDescription("/e perf hotzones", "Show sampled worlds with the highest current cost.");
        api.system().commands().registerHelpDescription("/e perf security", "Show security-performance diagnostics.");
        LOGGER.info("Elarion Optimization addon shell initialized");
    }

    private static LiteralArgumentBuilder<ServerCommandSource> perfCommand(ElarionApi api, PerformanceSampler sampler) {
        return literal("perf")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("status").executes(context -> sendStatus(context.getSource(), api, sampler)))
                .then(literal("queues").executes(context -> sendQueues(context.getSource(), api)))
                .then(literal("config").executes(context -> sendConfig(context.getSource(), api)))
                .then(literal("worlds").executes(context -> sendWorlds(context.getSource(), sampler)))
                .then(literal("realms").executes(context -> sendRealms(context.getSource(), sampler)))
                .then(literal("realm")
                        .then(net.minecraft.server.command.CommandManager.argument("realm", StringArgumentType.word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                        api.realm().realms().all().stream().map(realm -> realm.id()), builder))
                                .executes(context -> sendRealm(context.getSource(), sampler,
                                        StringArgumentType.getString(context, "realm")))))
                .then(literal("hotzones").executes(context -> sendHotzones(context.getSource(), sampler)))
                .then(literal("security").executes(context -> sendSecurity(context.getSource())));
    }

    private static int sendStatus(ServerCommandSource source, ElarionApi api, PerformanceSampler sampler) {
        ElarionTaskService.Snapshot snapshot = api.system().tasks().snapshot();
        PerformanceSampler.Sample sample = sampler.sample();
        CommandOutput.header(source, "Elarion Performance");
        CommandOutput.section(source, "Host");
        CommandOutput.kv(source, "Profile", snapshot.hardwareProfile());
        CommandOutput.kv(source, "CPU sharing risk", snapshot.cpuSharingRisk());
        CommandOutput.kv(source, "Fallback config", snapshot.usingFallbackConfig() ? "yes" : "no");
        CommandOutput.section(source, "Headroom");
        CommandOutput.kv(source, "State", sample.headroom());
        CommandOutput.kv(source, "Average tick", format(sample.averageTickMillis()) + "ms");
        CommandOutput.section(source, "Workers");
        CommandOutput.kv(source, "IO workers", snapshot.ioWorkers());
        CommandOutput.kv(source, "Compute workers", snapshot.computeWorkers());
        CommandOutput.kv(source, "Server apply budget", format(snapshot.maxServerApplyMillis()) + "ms");
        CommandOutput.kv(source, "Max server tasks/tick", snapshot.maxServerAppliesPerTick());
        CommandOutput.section(source, "Queues");
        CommandOutput.kv(source, "Server", "queued=" + snapshot.queuedServerTasks()
                + ", completed=" + snapshot.completedServerTasks()
                + ", failed=" + snapshot.failedServerTasks()
                + ", rejected=" + snapshot.rejectedServerTasks());
        CommandOutput.kv(source, "IO", "queued=" + snapshot.io().queuedTasks()
                + ", active=" + snapshot.io().activeTasks()
                + ", completed=" + snapshot.io().completedTasks()
                + ", failed=" + snapshot.io().failedTasks());
        CommandOutput.kv(source, "Compute", "queued=" + snapshot.compute().queuedTasks()
                + ", active=" + snapshot.compute().activeTasks()
                + ", completed=" + snapshot.compute().completedTasks()
                + ", failed=" + snapshot.compute().failedTasks());
        CommandOutput.section(source, "Server Apply");
        CommandOutput.kv(source, "Last tick tasks", snapshot.lastTickApplied());
        CommandOutput.kv(source, "Last apply time", format(snapshot.lastTickMillis()) + "ms");
        CommandOutput.kv(source, "Rolling apply time", format(snapshot.rollingApplyMillis()) + "ms");
        CommandOutput.kv(source, "Max apply time", format(snapshot.maxTickMillis()) + "ms");
        CommandOutput.kv(source, "Slow apply ticks", snapshot.slowServerApplyTicks());
        CommandOutput.section(source, "Pressure");
        CommandOutput.kv(source, "Queue pressure", snapshot.queuePressure() ? "yes" : "no");
        CommandOutput.kv(source, "Queue usage", format(snapshot.queueUsage() * 100.0D) + "%");
        CommandOutput.kv(source, "Apply over budget", snapshot.serverApplyOverBudget() ? "yes" : "no");
        CommandOutput.kv(source, "Queue warning threshold", snapshot.queueWarningThreshold());
        CommandOutput.section(source, "Slow Operations");
        CommandOutput.line(source, formatOperations(ElarionPerformanceMonitor.snapshot()));
        return 1;
    }

    private static int sendQueues(ServerCommandSource source, ElarionApi api) {
        ElarionTaskService.Snapshot snapshot = api.system().tasks().snapshot();
        CommandOutput.header(source, "Task Queues");
        CommandOutput.section(source, "Server Thread");
        CommandOutput.kv(source, "Rejected", formatFamilies(snapshot.rejectedByFamily()));
        CommandOutput.kv(source, "Completed", formatFamilies(snapshot.completedByFamily()));
        CommandOutput.kv(source, "Failed", formatFamilies(snapshot.failedByFamily()));
        CommandOutput.section(source, "IO Queue");
        CommandOutput.kv(source, "Submitted", formatFamilies(snapshot.io().submittedByFamily()));
        CommandOutput.kv(source, "Completed", formatFamilies(snapshot.io().completedByFamily()));
        CommandOutput.kv(source, "Failed", formatFamilies(snapshot.io().failedByFamily()));
        CommandOutput.section(source, "Compute Queue");
        CommandOutput.kv(source, "Submitted", formatFamilies(snapshot.compute().submittedByFamily()));
        CommandOutput.kv(source, "Completed", formatFamilies(snapshot.compute().completedByFamily()));
        CommandOutput.kv(source, "Failed", formatFamilies(snapshot.compute().failedByFamily()));
        return 1;
    }

    private static int sendConfig(ServerCommandSource source, ElarionApi api) {
        ElarionTaskService.Snapshot snapshot = api.system().tasks().snapshot();
        CommandOutput.header(source, "Performance Config");
        CommandOutput.kv(source, "Host profile", snapshot.hardwareProfile());
        CommandOutput.kv(source, "CPU sharing risk", snapshot.cpuSharingRisk());
        CommandOutput.kv(source, "Fallback", snapshot.usingFallbackConfig() ? "yes" : "no");
        CommandOutput.section(source, "Budgets");
        CommandOutput.kv(source, "IO workers", snapshot.ioWorkers());
        CommandOutput.kv(source, "Compute workers", snapshot.computeWorkers());
        CommandOutput.kv(source, "Max queued server tasks", snapshot.maxQueuedServerTasks());
        CommandOutput.kv(source, "Max server applies/tick", snapshot.maxServerAppliesPerTick());
        CommandOutput.kv(source, "Max server apply time", format(snapshot.maxServerApplyMillis()) + "ms");
        CommandOutput.section(source, "Monitoring");
        CommandOutput.kv(source, "Tick warning", format(snapshot.tickWarningMillis()) + "ms");
        CommandOutput.kv(source, "Queue warning threshold", snapshot.queueWarningThreshold());
        CommandOutput.kv(source, "Slow operation warning", format(snapshot.slowOperationWarningMillis()) + "ms");
        CommandOutput.kv(source, "Sample interval", snapshot.sampleIntervalSeconds() + "s");
        CommandOutput.kv(source, "World samples", snapshot.worldSamplesEnabled());
        CommandOutput.kv(source, "Realm samples", snapshot.realmSamplesEnabled());
        CommandOutput.section(source, "Headroom Thresholds");
        CommandOutput.kv(source, "Warm", format(snapshot.headroomWarmMillis()) + "ms");
        CommandOutput.kv(source, "Pressure", format(snapshot.headroomPressureMillis()) + "ms");
        CommandOutput.kv(source, "Overloaded", format(snapshot.headroomOverloadedMillis()) + "ms");
        CommandOutput.section(source, "Validation");
        CommandOutput.line(source, snapshot.validationWarnings().isEmpty()
                ? "(none)"
                : String.join("; ", snapshot.validationWarnings()));
        return 1;
    }

    private static int sendWorlds(ServerCommandSource source, PerformanceSampler sampler) {
        PerformanceSampler.Sample sample = sampler.sample();
        CommandOutput.header(source, "World Samples");
        CommandOutput.kv(source, "Sampled at",
                sample.timestamp() == 0L ? "never" : Instant.ofEpochMilli(sample.timestamp()).toString());
        if (sample.worlds().isEmpty()) {
            CommandOutput.empty(source, "No world samples available.");
            return 1;
        }
        for (PerformanceSampler.WorldSample world : sample.worlds()) {
            CommandOutput.section(source, world.worldId());
            CommandOutput.kv(source, "Players", world.players());
            CommandOutput.kv(source, "Loaded chunks",
                    world.loadedChunks() + " (" + signed(world.loadedChunkDelta()) + ")");
            CommandOutput.kv(source, "Entities",
                    world.entities() + " (" + signed(world.entityDelta()) + ")");
            CommandOutput.kv(source, "Entity groups", formatGroups(world.entityGroups()));
            CommandOutput.kv(source, "Block entities",
                    world.blockEntities() + " (" + signed(world.blockEntityDelta()) + ")");
            CommandOutput.kv(source, "Block entity types", formatGroups(world.blockEntityTypes()));
            CommandOutput.kv(source, "Trend", formatTrend(world));
        }
        return 1;
    }

    private static int sendRealms(ServerCommandSource source, PerformanceSampler sampler) {
        PerformanceSampler.Sample sample = sampler.sample();
        CommandOutput.header(source, "Realm Samples");
        CommandOutput.kv(source, "Sampled at",
                sample.timestamp() == 0L ? "never" : Instant.ofEpochMilli(sample.timestamp()).toString());
        if (sample.realms().isEmpty()) {
            CommandOutput.empty(source, "No Realm samples available.");
            return 1;
        }
        for (PerformanceSampler.RealmSample realm : sample.realms()) {
            sendRealmSample(source, realm);
        }
        return 1;
    }

    private static int sendRealm(ServerCommandSource source, PerformanceSampler sampler, String realmId) {
        PerformanceSampler.Sample sample = sampler.sample();
        for (PerformanceSampler.RealmSample realm : sample.realms()) {
            if (realm.realmId().equalsIgnoreCase(realmId)) {
                CommandOutput.header(source, "Realm Sample");
                sendRealmSample(source, realm);
                return 1;
            }
        }
        source.sendError(Text.literal("No sampled diagnostics for Realm " + realmId));
        return 0;
    }

    private static int sendHotzones(ServerCommandSource source, PerformanceSampler sampler) {
        PerformanceSampler.Sample sample = sampler.sample();
        CommandOutput.header(source, "Performance Hotzones");
        CommandOutput.kv(source, "Headroom", sample.headroom());
        CommandOutput.kv(source, "Average tick", format(sample.averageTickMillis()) + "ms");
        if (sample.worlds().isEmpty()) {
            CommandOutput.empty(source, "No world samples available.");
            return 1;
        }
        sample.worlds().stream()
                .sorted(Comparator
                        .comparingInt(ElarionOptimizationAddon::hotzoneScore)
                        .thenComparingInt(PerformanceSampler.WorldSample::entities)
                        .thenComparingInt(PerformanceSampler.WorldSample::loadedChunks)
                .reversed())
                .limit(5)
                .forEach(world -> {
                    CommandOutput.section(source, world.worldId());
                    CommandOutput.kv(source, "Players", world.players());
                    CommandOutput.kv(source, "Loaded chunks",
                            world.loadedChunks() + " (" + signed(world.loadedChunkDelta()) + ")");
                    CommandOutput.kv(source, "Entities",
                            world.entities() + " (" + signed(world.entityDelta()) + ")");
                    CommandOutput.kv(source, "Entity groups", formatGroups(world.entityGroups()));
                    CommandOutput.kv(source, "Block entities",
                            world.blockEntities() + " (" + signed(world.blockEntityDelta()) + ")");
                    CommandOutput.kv(source, "Block entity types", formatGroups(world.blockEntityTypes()));
                    CommandOutput.kv(source, "Trend", formatTrend(world));
                });
        return 1;
    }

    private static int sendSecurity(ServerCommandSource source) {
        Map<String, String> diagnostics = ElarionDiagnostics.snapshot("security");
        if (diagnostics.isEmpty()) {
            CommandOutput.empty(source, "Security diagnostics provider is not active.");
            return 1;
        }
        CommandOutput.header(source, "Security Diagnostics");
        CommandOutput.kv(source, "State", diagnostics.getOrDefault("state", "unknown"));
        CommandOutput.kv(source, "Total evidence", diagnostics.getOrDefault("totalEvidence", "0"));
        CommandOutput.kv(source, "Dirty", diagnostics.getOrDefault("dirty", "false"));
        CommandOutput.kv(source, "Evidence types", diagnostics.getOrDefault("types", "(none)"));
        CommandOutput.kv(source, "Last evidence", diagnostics.getOrDefault("lastEvidenceAt", "never"));
        return 1;
    }

    private static void sendRealmSample(ServerCommandSource source, PerformanceSampler.RealmSample realm) {
        CommandOutput.section(source, realm.realmId());
        CommandOutput.kv(source, "World", realm.worldId());
        CommandOutput.kv(source, "Online players", realm.onlinePlayers());
    }

    private static String formatFamilies(Map<String, Long> values) {
        if (values.isEmpty()) {
            return "(none)";
        }
        return values.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .orElse("(none)");
    }

    private static String formatOperations(Map<String, ElarionPerformanceMonitor.Snapshot> values) {
        if (values.isEmpty()) {
            return "(none)";
        }
        return values.entrySet().stream()
                .map(entry -> entry.getKey()
                        + " total=" + entry.getValue().total()
                        + " slow=" + entry.getValue().slow()
                        + " avg=" + format(entry.getValue().averageMillis()) + "ms"
                        + " max=" + format(entry.getValue().maxMillis()) + "ms")
                .reduce((left, right) -> left + "; " + right)
                .orElse("(none)");
    }

    private static int hotzoneScore(PerformanceSampler.WorldSample world) {
        return (world.entities() * 4) + (world.blockEntities() * 2) + world.loadedChunks();
    }

    private static String formatGroups(Map<String, Integer> values) {
        if (values.isEmpty()) {
            return "(none)";
        }
        return values.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .orElse("(none)");
    }

    private static String signed(int value) {
        if (value > 0) {
            return "+" + value;
        }
        return Integer.toString(value);
    }

    private static String formatTrend(PerformanceSampler.WorldSample world) {
        return world.trendSamples()
                + " samples chunks=" + signed(world.loadedChunkTrend())
                + " entities=" + signed(world.entityTrend())
                + " blockEntities=" + signed(world.blockEntityTrend());
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
