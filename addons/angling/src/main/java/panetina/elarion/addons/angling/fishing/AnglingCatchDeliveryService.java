package panetina.elarion.addons.angling.fishing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.component.AnglingCaughtFishComponent;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.component.AnglingAttachments;
import panetina.elarion.addons.angling.component.AnglingBaitDebitCursor;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.item.AnglingFishingRodItem;
import panetina.elarion.addons.angling.persistence.AnglingBaitDebitLedger;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CatchTelemetryDetails;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.service.DeferredRewardGrantService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;

/** Core deferred-reward adapter for exact caught components and forced entity catches. */
public final class AnglingCatchDeliveryService implements AnglingCatchCommitCoordinator.Delivery {
    static final String ITEM_ACTION = "elarion-angling-catch-item";
    static final String ENTITY_ACTION = "elarion-angling-catch-entity";
    static final String BAIT_DEBIT_ACTION = "elarion-angling-bait-debit";
    private static final String GRANT_PREFIX = "angling:catch:";

    private final ElarionApi api;
    private final AnglingBaitDebitLedger baitDebits = new AnglingBaitDebitLedger();
    private volatile MinecraftServer server;

    public AnglingCatchDeliveryService(ElarionApi api) {
        this.api = Objects.requireNonNull(api, "api");
        api.rewards().registerHandler(ITEM_ACTION, (context, action) ->
                deliverItem(context.player(), action));
        api.rewards().registerHandler(ENTITY_ACTION, (context, action) ->
                deliverEntity(context.server(), context.rewardId(), action));
        api.rewards().registerHandler(BAIT_DEBIT_ACTION, (context, action) ->
                debitBait(context.player(), context.rewardId(), action));
    }

    public void bind(MinecraftServer server, Path elarionRoot) {
        this.server = Objects.requireNonNull(server, "server");
        baitDebits.bind(elarionRoot);
    }

    public void unbind() {
        baitDebits.shutdown();
        this.server = null;
    }

    @Override
    public CompletionStage<Void> deliver(AnglingCatchCommit commit) {
        Objects.requireNonNull(commit, "commit");
        MinecraftServer bound = server;
        if (bound == null) return CompletableFuture.failedFuture(
                new IllegalStateException("Angling catch delivery is not bound"));
        UUID eventId = commit.telemetry().eventId();
        UUID actorId = commit.telemetry().actorId();
        try {
            baitDebits.record(commit);
        } catch (IOException | RuntimeException exception) {
            return CompletableFuture.failedFuture(new IllegalStateException(
                    "Failed to persist bait debit for Angling catch " + eventId, exception));
        }
        String grantId = GRANT_PREFIX + eventId;
        List<RewardAction> actions = actions(commit);
        DeferredRewardGrantService.EnqueueResult enqueue = api.deferredRewards().enqueueIdempotent(
                grantId, actorId, ElarionAnglingAddon.MOD_ID, eventId.toString(), actions);
        if (enqueue == DeferredRewardGrantService.EnqueueResult.CONFLICT
                || enqueue == DeferredRewardGrantService.EnqueueResult.INVALID) {
            return CompletableFuture.failedFuture(new IllegalStateException(
                    "Conflicting or invalid Core reward grant for Angling catch " + eventId));
        }

        CompletableFuture<Void> dispatched = new CompletableFuture<>();
        bound.execute(() -> {
            try {
                api.events().emitCatchTelemetry(commit.telemetry());
                ServerPlayerEntity player = bound.getPlayerManager().getPlayer(actorId);
                if (player != null && api.deferredRewards().isClaimable(grantId, actorId)) {
                    api.deferredRewards().claim(player, grantId);
                }
                dispatched.complete(null);
            } catch (RuntimeException exception) {
                dispatched.completeExceptionally(exception);
            }
        });
        return dispatched;
    }

