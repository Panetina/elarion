package panetina.elarion.addons.angling.fishing;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.definition.AnglingCatchResources;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.minigame.AnglingMinigameSpecFactory;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSession;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifiers;
import panetina.elarion.addons.angling.network.AnglingMinigameStartPayload;
import panetina.elarion.addons.angling.network.AnglingMinigameStatePayload;
import panetina.elarion.addons.angling.registry.AnglingEntities;
import panetina.elarion.addons.angling.restriction.AnglingRestriction;
import panetina.elarion.addons.angling.treasure.AnglingTreasureResolver;
import panetina.elarion.addons.angling.modifier.AnglingModifierValue;
import panetina.elarion.core.api.ElarionApi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Per-bobber, indexed server runtime. No tick path scans players, worlds, or catch history. */
public final class AnglingFishingService {
    private final ElarionApi api;
    private final AnglingCatchCommitCoordinator commits;
    private final AnglingEquipmentModifiers equipment = new AnglingEquipmentModifiers();
    private final AnglingCatchContextFactory contexts = new AnglingCatchContextFactory();
    private final AnglingCatchSelector selector = new AnglingCatchSelector();
    private final AnglingMinigameSpecFactory minigames = new AnglingMinigameSpecFactory();
    private final AnglingCatchOutcomeGenerator outcomes = new AnglingCatchOutcomeGenerator();
    private final AnglingTreasureResolver treasures = new AnglingTreasureResolver();
    private final Map<UUID, ActiveBobber> active = new LinkedHashMap<>();
    private MinecraftServer server;

    public AnglingFishingService(ElarionApi api, AnglingCatchCommitCoordinator commits) {
        this.api = Objects.requireNonNull(api, "api");
        this.commits = Objects.requireNonNull(commits, "commits");
    }

    public void bind(MinecraftServer server) {
        if (this.server != null) throw new IllegalStateException("Angling fishing service is already bound");
        this.server = Objects.requireNonNull(server, "server");
        active.clear();
        AnglingFishingRuntime.bind(this);
    }

    public void unbind() {
        AnglingFishingRuntime.unbind(this);
        active.clear();
        server = null;
    }

    public boolean use(ServerPlayerEntity player, Hand hand, ItemStack rod) {
        MinecraftServer bound = server;
        if (bound == null || !bound.isOnThread() || player.getServer() != bound) return false;
        ActiveBobber reference = active.get(player.getUuid());
        if (reference != null) {
            AnglingFishingBobberEntity bobber = resolve(reference);
            if (bobber == null) {
                active.remove(player.getUuid());
            } else if (bobber.tryReel()) {
                beginCatch(player, bobber);
                return true;
            } else {
                bobber.releaseAndDiscard();
                return true;
            }
        }
        cast(player, hand, rod);
        return true;
    }

    private void cast(ServerPlayerEntity player, Hand hand, ItemStack rod) {
        AnglingEquipmentModifiers.Resolved resolved = equipment.resolve(player, rod);
        AnglingEquipmentModifiers.LureTiming timing = resolved.lureTiming(100, 300, 0.005D);
        AnglingBobberStateMachine machine = new AnglingBobberStateMachine(
                timing.minimumTicks(), timing.maximumTicks(), timing.chancePerTick(), resolved.has("no_gravity"));
        AnglingFishingBobberEntity bobber = new AnglingFishingBobberEntity(
                AnglingEntities.FISHING_BOBBER, player.getWorld());
        bobber.configure(player, machine, resolved.throwSpeedMultiplier(), this, rod, resolved, hand);
        if (!player.getWorld().spawnEntity(bobber)) throw new IllegalStateException("Failed to spawn Angling bobber");
        active.put(player.getUuid(), new ActiveBobber(player.getWorld().getRegistryKey(), bobber.getUuid()));
    }

