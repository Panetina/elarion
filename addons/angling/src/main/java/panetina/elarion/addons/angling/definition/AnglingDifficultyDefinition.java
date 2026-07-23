package panetina.elarion.addons.angling.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Objects;

/** Complete reload-time minigame difficulty definition from the frozen schema. */
public record AnglingDifficultyDefinition(
        int hp,
        int speed,
        int missPenalty,
        float decay,
        List<AnglingTypedNode> modifiers,
        List<AnglingSweetSpotDefinition> sweetspots
) {
    public static final int MAX_MODIFIERS = 64;
    public static final int MAX_SWEETSPOTS = 32;

    private static final Codec<List<AnglingTypedNode>> MODIFIERS_CODEC = AnglingTypedNode.CODEC.listOf()
            .validate(values -> bounded(values, MAX_MODIFIERS, "minigame modifiers"));
    private static final Codec<List<AnglingSweetSpotDefinition>> SWEETSPOTS_CODEC = AnglingSweetSpotDefinition.CODEC.listOf()
            .validate(values -> bounded(values, MAX_SWEETSPOTS, "sweetspots"));

    public static final Codec<AnglingDifficultyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 1_000_000).fieldOf("hp").forGetter(AnglingDifficultyDefinition::hp),
            Codec.intRange(0, 1_000_000).fieldOf("speed").forGetter(AnglingDifficultyDefinition::speed),
            Codec.INT.fieldOf("missPenalty").forGetter(AnglingDifficultyDefinition::missPenalty),
            Codec.FLOAT.fieldOf("decay").forGetter(AnglingDifficultyDefinition::decay),
            MODIFIERS_CODEC.fieldOf("modifiers").forGetter(AnglingDifficultyDefinition::modifiers),
            SWEETSPOTS_CODEC.fieldOf("sweetspots").forGetter(AnglingDifficultyDefinition::sweetspots)
    ).apply(instance, AnglingDifficultyDefinition::new));

    public AnglingDifficultyDefinition {
        modifiers = List.copyOf(Objects.requireNonNull(modifiers, "modifiers"));
        sweetspots = List.copyOf(Objects.requireNonNull(sweetspots, "sweetspots"));
        if (hp < 1 || speed < 0 || modifiers.size() > MAX_MODIFIERS || sweetspots.size() > MAX_SWEETSPOTS
                || !Float.isFinite(decay)) {
            throw new IllegalArgumentException("Invalid Angling difficulty definition");
        }
    }

    private static <T> DataResult<List<T>> bounded(List<T> values, int maximum, String label) {
        return values.size() <= maximum
                ? DataResult.success(values)
                : DataResult.error(() -> "Angling " + label + " count exceeds " + maximum);
    }
}