    static List<RewardAction> actions(AnglingCatchCommit commit) {
        List<RewardAction> actions = new ArrayList<>(3 + commit.reward().additionalItems().size());
        commit.reward().baitDebit().ifPresent(debit -> actions.add(new RewardAction(BAIT_DEBIT_ACTION,
                Map.of("rod-item-id", debit.rodItemId().toString(),
                        "bait-item-id", debit.baitItemId().toString()))));
        commit.reward().item().ifPresent(item -> actions.add(new RewardAction(ITEM_ACTION,
                itemParameters(commit, item))));
        commit.reward().entity().ifPresent(entity -> actions.add(new RewardAction(ENTITY_ACTION,
                Map.of(
                        "entity-type-id", entity.entityTypeId().toString(),
                        "dimension-id", commit.telemetry().dimensionId().toString(),
                        "x", Double.toString(entity.x()),
                        "y", Double.toString(entity.y()),
                        "z", Double.toString(entity.z())))));
        for (AnglingCatchReward.ItemReward item : commit.reward().additionalItems()) {
            actions.add(new RewardAction(ITEM_ACTION, itemParameters(commit, item)));
        }
        if (actions.isEmpty()) throw new IllegalArgumentException("Angling catch has no delivery actions");
        return List.copyOf(actions);
    }

    private static Map<String, String> itemParameters(
            AnglingCatchCommit commit,
            AnglingCatchReward.ItemReward item
    ) {
        java.util.LinkedHashMap<String, String> values = new java.util.LinkedHashMap<>();
        values.put("item-id", item.itemId().toString());
        values.put("count", Integer.toString(item.count()));
        if (!item.stackNbt().isEmpty()) values.put("stack-nbt", item.stackNbt());
        values.put("caught-fish-component", Boolean.toString(item.caughtFishComponent()));
        values.put("definition-id", commit.telemetry().fishDefinitionId().toString());
        CatchTelemetryDetails details = Objects.requireNonNull(commit.telemetry().details(), "catch details");
        values.put("size-mm", Integer.toString(details.sizeMillimetres()));
        values.put("weight-grams", Long.toString(details.weightGrams()));
        values.put("percentile-bps", Integer.toString(details.percentileBasisPoints()));
        values.put("rarity", commit.telemetry().rarityId().getPath());
        values.put("golden", Boolean.toString(details.goldenCatch()));
        values.put("perfect", Boolean.toString(details.perfectCatch()));
        item.containedItem().ifPresent(contained -> {
            values.put("contained-item-id", contained.itemId().toString());
            values.put("contained-count", Integer.toString(contained.count()));
            values.put("contained-caught-fish-component", Boolean.toString(contained.caughtFishComponent()));
        });
        return Map.copyOf(values);
    }