    private void beginCatch(ServerPlayerEntity player, AnglingFishingBobberEntity bobber) {
        ServerWorld world = player.getServerWorld();
        AnglingCatchSnapshot snapshot = AnglingCatchResources.snapshot();
        java.util.Random selectionRandom = new java.util.Random(world.random.nextLong());
        var context = contexts.create(world, bobber.getBlockPos(), bobber.rod(),
                api.catchTelemetry().summary(player.getUuid()), snapshot, AnglingRestriction.Season.ALL,
                selectionRandom);
        Optional<AnglingCatchSnapshot.NativeCatch> selected = selector.select(
                snapshot, context, selectionRandom, true, bobber.equipment(), player.getLuck());
        if (selected.isEmpty()) {
            bobber.releaseAndDiscard();
            return;
        }
        bobber.selectCatch(selected.orElseThrow());
        bobber.setTreasure(treasures.select(player, bobber.getPos(), bobber.rod(),
                selected.orElseThrow(), selectionRandom).orElse(ItemStack.EMPTY));
        boolean skip = selected.orElseThrow().definition().source().skipsMinigame()
                || bobber.equipment().has("skip_minigame")
                || (bobber.equipment().has("skip_minigame_if_trigger_found")
                && bobber.equipment().has("trigger_skip_minigame"));
        if (skip) {
            complete(player, bobber, false, false, 0, 0);
            return;
        }
        long seed = world.random.nextLong();
        UUID sessionId = UUID.randomUUID();
        var spec = minigames.create(selected.orElseThrow(), bobber.equipment(), true);
        AnglingServerMinigameSession session = new AnglingServerMinigameSession(
                sessionId, player.getUuid(), bobber.getId(), player.getServer().getTicks(),
                AnglingServerMinigameSession.DEFAULT_LIFETIME_TICKS, seed, spec);
        bobber.attachMinigame(session);
        var output = selected.orElseThrow().definition().source().catchInfo();
        Identifier displayItem = output.overrideMinigameItem().orElse(output.item()).id();
        boolean hideCatch = rollWeighted(bobber.equipment(), "hide_catch", selectionRandom);
        ServerPlayNetworking.send(player, AnglingMinigameStartPayload.create(
                sessionId, bobber.getId(), seed, selected.orElseThrow().id(),
                selected.orElseThrow().definition().source().texture(), displayItem,
                bobber.treasure().isEmpty() ? Identifier.ofVanilla("air")
                        : Registries.ITEM.getId(bobber.treasure().getItem()),
                hideCatch, spec));
        ServerPlayNetworking.send(player, new AnglingMinigameStatePayload(session.snapshot()));
    }

    void complete(
            ServerPlayerEntity player,
            AnglingFishingBobberEntity bobber,
            boolean perfect,
            boolean treasure,
            int durationTicks,
            int hits
    ) {
        if (!bobber.beginCommit()) return;
        AnglingCatchSnapshot.NativeCatch selected = bobber.selectedCatch();
        java.util.Random outcomeRandom = new java.util.Random(player.getServerWorld().random.nextLong());
        boolean treasureAwarded = treasure || forcedTreasure(bobber.equipment(), perfect, outcomeRandom);
        List<ItemStack> additionalRewards = new java.util.ArrayList<>();
        if (treasureAwarded && !bobber.treasure().isEmpty()) additionalRewards.add(bobber.treasure());
        for (var modifier : bobber.equipment().modifiers()) {
            if (modifier.value() instanceof AnglingModifierValue.LootTable value) {
                treasures.fromLootTable(player, bobber.getPos(), bobber.rod(), value.lootTable(), outcomeRandom)
                        .ifPresent(additionalRewards::add);
            }
        }
        var previous = api.catchTelemetry().speciesSummary(player.getUuid(), selected.id());
        AnglingCatchOutcome outcome = outcomes.generate(selected, bobber.rod(),
                new AnglingCatchEvaluationContext.SpeciesProgress(
                        previous.totalCount(), previous.goldenCount() > 0),
                perfect, treasure, treasureAwarded, durationTicks, hits,
                outcomeRandom, bobber.equipment(), additionalRewards);
        BlockPos pos = bobber.getBlockPos();
        AnglingCatchReward.RewardPosition rewardPosition = new AnglingCatchReward.RewardPosition(
                bobber.getX(), bobber.getY(), bobber.getZ());
        AnglingCatchReward reward = AnglingCatchReward.from(
                outcome, rewardPosition, player.getServerWorld().getRegistryManager());
        Optional<AnglingCatchReward.BaitDebit> baitDebit = baitDebit(bobber.rod(), outcome);
        if (baitDebit.isPresent()) reward = reward.withBaitDebit(baitDebit.orElseThrow());
        Identifier realmId = realmId(api.citizens().getOrCreate(player).realmId());
        AnglingCatchCommitFactory.Facts facts = new AnglingCatchCommitFactory.Facts(
                UUID.randomUUID(), System.currentTimeMillis(), player.getUuid(),
                player.getWorld().getRegistryKey().getValue(), player.getWorld().getRegistryKey().getValue(),
                player.getWorld().getBiome(pos).getKey().orElseThrow().getValue(),
                itemId(bobber.rod(), AnglingDataComponents.BAIT), Registries.ITEM.getId(bobber.rod().getItem()),
                itemId(bobber.rod(), AnglingDataComponents.BOBBER),
                itemId(bobber.rod(), AnglingDataComponents.HOOK), fluidId(player.getServerWorld(), pos),
                realmId, null, rewardPosition, Optional.of(reward));
        commits.submit(outcome, facts).whenComplete((commit, failure) ->
                player.getServer().execute(bobber::releaseAndDiscard));
    }

