package panetina.elarion.addons.angling.fishing;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.component.AnglingCaughtFishComponent;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.item.AnglingBucketableFishItem;
import panetina.elarion.addons.angling.registry.AnglingItems;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifiers;
import panetina.elarion.addons.angling.modifier.AnglingModifierValue;
import panetina.elarion.addons.angling.definition.AnglingCatchType;

import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;
import java.util.ArrayList;
import java.util.List;

/** Frozen size/weight/golden distribution and component-safe item construction. */
public final class AnglingCatchOutcomeGenerator {
    private static final TagKey<Item> EMPTY_BUCKETS = TagKey.of(
            RegistryKeys.ITEM, Identifier.of("c", "buckets/empty"));

    public AnglingCatchOutcome generate(
            AnglingCatchSnapshot.NativeCatch selected,
            ItemStack rod,
            AnglingCatchEvaluationContext.SpeciesProgress previous,
            boolean perfect,
            boolean treasureCompleted,
            int durationTicks,
            int hits,
            RandomGenerator random
    ) {
        return generate(selected, rod, previous, perfect, treasureCompleted, durationTicks, hits,
                random, new AnglingEquipmentModifiers.Resolved(List.of()), List.of());
    }

    public AnglingCatchOutcome generate(
            AnglingCatchSnapshot.NativeCatch selected,
            ItemStack rod,
            AnglingCatchEvaluationContext.SpeciesProgress previous,
            boolean perfect,
            boolean treasureCompleted,
            int durationTicks,
            int hits,
            RandomGenerator random,
            AnglingEquipmentModifiers.Resolved modifiers,
            List<ItemStack> externalRewards
    ) {
        return generate(selected, rod, previous, perfect, treasureCompleted, treasureCompleted, durationTicks,
                hits, random, modifiers, externalRewards);
    }

    public AnglingCatchOutcome generate(
            AnglingCatchSnapshot.NativeCatch selected,
            ItemStack rod,
            AnglingCatchEvaluationContext.SpeciesProgress previous,
            boolean perfect,
            boolean treasureCompleted,
            boolean treasureAwarded,
            int durationTicks,
            int hits,
            RandomGenerator random,
            AnglingEquipmentModifiers.Resolved modifiers,
            List<ItemStack> externalRewards
    ) {
        Objects.requireNonNull(selected, "selected");
        Objects.requireNonNull(rod, "rod");
        Objects.requireNonNull(previous, "previous");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(modifiers, "modifiers");
        Objects.requireNonNull(externalRewards, "externalRewards");
        int percentile = random.nextInt(10_000);
        var distribution = selected.definition().source().sizeAndWeight();
        int sizeCentimetres = sample(
                distribution.averageSizeCentimeters(), distribution.deviationSizeCentimeters(), percentile);
        int weightGrams = sample(
                distribution.averageWeightGrams(), distribution.deviationWeightGrams(), percentile);
        int sizeMillimetres = Math.multiplyExact(sizeCentimetres, 10);
        boolean golden = !previous.goldenCaught() && random.nextFloat() < distribution.goldenChance();
        if (!previous.goldenCaught()) {
            for (AnglingCompiledModifier modifier : modifiers.modifiers()) {
                if (modifier.value() instanceof AnglingModifierValue.ExtraGolden value
                        && (!value.perfectOnly() || perfect)
                        && random.nextFloat() < value.weight()) {
                    golden = true;
                    break;
                }
            }
        }
        if (modifiers.has("cancel_golden")) golden = false;
        ItemStack reward = item(selected, rod, sizeMillimetres, weightGrams, percentile, golden, perfect);
        Optional<Identifier> availableEntity = selected.definition().source().catchInfo().entity();
        boolean forceEntity = selected.definition().source().catchInfo().alwaysSpawnEntity();
        if (!forceEntity && availableEntity.isPresent()) {
            for (AnglingCompiledModifier modifier : modifiers.modifiers()) {
                if (modifier.type().getPath().equals("force_fish_entity")
                        && modifier.value() instanceof AnglingModifierValue.Weighted value
                        && random.nextFloat() < value.weight()) {
                    forceEntity = true;
                    break;
                }
            }
        }
        Optional<Identifier> entity = forceEntity ? availableEntity : Optional.empty();
        if (entity.isPresent()) reward = ItemStack.EMPTY;
        if (modifiers.has("remove_base_fished_item")) reward = ItemStack.EMPTY;
        ArrayList<ItemStack> extras = new ArrayList<>(externalRewards.size() + 8);
        externalRewards.forEach(value -> extras.add(value.copy()));
        if (selected.type() == AnglingCatchType.FISH) {
            for (AnglingCompiledModifier modifier : modifiers.modifiers()) {
                if (!(modifier.value() instanceof AnglingModifierValue.CountAndPerfect value)
                        || !modifier.type().getPath().equals("extra_base_catch")
                        || (value.perfectOnly() && !perfect)) continue;
                if (extras.size() + value.count() > AnglingCatchOutcome.MAX_ADDITIONAL_REWARD_STACKS) {
                    throw new IllegalStateException("extra catch modifiers exceed the reward stack bound");
                }
                for (int index = 0; index < value.count(); index++) {
                    int extraPercentile = random.nextInt(10_000);
                    int extraSize = Math.multiplyExact(sample(distribution.averageSizeCentimeters(),
                            distribution.deviationSizeCentimeters(), extraPercentile), 10);
                    int extraWeight = sample(distribution.averageWeightGrams(),
                            distribution.deviationWeightGrams(), extraPercentile);
                    extras.add(nonBucketItem(selected, extraSize, extraWeight, extraPercentile, false, perfect));
                }
            }
        }
        return new AnglingCatchOutcome(selected, reward, entity, sizeMillimetres, weightGrams,
                percentile, golden, perfect, treasureCompleted, treasureAwarded, durationTicks, hits, extras);
    }

