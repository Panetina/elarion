package panetina.elarion.addons.angling.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import panetina.elarion.core.model.CatchSpeciesSummary;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable historical snapshot embedded in a signed guide.
 *
 * <p>This is presentation data captured from Core's projection when the guide is signed. It is never
 * updated as canonical catch state and must not be used to grant progression or rewards.</p>
 */
public record AnglingSignedGuideComponent(
        UUID owner,
        Map<Identifier, CatchSpeciesSummary> species,
        String signature,
        long signedAt
) {
    public static final int MAX_SPECIES = 512;
    public static final int MAX_SIGNATURE_LENGTH = 20;

    private static final Codec<Long> NON_NEGATIVE_LONG = Codec.LONG.validate(value -> value >= 0
            ? DataResult.success(value)
            : DataResult.error(() -> "value must be non-negative"));
    private static final Codec<String> SIGNATURE_CODEC = Codec.STRING.validate(value ->
            value.length() <= MAX_SIGNATURE_LENGTH
                    ? DataResult.success(value)
                    : DataResult.error(() -> "guide signature exceeds " + MAX_SIGNATURE_LENGTH + " characters"));
    private static final Codec<CatchSpeciesSummary> SPECIES_SUMMARY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    NON_NEGATIVE_LONG.fieldOf("total_count").forGetter(CatchSpeciesSummary::totalCount),
                    NON_NEGATIVE_LONG.fieldOf("first_catch_at").forGetter(CatchSpeciesSummary::firstCatchAt),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("fastest_time_ticks")
                            .forGetter(CatchSpeciesSummary::fastestTimeTicks),
                    NON_NEGATIVE_LONG.fieldOf("accumulated_time_ticks")
                            .forGetter(CatchSpeciesSummary::accumulatedTimeTicks),
                    NON_NEGATIVE_LONG.fieldOf("timed_sample_count")
                            .forGetter(CatchSpeciesSummary::timedSampleCount),
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("largest_size_mm")
                            .forGetter(CatchSpeciesSummary::largestSizeMillimetres),
                    NON_NEGATIVE_LONG.fieldOf("heaviest_weight_g")
                            .forGetter(CatchSpeciesSummary::heaviestWeightGrams),
                    Codec.intRange(CatchSpeciesSummary.NO_PERCENTILE, 10_000).fieldOf("best_percentile_bps")
                            .forGetter(CatchSpeciesSummary::bestPercentileBasisPoints),
                    NON_NEGATIVE_LONG.fieldOf("golden_count").forGetter(CatchSpeciesSummary::goldenCount),
                    NON_NEGATIVE_LONG.fieldOf("perfect_count").forGetter(CatchSpeciesSummary::perfectCount),
                    NON_NEGATIVE_LONG.fieldOf("treasure_count").forGetter(CatchSpeciesSummary::treasureCount)
            ).apply(instance, CatchSpeciesSummary::new));
    private static final Codec<Map<Identifier, CatchSpeciesSummary>> SPECIES_CODEC = Codec
            .unboundedMap(Identifier.CODEC, SPECIES_SUMMARY_CODEC)
            .validate(values -> values.size() <= MAX_SPECIES
                    ? DataResult.success(values)
                    : DataResult.error(() -> "signed guide exceeds " + MAX_SPECIES + " species"));

    public static final Codec<AnglingSignedGuideComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Uuids.CODEC.fieldOf("owner").forGetter(AnglingSignedGuideComponent::owner),
                    SPECIES_CODEC.fieldOf("species").forGetter(AnglingSignedGuideComponent::species),
                    SIGNATURE_CODEC.fieldOf("signature").forGetter(AnglingSignedGuideComponent::signature),
                    NON_NEGATIVE_LONG.fieldOf("signed_at").forGetter(AnglingSignedGuideComponent::signedAt)
            ).apply(instance, AnglingSignedGuideComponent::new));

    public AnglingSignedGuideComponent {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(signature, "signature");
        if (species.size() > MAX_SPECIES || species.entrySet().stream()
                .anyMatch(entry -> entry.getKey() == null || entry.getValue() == null)) {
            throw new IllegalArgumentException("Signed guide exceeds its bounded species contract");
        }
        if (signature.length() > MAX_SIGNATURE_LENGTH) {
            throw new IllegalArgumentException("Guide signature exceeds " + MAX_SIGNATURE_LENGTH + " characters");
        }
        if (signedAt < 0) {
            throw new IllegalArgumentException("Guide signing timestamp cannot be negative");
        }
        species = Map.copyOf(species);
    }
}