    void release(UUID ownerId, UUID bobberId) {
        ActiveBobber current = active.get(ownerId);
        if (current != null && current.entityId().equals(bobberId)) active.remove(ownerId);
    }

    void missedBite(AnglingFishingBobberEntity bobber) {
        ItemStack bait = bobber.rod().getOrDefault(
                AnglingDataComponents.BAIT, AnglingSingleStackComponent.EMPTY).stack();
        boolean bucket = bait.isOf(net.minecraft.item.Items.BUCKET)
                || bait.isIn(net.minecraft.registry.tag.TagKey.of(RegistryKeys.ITEM,
                Identifier.of("c", "buckets/empty")));
        if (!bait.isEmpty() && !bucket) {
            bait.decrement(1);
            bobber.rod().set(AnglingDataComponents.BAIT, new AnglingSingleStackComponent(bait));
        }
        bobber.releaseAndDiscard();
    }

    private AnglingFishingBobberEntity resolve(ActiveBobber reference) {
        ServerWorld world = server == null ? null : server.getWorld(reference.world());
        if (world == null) return null;
        return world.getEntity(reference.entityId()) instanceof AnglingFishingBobberEntity bobber ? bobber : null;
    }

    private static <T> Identifier itemId(
            ItemStack rod,
            net.minecraft.component.ComponentType<AnglingSingleStackComponent> component
    ) {
        ItemStack value = rod.getOrDefault(component, AnglingSingleStackComponent.EMPTY).stack();
        return value.isEmpty() ? null : Registries.ITEM.getId(value.getItem());
    }

    private static Identifier fluidId(ServerWorld world, BlockPos pos) {
        Fluid fluid = world.getFluidState(pos).getFluid();
        if (fluid instanceof FlowableFluid flowable) fluid = flowable.getStill();
        return Registries.FLUID.getId(fluid);
    }

    private static Identifier realmId(String value) {
        if (value == null || value.isBlank()) return null;
        String path = value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
        return Identifier.of("elarion", "realm/" + path);
    }

    private static Optional<AnglingCatchReward.BaitDebit> baitDebit(
            ItemStack rod,
            AnglingCatchOutcome outcome
    ) {
        ItemStack bait = rod.getOrDefault(AnglingDataComponents.BAIT, AnglingSingleStackComponent.EMPTY).stack();
        if (bait.isEmpty()) return Optional.empty();
        boolean bucket = bait.isOf(net.minecraft.item.Items.BUCKET)
                || bait.isIn(net.minecraft.registry.tag.TagKey.of(RegistryKeys.ITEM,
                Identifier.of("c", "buckets/empty")));
        if (bucket && !outcome.item().contains(AnglingDataComponents.BUCKETED_FISH)) return Optional.empty();
        return Optional.of(new AnglingCatchReward.BaitDebit(
                Registries.ITEM.getId(rod.getItem()), Registries.ITEM.getId(bait.getItem())));
    }

    private static boolean forcedTreasure(
            AnglingEquipmentModifiers.Resolved modifiers,
            boolean perfect,
            java.util.random.RandomGenerator random
    ) {
        for (var modifier : modifiers.modifiers()) {
            if (modifier.type().getPath().equals("award_treasure_on_perfect_catch")
                    && modifier.value() instanceof AnglingModifierValue.Weighted value
                    && perfect && random.nextFloat() < value.weight()) return true;
        }
        boolean directSkip = modifiers.has("skip_minigame");
        boolean conditionalSkip = modifiers.has("skip_minigame_if_trigger_found")
                && modifiers.has("trigger_skip_minigame");
        return (directSkip || conditionalSkip) && random.nextFloat() < 0.1F;
    }

    private static boolean rollWeighted(
            AnglingEquipmentModifiers.Resolved modifiers,
            String path,
            java.util.random.RandomGenerator random
    ) {
        for (var modifier : modifiers.modifiers()) {
            if (modifier.type().getPath().equals(path)
                    && modifier.value() instanceof AnglingModifierValue.Weighted value
                    && random.nextFloat() < value.weight()) return true;
        }
        return false;
    }

    private record ActiveBobber(RegistryKey<World> world, UUID entityId) {
    }
}
