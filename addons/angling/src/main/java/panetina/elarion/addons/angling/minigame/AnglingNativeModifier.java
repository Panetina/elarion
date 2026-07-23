package panetina.elarion.addons.angling.minigame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import panetina.elarion.addons.angling.modifier.AnglingModifierValue;

/** Typed configuration for all nine modifier types used by native catches. */
public interface AnglingNativeModifier extends AnglingModifierValue {

    record BurnOnMiss(int length, int rampTime, int extraSpeed, String translationOverride)
            implements AnglingNativeModifier {
        public static final Codec<BurnOnMiss> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("length").forGetter(BurnOnMiss::length),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("ramp_time").forGetter(BurnOnMiss::rampTime),
                Codec.INT.fieldOf("extra_speed").forGetter(BurnOnMiss::extraSpeed),
                Codec.STRING.fieldOf("translation_override").forGetter(BurnOnMiss::translationOverride)
        ).apply(instance, BurnOnMiss::new));

        public BurnOnMiss {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (length < 0 || rampTime < 0) throw new IllegalArgumentException("burn timings cannot be negative");
        }
    }

    record DeepDark(String translationOverride) implements AnglingNativeModifier {
        public static final Codec<DeepDark> CODEC = stringOnly(DeepDark::new, DeepDark::translationOverride);

        public DeepDark { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record DisableHitSounds(String translationOverride) implements AnglingNativeModifier {
        public static final Codec<DisableHitSounds> CODEC = stringOnly(
                DisableHitSounds::new, DisableHitSounds::translationOverride);

        public DisableHitSounds { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record DisableMissSounds(String translationOverride) implements AnglingNativeModifier {
        public static final Codec<DisableMissSounds> CODEC = stringOnly(
                DisableMissSounds::new, DisableMissSounds::translationOverride);

        public DisableMissSounds { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record FlipSweetspotsOnMiss(float chance, String translationOverride) implements AnglingNativeModifier {
        public static final Codec<FlipSweetspotsOnMiss> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(FlipSweetspotsOnMiss::chance),
                Codec.STRING.fieldOf("translation_override").forGetter(FlipSweetspotsOnMiss::translationOverride)
        ).apply(instance, FlipSweetspotsOnMiss::new));

        public FlipSweetspotsOnMiss {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (!Float.isFinite(chance) || chance < 0.0F || chance > 1.0F) {
                throw new IllegalArgumentException("flip chance must be between zero and one");
            }
        }
    }

    record FreezeOnMiss(int length, int rampTime, String translationOverride) implements AnglingNativeModifier {
        public static final Codec<FreezeOnMiss> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("length").forGetter(FreezeOnMiss::length),
                Codec.INT.optionalFieldOf("rampTime", -1).forGetter(FreezeOnMiss::rampTime),
                Codec.STRING.fieldOf("translation_override").forGetter(FreezeOnMiss::translationOverride)
        ).apply(instance, FreezeOnMiss::new));

        public FreezeOnMiss {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (length < 0 || rampTime < -1) throw new IllegalArgumentException("invalid freeze timings");
        }
    }

    record MultiLayer(int maximumLayers, String translationOverride) implements AnglingNativeModifier {
        public static final Codec<MultiLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 256).fieldOf("max_layers").forGetter(MultiLayer::maximumLayers),
                Codec.STRING.fieldOf("translation_override").forGetter(MultiLayer::translationOverride)
        ).apply(instance, MultiLayer::new));

        public MultiLayer {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (maximumLayers < 1 || maximumLayers > 256) {
                throw new IllegalArgumentException("invalid maximum pointer layers");
            }
        }
    }

    record PullDown(String translationOverride) implements AnglingNativeModifier {
        public static final Codec<PullDown> CODEC = stringOnly(PullDown::new, PullDown::translationOverride);

        public PullDown { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record Teleport(String translationOverride) implements AnglingNativeModifier {
        public static final Codec<Teleport> CODEC = stringOnly(Teleport::new, Teleport::translationOverride);

        public Teleport { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record AddLeaves(float chancePerTick, String translationOverride) implements AnglingNativeModifier {
        public AddLeaves {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (!Float.isFinite(chancePerTick) || chancePerTick < 0 || chancePerTick > 1) {
                throw new IllegalArgumentException("leaf chance must be between zero and one");
            }
        }
    }

    record BounceBack(String translationOverride) implements AnglingNativeModifier {
        public BounceBack { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record FlipEveryHit(String translationOverride) implements AnglingNativeModifier {
        public FlipEveryHit { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record MoveSweetspotsOnMiss(String translationOverride) implements AnglingNativeModifier {
        public MoveSweetspotsOnMiss { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record NeverLose(String translationOverride) implements AnglingNativeModifier {
        public NeverLose { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record PreventFrozen(String translationOverride) implements AnglingNativeModifier {
        public PreventFrozen { Objects.requireNonNull(translationOverride, "translationOverride"); }
    }

    record StopDecayOnHit(int graceTicks, String translationOverride) implements AnglingNativeModifier {
        public StopDecayOnHit {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (graceTicks < 0) throw new IllegalArgumentException("grace ticks cannot be negative");
        }
    }

    record SpawnTreasureOnHit(int hits, String translationOverride) implements AnglingNativeModifier {
        public SpawnTreasureOnHit {
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (hits < 1) throw new IllegalArgumentException("treasure hit threshold must be positive");
        }
    }

    record SpawnSweetspots(
            int length,
            int cooldown,
            float chance,
            AnglingServerMinigameSpec.Sweetspot sweetspot,
            boolean sudokuVanish,
            String translationOverride
    ) implements AnglingNativeModifier {
        public SpawnSweetspots {
            Objects.requireNonNull(sweetspot, "sweetspot");
            Objects.requireNonNull(translationOverride, "translationOverride");
            if (length < -1 || cooldown < 1 || !Float.isFinite(chance) || chance < 0 || chance > 1) {
                throw new IllegalArgumentException("invalid spawning sweetspot modifier");
            }
        }
    }

    private static <T> Codec<T> stringOnly(
            java.util.function.Function<String, T> constructor,
            java.util.function.Function<T, String> getter
    ) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("translation_override").forGetter(getter)
        ).apply(instance, constructor));
    }
}
