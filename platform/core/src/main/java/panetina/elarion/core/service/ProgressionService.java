package panetina.elarion.core.service;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ProgressionEvent;
import panetina.elarion.core.model.ProgressionRegion;
import panetina.elarion.core.model.TitleActiveEffect;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.TitleProgressRecord;
import panetina.elarion.core.model.TitleUnlockRule;
import panetina.elarion.core.storage.DirtyTracker;
import panetina.elarion.core.storage.TitleProgressStorage;
import panetina.elarion.core.metric.MetricProjectionWorker;
import panetina.elarion.core.metric.MetricQuery;
import panetina.elarion.core.metric.MetricUpdatedEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ProgressionService {
    public static final String ADVANCEMENTS_COMPLETED = "advancements_completed";
    private static final long SAVE_INTERVAL_MILLIS = 300_000L;
    private static final int ACTIVE_EFFECT_DURATION_TICKS = 220;

    private final CoreConfigManager config;
    private final CitizenService citizens;
    private final TitleService titles;
    private final PlayerStatsService playerStats;
    private final TitleProgressStorage storage;
    private final Map<UUID, TitleProgressRecord> progress = new ConcurrentHashMap<>();
    private final DirtyTracker dirty = new DirtyTracker();
    private Map<String, List<TitleUnlockRule>> eventRulesByTrigger = Map.of();
    private Map<String, List<TitleUnlockRule>> statRulesByTrigger = Map.of();
    private List<TitleUnlockRule> continuousRules = List.of();
    private Map<Long, List<TitleUnlockRule>> continuousRulesByInterval = Map.of();
    private List<TitleUnlockRule> regionEnterRules = List.of();
    private Map<Identifier, List<TitleUnlockRule>> metricRulesById = Map.of();
    private Map<String, List<ProgressionRegion>> progressionRegionsByWorld = Map.of();
    private MinecraftServer server;
    private long lastSaveAt;
    private long ticks;
    private Consumer<UUID> advancementCountListener = ignored -> {};
    private MetricProjectionWorker metrics;

    public ProgressionService(
            CoreConfigManager config,
            CitizenService citizens,
            TitleService titles,
            PlayerStatsService playerStats,
            TitleProgressStorage storage
    ) {
        this.config = config;
        this.citizens = citizens;
        this.titles = titles;
        this.playerStats = playerStats;
        this.storage = storage;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        this.progress.clear();
        this.dirty.clear();
        rebuildRuleIndexes();
        this.lastSaveAt = System.currentTimeMillis();
        this.ticks = 0;
    }

    public void setMetricProjection(MetricProjectionWorker metrics) {
        this.metrics = java.util.Objects.requireNonNull(metrics, "metrics");
    }

    public void reloadRules() {
        rebuildRuleIndexes();
    }

    /** Bounded lazy reconciliation for one joining or already-online citizen. */
    public void reconcileMetricRules(ServerPlayerEntity player) {
        if (player == null || metrics == null) return;
        Identifier realmId = metricRealmId(citizens.getOrCreate(player).realmId());
        for (List<TitleUnlockRule> rules : metricRulesById.values()) {
            for (TitleUnlockRule rule : rules) {
                TitleUnlockRule.MetricCondition condition = rule.metric();
                panetina.elarion.core.metric.MetricScope scope;
                try {
                    scope = condition.resolveScope(realmId);
                } catch (RuntimeException exception) {
                    continue;
                }
                var current = metrics.player(new MetricQuery(
                        condition.metricId(), scope, condition.dimensions()), player.getUuid());
                long value = current == null ? 0 : current.fixedPointValue();
                if (!condition.accepts(value)) continue;
                titles.unlockFromProgression(player.getUuid(), rule,
                        ProgressionEvent.builder("metric", player.getUuid())
                                .amount(value).metadata("metric", condition.metricId().toString()).build(), value);
            }
        }
    }

    /** May be called by the metric worker; title mutation is always rescheduled onto the server thread. */
    public void recordMetric(MetricUpdatedEvent event) {
        MinecraftServer bound = server;
        if (bound == null || event == null) return;
        bound.execute(() -> evaluateMetric(event));
    }

    private void evaluateMetric(MetricUpdatedEvent event) {
        MetricProjectionWorker projection = metrics;
        if (projection == null) return;
        var batch = event.batch();
        java.util.LinkedHashSet<TitleUnlockRule> candidates = new java.util.LinkedHashSet<>();
        for (var update : batch.updates()) {
            candidates.addAll(metricRulesById.getOrDefault(update.metricId(), List.of()));
        }
        for (TitleUnlockRule rule : candidates) {
            TitleUnlockRule.MetricCondition condition = rule.metric();
            if (condition == null || batch.updates().stream().noneMatch(update -> condition.matches(update, batch))) {
                continue;
            }
            panetina.elarion.core.metric.MetricRankEntry current = projection.player(
                    new MetricQuery(condition.metricId(), condition.resolveScope(batch), condition.dimensions()),
                    batch.actorId());
            long value = current == null ? 0 : current.fixedPointValue();
            if (!condition.accepts(value)) continue;
            ProgressionEvent progressionEvent = ProgressionEvent.builder("metric", batch.actorId())
                    .amount(value)
                    .metadata("metric", condition.metricId().toString())
                    .build();
            titles.unlockFromProgression(batch.actorId(), rule, progressionEvent, value);
        }
    }

    private static Identifier metricRealmId(String value) {
        if (value == null || value.isBlank()) return null;
        Identifier direct = Identifier.tryParse(value);
        if (direct != null && value.contains(":")) return direct;
        String path = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
        return Identifier.of("elarion", "realm/" + path);
    }

    public void registerEvents() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            EntityType<?> type = killedEntity.getType();
            recordEvent(ProgressionEvent.builder("entity-kill", player.getUuid())
                    .world(world.getRegistryKey().getValue().toString())
                    .dimension(world.getRegistryKey().getValue().toString())
                    .biome(biomeId(world, killedEntity.getBlockPos()))
                    .entity(Registries.ENTITY_TYPE.getId(type),
                            ProgressionEvent.tagIds(Registries.ENTITY_TYPE.getEntry(type).streamTags()))
                    .build());
        });
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld)) return;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
            recordEvent(blockEvent("block-break", serverPlayer, serverWorld, pos, state));
            if (isCropLike(state)) {
                recordEvent(blockEvent("crop-harvest", serverPlayer, serverWorld, pos, state));
            }
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                BlockPos pos = hitResult.getBlockPos();
                recordEvent(blockEvent("block-use", serverPlayer, serverWorld, pos, world.getBlockState(pos)));
            }
            return ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
                ItemStack stack = player.getStackInHand(hand);
                recordEvent(itemEvent("item-use", serverPlayer, serverWorld, stack));
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }

    public void recordEvent(ProgressionEvent event) {
        if (event == null || event.actorId() == null) return;
        for (TitleUnlockRule rule : eventRulesByTrigger.getOrDefault(event.type(), List.of())) {
            if (!rule.matches(event)) continue;
            if (!rule.statKey().isBlank()) {
                long value = playerStats.increment(event.actorId(), rule.statKey(), rule.amount());
                if (value >= rule.threshold()) titles.unlockFromProgression(event.actorId(), rule, event, value);
            } else {
                titles.unlockFromProgression(event.actorId(), rule, event, event.amount());
            }
        }
        for (TitleUnlockRule rule : statRulesByTrigger.getOrDefault(event.type(), List.of())) {
            if (!rule.matches(event)) continue;
            playerStats.increment(event.actorId(), rule.statKey(), rule.amount());
        }
    }

    public void recordCraft(ServerPlayerEntity player, ItemStack stack, Identifier recipeId) {
        if (player == null || stack == null || stack.isEmpty()) return;
        ProgressionEvent itemEvent = itemEvent("item-craft", player, player.getServerWorld(), stack);
        recordEvent(recipeId == null ? itemEvent : ProgressionEvent.builder("item-craft", player.getUuid())
                .world(itemEvent.worldId())
                .dimension(itemEvent.dimensionId())
                .biome(itemEvent.biomeId())
                .item(itemEvent.itemId(), itemEvent.itemTags())
                .recipe(recipeId)
                .amount(stack.getCount())
                .metadata("regions", itemEvent.metadata().getOrDefault("regions", ""))
                .build());
        if (recipeId != null) {
            recordEvent(ProgressionEvent.builder("recipe-craft", player.getUuid())
                    .world(player.getWorld().getRegistryKey().getValue().toString())
                    .dimension(player.getWorld().getRegistryKey().getValue().toString())
                    .biome(biomeId(player.getServerWorld(), player.getBlockPos()))
                    .recipe(recipeId)
                    .amount(stack.getCount())
                    .metadata("regions", regionsAt(player.getServerWorld(), player.getBlockPos()))
                    .build());
        }
    }

    public void recordAdvancement(ServerPlayerEntity player, Identifier advancementId) {
        if (player == null || advancementId == null) return;
        synchronizeAdvancementCount(player);
        recordEvent(ProgressionEvent.builder("advancement", player.getUuid())
                .world(player.getWorld().getRegistryKey().getValue().toString())
                .dimension(player.getWorld().getRegistryKey().getValue().toString())
                .biome(biomeId(player.getServerWorld(), player.getBlockPos()))
                .metadata("advancement", advancementId.toString())
                .metadata("regions", regionsAt(player.getServerWorld(), player.getBlockPos()))
                .build());
    }

    public void synchronizeAdvancementCount(ServerPlayerEntity player) {
        if (player == null || player.getServer() == null) return;
        long completed = player.getServer().getAdvancementLoader().getAdvancements().stream()
                .filter(advancement -> advancement.value().display().isPresent())
                .filter(advancement -> player.getAdvancementTracker().getProgress(advancement).isDone())
                .count();
        playerStats.set(player.getUuid(), ADVANCEMENTS_COMPLETED, completed);
        advancementCountListener.accept(player.getUuid());
    }

    public void setAdvancementCountListener(Consumer<UUID> listener) {
        advancementCountListener = listener == null ? ignored -> {} : listener;
    }

    public void recordCustom(ServerPlayerEntity player, String eventType, String subject) {
        if (player == null || eventType == null || eventType.isBlank()) return;
        recordEvent(ProgressionEvent.builder(eventType, player.getUuid())
                .world(player.getWorld().getRegistryKey().getValue().toString())
                .dimension(player.getWorld().getRegistryKey().getValue().toString())
                .biome(biomeId(player.getServerWorld(), player.getBlockPos()))
                .metadata("subject", subject == null ? "" : subject)
                .metadata("regions", regionsAt(player.getServerWorld(), player.getBlockPos()))
                .build());
    }

    public Map<String, Long> progressFor(UUID uuid) {
        return Map.copyOf(progress(uuid).progressTicks());
    }

    public List<String> ruleIds() {
        return config.titleUnlockRules().keySet().stream().sorted().toList();
    }

    public void resetProgress(UUID uuid, String ruleId) {
        TitleProgressRecord record = progress(uuid);
        if (ruleId == null || ruleId.isBlank()) {
            record.progressTicks().clear();
        } else {
            record.reset(ruleId);
        }
        dirty.mark(uuid);
    }

    public int resetAllPlayerState() throws java.io.IOException {
        int count = progress.size();
        progress.clear();
        dirty.clear();
        storage.deleteAll(server);
        return count;
    }

    public boolean testRule(ServerPlayerEntity player, String ruleId) {
        TitleUnlockRule rule = config.titleUnlockRules().get(normalize(ruleId));
        if (rule == null) return false;
        if (rule.isContinuousRule()) return continuousMatches(player, rule);
        ProgressionEvent event = ProgressionEvent.builder(rule.trigger(), player.getUuid())
                .world(player.getWorld().getRegistryKey().getValue().toString())
                .dimension(player.getWorld().getRegistryKey().getValue().toString())
                .biome(biomeId(player.getServerWorld(), player.getBlockPos()))
                .metadata("regions", regionsAt(player.getServerWorld(), player.getBlockPos()))
                .build();
        return rule.matches(event);
    }

    public void tick(MinecraftServer server) {
        ticks++;
        applyActiveEffects(server);
        runRegionRules(server);
        runContinuousRules(server);
        saveIfDue();
    }

    public void save(UUID uuid) {
        if (dirty.remove(uuid)) {
            TitleProgressRecord record = progress.get(uuid);
            if (record != null) storage.save(server, record);
        }
    }

    public void saveDirty() {
        dirty.flush(this::save);
    }

    private void runContinuousRules(MinecraftServer server) {
        if (continuousRulesByInterval.isEmpty()) return;
        for (Map.Entry<Long, List<TitleUnlockRule>> entry : continuousRulesByInterval.entrySet()) {
            long interval = entry.getKey();
            if (ticks % interval != 0) continue;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                CitizenRecord citizen = citizens.getOrCreate(player);
                for (TitleUnlockRule rule : entry.getValue()) {
                    if (citizen.hasUnlockedTitle(rule.titleId())) continue;
                    TitleUnlockRule.Continuous continuous = rule.continuous();
                    boolean matches = continuousMatches(player, rule);
                    TitleProgressRecord record = progress(player.getUuid());
                    if (matches) {
                        long value = record.addTicks(rule.id(), continuous.sampleIntervalTicks());
                        dirty.mark(player.getUuid());
                        if (value >= continuous.requiredTicks()) {
                            titles.unlockFromProgression(player.getUuid(), rule,
                                    ProgressionEvent.builder("continuous", player.getUuid())
                                            .world(player.getWorld().getRegistryKey().getValue().toString())
                                            .dimension(player.getWorld().getRegistryKey().getValue().toString())
                                            .biome(biomeId(player.getServerWorld(), player.getBlockPos()))
                                            .metadata("continuous", rule.id())
                                            .build(),
                                    value);
                        }
                    } else if (continuous.resetOnFailure()) {
                        record.reset(rule.id());
                        dirty.mark(player.getUuid());
                    }
                }
            }
        }
    }

    private void runRegionRules(MinecraftServer server) {
        if (ticks % 20 != 0 || progressionRegionsByWorld.isEmpty()) return;
        if (regionEnterRules.isEmpty()) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            String worldId = player.getServerWorld().getRegistryKey().getValue().toString();
            List<ProgressionRegion> regionsForWorld = progressionRegionsByWorld.getOrDefault(worldId, List.of());
            if (regionsForWorld.isEmpty()) continue;
            String regions = regionsAt(player.getServerWorld(), player.getBlockPos());
            if (regions.isBlank()) continue;
            recordEvent(ProgressionEvent.builder("region-enter", player.getUuid())
                    .world(player.getWorld().getRegistryKey().getValue().toString())
                    .dimension(player.getWorld().getRegistryKey().getValue().toString())
                    .biome(biomeId(player.getServerWorld(), player.getBlockPos()))
                    .metadata("regions", regions)
                    .build());
        }
    }

    private boolean continuousMatches(ServerPlayerEntity player, TitleUnlockRule rule) {
        ProgressionEvent event = ProgressionEvent.builder("continuous", player.getUuid())
                .world(player.getWorld().getRegistryKey().getValue().toString())
                .dimension(player.getWorld().getRegistryKey().getValue().toString())
                .biome(biomeId(player.getServerWorld(), player.getBlockPos()))
                .metadata("underwater", String.valueOf(player.isSubmergedInWater()))
                .metadata("regions", regionsAt(player.getServerWorld(), player.getBlockPos()))
                .build();
        if (!rule.matches(event)) return false;
        for (String key : rule.continuous().requiredMetadata()) {
            if ("underwater".equals(key) && !player.isSubmergedInWater()) return false;
        }
        for (String effect : rule.continuous().requiredStatusEffects()) {
            if (!hasStatusEffect(player, effect)) return false;
        }
        return true;
    }

    private void applyActiveEffects(MinecraftServer server) {
        if (ticks % 100 != 0) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            CitizenRecord citizen = citizens.getOrCreate(player);
            Optional<TitleDefinition> title = titles.forCitizen(citizen);
            if (title.isEmpty()) continue;
            for (TitleActiveEffect effect : title.get().activeEffects()) {
                applyEffect(player, effect);
            }
        }
    }

    private void applyEffect(ServerPlayerEntity player, TitleActiveEffect effect) {
        if (!"status_effect".equals(effect.type())) return;
        String id = effect.parameters().getOrDefault("id", "");
        Optional<RegistryEntry.Reference<StatusEffect>> statusEffect = Registries.STATUS_EFFECT.getEntry(Identifier.of(id));
        if (statusEffect.isEmpty()) return;
        int amplifier = parseInt(effect.parameters().get("amplifier"), 0);
        boolean particles = parseBoolean(effect.parameters().get("show-particles"), false);
        boolean icon = parseBoolean(effect.parameters().get("show-icon"), true);
        player.addStatusEffect(new StatusEffectInstance(statusEffect.get(), ACTIVE_EFFECT_DURATION_TICKS,
                amplifier, true, particles, icon));
    }

    private TitleProgressRecord progress(UUID uuid) {
        if (server == null) throw new IllegalStateException("ProgressionService is not bound to a server");
        return progress.computeIfAbsent(uuid, id -> storage.load(server, id));
    }

    private void saveIfDue() {
        long now = System.currentTimeMillis();
        if (now - lastSaveAt < SAVE_INTERVAL_MILLIS) return;
        saveDirty();
        lastSaveAt = now;
    }

    private void rebuildRuleIndexes() {
        Map<String, List<TitleUnlockRule>> eventRules = new HashMap<>();
        Map<String, List<TitleUnlockRule>> statRules = new HashMap<>();
        List<TitleUnlockRule> continuous = new ArrayList<>();
        Map<Identifier, List<TitleUnlockRule>> metricRules = new HashMap<>();
        for (TitleUnlockRule rule : config.titleUnlockRules().values()) {
            if (rule.isMetricRule()) {
                metricRules.computeIfAbsent(rule.metric().metricId(), ignored -> new ArrayList<>()).add(rule);
            } else if (rule.isContinuousRule()) {
                continuous.add(rule);
            } else if (rule.isStatRule()) {
                statRules.computeIfAbsent(rule.trigger(), ignored -> new ArrayList<>()).add(rule);
            } else {
                eventRules.computeIfAbsent(rule.trigger(), ignored -> new ArrayList<>()).add(rule);
            }
        }
        eventRulesByTrigger = immutableRuleLists(eventRules);
        statRulesByTrigger = immutableRuleLists(statRules);
        continuousRules = List.copyOf(continuous);
        continuousRulesByInterval = indexContinuousRules(continuousRules);
        regionEnterRules = eventRulesByTrigger.getOrDefault("region-enter", List.of());
        java.util.LinkedHashMap<Identifier, List<TitleUnlockRule>> immutableMetrics = new java.util.LinkedHashMap<>();
        metricRules.forEach((id, rules) -> immutableMetrics.put(id, List.copyOf(rules)));
        metricRulesById = Map.copyOf(immutableMetrics);
        progressionRegionsByWorld = indexRegionsByWorld(config.progressionRegions().values());
    }

    static Map<Long, List<TitleUnlockRule>> indexContinuousRules(List<TitleUnlockRule> rules) {
        Map<Long, List<TitleUnlockRule>> indexed = new HashMap<>();
        for (TitleUnlockRule rule : rules) {
            if (!rule.isContinuousRule()) continue;
            long interval = Math.max(1L, rule.continuous().sampleIntervalTicks());
            indexed.computeIfAbsent(interval, ignored -> new ArrayList<>()).add(rule);
        }
        Map<Long, List<TitleUnlockRule>> output = new HashMap<>();
        indexed.forEach((interval, values) -> output.put(interval, List.copyOf(values)));
        return Map.copyOf(output);
    }

    static Map<String, List<ProgressionRegion>> indexRegionsByWorld(Iterable<ProgressionRegion> regions) {
        Map<String, List<ProgressionRegion>> indexed = new HashMap<>();
        for (ProgressionRegion region : regions) {
            indexed.computeIfAbsent(region.world(), ignored -> new ArrayList<>()).add(region);
        }
        Map<String, List<ProgressionRegion>> output = new HashMap<>();
        indexed.forEach((worldId, values) -> output.put(worldId, List.copyOf(values)));
        return Map.copyOf(output);
    }

    private static Map<String, List<TitleUnlockRule>> immutableRuleLists(Map<String, List<TitleUnlockRule>> input) {
        Map<String, List<TitleUnlockRule>> output = new HashMap<>();
        input.forEach((trigger, rules) -> output.put(trigger, List.copyOf(rules)));
        return Map.copyOf(output);
    }

    private ProgressionEvent blockEvent(
            String type, ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state
    ) {
        return ProgressionEvent.builder(type, player.getUuid())
                .world(world.getRegistryKey().getValue().toString())
                .dimension(world.getRegistryKey().getValue().toString())
                .biome(biomeId(world, pos))
                .block(Registries.BLOCK.getId(state.getBlock()), ProgressionEvent.tagIds(state.streamTags()))
                .metadata("regions", regionsAt(world, pos))
                .build();
    }

    private ProgressionEvent itemEvent(
            String type, ServerPlayerEntity player, ServerWorld world, ItemStack stack
    ) {
        return ProgressionEvent.builder(type, player.getUuid())
                .world(world.getRegistryKey().getValue().toString())
                .dimension(world.getRegistryKey().getValue().toString())
                .biome(biomeId(world, player.getBlockPos()))
                .item(Registries.ITEM.getId(stack.getItem()), ProgressionEvent.tagIds(stack.streamTags()))
                .metadata("regions", regionsAt(world, player.getBlockPos()))
                .build();
    }

    private String regionsAt(ServerWorld world, BlockPos pos) {
        String worldId = world.getRegistryKey().getValue().toString();
        return regionsAtStatic(worldId, pos, progressionRegionsByWorld.getOrDefault(worldId, List.of()));
    }

    private static String regionsAtStatic(String worldId, BlockPos pos, List<ProgressionRegion> regions) {
        if (regions == null || regions.isEmpty()) return "";
        List<String> matches = regions.stream()
                .filter(region -> region.contains(worldId, pos.getX(), pos.getY(), pos.getZ()))
                .map(ProgressionRegion::id)
                .sorted()
                .toList();
        return String.join(",", matches);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String biomeId(ServerWorld world, BlockPos pos) {
        return world.getBiome(pos).getKey()
                .map(RegistryKey::getValue)
                .map(Identifier::toString)
                .orElse("");
    }

    private static boolean isCropLike(BlockState state) {
        String path = Registries.BLOCK.getId(state.getBlock()).getPath().toLowerCase(Locale.ROOT);
        return path.contains("crop") || path.contains("wheat") || path.contains("carrot")
                || path.contains("potato") || path.contains("beetroot") || path.contains("cocoa");
    }

    private static boolean hasStatusEffect(LivingEntity entity, String id) {
        String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        return entity.getActiveStatusEffects().keySet().stream()
                .map(RegistryEntry::getKey)
                .flatMap(Optional::stream)
                .map(RegistryKey::getValue)
                .map(Identifier::toString)
                .anyMatch(normalized::equals);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        return value == null ? fallback : Boolean.parseBoolean(value);
    }
}
