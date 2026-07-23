package panetina.elarion.addons.angling.minigame;

import panetina.elarion.addons.angling.compile.AnglingCompiledCatchDefinition;
import panetina.elarion.addons.angling.compile.AnglingCompiledSweetSpot;
import panetina.elarion.addons.angling.definition.AnglingDifficultyDefinition;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

/** Immutable, reload-snapshot-owned input for one server minigame session. */
public record AnglingServerMinigameSpec(
        int hitPoints,
        float pointerSpeed,
        float missPenalty,
        float decay,
        float hitDelayTicks,
        float initialProgress,
        boolean treasureAvailable,
        List<AnglingNativeModifier> modifiers,
        List<Sweetspot> sweetspots
) {
    public static final int MAX_SWEETSPOTS = 32;
    /** The frozen treasure-threshold modifier can add two spots to a full definition. */
    public static final int MAX_RUNTIME_SWEETSPOTS = MAX_SWEETSPOTS + 2;
    public static final int MAX_MODIFIERS = 64;

    public AnglingServerMinigameSpec {
        modifiers = List.copyOf(Objects.requireNonNull(modifiers, "modifiers"));
        sweetspots = List.copyOf(Objects.requireNonNull(sweetspots, "sweetspots"));
        if (hitPoints < 1 || hitPoints > 1_000_000
                || !finite(pointerSpeed) || !finite(missPenalty) || !finite(decay)
                || !finite(hitDelayTicks) || !finite(initialProgress)
                || modifiers.size() > MAX_MODIFIERS || sweetspots.size() > MAX_SWEETSPOTS) {
            throw new IllegalArgumentException("Invalid server minigame specification");
        }
    }

    public static AnglingServerMinigameSpec from(
            AnglingCompiledCatchDefinition<?, AnglingNativeModifier, AnglingSweetspotBehaviorType> compiled,
            float pointerSpeedMultiplier,
            float penaltyMultiplier,
            float decayMultiplier,
            float requiredScoreMultiplier,
            float vanishingRateMultiplier,
            float movingRateMultiplier,
            float hitDelayTicks,
            float initialProgress,
            boolean treasureAvailable
    ) {
        Objects.requireNonNull(compiled, "compiled");
        AnglingDifficultyDefinition difficulty = compiled.source().difficulty();
        List<Sweetspot> sweetspots = compiled.sweetspots().stream()
                .map(value -> sweetspot(value, vanishingRateMultiplier, movingRateMultiplier))
                .toList();
        return new AnglingServerMinigameSpec(
                scalePositive(difficulty.hp(), requiredScoreMultiplier),
                multiply(difficulty.speed(), pointerSpeedMultiplier),
                (int) multiply(difficulty.missPenalty(), penaltyMultiplier),
                multiply(difficulty.decay(), decayMultiplier),
                hitDelayTicks,
                initialProgress,
                treasureAvailable,
                compiled.minigameModifiers(),
                sweetspots
        );
    }

    private static Sweetspot sweetspot(
            AnglingCompiledSweetSpot<AnglingNativeModifier, AnglingSweetspotBehaviorType> compiled,
            float vanishingRateMultiplier,
            float movingRateMultiplier
    ) {
        var definition = compiled.definition();
        return new Sweetspot(
                compiled.behavior(),
                definition.texturePath(),
                definition.hitboxSizePixels(),
                definition.reward(),
                definition.flip(),
                multiply(definition.vanishingRate(), vanishingRateMultiplier),
                multiply(definition.movingRate(), movingRateMultiplier),
                definition.color(),
                compiled.onHitModifiers()
        );
    }

    private static int scalePositive(int value, float multiplier) {
        float result = multiply(value, multiplier);
        if (result < 1.0F || result > 1_000_000.0F) {
            throw new IllegalArgumentException("Scaled minigame hit points are outside the supported range");
        }
        return (int) result;
    }

    private static float multiply(float value, float multiplier) {
        float result = value * multiplier;
        if (!finite(multiplier) || !finite(result)) {
            throw new IllegalArgumentException("Non-finite minigame multiplier");
        }
        return result;
    }

    private static boolean finite(float value) {
        return Float.isFinite(value);
    }

    public record Sweetspot(
            AnglingSweetspotBehaviorType behavior,
            Identifier texturePath,
            int hitboxSizePixels,
            int reward,
            boolean flip,
            float vanishingRate,
            float movingRate,
            int color,
            List<AnglingNativeModifier> onHitModifiers
    ) {
        public Sweetspot {
            Objects.requireNonNull(behavior, "behavior");
            Objects.requireNonNull(texturePath, "texturePath");
            onHitModifiers = List.copyOf(Objects.requireNonNull(onHitModifiers, "onHitModifiers"));
            if (hitboxSizePixels < 1 || hitboxSizePixels > 512
                    || !finite(vanishingRate) || !finite(movingRate)
                    || onHitModifiers.size() > MAX_MODIFIERS) {
                throw new IllegalArgumentException("Invalid server sweetspot specification");
            }
        }
    }
}
