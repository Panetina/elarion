package panetina.elarion.addons.angling.fishing;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.structure.Structure;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.definition.AnglingCatchType;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.restriction.AnglingRestriction;
import panetina.elarion.core.model.CatchSpeciesSummary;
import panetina.elarion.core.model.CatchSummary;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.random.RandomGenerator;

/** Captures all world/player facts once per reel so the definition pass performs no repeated registry queries. */
public final class AnglingCatchContextFactory {
    public AnglingCatchEvaluationContext create(
            ServerWorld world,
            BlockPos bobberPosition,
            ItemStack rod,
            CatchSummary summary,
            AnglingCatchSnapshot snapshot,
            AnglingRestriction.Season season,
            RandomGenerator random
    ) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(bobberPosition, "bobberPosition");
        Objects.requireNonNull(rod, "rod");
        Objects.requireNonNull(summary, "summary");
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(season, "season");
        Objects.requireNonNull(random, "random");

        Identifier dimensionId = world.getRegistryKey().getValue();
        Set<Identifier> dimensionTags = dimensionTags(world, dimensionId);
        RegistryEntry<net.minecraft.world.biome.Biome> biome = world.getBiome(bobberPosition);
        Identifier biomeId = biome.getKey().orElseThrow().getValue();
        Set<Identifier> biomeTags = new LinkedHashSet<>();
        biome.streamTags().forEach(tag -> biomeTags.add(tag.id()));
        ItemStack bait = rod.getOrDefault(AnglingDataComponents.BAIT, AnglingSingleStackComponent.EMPTY).stack();

        Map<Identifier, AnglingCatchEvaluationContext.SpeciesProgress> progress = new LinkedHashMap<>();
        for (Map.Entry<Identifier, CatchSpeciesSummary> entry : summary.speciesSummaries().entrySet()) {
            progress.put(entry.getKey(), new AnglingCatchEvaluationContext.SpeciesProgress(
                    entry.getValue().totalCount(), entry.getValue().goldenCount() > 0));
        }
        Map<Identifier, AnglingRarity> catalogue = new LinkedHashMap<>();
        snapshot.all().forEach((id, value) -> {
            if (value.type() == AnglingCatchType.FISH && value.definition().source().hasGuideEntry()) {
                catalogue.put(id, value.rarity());
            }
        });

        return new AnglingCatchEvaluationContext(
                dimensionId,
                dimensionTags,
                biomeId,
                biomeTags,
                Registries.ITEM.getId(bait.getItem()),
                nearbyFluids(world, bobberPosition),
                bobberPosition.getY(),
                world.getRainGradient(1.0F),
                world.getThunderGradient(1.0F),
                world.getTimeOfDay(),
                season,
                structuresAt(world, bobberPosition),
                progress,
                catalogue,
                random::nextDouble);
    }

    private static Set<Identifier> dimensionTags(ServerWorld world, Identifier dimensionId) {
        Registry<DimensionOptions> dimensions = world.getRegistryManager().get(RegistryKeys.DIMENSION);
        RegistryKey<DimensionOptions> key = RegistryKey.of(RegistryKeys.DIMENSION, dimensionId);
        Set<Identifier> tags = new LinkedHashSet<>();
        dimensions.getEntry(key).ifPresent(entry -> entry.streamTags().forEach(tag -> tags.add(tag.id())));
        return tags;
    }

    private static Set<Identifier> nearbyFluids(ServerWorld world, BlockPos position) {
        Set<Identifier> fluids = new LinkedHashSet<>();
        addFluid(fluids, world.getFluidState(position).getFluid());
        addFluid(fluids, world.getFluidState(position.up()).getFluid());
        addFluid(fluids, world.getFluidState(position.down()).getFluid());
        return fluids;
    }

    private static void addFluid(Set<Identifier> fluids, Fluid fluid) {
        Fluid source = fluid instanceof FlowableFluid flowable ? flowable.getStill() : fluid;
        fluids.add(Registries.FLUID.getId(source));
    }

    private static Set<Identifier> structuresAt(ServerWorld world, BlockPos position) {
        Registry<Structure> structures = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        Set<Identifier> ids = new LinkedHashSet<>();
        for (Structure structure : world.getStructureAccessor().getStructureReferences(position).keySet()) {
            if (world.getStructureAccessor().getStructureAt(position, structure).hasChildren()) {
                Identifier id = structures.getId(structure);
                if (id != null) ids.add(id);
            }
        }
        return ids;
    }
}
