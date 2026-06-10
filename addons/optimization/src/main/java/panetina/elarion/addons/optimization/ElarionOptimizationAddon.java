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
        source.sendFeedback(() -> Text.literal("Elarion performance: host="
                + snapshot.hardwareProfile()
                + " cpuSharingRisk=" + snapshot.cpuSharingRisk()
                + " fallbackConfig=" + (snapshot.usingFallbackConfig() ? "yes" : "no")), false);
        source.sendFeedback(() -> Text.literal("Headroom: "
                + sample.headroom()
                + " averageTick=" + format(sample.averageTickMillis()) + "ms"), false);
        source.sendFeedback(() -> Text.literal("Workers: io="
                + snapshot.ioWorkers()
                + " compute=" + snapshot.computeWorkers()
                + " serverApplyBudget=" + format(snapshot.maxServerApplyMillis()) + "ms"
                + " maxTasks=" + snapshot.maxServerAppliesPerTick()), false);
        source.sendFeedback(() -> Text.literal("Server queue: queued="
                + snapshot.queuedServerTasks()
                + " completed=" + snapshot.completedServerTasks()
                + " failed=" + snapshot.failedServerTasks()
                + " rejected=" + snapshot.rejectedServerTasks()), false);
        source.sendFeedback(() -> Text.literal("IO queue: queued="
                + snapshot.io().queuedTasks()
                + " active=" + snapshot.io().activeTasks()
                + " completed=" + snapshot.io().completedTasks()
                + " failed=" + snapshot.io().failedTasks()), false);
        source.sendFeedback(() -> Text.literal("Compute queue: queued="
                + snapshot.compute().queuedTasks()
                + " active=" + snapshot.compute().activeTasks()
                + " completed=" + snapshot.compute().completedTasks()
                + " failed=" + snapshot.compute().failedTasks()), false);
        source.sendFeedback(() -> Text.literal("Server apply: tasks="
                + snapshot.lastTickApplied()
                + " last=" + format(snapshot.lastTickMillis())
                + "ms rolling=" + format(snapshot.rollingApplyMillis())
                + "ms max=" + format(snapshot.maxTickMillis())
                + "ms slowTicks=" + snapshot.slowServerApplyTicks()), false);
        source.sendFeedback(() -> Text.literal("Queue pressure: "
                + (snapshot.queuePressure() ? "yes" : "no")
                + " usage=" + format(snapshot.queueUsage() * 100.0D)
                + "% applyOverBudget=" + (snapshot.serverApplyOverBudget() ? "yes" : "no")
                + " warningAt=" + snapshot.queueWarningThreshold()), false);
        source.sendFeedback(() -> Text.literal("Slow operations: "
                + formatOperations(ElarionPerformanceMonitor.snapshot())), false);
        return 1;
    }

    private static int sendQueues(ServerCommandSource source, ElarionApi api) {
        ElarionTaskService.Snapshot snapshot = api.system().tasks().snapshot();
        source.sendFeedback(() -> Text.literal("Rejected by family: "
                + formatFamilies(snapshot.rejectedByFamily())), false);
        source.sendFeedback(() -> Text.literal("Completed by family: "
                + formatFamilies(snapshot.completedByFamily())), false);
        source.sendFeedback(() -> Text.literal("Failed by family: "
                + formatFamilies(snapshot.failedByFamily())), false);
        source.sendFeedback(() -> Text.literal("IO submitted by family: "
                + formatFamilies(snapshot.io().submittedByFamily())), false);
        source.sendFeedback(() -> Text.literal("IO completed by family: "
                + formatFamilies(snapshot.io().completedByFamily())), false);
        source.sendFeedback(() -> Text.literal("IO failed by family: "
                + formatFamilies(snapshot.io().failedByFamily())), false);
        source.sendFeedback(() -> Text.literal("Compute submitted by family: "
                + formatFamilies(snapshot.compute().submittedByFamily())), false);
        source.sendFeedback(() -> Text.literal("Compute completed by family: "
                + formatFamilies(snapshot.compute().completedByFamily())), false);
        source.sendFeedback(() -> Text.literal("Compute failed by family: "
                + formatFamilies(snapshot.compute().failedByFamily())), false);
        return 1;
    }

    private static int sendConfig(ServerCommandSource source, ElarionApi api) {
        ElarionTaskService.Snapshot snapshot = api.system().tasks().snapshot();
        source.sendFeedback(() -> Text.literal("Performance config: host="
                + snapshot.hardwareProfile()
                + " cpuSharingRisk=" + snapshot.cpuSharingRisk()
                + " fallback=" + (snapshot.usingFallbackConfig() ? "yes" : "no")), false);
        source.sendFeedback(() -> Text.literal("Budgets: ioWorkers="
                + snapshot.ioWorkers()
                + " computeWorkers=" + snapshot.computeWorkers()
                + " maxQueuedServerTasks=" + snapshot.maxQueuedServerTasks()
                + " maxServerAppliesPerTick=" + snapshot.maxServerAppliesPerTick()
                + " maxServerApplyMillis=" + format(snapshot.maxServerApplyMillis())), false);
        source.sendFeedback(() -> Text.literal("Monitoring: tickWarningMillis="
                + format(snapshot.tickWarningMillis())
                + " queueWarningThreshold=" + snapshot.queueWarningThreshold()
                + " slowOperationWarningMillis=" + format(snapshot.slowOperationWarningMillis())
                + " sampleIntervalSeconds=" + snapshot.sampleIntervalSeconds()
                + " worldSamples=" + snapshot.worldSamplesEnabled()
                + " realmSamples=" + snapshot.realmSamplesEnabled()), false);
        source.sendFeedback(() -> Text.literal("Headroom thresholds: warm="
                + format(snapshot.headroomWarmMillis())
                + "ms pressure=" + format(snapshot.headroomPressureMillis())
                + "ms overloaded=" + format(snapshot.headroomOverloadedMillis())
                + "ms"), false);
        source.sendFeedback(() -> Text.literal("Validation warnings: "
                + (snapshot.validationWarnings().isEmpty()
                ? "(none)"
                : String.join("; ", snapshot.validationWarnings()))), false);
        return 1;
    }

    private static int sendWorlds(ServerCommandSource source, PerformanceSampler sampler) {
        PerformanceSampler.Sample sample = sampler.sample();
        source.sendFeedback(() -> Text.literal("World samples at "
                + (sample.timestamp() == 0L ? "never" : Instant.ofEpochMilli(sample.timestamp()).toString())), false);
        if (sample.worlds().isEmpty()) {
            source.sendFeedback(() -> Text.literal("No world samples available."), false);
            return 1;
        }
        for (PerformanceSampler.WorldSample world : sample.worlds()) {
            source.sendFeedback(() -> Text.literal(world.worldId()
                    + ": players=" + world.players()
                    + " loadedChunks=" + world.loadedChunks() + " (" + signed(world.loadedChunkDelta()) + ")"
                    + " entities=" + world.entities() + " (" + signed(world.entityDelta()) + ")"
                    + " groups=" + formatGroups(world.entityGroups())
                    + " blockEntities=" + world.blockEntities() + " (" + signed(world.blockEntityDelta()) + ")"
                    + " blockEntityTypes=" + formatGroups(world.blockEntityTypes())
                    + " trend=" + formatTrend(world)), false);
        }
        return 1;
    }

    private static int sendRealms(ServerCommandSource source, PerformanceSampler sampler) {
        PerformanceSampler.Sample sample = sampler.sample();
        source.sendFeedback(() -> Text.literal("Realm samples at "
                + (sample.timestamp() == 0L ? "never" : Instant.ofEpochMilli(sample.timestamp()).toString())), false);
        if (sample.realms().isEmpty()) {
            source.sendFeedback(() -> Text.literal("No Realm samples available."), false);
            return 1;
        }
        for (PerformanceSampler.RealmSample realm : sample.realms()) {
            source.sendFeedback(() -> Text.literal(realm.realmId()
                    + ": world=" + realm.worldId()
                    + " onlinePlayers=" + realm.onlinePlayers()), false);
        }
        return 1;
    }

    private static int sendRealm(ServerCommandSource source, PerformanceSampler sampler, String realmId) {
        PerformanceSampler.Sample sample = sampler.sample();
        for (PerformanceSampler.RealmSample realm : sample.realms()) {
            if (realm.realmId().equalsIgnoreCase(realmId)) {
                source.sendFeedback(() -> Text.literal(realm.realmId()
                        + ": world=" + realm.worldId()
                        + " onlinePlayers=" + realm.onlinePlayers()), false);
                return 1;
            }
        }
        source.sendError(Text.literal("No sampled diagnostics for Realm " + realmId));
        return 0;
    }

    private static int sendHotzones(ServerCommandSource source, PerformanceSampler sampler) {
        PerformanceSampler.Sample sample = sampler.sample();
        source.sendFeedback(() -> Text.literal("Headroom: "
                + sample.headroom()
                + " averageTick=" + format(sample.averageTickMillis()) + "ms"), false);
        if (sample.worlds().isEmpty()) {
            source.sendFeedback(() -> Text.literal("No world samples available."), false);
            return 1;
        }
        sample.worlds().stream()
                .sorted(Comparator
                        .comparingInt(ElarionOptimizationAddon::hotzoneScore)
                        .thenComparingInt(PerformanceSampler.WorldSample::entities)
                        .thenComparingInt(PerformanceSampler.WorldSample::loadedChunks)
                        .reversed())
                .limit(5)
                .forEach(world -> source.sendFeedback(() -> Text.literal(world.worldId()
                        + ": players=" + world.players()
                        + " loadedChunks=" + world.loadedChunks() + " (" + signed(world.loadedChunkDelta()) + ")"
                        + " entities=" + world.entities() + " (" + signed(world.entityDelta()) + ")"
                        + " groups=" + formatGroups(world.entityGroups())
                        + " blockEntities=" + world.blockEntities() + " (" + signed(world.blockEntityDelta()) + ")"
                        + " blockEntityTypes=" + formatGroups(world.blockEntityTypes())
                        + " trend=" + formatTrend(world)), false));
        return 1;
    }

    private static int sendSecurity(ServerCommandSource source) {
        Map<String, String> diagnostics = ElarionDiagnostics.snapshot("security");
        if (diagnostics.isEmpty()) {
            source.sendFeedback(() -> Text.literal("Security diagnostics: provider not active."), false);
            return 1;
        }
        source.sendFeedback(() -> Text.literal("Security diagnostics: state="
                + diagnostics.getOrDefault("state", "unknown")
                + " totalEvidence=" + diagnostics.getOrDefault("totalEvidence", "0")
                + " dirty=" + diagnostics.getOrDefault("dirty", "false")), false);
        source.sendFeedback(() -> Text.literal("Evidence types: "
                + diagnostics.getOrDefault("types", "(none)")), false);
        source.sendFeedback(() -> Text.literal("Last evidence: "
                + diagnostics.getOrDefault("lastEvidenceAt", "never")), false);
        return 1;
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
