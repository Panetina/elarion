package panetina.elarion.addons.angling.restriction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingRarity;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Immutable typed reload products for every frozen catch-restriction schema. */
public interface AnglingRestriction {
    int MAX_IDS = 256;
    int MAX_RANGES = 64;

    static Codec<List<Identifier>> identifierList(String label) {
        return Identifier.CODEC.listOf().validate(values -> values.size() <= MAX_IDS
                ? DataResult.success(values)
                : DataResult.error(() -> label + " exceeds " + MAX_IDS + " entries"));
    }

    static <K, V> Codec<Map<K, V>> boundedMap(Codec<K> keyCodec, Codec<V> valueCodec, String label) {
        return Codec.unboundedMap(keyCodec, valueCodec).validate(values -> values.size() <= MAX_IDS
                ? DataResult.success(values)
                : DataResult.error(() -> label + " exceeds " + MAX_IDS + " entries"));
    }

    record Empty(String translationOverride) implements AnglingRestriction {
        public static final Codec<Empty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(Empty::translationOverride)
        ).apply(instance, Empty::new));

        public Empty {
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record Dimension(
            List<Identifier> dimensions,
            List<Identifier> dimensionTags,
            String hoverTranslation,
            String translationOverride
    ) implements AnglingRestriction {
        public static final Codec<Dimension> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                identifierList("dimensions").fieldOf("dimensions").forGetter(Dimension::dimensions),
                identifierList("dimension tags").fieldOf("dimensions_tags").forGetter(Dimension::dimensionTags),
                Codec.STRING.optionalFieldOf("hover_translation", "").forGetter(Dimension::hoverTranslation),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(Dimension::translationOverride)
        ).apply(instance, Dimension::new));

        public Dimension {
            dimensions = List.copyOf(dimensions);
            dimensionTags = List.copyOf(dimensionTags);
            Objects.requireNonNull(hoverTranslation, "hoverTranslation");
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record Biome(
            List<Identifier> biomes,
            List<Identifier> biomeTags,
            List<Identifier> biomeBlacklist,
            List<Identifier> biomeBlacklistTags,
            String hoverTranslation,
            String translationOverride
    ) implements AnglingRestriction {
        public static final Codec<Biome> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                identifierList("biomes").fieldOf("biomes").forGetter(Biome::biomes),
                identifierList("biome tags").fieldOf("biomes_tags").forGetter(Biome::biomeTags),
                identifierList("biome blacklist").fieldOf("biomes_blacklist").forGetter(Biome::biomeBlacklist),
                identifierList("biome blacklist tags").fieldOf("biomes_blacklist_tags")
                        .forGetter(Biome::biomeBlacklistTags),
                Codec.STRING.optionalFieldOf("hover_translation", "").forGetter(Biome::hoverTranslation),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(Biome::translationOverride)
        ).apply(instance, Biome::new));

        public Biome {
            biomes = List.copyOf(biomes);
            biomeTags = List.copyOf(biomeTags);
            biomeBlacklist = List.copyOf(biomeBlacklist);
            biomeBlacklistTags = List.copyOf(biomeBlacklistTags);
            Objects.requireNonNull(hoverTranslation, "hoverTranslation");
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record Bait(Map<Identifier, Integer> chances, String translationOverride) implements AnglingRestriction {
        public static final Codec<Bait> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                boundedMap(Identifier.CODEC, Codec.INT, "baits").fieldOf("baits").forGetter(Bait::chances),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(Bait::translationOverride)
        ).apply(instance, Bait::new));

        public Bait {
            chances = Map.copyOf(chances);
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record Fluid(List<Identifier> fluids, String translationOverride) implements AnglingRestriction {
        public static final Codec<Fluid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                identifierList("fluids").fieldOf("fluids").forGetter(Fluid::fluids),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(Fluid::translationOverride)
        ).apply(instance, Fluid::new));

        public Fluid {
            fluids = List.copyOf(fluids);
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record Elevation(int minimumY, int maximumY, String translationOverride) implements AnglingRestriction {
        public static final Codec<Elevation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("min_y").forGetter(Elevation::minimumY),
                Codec.INT.fieldOf("max_y").forGetter(Elevation::maximumY),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(Elevation::translationOverride)
        ).apply(instance, Elevation::new));

        public Elevation {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (minimumY > maximumY) {
                throw new IllegalArgumentException("minimum elevation exceeds maximum elevation");
            }
        }
    }

    record ElevationBias(int bestY, int range, int extraChance, String translationOverride)
            implements AnglingRestriction {
        public static final Codec<ElevationBias> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("best_y").forGetter(ElevationBias::bestY),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("range").forGetter(ElevationBias::range),
                Codec.INT.fieldOf("extra_chance_at_best").forGetter(ElevationBias::extraChance),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(ElevationBias::translationOverride)
        ).apply(instance, ElevationBias::new));

        public ElevationBias {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (range < 1) throw new IllegalArgumentException("elevation bias range must be positive");
        }
    }

    record WeatherRule(Weather weather, String translationOverride) implements AnglingRestriction {
        public static final Codec<WeatherRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Weather.CODEC.fieldOf("weather").forGetter(WeatherRule::weather),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(WeatherRule::translationOverride)
        ).apply(instance, WeatherRule::new));

        public WeatherRule {
            Objects.requireNonNull(weather, "weather");
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    enum Weather {
        CLEAR,
        RAIN,
        THUNDER,
        CLEAR_OR_RAIN,
        CLEAR_OR_THUNDER,
        RAIN_OR_THUNDER;

        public static final Codec<Weather> CODEC = enumCodec(Weather.class, "weather");
    }

    record Daytime(List<TimeRange> ranges, String translationOverride) implements AnglingRestriction {
        private static final Codec<List<TimeRange>> RANGES_CODEC = TimeRange.CODEC.listOf()
                .validate(values -> values.size() <= MAX_RANGES
                        ? DataResult.success(values)
                        : DataResult.error(() -> "daytime ranges exceed " + MAX_RANGES));
        public static final Codec<Daytime> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RANGES_CODEC.fieldOf("ranges").forGetter(Daytime::ranges),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(Daytime::translationOverride)
        ).apply(instance, Daytime::new));

        public Daytime {
            ranges = List.copyOf(ranges);
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record TimeRange(int first, int second) {
        public static final Codec<TimeRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("first").forGetter(TimeRange::first),
                Codec.INT.fieldOf("second").forGetter(TimeRange::second)
        ).apply(instance, TimeRange::new));
    }

    record DaytimeBias(int bestDaytime, int range, int extraChance, String translationOverride)
            implements AnglingRestriction {
        public static final Codec<DaytimeBias> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("best_daytime").forGetter(DaytimeBias::bestDaytime),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("range").forGetter(DaytimeBias::range),
                Codec.INT.fieldOf("extra_chance_at_best").forGetter(DaytimeBias::extraChance),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(DaytimeBias::translationOverride)
        ).apply(instance, DaytimeBias::new));

        public DaytimeBias {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (range < 1) throw new IllegalArgumentException("daytime bias range must be positive");
        }
    }

    record MoonPhase(String translationOverride) implements AnglingRestriction {
        public static final Codec<MoonPhase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(MoonPhase::translationOverride)
        ).apply(instance, MoonPhase::new));

        public MoonPhase {
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record SeasonRule(Map<Season, Integer> extraChance, String translationOverride) implements AnglingRestriction {
        public static final Codec<SeasonRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                boundedMap(Season.CODEC, Codec.INT, "seasons").fieldOf("season_extra_chance")
                        .forGetter(SeasonRule::extraChance),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(SeasonRule::translationOverride)
        ).apply(instance, SeasonRule::new));

        public SeasonRule {
            extraChance = Map.copyOf(extraChance);
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    enum Season {
        ALL,
        EARLY_SPRING,
        MID_SPRING,
        LATE_SPRING,
        EARLY_SUMMER,
        MID_SUMMER,
        LATE_SUMMER,
        EARLY_AUTUMN,
        MID_AUTUMN,
        LATE_AUTUMN,
        EARLY_WINTER,
        MID_WINTER,
        LATE_WINTER;

        public static final Codec<Season> CODEC = enumCodec(Season.class, "season");
    }

    record CaughtLimit(int limit, String translationOverride) implements AnglingRestriction {
        public static final Codec<CaughtLimit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("limit").forGetter(CaughtLimit::limit),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(CaughtLimit::translationOverride)
        ).apply(instance, CaughtLimit::new));

        public CaughtLimit {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (limit < 0) throw new IllegalArgumentException("caught limit cannot be negative");
        }
    }

    record RarityCount(List<RarityRequirement> rarities, String translationOverride) implements AnglingRestriction {
        private static final Codec<List<RarityRequirement>> RARITIES_CODEC = RarityRequirement.CODEC.listOf()
                .validate(values -> values.size() <= MAX_IDS
                        ? DataResult.success(values)
                        : DataResult.error(() -> "rarity requirements exceed " + MAX_IDS));
        public static final Codec<RarityCount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RARITIES_CODEC.fieldOf("rarities").forGetter(RarityCount::rarities),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(RarityCount::translationOverride)
        ).apply(instance, RarityCount::new));

        public RarityCount {
            rarities = List.copyOf(rarities);
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record RarityRequirement(AnglingRarity rarity, int count, CountType countType) {
        public static final Codec<RarityRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                AnglingRarity.CODEC.optionalFieldOf("rarity", AnglingRarity.NONE)
                        .forGetter(RarityRequirement::rarity),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("count", 0)
                        .forGetter(RarityRequirement::count),
                CountType.CODEC.fieldOf("count_type").forGetter(RarityRequirement::countType)
        ).apply(instance, RarityRequirement::new));

        public RarityRequirement {
            Objects.requireNonNull(rarity, "rarity");
            Objects.requireNonNull(countType, "countType");
            if (count < 0) throw new IllegalArgumentException("rarity count cannot be negative");
        }
    }

    enum CountType {
        ALL,
        UNIQUE,
        TOTAL;

        public static final Codec<CountType> CODEC = enumCodec(CountType.class, "count type");
    }

    record PercentageChance(float chance, String translationOverride) implements AnglingRestriction {
        public static final Codec<PercentageChance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(PercentageChance::chance),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(PercentageChance::translationOverride)
        ).apply(instance, PercentageChance::new));

        public PercentageChance {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (!Float.isFinite(chance) || chance < 0.0F || chance > 1.0F) {
                throw new IllegalArgumentException("percentage chance must be between zero and one");
            }
        }
    }

    record Structure(List<Identifier> structures, String translationOverride) implements AnglingRestriction {
        public static final Codec<Structure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                identifierList("structures").fieldOf("structure").forGetter(Structure::structures),
                Codec.STRING.optionalFieldOf("translation_override", "").forGetter(Structure::translationOverride)
        ).apply(instance, Structure::new));

        public Structure {
            structures = List.copyOf(structures);
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    private static <E extends Enum<E>> Codec<E> enumCodec(Class<E> type, String label) {
        return Codec.STRING.comapFlatMap(value -> {
            try {
                return DataResult.success(Enum.valueOf(type, value.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                return DataResult.error(() -> "Unknown Angling " + label + ": " + value);
            }
        }, value -> value.name().toLowerCase(Locale.ROOT));
    }
}
