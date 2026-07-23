package panetina.elarion.addons.angling.fishing;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.restriction.AnglingRestriction;

import java.util.Map;

/** Exact native restriction math, with the documented caught-limit and daytime-bias defects corrected. */
public final class AnglingRestrictionEvaluator {
    public static final int UNAVAILABLE = -9_999;

    private AnglingRestrictionEvaluator() {
    }

    public static int adjustment(
            AnglingRestriction restriction,
            Identifier catchId,
            AnglingCatchEvaluationContext context
    ) {
        return adjustment(restriction, catchId, context, false, false);
    }

    public static int adjustment(
            AnglingRestriction restriction,
            Identifier catchId,
            AnglingCatchEvaluationContext context,
            boolean ignoreWeather,
            boolean ignoreDaytime
    ) {
        if (restriction instanceof AnglingRestriction.Empty || restriction instanceof AnglingRestriction.MoonPhase) {
            return 0;
        }
        if (restriction instanceof AnglingRestriction.Dimension value) {
            return (value.dimensions().isEmpty() && value.dimensionTags().isEmpty())
                    || matchesAny(value.dimensions(), value.dimensionTags(), context.dimensionId(), context.dimensionTags())
                    ? 0 : UNAVAILABLE;
        }
        if (restriction instanceof AnglingRestriction.Biome value) {
            boolean allowListPresent = !value.biomes().isEmpty() || !value.biomeTags().isEmpty();
            if (allowListPresent && !matchesAny(value.biomes(), value.biomeTags(),
                    context.biomeId(), context.biomeTags())) return UNAVAILABLE;
            return matchesAny(value.biomeBlacklist(), value.biomeBlacklistTags(),
                    context.biomeId(), context.biomeTags()) ? UNAVAILABLE : 0;
        }
        if (restriction instanceof AnglingRestriction.Bait value) {
            return value.chances().getOrDefault(context.baitId(), 0);
        }
        if (restriction instanceof AnglingRestriction.Fluid value) {
            return value.fluids().stream().anyMatch(context.nearbyFluidIds()::contains) ? 0 : UNAVAILABLE;
        }
        if (restriction instanceof AnglingRestriction.Elevation value) {
            return context.elevation() > value.minimumY() && context.elevation() < value.maximumY()
                    ? 0 : UNAVAILABLE;
        }
        if (restriction instanceof AnglingRestriction.ElevationBias value) {
            return linearBias(context.elevation(), value.bestY(), value.range(), value.extraChance(), false);
        }
        if (restriction instanceof AnglingRestriction.WeatherRule value) {
            if (ignoreWeather) return 0;
            boolean clear = context.rainLevel() < 0.2F && context.thunderLevel() < 0.2F;
            boolean rain = context.rainLevel() > 0.2F;
            boolean thunder = context.thunderLevel() > 0.2F;
            boolean matches = switch (value.weather()) {
                case CLEAR -> clear;
                case RAIN -> rain;
                case THUNDER -> thunder;
                case CLEAR_OR_RAIN -> clear || rain;
                case CLEAR_OR_THUNDER -> clear || thunder;
                case RAIN_OR_THUNDER -> rain || thunder;
            };
            return matches ? 0 : UNAVAILABLE;
        }
        if (restriction instanceof AnglingRestriction.Daytime value) {
            if (ignoreDaytime) return 0;
            return value.ranges().stream().anyMatch(range ->
                    context.daytime() > range.first() && context.daytime() < range.second()) ? 0 : UNAVAILABLE;
        }
        if (restriction instanceof AnglingRestriction.DaytimeBias value) {
            if (ignoreDaytime) return 0;
            return linearBias((int) context.daytime(), value.bestDaytime(), value.range(), value.extraChance(), true);
        }
        if (restriction instanceof AnglingRestriction.SeasonRule value) {
            if (context.season() == AnglingRestriction.Season.ALL) return 0;
            return value.extraChance().getOrDefault(context.season(), UNAVAILABLE);
        }
        if (restriction instanceof AnglingRestriction.CaughtLimit value) {
            return context.speciesProgress().getOrDefault(catchId,
                    new AnglingCatchEvaluationContext.SpeciesProgress(0, false)).totalCount() >= value.limit()
                    ? UNAVAILABLE : 0;
        }
        if (restriction instanceof AnglingRestriction.RarityCount value) {
            return value.rarities().stream().allMatch(requirement -> requirementMet(requirement, context))
                    ? 0 : UNAVAILABLE;
        }
        if (restriction instanceof AnglingRestriction.PercentageChance value) {
            double roll = context.randomUnit().getAsDouble();
            if (!Double.isFinite(roll) || roll < 0.0D || roll >= 1.0D) {
                throw new IllegalArgumentException("catch-selection random source must return [0, 1)");
            }
            return roll > value.chance() ? UNAVAILABLE : 0;
        }
        if (restriction instanceof AnglingRestriction.Structure value) {
            return value.structures().stream().anyMatch(context.structureTypeIds()::contains) ? 0 : UNAVAILABLE;
        }
        throw new IllegalArgumentException("Unsupported Angling restriction " + restriction.getClass().getName());
    }

    private static boolean matchesAny(
            java.util.List<Identifier> ids,
            java.util.List<Identifier> tags,
            Identifier currentId,
            java.util.Set<Identifier> currentTags
    ) {
        return ids.contains(currentId) || tags.stream().anyMatch(currentTags::contains);
    }

    private static int linearBias(int current, int best, int range, int extraChance, boolean cyclicDay) {
        long distance;
        if (cyclicDay) {
            int normalizedCurrent = Math.floorMod(current, 24_000);
            int normalizedBest = Math.floorMod(best, 24_000);
            distance = Math.abs((long) normalizedCurrent - normalizedBest);
            distance = Math.min(distance, 24_000L - distance);
        } else {
            distance = Math.abs((long) current - best);
        }
        if (distance >= range) return 0;
        double ratio = 1.0D - distance / (double) range;
        return Math.max(0, (int) Math.floor(extraChance * ratio));
    }

    private static boolean requirementMet(
            AnglingRestriction.RarityRequirement requirement,
            AnglingCatchEvaluationContext context
    ) {
        if (requirement.countType() == AnglingRestriction.CountType.ALL) {
            return context.catalogueRarities().entrySet().stream()
                    .filter(entry -> requirement.rarity() == AnglingRarity.NONE
                            || requirement.rarity() == AnglingRarity.GOLDEN
                            || entry.getValue() == requirement.rarity())
                    .allMatch(entry -> {
                        var progress = context.speciesProgress().get(entry.getKey());
                        return progress != null && progress.totalCount() > 0
                                && (requirement.rarity() != AnglingRarity.GOLDEN || progress.goldenCaught());
                    });
        }
        long count = 0;
        for (Map.Entry<Identifier, AnglingRarity> entry : context.catalogueRarities().entrySet()) {
            var progress = context.speciesProgress().get(entry.getKey());
            if (progress == null || progress.totalCount() == 0) continue;
            if (requirement.rarity() == AnglingRarity.GOLDEN) {
                if (progress.goldenCaught()) count++;
            } else if (requirement.rarity() == AnglingRarity.NONE || entry.getValue() == requirement.rarity()) {
                count = Math.addExact(count, requirement.countType() == AnglingRestriction.CountType.UNIQUE
                        ? 1 : progress.totalCount());
            }
        }
        return count >= requirement.count();
    }
}
