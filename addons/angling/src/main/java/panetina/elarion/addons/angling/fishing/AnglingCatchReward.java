package panetina.elarion.addons.angling.fishing;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.component.AnglingCaughtFishComponent;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;

import java.util.Objects;
import java.util.Optional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/** Minimal immutable delivery payload required to reproduce the server-created reward after restart. */
public record AnglingCatchReward(Optional<ItemReward> item, Optional<EntityReward> entity,
                                 java.util.List<ItemReward> additionalItems,
                                 Optional<BaitDebit> baitDebit) {
    public static final int MAX_SERIALIZED_STACK_CHARS = 262_144;
    public AnglingCatchReward {
        item = Objects.requireNonNull(item, "item");
        entity = Objects.requireNonNull(entity, "entity");
        additionalItems = java.util.List.copyOf(Objects.requireNonNull(additionalItems, "additionalItems"));
        baitDebit = Objects.requireNonNull(baitDebit, "baitDebit");
        if (additionalItems.size() > AnglingCatchOutcome.MAX_ADDITIONAL_REWARD_STACKS) {
            throw new IllegalArgumentException("catch reward has too many additional items");
        }
        if (item.isEmpty() && entity.isEmpty() && additionalItems.isEmpty()) {
            throw new IllegalArgumentException("catch reward is empty");
        }
    }

    public AnglingCatchReward(Optional<ItemReward> item, Optional<EntityReward> entity) {
        this(item, entity, java.util.List.of(), Optional.empty());
    }

    public AnglingCatchReward(Optional<ItemReward> item, Optional<EntityReward> entity,
                              java.util.List<ItemReward> additionalItems) {
        this(item, entity, additionalItems, Optional.empty());
    }

    public static AnglingCatchReward from(AnglingCatchOutcome outcome, RewardPosition position) {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(position, "position");
        ItemStack stack = outcome.item();
        Optional<ItemReward> item = stack.isEmpty() ? Optional.empty() : Optional.of(item(stack, ""));
        Optional<EntityReward> entity = outcome.entityTypeId().map(id ->
                new EntityReward(id, position.x(), position.y(), position.z()));
        java.util.List<ItemReward> extras = outcome.additionalItems().stream()
                .map(value -> item(value, "")).toList();
        return new AnglingCatchReward(item, entity, extras, Optional.empty());
    }

    public static AnglingCatchReward from(
            AnglingCatchOutcome outcome,
            RewardPosition position,
            RegistryWrapper.WrapperLookup registries
    ) {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(registries, "registries");
        ItemStack stack = outcome.item();
        Optional<ItemReward> item = stack.isEmpty()
                ? Optional.empty()
                : Optional.of(item(stack, encode(stack, registries)));
        Optional<EntityReward> entity = outcome.entityTypeId().map(id ->
                new EntityReward(id, position.x(), position.y(), position.z()));
        java.util.List<ItemReward> extras = outcome.additionalItems().stream()
                .map(value -> item(value, encode(value, registries))).toList();
        return new AnglingCatchReward(item, entity, extras, Optional.empty());
    }

    public AnglingCatchReward withBaitDebit(BaitDebit debit) {
        return new AnglingCatchReward(item, entity, additionalItems,
                Optional.of(Objects.requireNonNull(debit, "debit")));
    }

    private static ItemReward item(ItemStack stack, String stackNbt) {
        AnglingCaughtFishComponent component = stack.get(AnglingDataComponents.CAUGHT_FISH_INFO);
        AnglingSingleStackComponent nested = stack.get(AnglingDataComponents.BUCKETED_FISH);
        Optional<ContainedItem> contained = Optional.empty();
        if (nested != null && !nested.stack().isEmpty()) {
            ItemStack nestedStack = nested.stack();
            contained = Optional.of(new ContainedItem(
                    Registries.ITEM.getId(nestedStack.getItem()), nestedStack.getCount(),
                    nestedStack.contains(AnglingDataComponents.CAUGHT_FISH_INFO)));
        }
        return new ItemReward(Registries.ITEM.getId(stack.getItem()), stack.getCount(), component != null,
                contained, stackNbt);
    }

    private static String encode(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        try {
            NbtCompound root = new NbtCompound();
            root.put("stack", stack.encode(registries));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NbtIo.writeCompressed(root, output);
            String encoded = Base64.getEncoder().encodeToString(output.toByteArray());
            if (encoded.length() > MAX_SERIALIZED_STACK_CHARS) {
                throw new IllegalArgumentException("serialized catch reward exceeds the journal bound");
            }
            return encoded;
        } catch (IOException exception) {
            throw new IllegalStateException("failed to serialize catch reward", exception);
        }
    }

    public record ItemReward(
            Identifier itemId,
            int count,
            boolean caughtFishComponent,
            Optional<ContainedItem> containedItem,
            String stackNbt
    ) {
        public ItemReward(
                Identifier itemId,
                int count,
                boolean caughtFishComponent,
                Optional<ContainedItem> containedItem
        ) {
            this(itemId, count, caughtFishComponent, containedItem, "");
        }

        public ItemReward {
            Objects.requireNonNull(itemId, "itemId");
            containedItem = Objects.requireNonNull(containedItem, "containedItem");
            stackNbt = Objects.requireNonNull(stackNbt, "stackNbt");
            if (count < 1 || count > 99_999) throw new IllegalArgumentException("catch reward item count is invalid");
            if (stackNbt.length() > MAX_SERIALIZED_STACK_CHARS) {
                throw new IllegalArgumentException("serialized catch reward exceeds the journal bound");
            }
        }
    }

    public record ContainedItem(Identifier itemId, int count, boolean caughtFishComponent) {
        public ContainedItem {
            Objects.requireNonNull(itemId, "itemId");
            if (count < 1 || count > 99_999) throw new IllegalArgumentException("contained item count is invalid");
        }
    }

    public record EntityReward(Identifier entityTypeId, double x, double y, double z) {
        public EntityReward {
            Objects.requireNonNull(entityTypeId, "entityTypeId");
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || Math.abs(x) > 30_000_000 || Math.abs(z) > 30_000_000 || Math.abs(y) > 4096) {
                throw new IllegalArgumentException("catch reward entity position is invalid");
            }
        }
    }

    /** Immutable resource cost carried through the same restart-safe grant as the catch reward. */
    public record BaitDebit(Identifier rodItemId, Identifier baitItemId) {
        public BaitDebit {
            Objects.requireNonNull(rodItemId, "rodItemId");
            Objects.requireNonNull(baitItemId, "baitItemId");
        }
    }

    public record RewardPosition(double x, double y, double z) {
        public RewardPosition {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || Math.abs(x) > 30_000_000 || Math.abs(z) > 30_000_000 || Math.abs(y) > 4096) {
                throw new IllegalArgumentException("catch reward position is invalid");
            }
        }
    }
}
