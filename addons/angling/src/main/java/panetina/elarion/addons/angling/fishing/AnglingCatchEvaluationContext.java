package panetina.elarion.addons.angling.fishing;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.restriction.AnglingRestriction;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleSupplier;

/** Immutable server-derived facts used by the bounded catch-selection pass. */
public record AnglingCatchEvaluationContext(
        Identifier dimensionId,
        Set<Identifier> dimensionTags,
        Identifier biomeId,
        Set<Identifier> biomeTags,
        Identifier baitId,
        Set<Identifier> nearbyFluidIds,
        int elevation,
        float rainLevel,
        float thunderLevel,
        long daytime,
        AnglingRestriction.Season season,
        Set<Identifier> structureTypeIds,
        Map<Identifier, SpeciesProgress> speciesProgress,
        Map<Identifier, AnglingRarity> catalogueRarities,
        DoubleSupplier randomUnit
) {
    public AnglingCatchEvaluationContext {
        Objects.requireNonNull(dimensionId, "dimensionId");
        dimensionTags = Set.copyOf(Objects.requireNonNull(dimensionTags, "dimensionTags"));
        Objects.requireNonNull(biomeId, "biomeId");
        biomeTags = Set.copyOf(Objects.requireNonNull(biomeTags, "biomeTags"));
        Objects.requireNonNull(baitId, "baitId");
        nearbyFluidIds = Set.copyOf(Objects.requireNonNull(nearbyFluidIds, "nearbyFluidIds"));
        if (!Float.isFinite(rainLevel) || !Float.isFinite(thunderLevel)) {
            throw new IllegalArgumentException("weather levels must be finite");
        }
        daytime = Math.floorMod(daytime, 24_000L);
        Objects.requireNonNull(season, "season");
        structureTypeIds = Set.copyOf(Objects.requireNonNull(structureTypeIds, "structureTypeIds"));
        speciesProgress = Map.copyOf(Objects.requireNonNull(speciesProgress, "speciesProgress"));
        catalogueRarities = Map.copyOf(Objects.requireNonNull(catalogueRarities, "catalogueRarities"));
        Objects.requireNonNull(randomUnit, "randomUnit");
    }

    public record SpeciesProgress(long totalCount, boolean goldenCaught) {
        public SpeciesProgress {
            if (totalCount < 0) throw new IllegalArgumentException("species count cannot be negative");
        }
    }
}
