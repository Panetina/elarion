package panetina.elarion.addons.angling.fishing;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;

import java.util.Objects;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

/** Immutable server-computed catch result; clients never supply any field. */
public record AnglingCatchOutcome(
        AnglingCatchSnapshot.NativeCatch catchDefinition,
        ItemStack item,
        Optional<Identifier> entityTypeId,
        int sizeMillimetres,
        int weightGrams,
        int percentileBasisPoints,
        boolean golden,
        boolean perfect,
        boolean treasureCompleted,
        boolean treasureAwarded,
        int minigameDurationTicks,
        int minigameHits,
        List<ItemStack> additionalItems
) {
    public static final int MAX_ADDITIONAL_REWARD_STACKS = 256;

    public AnglingCatchOutcome(
            AnglingCatchSnapshot.NativeCatch catchDefinition,
            ItemStack item,
            Optional<Identifier> entityTypeId,
            int sizeMillimetres,
            int weightGrams,
            int percentileBasisPoints,
            boolean golden,
            boolean perfect,
            boolean treasureCompleted,
            int minigameDurationTicks,
            int minigameHits
    ) {
        this(catchDefinition, item, entityTypeId, sizeMillimetres, weightGrams, percentileBasisPoints,
                golden, perfect, treasureCompleted, treasureCompleted, minigameDurationTicks, minigameHits,
                List.of());
    }

    public AnglingCatchOutcome {
        Objects.requireNonNull(catchDefinition, "catchDefinition");
        item = Objects.requireNonNull(item, "item").copy();
        entityTypeId = Objects.requireNonNull(entityTypeId, "entityTypeId");
        if (sizeMillimetres < 0 || weightGrams < 0 || percentileBasisPoints < 0
                || percentileBasisPoints > 10_000 || minigameDurationTicks < 0 || minigameHits < 0) {
            throw new IllegalArgumentException("catch outcome values are outside their bounded range");
        }
        additionalItems = copyItems(additionalItems);
        if (additionalItems.size() > MAX_ADDITIONAL_REWARD_STACKS) {
            throw new IllegalArgumentException("catch outcome has too many additional rewards");
        }
        if (item.isEmpty() && entityTypeId.isEmpty() && additionalItems.isEmpty()) {
            throw new IllegalArgumentException("catch outcome requires an item or entity reward");
        }
    }

    @Override
    public ItemStack item() {
        return item.copy();
    }

    @Override
    public List<ItemStack> additionalItems() {
        return copyItems(additionalItems);
    }

    public AnglingCatchOutcome withRewards(
            ItemStack primary,
            Optional<Identifier> entity,
            List<ItemStack> extras,
            boolean goldenResult,
            boolean treasureResult
    ) {
        return new AnglingCatchOutcome(catchDefinition, primary, entity, sizeMillimetres, weightGrams,
                percentileBasisPoints, goldenResult, perfect, treasureCompleted, treasureResult,
                minigameDurationTicks,
                minigameHits, extras);
    }

    private static List<ItemStack> copyItems(List<ItemStack> values) {
        Objects.requireNonNull(values, "additionalItems");
        ArrayList<ItemStack> copy = new ArrayList<>(values.size());
        for (ItemStack value : values) {
            ItemStack stack = Objects.requireNonNull(value, "additional reward").copy();
            if (stack.isEmpty()) throw new IllegalArgumentException("additional catch reward cannot be empty");
            copy.add(stack);
        }
        return List.copyOf(copy);
    }
}