    private static boolean deliverItem(ServerPlayerEntity player, RewardAction action) {
        try {
            ItemStack prototype = itemStack(action.parameters(), player.getRegistryManager());
            int count = positiveInt(action.parameters(), "count");
            if (!canFit(player.getInventory(), prototype, count)) return false;
            int remaining = count;
            while (remaining > 0) {
                ItemStack stack = prototype.copy();
                stack.setCount(Math.min(remaining, stack.getMaxCount()));
                int inserted = stack.getCount();
                if (!player.getInventory().insertStack(stack) || !stack.isEmpty()) return false;
                remaining -= inserted;
            }
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    static ItemStack itemStack(Map<String, String> values) {
        return itemStack(values, null);
    }

    static ItemStack itemStack(Map<String, String> values, RegistryWrapper.WrapperLookup registries) {
        String serialized = values.getOrDefault("stack-nbt", "");
        if (!serialized.isEmpty()) {
            if (registries == null) throw new IllegalArgumentException("registry lookup is required for exact stack");
            if (serialized.length() > AnglingCatchReward.MAX_SERIALIZED_STACK_CHARS) {
                throw new IllegalArgumentException("serialized catch reward exceeds the delivery bound");
            }
            try {
                byte[] bytes = Base64.getDecoder().decode(serialized);
                NbtCompound root = NbtIo.readCompressed(
                        new ByteArrayInputStream(bytes), NbtSizeTracker.of(1_048_576L));
                ItemStack decoded = ItemStack.fromNbt(registries, root.get("stack")).orElse(ItemStack.EMPTY);
                if (decoded.isEmpty()) throw new IllegalArgumentException("serialized catch reward is empty");
                if (!Registries.ITEM.getId(decoded.getItem()).equals(requiredId(values, "item-id"))) {
                    throw new IllegalArgumentException("serialized catch reward item identity does not match");
                }
                return decoded;
            } catch (IOException | IllegalArgumentException exception) {
                throw new IllegalArgumentException("invalid serialized catch reward", exception);
            }
        }
        Identifier itemId = requiredId(values, "item-id");
        if (!Registries.ITEM.containsId(itemId)) throw new IllegalArgumentException("unknown catch item " + itemId);
        ItemStack stack = new ItemStack(Registries.ITEM.get(itemId));
        AnglingCaughtFishComponent caught = caughtComponent(values);
        if (Boolean.parseBoolean(values.getOrDefault("caught-fish-component", "false"))) {
            stack.set(AnglingDataComponents.CAUGHT_FISH_INFO, caught);
        }
        String nestedId = values.get("contained-item-id");
        if (nestedId != null) {
            Identifier containedId = requiredId(values, "contained-item-id");
            if (!Registries.ITEM.containsId(containedId)) {
                throw new IllegalArgumentException("unknown contained catch item " + containedId);
            }
            ItemStack contained = new ItemStack(Registries.ITEM.get(containedId),
                    positiveInt(values, "contained-count"));
            if (Boolean.parseBoolean(values.getOrDefault("contained-caught-fish-component", "false"))) {
                contained.set(AnglingDataComponents.CAUGHT_FISH_INFO, caught);
            }
            stack.set(AnglingDataComponents.BUCKETED_FISH, new AnglingSingleStackComponent(contained));
        }
        return stack;
    }

    private static AnglingCaughtFishComponent caughtComponent(Map<String, String> values) {
        long weight = Long.parseLong(values.getOrDefault("weight-grams", "-1"));
        if (weight < 0 || weight > Integer.MAX_VALUE) throw new IllegalArgumentException("caught weight is invalid");
        return new AnglingCaughtFishComponent(
                AnglingCaughtFishComponent.CURRENT_SCHEMA_VERSION,
                requiredId(values, "definition-id"),
                nonnegativeInt(values, "size-mm"),
                (int) weight,
                boundedInt(values, "percentile-bps", 0, 10_000),
                AnglingRarity.fromSerializedName(values.getOrDefault("rarity", "")),
                Boolean.parseBoolean(values.getOrDefault("golden", "false")),
                Boolean.parseBoolean(values.getOrDefault("perfect", "false")));
    }

    private static boolean deliverEntity(MinecraftServer server, String rewardId, RewardAction action) {
        try {
            UUID entityUuid = UUID.fromString(rewardId);
            Identifier dimensionId = requiredId(action.parameters(), "dimension-id");
            ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, dimensionId));
            if (world == null) return false;
            double x = finiteDouble(action.parameters(), "x");
            double y = finiteDouble(action.parameters(), "y");
            double z = finiteDouble(action.parameters(), "z");
            world.getChunk(BlockPos.ofFloored(x, y, z));
            if (world.getEntity(entityUuid) != null) return true;
            Identifier typeId = requiredId(action.parameters(), "entity-type-id");
            if (!Registries.ENTITY_TYPE.containsId(typeId)) return false;
            EntityType<?> type = Registries.ENTITY_TYPE.get(typeId);
            Entity entity = type.create(world);
            if (entity == null) return false;
            entity.setUuid(entityUuid);
            entity.refreshPositionAndAngles(x, y, z, 0, 0);
            return world.spawnEntity(entity);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * Applies the catch cost before any reward action. The rod marker is saved
     * with player data before Core records this action complete, so replay after
     * either side of a crash observes exactly one debit.
     */
    private boolean debitBait(ServerPlayerEntity player, String rewardId, RewardAction action) {
        try {
            UUID.fromString(rewardId);
            requiredId(action.parameters(), "rod-item-id");
            Identifier baitId = requiredId(action.parameters(), "bait-item-id");
            long target = baitDebits.totals(player.getUuid()).getOrDefault(baitId, 0L);
            return applyDebit(player, baitId, target);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /** Reconciles only this player's bounded bait types, normally on join. */
    public void reconcile(ServerPlayerEntity player) {
        for (Map.Entry<Identifier, Long> entry : baitDebits.totals(player.getUuid()).entrySet()) {
            if (!applyDebit(player, entry.getKey(), entry.getValue())) return;
        }
    }

    public void unload(UUID actorId) {
        baitDebits.unload(actorId);
    }

    static boolean applyDebit(ServerPlayerEntity player, Identifier baitId, long target) {
        AnglingBaitDebitCursor cursor = player.getAttachedOrCreate(AnglingAttachments.BAIT_DEBIT_CURSOR);
        long applied = cursor.applied(baitId);
        if (applied == target) return true;
        if (applied > target || target < 0) return false;
        long remaining = target - applied;
        PlayerInventory inventory = player.getInventory();
        long available = availableBait(inventory.main, baitId) + availableBait(inventory.offHand, baitId);
        if (available < remaining) return false;
        remaining = consumeBait(inventory.main, baitId, remaining);
        remaining = consumeBait(inventory.offHand, baitId, remaining);
        if (remaining != 0) throw new IllegalStateException("bait availability changed during server-thread debit");
        player.setAttached(AnglingAttachments.BAIT_DEBIT_CURSOR, cursor.withApplied(baitId, target));
        inventory.markDirty();
        player.currentScreenHandler.sendContentUpdates();
        return true;
    }

    private static long availableBait(List<ItemStack> rods, Identifier baitId) {
        long available = 0;
        for (ItemStack rod : rods) {
            if (!(rod.getItem() instanceof AnglingFishingRodItem)) continue;
            ItemStack bait = rod.getOrDefault(
                    AnglingDataComponents.BAIT, AnglingSingleStackComponent.EMPTY).stack();
            if (!bait.isEmpty() && Registries.ITEM.getId(bait.getItem()).equals(baitId)) {
                available = Math.addExact(available, bait.getCount());
            }
        }
        return available;
    }

    private static long consumeBait(List<ItemStack> rods, Identifier baitId, long remaining) {
        for (ItemStack rod : rods) {
            if (remaining == 0) break;
            if (!(rod.getItem() instanceof AnglingFishingRodItem)) continue;
            ItemStack bait = rod.getOrDefault(
                    AnglingDataComponents.BAIT, AnglingSingleStackComponent.EMPTY).stack();
            if (bait.isEmpty() || !Registries.ITEM.getId(bait.getItem()).equals(baitId)) continue;
            int debit = (int) Math.min(remaining, bait.getCount());
            bait.decrement(debit);
            rod.set(AnglingDataComponents.BAIT, new AnglingSingleStackComponent(bait));
            remaining -= debit;
        }
        return remaining;
    }

    private static boolean canFit(PlayerInventory inventory, ItemStack prototype, int count) {
        int remaining = count;
        for (ItemStack existing : inventory.main) {
            if (existing.isEmpty()) {
                remaining -= Math.min(remaining, prototype.getMaxCount());
            } else if (ItemStack.areItemsAndComponentsEqual(existing, prototype)) {
                remaining -= Math.max(0, existing.getMaxCount() - existing.getCount());
            }
            if (remaining <= 0) return true;
        }
        for (ItemStack existing : inventory.offHand) {
            if (ItemStack.areItemsAndComponentsEqual(existing, prototype)) {
                remaining -= Math.max(0, existing.getMaxCount() - existing.getCount());
            }
            if (remaining <= 0) return true;
        }
        return false;
    }

    private static Identifier requiredId(Map<String, String> values, String key) {
        Identifier id = Identifier.tryParse(values.getOrDefault(key, ""));
        if (id == null) throw new IllegalArgumentException("invalid " + key);
        return id;
    }

    private static int positiveInt(Map<String, String> values, String key) {
        return boundedInt(values, key, 1, 99_999);
    }

    private static int nonnegativeInt(Map<String, String> values, String key) {
        return boundedInt(values, key, 0, Integer.MAX_VALUE);
    }

    private static int boundedInt(Map<String, String> values, String key, int minimum, int maximum) {
        int value = Integer.parseInt(values.getOrDefault(key, ""));
        if (value < minimum || value > maximum) throw new IllegalArgumentException("invalid " + key);
        return value;
    }

    private static double finiteDouble(Map<String, String> values, String key) {
        double value = Double.parseDouble(values.getOrDefault(key, ""));
        if (!Double.isFinite(value)) throw new IllegalArgumentException("invalid " + key);
        return value;
    }
}
