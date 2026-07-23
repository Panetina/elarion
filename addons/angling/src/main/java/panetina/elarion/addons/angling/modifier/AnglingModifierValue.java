package panetina.elarion.addons.angling.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingCatchDefinition;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.definition.AnglingSweetSpotDefinition;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Typed reload values shared by catch and minigame modifier dispatch. */
public interface AnglingModifierValue {
    String translationOverride();

    record TranslationOnly(String translationOverride) implements AnglingModifierValue {
        public static final Codec<TranslationOnly> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("translation_override").forGetter(TranslationOnly::translationOverride)
        ).apply(instance, TranslationOnly::new));

        public TranslationOnly { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record Multiplier(float multiplier, String translationOverride) implements AnglingModifierValue {
        public static final Codec<Multiplier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                finiteFloat("multiplier").fieldOf("multiplier").forGetter(Multiplier::multiplier),
                Codec.STRING.fieldOf("translation_override").forGetter(Multiplier::translationOverride)
        ).apply(instance, Multiplier::new));

        public Multiplier {
            Objects.requireNonNull(translationOverride, "translationOverride");
            requireFinite(multiplier, "multiplier");
        }
    }

    record Weighted(float weight, String translationOverride) implements AnglingModifierValue {
        public static final Codec<Weighted> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("weight").forGetter(Weighted::weight),
                Codec.STRING.fieldOf("translation_override").forGetter(Weighted::translationOverride)
        ).apply(instance, Weighted::new));

        public Weighted {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (!Float.isFinite(weight) || weight < 0) throw new IllegalArgumentException("weight must be finite and non-negative");
        }
    }

    record ExtraGolden(float weight, boolean perfectOnly, String translationOverride) implements AnglingModifierValue {
        public static final Codec<ExtraGolden> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("weight").forGetter(ExtraGolden::weight),
                Codec.BOOL.fieldOf("only_for_perfect_catch").forGetter(ExtraGolden::perfectOnly),
                Codec.STRING.fieldOf("translation_override").forGetter(ExtraGolden::translationOverride)
        ).apply(instance, ExtraGolden::new));

        public ExtraGolden {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (!Float.isFinite(weight) || weight < 0) throw new IllegalArgumentException("weight must be finite and non-negative");
        }
    }

    record AdjustLureTime(float minimumMultiplier, float maximumMultiplier, float chanceMultiplier,
                          String translationOverride) implements AnglingModifierValue {
        public static final Codec<AdjustLureTime> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                finiteFloat("minimum multiplier").fieldOf("min_ticks_multiplier").forGetter(AdjustLureTime::minimumMultiplier),
                finiteFloat("maximum multiplier").fieldOf("max_ticks_multiplier").forGetter(AdjustLureTime::maximumMultiplier),
                finiteFloat("chance multiplier").fieldOf("chance_every_tick_multiplier").forGetter(AdjustLureTime::chanceMultiplier),
                Codec.STRING.fieldOf("translation_override").forGetter(AdjustLureTime::translationOverride)
        ).apply(instance, AdjustLureTime::new));

        public AdjustLureTime {
            Objects.requireNonNull(translationOverride, "translationOverride");
            requireFinite(minimumMultiplier, "minimum multiplier");
            requireFinite(maximumMultiplier, "maximum multiplier");
            requireFinite(chanceMultiplier, "chance multiplier");
        }
    }

    record AddBasicSweetspot(AnglingSweetSpotDefinition sweetspot, String translationOverride)
            implements AnglingModifierValue {
        public static final Codec<AddBasicSweetspot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                AnglingSweetSpotDefinition.CODEC.fieldOf("sweetspot_to_add").forGetter(AddBasicSweetspot::sweetspot),
                Codec.STRING.fieldOf("translation_override").forGetter(AddBasicSweetspot::translationOverride)
        ).apply(instance, AddBasicSweetspot::new));

        public AddBasicSweetspot {
            Objects.requireNonNull(sweetspot, "sweetspot");
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record AddLeaves(float chancePerTick, String translationOverride) implements AnglingModifierValue {
        public static final Codec<AddLeaves> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance_per_tick", 0.025F)
                        .forGetter(AddLeaves::chancePerTick),
                Codec.STRING.fieldOf("translation_override").forGetter(AddLeaves::translationOverride)
        ).apply(instance, AddLeaves::new));

        public AddLeaves {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (!Float.isFinite(chancePerTick) || chancePerTick < 0 || chancePerTick > 1) {
                throw new IllegalArgumentException("leaf chance must be between zero and one");
            }
        }
    }

    record LuckByRarity(Map<AnglingRarity, Integer> increases, String translationOverride)
            implements AnglingModifierValue {
        private static final Codec<Map<AnglingRarity, Integer>> MAP_CODEC = Codec
                .unboundedMap(AnglingRarity.CODEC, Codec.INT)
                .validate(values -> values.size() <= AnglingRarity.values().length
                        ? DataResult.success(values)
                        : DataResult.error(() -> "luck modifier has too many rarity entries"));
        public static final Codec<LuckByRarity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MAP_CODEC.fieldOf("rarity_count_increase_times_luck").forGetter(LuckByRarity::increases),
                Codec.STRING.fieldOf("translation_override").forGetter(LuckByRarity::translationOverride)
        ).apply(instance, LuckByRarity::new));

        public LuckByRarity {
            increases = Map.copyOf(Objects.requireNonNull(increases, "increases"));
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record IntegerValue(int value, String translationOverride) implements AnglingModifierValue {
        public IntegerValue { Objects.requireNonNull(translationOverride, "translationOverride"); }

        public static Codec<IntegerValue> codec(String field, int minimum, int maximum) {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(minimum, maximum).fieldOf(field).forGetter(IntegerValue::value),
                    Codec.STRING.fieldOf("translation_override").forGetter(IntegerValue::translationOverride)
            ).apply(instance, IntegerValue::new));
        }
    }

    record CountAndPerfect(int count, boolean perfectOnly, String translationOverride)
            implements AnglingModifierValue {
        public static final Codec<CountAndPerfect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 4_096).fieldOf("count").forGetter(CountAndPerfect::count),
                Codec.BOOL.fieldOf("only_for_perfect_catch").forGetter(CountAndPerfect::perfectOnly),
                Codec.STRING.fieldOf("translation_override").forGetter(CountAndPerfect::translationOverride)
        ).apply(instance, CountAndPerfect::new));

        public CountAndPerfect { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record BurnOnMiss(int length, int rampTime, int extraSpeed, String translationOverride)
            implements AnglingModifierValue {
        public static final Codec<BurnOnMiss> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("length").forGetter(BurnOnMiss::length),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("ramp_time").forGetter(BurnOnMiss::rampTime),
                Codec.INT.fieldOf("extra_speed").forGetter(BurnOnMiss::extraSpeed),
                Codec.STRING.fieldOf("translation_override").forGetter(BurnOnMiss::translationOverride)
        ).apply(instance, BurnOnMiss::new));

        public BurnOnMiss { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record FreezeOnMiss(int length, int rampTime, String translationOverride) implements AnglingModifierValue {
        public static final Codec<FreezeOnMiss> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("length").forGetter(FreezeOnMiss::length),
                Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("rampTime", -1).forGetter(FreezeOnMiss::rampTime),
                Codec.STRING.fieldOf("translation_override").forGetter(FreezeOnMiss::translationOverride)
        ).apply(instance, FreezeOnMiss::new));

        public FreezeOnMiss { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record AddToPool(Optional<AnglingCatchDefinition> definition, Optional<Identifier> definitionId,
                     int quantity, String translationOverride) implements AnglingModifierValue {
        public static final Codec<AddToPool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                AnglingCatchDefinition.CODEC.optionalFieldOf("fish_properties").forGetter(AddToPool::definition),
                Identifier.CODEC.optionalFieldOf("fish_properties_location").forGetter(AddToPool::definitionId),
                Codec.intRange(0, 4_096).fieldOf("quantity_to_add").forGetter(AddToPool::quantity),
                Codec.STRING.fieldOf("translation_override").forGetter(AddToPool::translationOverride)
        ).apply(instance, AddToPool::new));

        public AddToPool {
            Objects.requireNonNull(definition, "definition");
            Objects.requireNonNull(definitionId, "definitionId");
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record LootTable(Identifier lootTable, String translationOverride) implements AnglingModifierValue {
        public static final Codec<LootTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("loot_table_to_add").forGetter(LootTable::lootTable),
                Codec.STRING.fieldOf("translation_override").forGetter(LootTable::translationOverride)
        ).apply(instance, LootTable::new));

        public LootTable {
            Objects.requireNonNull(lootTable, "lootTable");
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record AwardFish(Identifier definitionId, String translationOverride) implements AnglingModifierValue {
        public static final Codec<AwardFish> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("resource_location_to_award").forGetter(AwardFish::definitionId),
                Codec.STRING.fieldOf("translation_override").forGetter(AwardFish::translationOverride)
        ).apply(instance, AwardFish::new));

        public AwardFish {
            Objects.requireNonNull(definitionId, "definitionId");
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record OverrideCatch(AnglingCatchDefinition definition, String translationOverride)
            implements AnglingModifierValue {
        public static final Codec<OverrideCatch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                AnglingCatchDefinition.CODEC.fieldOf("fish_properties").forGetter(OverrideCatch::definition),
                Codec.STRING.fieldOf("translation_override").forGetter(OverrideCatch::translationOverride)
        ).apply(instance, OverrideCatch::new));

        public OverrideCatch {
            Objects.requireNonNull(definition, "definition");
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    record SpawnSweetspots(int length, int cooldown, float chance, AnglingSweetSpotDefinition sweetspot,
                           boolean sudokuVanish, String translationOverride) implements AnglingModifierValue {
        public static final Codec<SpawnSweetspots> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("length", -1).forGetter(SpawnSweetspots::length),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("cooldown").forGetter(SpawnSweetspots::cooldown),
                Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(SpawnSweetspots::chance),
                AnglingSweetSpotDefinition.CODEC.fieldOf("sweetspot").forGetter(SpawnSweetspots::sweetspot),
                Codec.BOOL.fieldOf("sudoku_vanish").forGetter(SpawnSweetspots::sudokuVanish),
                Codec.STRING.fieldOf("translation_override").forGetter(SpawnSweetspots::translationOverride)
        ).apply(instance, SpawnSweetspots::new));

        public SpawnSweetspots {
            Objects.requireNonNull(sweetspot, "sweetspot");
            Objects.requireNonNull(translationOverride, "translationOverride");
        }
    }

    private static Codec<Float> finiteFloat(String label) {
        return Codec.FLOAT.validate(value -> Float.isFinite(value)
                ? DataResult.success(value)
                : DataResult.error(() -> label + " must be finite"));
    }

    private static void requireFinite(float value, String label) {
        if (!Float.isFinite(value)) throw new IllegalArgumentException(label + " must be finite");
    }
}
