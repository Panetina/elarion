package panetina.elarion.addons.offerings.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import panetina.elarion.addons.offerings.model.OfferingAnchor;
import panetina.elarion.addons.offerings.model.OfferingContributionResult;
import panetina.elarion.addons.offerings.model.OfferingDonationRecord;
import panetina.elarion.addons.offerings.model.OfferingInstance;
import panetina.elarion.addons.offerings.model.OfferingMilestone;
import panetina.elarion.addons.offerings.model.OfferingProgress;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingProjectLevel;
import panetina.elarion.addons.offerings.model.OfferingRequirement;
import panetina.elarion.addons.offerings.model.OfferingScope;
import panetina.elarion.addons.offerings.storage.OfferingState;
import panetina.elarion.addons.offerings.storage.OfferingStorage;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.registry.ActionContext;
import panetina.elarion.core.registry.MilestoneContext;
import panetina.elarion.core.registry.RegistryExecutionContext;
import panetina.elarion.core.registry.RegistryExecutionResult;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class OfferingService {
    public static final String GLOBAL_NOTIFICATION_FLAG = "ancient_gate_unlocked";
    public static final String OFFERING_SCORE_STAT = "offerings_score";
    private static final int MAX_RECENT_DONATIONS = 50;
    private static final double SHRINE_INTERACTION_RANGE_SQUARED = 64.0D;
    private final Logger logger;
    private final ElarionApi api;
    private final OfferingDefinitionService definitions;
    private final OfferingStorage storage;
    private final OfferingAnchorLocationIndex anchorLocations = new OfferingAnchorLocationIndex();
    private OfferingState state = new OfferingState();
    private MinecraftServer server;
    private boolean completionResumePending;
    private final List<Consumer<Change>> changeListeners = new CopyOnWriteArrayList<>();

    public enum ChangeType { UPSERT, DELETE }

    public record Change(ChangeType type, OfferingInstance instance) {}

    public OfferingService(
            Logger logger,
            ElarionApi api,
            OfferingDefinitionService definitions,
            OfferingStorage storage
    ) {
        this.logger = logger;
        this.api = api;
        this.definitions = definitions;
        this.storage = storage;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        state = storage.load(server);
        anchorLocations.rebuild(state.anchors.values());
        refreshGlobalAccessProjection();
        // Completion resume can emit history/reward events. Core binds history
        // after addon SERVER_STARTED handlers, so run it from the tick hook once
        // Core history is available.
        completionResumePending = true;
    }

    public AutoCloseable onChanged(Consumer<Change> listener) {
        changeListeners.add(listener);
        return () -> changeListeners.remove(listener);
    }

    public synchronized void tick(MinecraftServer server) {
        if (!completionResumePending || this.server != server || !api.history().isBound()) {
            return;
        }
        completionResumePending = false;
        resumeIncompleteCompletions();
    }

    public synchronized void save() {
        if (server != null) storage.save(server, state);
    }

    public synchronized Collection<OfferingInstance> instances() {
        return state.instances.values().stream()
                .sorted(Comparator.comparing(OfferingInstance::id))
                .toList();
    }

    public synchronized Collection<OfferingAnchor> anchors() {
        return state.anchors.values().stream()
                .sorted(Comparator.comparing(OfferingAnchor::id))
                .toList();
    }

    public synchronized Optional<OfferingInstance> findInstance(String id) {
        return Optional.ofNullable(state.instances.get(id));
    }

    public synchronized Optional<OfferingAnchor> findAnchor(String id) {
        return Optional.ofNullable(state.anchors.get(id));
    }

    public synchronized OfferingInstance setDisplayNameOverride(
            String instanceId,
            String displayName,
            ServerPlayerEntity actor
    ) {
        OfferingInstance current = requireInstance(instanceId);
        OfferingInstance updated = current.withDisplayNameOverride(displayName);
        state.instances.put(updated.id(), updated);
        save();
        notifyChanged(ChangeType.UPSERT, updated);
        history("display-name-updated", actorId(actor), updated,
                Map.of("displayNameOverride", updated.displayNameOverride()));
        return updated;
    }

    public synchronized Optional<OfferingAnchor> findAnchorAt(String worldId, BlockPos pos) {
        return anchorLocations.find(worldId, pos);
    }

    public synchronized List<OfferingDonationRecord> recentDonations(String instanceId, int limit) {
        return recentDonations(instanceId, "", limit);
    }

    public synchronized List<OfferingDonationRecord> recentDonations(String instanceId, String levelId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        List<OfferingDonationRecord> records = state.donations.getOrDefault(instanceId, List.of()).stream()
                .filter(record -> levelId == null || levelId.isBlank() || levelId.equals(record.levelId()))
                .toList();
        int from = Math.max(0, records.size() - safeLimit);
        List<OfferingDonationRecord> result = new ArrayList<>(records.subList(from, records.size()));
        java.util.Collections.reverse(result);
        return List.copyOf(result);
    }

    public synchronized OfferingInstance startRealm(String realmId, String projectId, ServerPlayerEntity actor) {
        if (api.realms().find(realmId).isEmpty()) throw new IllegalArgumentException("Unknown Realm " + realmId);
        OfferingProjectDefinition project = requireProject(projectId);
        OfferingInstance instance = create(project, OfferingScope.REALM, realmId, "", 0, 0, 0);
        put(instance);
        history("project-started", actorId(actor), instance, Map.of("scope", "realm"));
        return instance;
    }

    public synchronized OfferingInstance startGlobal(String projectId, ServerPlayerEntity actor) {
        OfferingProjectDefinition project = requireProject(projectId);
        OfferingInstance instance = create(project, OfferingScope.GLOBAL, "", "", 0, 0, 0);
        put(instance);
        history("project-started", actorId(actor), instance, Map.of("scope", "global"));
        return instance;
    }

    public synchronized OfferingInstance startLocation(String projectId, ServerPlayerEntity actor) {
        if (actor == null) throw new IllegalArgumentException("A player source is required for location projects.");
        OfferingProjectDefinition project = requireProject(projectId);
        BlockPos pos = actor.getBlockPos();
        String world = actor.getWorld().getRegistryKey().getValue().toString();
        OfferingInstance instance = create(project, OfferingScope.LOCATION, "", world,
                pos.getX(), pos.getY(), pos.getZ());
        put(instance);
        history("project-started", actor.getUuid(), instance, Map.of("scope", "location", "world", world));
        return instance;
    }

    public synchronized OfferingAnchor linkAnchorAt(
            String instanceId,
            String worldId,
            BlockPos pos,
            ServerPlayerEntity actor
    ) {
        OfferingInstance instance = requireInstance(instanceId);
        findAnchorAt(worldId, pos).ifPresent(existing -> {
            if (!existing.instanceId().equals(instanceId)) {
                throw new IllegalArgumentException("This Shrine is already linked to " + existing.instanceId());
            }
        });
        if (!instance.anchorId().isBlank()) {
            removeStoredAnchor(instance.anchorId());
        }
        String id = nextId(instanceId + "_shrine", state.anchors.keySet());
        OfferingAnchor anchor = new OfferingAnchor(id, instanceId, worldId, pos.getX(), pos.getY(), pos.getZ(),
                actorId(actor), System.currentTimeMillis());
        state.anchors.put(id, anchor);
        anchorLocations.add(anchor);
        OfferingInstance linked = instance.withAnchor(id);
        state.instances.put(instance.id(), linked);
        save();
        notifyChanged(ChangeType.UPSERT, linked);
        history("shrine-linked", actorId(actor), instance, Map.of("anchor", id, "world", worldId));
        return anchor;
    }

    public synchronized void unlinkAnchorAt(String worldId, BlockPos pos, ServerPlayerEntity actor) {
        OfferingAnchor removed = findAnchorAt(worldId, pos)
                .orElseThrow(() -> new IllegalArgumentException("This Shrine is not linked."));
        removeAnchor(removed.id(), actor);
    }

    public synchronized void removeAnchor(String anchorId, ServerPlayerEntity actor) {
        OfferingAnchor removed = removeStoredAnchor(anchorId);
        if (removed == null) throw new IllegalArgumentException("Unknown anchor " + anchorId);
        OfferingInstance instance = requireInstance(removed.instanceId());
        if (instance.anchorId().equals(anchorId)) {
            instance = instance.withAnchor("");
            state.instances.put(instance.id(), instance);
        }
        save();
        notifyChanged(ChangeType.UPSERT, instance);
        history("anchor-removed", actorId(actor), instance, Map.of("anchor", anchorId));
    }

    public synchronized OfferingInstance deleteInstance(String instanceId, ServerPlayerEntity actor) {
        OfferingInstance instance = requireInstance(instanceId);
        history("project-deleted", actorId(actor), instance,
                instance.anchorId().isBlank() ? Map.of() : Map.of("anchor", instance.anchorId()));
        if (!instance.anchorId().isBlank()) {
            removeStoredAnchor(instance.anchorId());
        }
        state.anchors.values().stream()
                .filter(anchor -> anchor.instanceId().equals(instanceId))
                .map(OfferingAnchor::id)
                .toList()
                .forEach(this::removeStoredAnchor);
        state.donations.remove(instanceId);
        state.instances.remove(instanceId);
        save();
        notifyChanged(ChangeType.DELETE, instance);
        return instance;
    }

    public synchronized Optional<OfferingInstance> deleteLinkedInstanceAt(
            String worldId,
            BlockPos pos,
            ServerPlayerEntity actor
    ) {
        Optional<OfferingAnchor> anchor = findAnchorAt(worldId, pos);
        if (anchor.isEmpty()) return Optional.empty();
        OfferingInstance instance = state.instances.get(anchor.get().instanceId());
        if (instance == null) {
            removeStoredAnchor(anchor.get().id());
            save();
            logger.warn("Removed orphaned Shrine link {} for missing instance {}",
                    anchor.get().id(), anchor.get().instanceId());
            return Optional.empty();
        }
        return Optional.of(deleteInstance(instance.id(), actor));
    }

    private OfferingAnchor removeStoredAnchor(String anchorId) {
        OfferingAnchor removed = state.anchors.remove(anchorId);
        if (removed != null) anchorLocations.remove(removed);
        return removed;
    }

    public synchronized OfferingInstance contributeEvent(
            String instanceId,
            String eventId,
            long amount,
            ServerPlayerEntity actor
    ) {
        if (amount < 1) throw new IllegalArgumentException("Amount must be positive.");
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("Event ID is required.");
        OfferingInstance current = requireInstance(instanceId);
        OfferingProjectDefinition project = requireProject(current.projectId());
        OfferingProjectLevel level = currentLevel(project, current);
        String key = "event:" + eventId;
        boolean configured = level.requirements().stream().anyMatch(requirement -> requirement.key().equals(key));
        if (!configured) throw new IllegalArgumentException("Unknown event requirement " + eventId);
        OfferingInstance updated = current.withProgress(key, amount, actorId(actor));
        state.instances.put(updated.id(), updated);
        history("offering-accepted", actorId(actor), updated, Map.of("key", key, "amount", Long.toString(amount)));
        if (isComplete(level, updated)) {
            updated = complete(updated.id(), actor, false);
        } else {
            save();
            notifyChanged(ChangeType.UPSERT, updated);
        }
        return updated;
    }

    public synchronized OfferingContributionResult contributePlayer(
            String instanceId,
            String requirementKey,
            long requestedAmount,
            ServerPlayerEntity player
    ) {
        if (player == null) return OfferingContributionResult.failure("A player is required.");
        if (requestedAmount < 1) return OfferingContributionResult.failure("Amount must be positive.");
        OfferingInstance current;
        OfferingProjectDefinition project;
        OfferingRequirement requirement;
        try {
            current = requireInstance(instanceId);
            project = requireProject(current.projectId());
            OfferingProjectLevel level = currentLevel(project, current);
            requirement = level.requirements().stream()
                    .filter(candidate -> candidate.key().equals(requirementKey))
                    .findFirst()
                    .orElse(null);
        } catch (IllegalArgumentException exception) {
            return OfferingContributionResult.failure(exception.getMessage());
        }
        if (current.completed()) return OfferingContributionResult.failure("This project is already complete.");
        if (requirement == null || (!"items".equals(requirement.type())
                && !"currency".equals(requirement.type()))) {
            return OfferingContributionResult.failure("That requirement cannot be offered directly.");
        }
        String locationError = validateShrineInteraction(current, player);
        if (locationError != null) return OfferingContributionResult.failure(locationError);
        long existing = current.progress().getOrDefault(requirement.key(), 0L);
        long remaining = Math.max(0L, requirement.count() - existing);
        if (remaining == 0L) return OfferingContributionResult.failure("That requirement is already complete.");
        long accepted = Math.min(requestedAmount, remaining);
        return "items".equals(requirement.type())
                ? contributePlayerItems(current, project, requirement, accepted, player)
                : contributePlayerCurrency(current, project, requirement, accepted, player);
    }

    public synchronized OfferingInstance complete(String instanceId, ServerPlayerEntity actor, boolean forced) {
        OfferingInstance current = requireInstance(instanceId);

        OfferingProjectDefinition project = requireProject(current.projectId());
        OfferingProjectLevel level = currentLevel(project, current);

        OfferingInstance updated = executeLevelMilestones(current, level, actor);

        Optional<OfferingProjectLevel> next = project.nextLevel(level.id());
        if (next.isPresent()) {
            history(forced ? "level-force-completed" : "level-completed", actorId(actor), updated,
                    Map.of("level", level.id()));
            notifyRealmOfferingLevel(updated, level, next.get());

            updated = updated.advanceToLevel(next.get().id());
            state.instances.put(updated.id(), updated);
            save();
            notifyChanged(ChangeType.UPSERT, updated);

            history("level-started", actorId(actor), updated, Map.of("level", next.get().id()));
            return updated;
        }

        boolean newlyCompleted = !updated.completed();

        updated = updated.withCompletion(System.currentTimeMillis(), updated.completedMilestones());
        state.instances.put(updated.id(), updated);
        save();
        notifyChanged(ChangeType.UPSERT, updated);

        if (newlyCompleted || forced) {
            history(forced ? "project-force-completed" : "project-completed",
                    actorId(actor), updated, Map.of("level", level.id()));
            notifyRealmOfferingComplete(updated, level);
        }

        return updated;
    }

    public synchronized OfferingInstance reset(String instanceId, ServerPlayerEntity actor) {
        OfferingInstance current = requireInstance(instanceId);
        revertProjectSideEffects(current, actor);
        OfferingInstance reset = resetInstance(current);
        state.instances.put(reset.id(), reset);
        state.donations.remove(instanceId);
        save();
        notifyChanged(ChangeType.UPSERT, reset);
        refreshGlobalAccessProjection();
        history("project-reset", actorId(actor), reset, Map.of());
        return reset;
    }

    public synchronized int resetRealmProgression(String realmId, ServerPlayerEntity actor) {
        String normalizedRealm = realmId == null ? "" : realmId.trim().toLowerCase(java.util.Locale.ROOT);
        int reset = 0;
        for (OfferingInstance instance : List.copyOf(state.instances.values())) {
            if (!normalizedRealm.equals(instance.realmId())) continue;
            revertProjectSideEffects(instance, actor);
            OfferingInstance updated = resetInstance(instance);
            state.instances.put(updated.id(), updated);
            state.donations.remove(updated.id());
            history("project-reset", actorId(actor), updated, Map.of("test-reset", "true"));
            reset++;
        }
        save();
        refreshGlobalAccessProjection();
        state.instances.values().stream()
                .filter(instance -> normalizedRealm.equals(instance.realmId()))
                .forEach(instance -> notifyChanged(ChangeType.UPSERT, instance));
        return reset;
    }

    public synchronized int resetAllProgression(ServerPlayerEntity actor) {
        int reset = 0;
        for (OfferingInstance instance : List.copyOf(state.instances.values())) {
            revertProjectSideEffects(instance, actor);
            OfferingInstance updated = resetInstance(instance);
            state.instances.put(updated.id(), updated);
            history("project-reset", actorId(actor), updated, Map.of("test-reset", "true"));
            reset++;
        }
        state.donations.clear();
        save();
        refreshGlobalAccessProjection();
        state.instances.values().forEach(instance -> notifyChanged(ChangeType.UPSERT, instance));
        return reset;
    }

    public synchronized int deleteWorld(String worldId) {
        List<String> ids = state.instances.values().stream()
                .filter(instance -> worldId.equals(instance.worldId())
                        || (!instance.anchorId().isBlank() && state.anchors.containsKey(instance.anchorId())
                        && worldId.equals(state.anchors.get(instance.anchorId()).worldId())))
                .map(OfferingInstance::id).toList();
        for (String id : ids) deleteInstance(id, null);
        return ids.size();
    }

    public synchronized OfferingProgress progress(String instanceId) {
        OfferingInstance instance = requireInstance(instanceId);
        OfferingProjectDefinition project = requireProject(instance.projectId());
        OfferingProjectLevel level = currentLevel(project, instance);
        List<OfferingProgress.Row> rows = new ArrayList<>();
        boolean complete = true;
        for (OfferingRequirement requirement : level.requirements()) {
            long current = instance.progress().getOrDefault(requirement.key(), 0L);
            boolean rowComplete = current >= requirement.count();
            complete &= rowComplete;
            rows.add(new OfferingProgress.Row(requirement.key(), current, requirement.count(), rowComplete));
        }
        return new OfferingProgress(instanceId, complete, rows);
    }

    public synchronized boolean hasRealmFlag(String realmId, String flag) {
        return state.realmFlags.getOrDefault(realmId, Set.of()).contains(flag);
    }

    public synchronized boolean setRealmFlag(String realmId, String flag, boolean enabled) {
        RegistryExecutionResult result = setRealmFlagInternal(realmId, flag, enabled);
        if (!result.success()) {
            throw new IllegalArgumentException(result.message().isBlank() ? "Could not update Realm flag." : result.message());
        }
        save();
        refreshGlobalAccessProjection();
        return hasRealmFlag(realmId, flag);
    }

    private OfferingProjectDefinition requireProject(String projectId) {
        return definitions.find(projectId)
                .filter(OfferingProjectDefinition::enabled)
                .orElseThrow(() -> new IllegalArgumentException("Unknown or disabled project " + projectId));
    }

    private OfferingInstance requireInstance(String instanceId) {
        OfferingInstance instance = state.instances.get(instanceId);
        if (instance == null) throw new IllegalArgumentException("Unknown project instance " + instanceId);
        return instance;
    }

    private OfferingInstance resetInstance(OfferingInstance instance) {
        OfferingProjectDefinition project = requireProject(instance.projectId());
        return instance.reset(project.firstLevel().id());
    }

    private void revertProjectSideEffects(OfferingInstance instance, ServerPlayerEntity actor) {
        OfferingProjectDefinition project = requireProject(instance.projectId());
        for (OfferingProjectLevel level : project.levels()) {
            for (OfferingMilestone milestone : level.milestones()) {
                if ("elarion:set_realm_flag".equals(milestone.type())) {
                    String flag = milestone.parameters().get("flag");
                    if (flag != null && !flag.isBlank()) {
                        setRealmFlagInternal(instance.realmId(), flag, false);
                    }
                } else if ("elarion:portal_unlock".equals(milestone.type())) {
                    lockPortalMilestone(instance, milestone, actor);
                }
            }
        }
    }

    private void lockPortalMilestone(
            OfferingInstance instance,
            OfferingMilestone milestone,
            ServerPlayerEntity actor
    ) {
        if (!api.registries().actions().contains("elarion:portal_lock")) return;
        Map<String, String> parameters = new LinkedHashMap<>(milestone.parameters());
        parameters.putIfAbsent("project", instance.projectId());
        parameters.putIfAbsent("instance", instance.id());
        parameters.putIfAbsent("realm", instance.realmId());
        parameters.putIfAbsent("world", instance.worldId());
        parameters.putIfAbsent("route", instance.realmId());
        RegistryExecutionContext execution = new RegistryExecutionContext(
                api, server, actor, actorId(actor), instance.realmId(), null, instance.realmId(),
                instance.worldId(), "elarion_offerings", parameters);
        RegistryExecutionResult result = api.registries().execute(new ActionContext(
                execution, "elarion:portal_lock", parameters));
        if (!result.success()) {
            logger.warn("offering reset could not lock portal route for {}: {}", instance.id(), result.message());
        }
    }

    private OfferingInstance create(
            OfferingProjectDefinition project,
            OfferingScope scope,
            String realmId,
            String worldId,
            int x,
            int y,
            int z
    ) {
        if (!project.allowMultipleInstances() && state.instances.values().stream()
                .anyMatch(existing -> existing.projectId().equals(project.id()) && !existing.completed())) {
            throw new IllegalArgumentException("Project " + project.id() + " already has an active instance.");
        }
        String base = instanceBase(scope, realmId);
        int counter = state.projectCounters.merge(base, 1, Integer::sum);
        String id = base + "_" + counter;
        while (state.instances.containsKey(id)) {
            counter = state.projectCounters.merge(base, 1, Integer::sum);
            id = base + "_" + counter;
        }
        return new OfferingInstance(id, project.id(), project.firstLevel().id(), scope, realmId, worldId, x, y, z, "",
                Map.of(), Map.of(), Set.of(), System.currentTimeMillis(), 0L);
    }

    private static String instanceBase(OfferingScope scope, String realmId) {
        return switch (scope) {
            case REALM -> "offering_realm_" + safeId(realmId);
            case GLOBAL -> "offering_global";
            case LOCATION -> "offering_location";
        };
    }

    private static String safeId(String value) {
        if (value == null || value.isBlank()) return "unknown";
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
    }

    private void put(OfferingInstance instance) {
        state.instances.put(instance.id(), instance);
        save();
        notifyChanged(ChangeType.UPSERT, instance);
    }

    private OfferingContributionResult contributePlayerItems(
            OfferingInstance current,
            OfferingProjectDefinition project,
            OfferingRequirement requirement,
            long amount,
            ServerPlayerEntity player
    ) {
        if (amount > Integer.MAX_VALUE) return OfferingContributionResult.failure("Item amount is too large.");
        List<ItemStack> removed = removeMatchingItems(player, requirement.id(), (int) amount);
        long removedCount = removed.stream().mapToLong(ItemStack::getCount).sum();
        if (removedCount < amount) {
            restoreItems(current, player, removed);
            return OfferingContributionResult.failure("You do not have enough matching items.");
        }
        try {
            OfferingInstance updated = persistPlayerContribution(
                    current, project, requirement, amount, player, "items");
            return OfferingContributionResult.success(
                    "Offered " + amount + " item" + (amount == 1 ? "" : "s") + ".", amount, updated);
        } catch (RuntimeException exception) {
            restoreItems(current, player, removed);
            return OfferingContributionResult.failure("The offering could not be saved; your items were restored.");
        }
    }

    private OfferingContributionResult contributePlayerCurrency(
            OfferingInstance current,
            OfferingProjectDefinition project,
            OfferingRequirement requirement,
            long amount,
            ServerPlayerEntity player
    ) {
        var economy = ElarionEconomyApi.get();
        var payment = economy.payPhysicalOnly(
                player,
                amount,
                "Shrine offering for " + current.id(),
                "elarion_offerings");
        if (!payment.successful()) {
            return OfferingContributionResult.failure(payment.message());
        }
        try {
            OfferingInstance updated = persistPlayerContribution(
                    current, project, requirement, amount, player, "currency");
            return OfferingContributionResult.success(
                    "Offered " + api.serverIdentity().currencyAmount(amount) + ".", amount, updated);
        } catch (RuntimeException exception) {
            economy.refundMixedPayment(player, payment,
                    "Offering persistence compensation for " + current.id(), "elarion_offerings");
            return OfferingContributionResult.failure(
                    "The offering could not be saved; your carried currency was restored.");
        }
    }

    private OfferingInstance persistPlayerContribution(
            OfferingInstance current,
            OfferingProjectDefinition project,
            OfferingRequirement requirement,
            long amount,
            ServerPlayerEntity player,
            String donationType
    ) {
        OfferingState previous = state.copy();
        OfferingInstance updated = current.withProgress(requirement.key(), amount, player.getUuid());
        state.instances.put(updated.id(), updated);
        addDonation(updated.id(), new OfferingDonationRecord(
                player.getUuid(), player.getGameProfile().getName(), updated.activeLevelId(), requirement.key(),
                donationType, amount, System.currentTimeMillis()));
        try {
            storage.saveChecked(server, state);
        } catch (IOException exception) {
            state = previous;
            throw new IllegalStateException("Unable to persist offering contribution", exception);
        }
        incrementOfferingScore(player.getUuid(), amount);
        history("offering-accepted", player.getUuid(), updated,
                Map.of("key", requirement.key(), "amount", Long.toString(amount), "type", donationType));
        if (isComplete(currentLevel(project, updated), updated)) {
            updated = complete(updated.id(), player, false);
        } else {
            notifyChanged(ChangeType.UPSERT, updated);
        }
        return updated;
    }

    private void notifyChanged(ChangeType type, OfferingInstance instance) {
        Change change = new Change(type, instance);
        for (Consumer<Change> listener : changeListeners) {
            try {
                listener.accept(change);
            } catch (RuntimeException exception) {
                logger.error("Offering projection listener failed for {}", instance.id(), exception);
            }
        }
    }

    private void incrementOfferingScore(UUID playerId, long amount) {
        api.playerStats().increment(playerId, OFFERING_SCORE_STAT, Math.max(0L, amount));
    }

    private void addDonation(String instanceId, OfferingDonationRecord donation) {
        List<OfferingDonationRecord> records =
                new ArrayList<>(state.donations.getOrDefault(instanceId, List.of()));
        records.add(donation);
        if (records.size() > MAX_RECENT_DONATIONS) {
            records = new ArrayList<>(records.subList(records.size() - MAX_RECENT_DONATIONS, records.size()));
        }
        state.donations.put(instanceId, records);
    }

    private String validateShrineInteraction(OfferingInstance instance, ServerPlayerEntity player) {
        if (instance.anchorId().isBlank()) return "This project is not linked to a Shrine.";
        OfferingAnchor anchor = state.anchors.get(instance.anchorId());
        if (anchor == null || !anchor.instanceId().equals(instance.id())) return "The Shrine link is invalid.";
        String playerWorld = player.getWorld().getRegistryKey().getValue().toString();
        if (!anchor.worldId().equals(playerWorld)) return "You are not at this Shrine.";
        double distance = player.squaredDistanceTo(
                anchor.x() + 0.5D, anchor.y() + 0.5D, anchor.z() + 0.5D);
        return distance <= SHRINE_INTERACTION_RANGE_SQUARED ? null : "You are too far from the Shrine.";
    }

    private static List<ItemStack> removeMatchingItems(
            ServerPlayerEntity player,
            String itemOrTag,
            int amount
    ) {
        List<ItemStack> removed = new ArrayList<>();
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().size() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isEmpty() || !matches(stack, itemOrTag)) continue;
            int take = Math.min(remaining, stack.getCount());
            ItemStack copy = stack.copyWithCount(take);
            stack.decrement(take);
            removed.add(copy);
            remaining -= take;
        }
        return removed;
    }

    private static boolean matches(ItemStack stack, String itemOrTag) {
        if (itemOrTag == null || itemOrTag.isBlank()) return false;
        if (itemOrTag.startsWith("#")) {
            Identifier id = Identifier.tryParse(itemOrTag.substring(1));
            return id != null && stack.isIn(TagKey.of(RegistryKeys.ITEM, id));
        }
        Identifier id = Identifier.tryParse(itemOrTag);
        return id != null && Registries.ITEM.getId(stack.getItem()).equals(id);
    }

    private void restoreItems(
            OfferingInstance instance,
            ServerPlayerEntity player,
            List<ItemStack> stacks
    ) {
        long dropped = 0L;
        for (ItemStack stack : stacks) {
            ItemStack restored = stack.copy();
            if (!player.getInventory().insertStack(restored) && !restored.isEmpty()) {
                dropped += restored.getCount();
                player.dropItem(restored, false);
            }
        }
        if (dropped > 0) {
            history("offering-rollback-dropped", player.getUuid(), instance,
                    Map.of("amount", Long.toString(dropped)));
            logger.warn("Offering rollback dropped {} item(s) at player {} because inventory restore was full",
                    dropped, player.getGameProfile().getName());
        }
    }

    private void distributeReward(OfferingInstance instance, OfferingMilestone milestone) {
        String rewardId = milestone.parameters().getOrDefault(
                "reward", milestone.parameters().getOrDefault("id", ""));
        List<panetina.elarion.core.model.RewardAction> actions = api.rewards().actions(rewardId);
        if (rewardId.isBlank() || actions.isEmpty()) {
            history("milestone-failed", null, instance,
                    Map.of("milestone", milestone.id(), "message", "unknown or empty reward " + rewardId));
            return;
        }
        for (panetina.elarion.core.model.CitizenRecord citizen : rewardRecipients(instance)) {
            String grantId = "offering:" + instance.id() + ":g" + instance.resetGeneration()
                    + ":" + milestone.id() + ":" + citizen.uuid();
            api.deferredRewards().enqueue(grantId, citizen.uuid(), "elarion_offerings",
                    rewardId, actions);
        }
        history("milestone-completed", null, instance,
                Map.of("milestone", milestone.id(), "reward", rewardId));
    }

    private OfferingInstance executeLevelMilestones(
            OfferingInstance instance,
            OfferingProjectLevel level,
            ServerPlayerEntity actor
    ) {
        Set<String> milestones = new LinkedHashSet<>(instance.completedMilestones());
        OfferingInstance updated = instance;
        for (OfferingMilestone milestone : level.milestones()) {
            if (milestones.contains(milestone.id())) continue;
            if ("elarion:run_reward".equals(milestone.type())) distributeReward(updated, milestone);
            else executeMilestone(updated, milestone, actor);
            milestones.add(milestone.id());
            updated = updated.withCompletedMilestones(milestones);
            state.instances.put(updated.id(), updated);
            save();
        }
        return updated;
    }

    private void resumeIncompleteCompletions() {
        if (server == null) return;
        for (OfferingInstance instance : List.copyOf(state.instances.values())) {
            if (!instance.completed()) continue;
            OfferingProjectDefinition project = definitions.find(instance.projectId()).orElse(null);
            if (project == null) continue;
            OfferingProjectLevel level = currentLevel(project, instance);
            if (level.milestones().stream()
                    .allMatch(milestone -> instance.completedMilestones().contains(milestone.id()))) {
                continue;
            }
            try {
                complete(instance.id(), null, false);
            } catch (RuntimeException exception) {
                logger.error("Failed to resume completion for Offering instance {}", instance.id(), exception);
            }
        }
    }

    private List<CitizenRecord> rewardRecipients(OfferingInstance instance) {
        Set<String> contributors = instance.contributorTotals().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());
        java.util.stream.Stream<CitizenRecord> candidates = switch (instance.scope()) {
            case REALM -> api.citizens().citizenIdsInRealm(instance.realmId()).stream()
                    .map(api.citizens()::find)
                    .flatMap(Optional::stream);
            case GLOBAL -> api.citizens().all().stream();
            case LOCATION -> contributors.stream()
                    .map(this::findCitizen)
                    .flatMap(Optional::stream);
        };
        return candidates
                .filter(api.citizens()::isActiveCitizen)
                .toList();
    }

    private Optional<CitizenRecord> findCitizen(String citizenId) {
        try {
            return api.citizens().find(UUID.fromString(citizenId));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private OfferingProjectLevel currentLevel(OfferingProjectDefinition project, OfferingInstance instance) {
        return project.level(instance.activeLevelId()).orElse(project.firstLevel());
    }

    private boolean isComplete(OfferingProjectLevel level, OfferingInstance instance) {
        return level.requirements().stream()
                .allMatch(req -> instance.progress().getOrDefault(req.key(), 0L) >= req.count());
    }

    private void executeMilestone(OfferingInstance instance, OfferingMilestone milestone, ServerPlayerEntity actor) {
        Map<String, String> parameters = new LinkedHashMap<>(milestone.parameters());
        parameters.putIfAbsent("project", instance.projectId());
        parameters.putIfAbsent("instance", instance.id());
        parameters.putIfAbsent("realm", instance.realmId());
        RegistryExecutionContext execution = new RegistryExecutionContext(
                api, server, actor, actorId(actor), instance.realmId(), null, instance.realmId(),
                instance.worldId(), "elarion_offerings", parameters);
        RegistryExecutionResult result = switch (milestone.type()) {
            case "elarion:set_realm_flag" -> setRealmFlagInternal(instance.realmId(), parameters.get("flag"), true);
            case "elarion:clear_realm_flag" -> setRealmFlagInternal(instance.realmId(), parameters.get("flag"), false);
            case "elarion:run_reward" -> api.registries().execute(new MilestoneContext(execution,
                    "elarion:run_reward", parameters));
            case "elarion:emit_history" -> api.registries().execute(new MilestoneContext(execution,
                    "elarion:emit_history", parameters));
            case "elarion:notify_realm" -> notifyMilestone(instance, milestone, parameters, false);
            case "elarion:notify_world" -> notifyMilestone(instance, milestone, parameters, true);
            default -> api.registries().actions().contains(milestone.type())
                    ? api.registries().execute(new ActionContext(execution, milestone.type(), parameters))
                    : api.registries().execute(new MilestoneContext(execution, milestone.type(), parameters));
        };
        if (!result.success()) {
            logger.warn("offering milestone {} failed for {}: {}", milestone.id(), instance.id(), result.message());
            history("milestone-failed", actorId(actor), instance,
                    Map.of("milestone", milestone.id(), "message", result.message()));
        } else {
            history("milestone-completed", actorId(actor), instance, Map.of("milestone", milestone.id()));
        }
    }

    private RegistryExecutionResult notifyMilestone(
            OfferingInstance instance,
            OfferingMilestone milestone,
            Map<String, String> parameters,
            boolean world
    ) {
        String title = parameters.getOrDefault("title", "Offering Milestone");
        String body = parameters.getOrDefault("body", "A Shrine milestone was completed.");
        String icon = parameters.getOrDefault("icon", "item:minecraft:amethyst_shard");
        String dedupe = "offering:" + instance.id() + ":g" + instance.resetGeneration() + ":" + milestone.id();
        var actions = List.of(new panetina.elarion.core.model.ElarionNotificationAction(
                panetina.elarion.core.service.ElarionNotificationService.DISMISS, "Dismiss", true));
        if (world) {
            api.notifications().publishWorld("elarion_offerings", "milestone", dedupe,
                    title, body, "World Offering", icon, actions,
                    Map.of("instanceId", instance.id(), "projectId", instance.projectId()),
                    api.notifications().defaultExpiry());
        } else {
            if (instance.realmId().isBlank()) {
                return RegistryExecutionResult.failure("Realm notification milestone requires a Realm instance.");
            }
            api.notifications().publishRealm(instance.realmId(),
                    panetina.elarion.core.model.ElarionNotificationCategory.REALM,
                    "elarion_offerings", "milestone", dedupe,
                    title, body, "Shrine Milestone", icon, actions,
                    Map.of("instanceId", instance.id(), "projectId", instance.projectId()),
                    api.notifications().defaultExpiry());
        }
        return RegistryExecutionResult.ok("Notification published.");
    }

    private RegistryExecutionResult setRealmFlagInternal(String realmId, String flag, boolean enabled) {
        if (realmId == null || realmId.isBlank()) return RegistryExecutionResult.failure("realm flag needs a Realm");
        if (flag == null || flag.isBlank()) return RegistryExecutionResult.failure("realm flag needs a flag");
        state.realmFlags.computeIfAbsent(realmId, ignored -> new LinkedHashSet<>());
        boolean changed = enabled
                ? state.realmFlags.get(realmId).add(flag)
                : state.realmFlags.get(realmId).remove(flag);
        if (GLOBAL_NOTIFICATION_FLAG.equals(flag)) {
            api.notifications().setWorldRealmEligible(realmId, enabled);
            if (server != null) {
                api.identitySync().syncAll(server);
            }
            if (changed) {
                realmHistory("realm-global-access-changed", realmId, Map.of(
                        "flag", flag,
                        "enabled", Boolean.toString(enabled)));
                api.system().events().emitDomainEvent(ElarionDomainEvent.of(
                        "elarion_offerings",
                        "realm-global-access-changed",
                        null,
                        realmId,
                        "realm",
                        realmId,
                        Map.of(
                                "flag", flag,
                                "enabled", Boolean.toString(enabled))));
            }
        }
        return RegistryExecutionResult.ok();
    }

    private void refreshGlobalAccessProjection() {
        api.notifications().replaceWorldEligibleRealms(state.realmFlags.entrySet().stream()
                .filter(entry -> entry.getValue().contains(GLOBAL_NOTIFICATION_FLAG))
                .map(Map.Entry::getKey)
                .toList());
        if (server != null) {
            api.identitySync().syncAll(server);
        }
    }

    private void history(String type, UUID actorId, OfferingInstance instance, Map<String, String> metadata) {
        if (server == null) return;
        Map<String, String> data = new LinkedHashMap<>(metadata);
        data.put("project", instance.projectId());
        data.put("instance", instance.id());
        data.put("scope", instance.scope().name().toLowerCase());
        api.history().recordChronicle("offering", type, actorId, "project", instance.id(), instance.realmId(),
                data, "The project " + instance.projectId() + " recorded offering event " + type + ".");
    }

    private void realmHistory(String type, String realmId, Map<String, String> metadata) {
        if (server == null) return;
        api.history().recordChronicle("offering", type, null, "realm", realmId, realmId,
                metadata, "The Realm " + realmId + " recorded offering event " + type + ".");
    }

    private void notifyRealmOfferingLevel(
            OfferingInstance instance,
            OfferingProjectLevel completed,
            OfferingProjectLevel next
    ) {
        if (instance.scope() != OfferingScope.REALM || instance.realmId().isBlank()) return;
        api.realmDeliveries().notifyRealm(
                instance.realmId(),
                "Shrine Level Up",
                completed.displayName() + " is complete. " + next.displayName() + " is now active.",
                "offering",
                null);
    }

    private void notifyRealmOfferingComplete(OfferingInstance instance, OfferingProjectLevel completed) {
        if (instance.scope() != OfferingScope.REALM || instance.realmId().isBlank()) return;
        api.realmDeliveries().notifyRealm(
                instance.realmId(),
                "Shrine Project Complete",
                completed.displayName() + " is complete.",
                "offering",
                null);
    }

    private static UUID actorId(ServerPlayerEntity actor) {
        return actor == null ? null : actor.getUuid();
    }

    private static String nextId(String base, Collection<String> existing) {
        String sanitized = base.replaceAll("[^a-zA-Z0-9_.-]", "_");
        int index = 1;
        String id = sanitized + "_" + index;
        while (existing.contains(id)) id = sanitized + "_" + ++index;
        return id;
    }
}