    private static ItemStack item(
            AnglingCatchSnapshot.NativeCatch selected,
            ItemStack rod,
            int sizeMillimetres,
            int weightGrams,
            int percentile,
            boolean golden,
            boolean perfect
    ) {
        ItemStack base = nonBucketItem(selected, sizeMillimetres, weightGrams, percentile, golden, perfect);
        var output = selected.definition().source().catchInfo();
        Item outputItem = Registries.ITEM.get(output.item().id());
        AnglingCaughtFishComponent component = base.get(AnglingDataComponents.CAUGHT_FISH_INFO);

        ItemStack bait = rod.getOrDefault(AnglingDataComponents.BAIT, AnglingSingleStackComponent.EMPTY).stack();
        if (output.fishBucket().isPresent() && (bait.isOf(Items.BUCKET) || bait.isIn(EMPTY_BUCKETS))) {
            if (outputItem instanceof AnglingBucketableFishItem) {
                ItemStack bucket = new ItemStack(AnglingItems.require("starcaught_bucket"));
                bucket.set(AnglingDataComponents.BUCKETED_FISH, new AnglingSingleStackComponent(base));
                if (selected.definition().source().hasGuideEntry()) {
                    bucket.set(AnglingDataComponents.CAUGHT_FISH_INFO, component);
                }
                return bucket;
            }
            var bucketReference = output.fishBucket().orElseThrow();
            return new ItemStack(Registries.ITEM.get(bucketReference.id()), bucketReference.count());
        }
        return base;
    }

    static ItemStack nonBucketItem(
            AnglingCatchSnapshot.NativeCatch selected,
            int sizeMillimetres,
            int weightGrams,
            int percentile,
            boolean golden,
            boolean perfect
    ) {
        var output = selected.definition().source().catchInfo();
        Item outputItem = Registries.ITEM.get(output.item().id());
        ItemStack base = new ItemStack(outputItem, output.item().count());
        AnglingCaughtFishComponent component = new AnglingCaughtFishComponent(
                AnglingCaughtFishComponent.CURRENT_SCHEMA_VERSION, selected.id(), sizeMillimetres,
                weightGrams, percentile, selected.rarity(), golden, perfect);
        if (selected.definition().source().hasGuideEntry() && !base.isEmpty()) {
            base.set(AnglingDataComponents.CAUGHT_FISH_INFO, component);
        }
        return base;
    }

    static int sample(float average, float deviation, int percentileBasisPoints) {
        float percentile = Math.clamp(percentileBasisPoints / 100.0F, 0.01F, 99.999F);
        float inverse = (100.0F - percentile) / 100.0F;
        float width = deviation * 2.0F;
        return Math.max(0, (int) (average + inverse * width - width / 2.0F));
    }
}
