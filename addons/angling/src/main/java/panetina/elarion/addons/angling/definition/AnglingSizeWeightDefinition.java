package panetina.elarion.addons.angling.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Frozen size, weight, and golden-roll distribution parameters. */
public record AnglingSizeWeightDefinition(
        float averageSizeCentimeters,
        float deviationSizeCentimeters,
        float averageWeightGrams,
        float deviationWeightGrams,
        float goldenChance
) {
    public static final Codec<AnglingSizeWeightDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("average_size_cm").forGetter(AnglingSizeWeightDefinition::averageSizeCentimeters),
            Codec.FLOAT.fieldOf("deviation_size_cm").forGetter(AnglingSizeWeightDefinition::deviationSizeCentimeters),
            Codec.FLOAT.fieldOf("average_weight_grams").forGetter(AnglingSizeWeightDefinition::averageWeightGrams),
            Codec.FLOAT.fieldOf("deviation_weight_grams").forGetter(AnglingSizeWeightDefinition::deviationWeightGrams),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("golden_chance", 0.02F)
                    .forGetter(AnglingSizeWeightDefinition::goldenChance)
    ).apply(instance, AnglingSizeWeightDefinition::new));

    public AnglingSizeWeightDefinition {
        if (!Float.isFinite(averageSizeCentimeters) || averageSizeCentimeters < 0.0F
                || !Float.isFinite(deviationSizeCentimeters) || deviationSizeCentimeters < 0.0F
                || !Float.isFinite(averageWeightGrams) || averageWeightGrams < 0.0F
                || !Float.isFinite(deviationWeightGrams) || deviationWeightGrams < 0.0F
                || !Float.isFinite(goldenChance) || goldenChance < 0.0F || goldenChance > 1.0F) {
            throw new IllegalArgumentException("Invalid Angling size/weight distribution");
        }
    }
}
