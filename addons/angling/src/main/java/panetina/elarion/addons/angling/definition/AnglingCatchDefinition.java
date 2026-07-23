package panetina.elarion.addons.angling.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

/**
 * Versioned, immutable catch-definition reload DTO. Restriction/modifier nodes
 * are compiled to registered typed handlers before an atomic runtime snapshot
 * is published.
 */
public record AnglingCatchDefinition(
        int schemaVersion,
        AnglingCatchOutput catchInfo,
        int baseChance,
        AnglingSizeWeightDefinition sizeAndWeight,
        AnglingRarity rarity,
        List<AnglingTypedNode> restrictions,
        AnglingDifficultyDefinition difficulty,
        boolean skipsMinigame,
        boolean hasGuideEntry,
        Identifier texture
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    public static final int MAX_RESTRICTIONS = 64;

    private static final Codec<List<AnglingTypedNode>> RESTRICTIONS_CODEC = AnglingTypedNode.CODEC.listOf()
            .validate(values -> values.size() <= MAX_RESTRICTIONS
                    ? DataResult.success(values)
                    : DataResult.error(() -> "Catch restriction count exceeds " + MAX_RESTRICTIONS));

    public static final Codec<AnglingCatchDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, CURRENT_SCHEMA_VERSION).optionalFieldOf("schema_version", CURRENT_SCHEMA_VERSION)
                    .forGetter(AnglingCatchDefinition::schemaVersion),
            AnglingCatchOutput.CODEC.fieldOf("catch_info").forGetter(AnglingCatchDefinition::catchInfo),
            Codec.intRange(0, 1_000_000).fieldOf("base_chance").forGetter(AnglingCatchDefinition::baseChance),
            AnglingSizeWeightDefinition.CODEC.fieldOf("size_and_weight").forGetter(AnglingCatchDefinition::sizeAndWeight),
            AnglingRarity.CODEC.fieldOf("rarity").forGetter(AnglingCatchDefinition::rarity),
            RESTRICTIONS_CODEC.fieldOf("restrictions").forGetter(AnglingCatchDefinition::restrictions),
            AnglingDifficultyDefinition.CODEC.fieldOf("difficulty").forGetter(AnglingCatchDefinition::difficulty),
            Codec.BOOL.fieldOf("skips_minigame").forGetter(AnglingCatchDefinition::skipsMinigame),
            Codec.BOOL.fieldOf("has_guide_entry").forGetter(AnglingCatchDefinition::hasGuideEntry),
            Identifier.CODEC.fieldOf("textures").forGetter(AnglingCatchDefinition::texture)
    ).apply(instance, AnglingCatchDefinition::new));

    public AnglingCatchDefinition {
        Objects.requireNonNull(catchInfo, "catchInfo");
        Objects.requireNonNull(sizeAndWeight, "sizeAndWeight");
        Objects.requireNonNull(rarity, "rarity");
        restrictions = List.copyOf(Objects.requireNonNull(restrictions, "restrictions"));
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(texture, "texture");
        if (schemaVersion != CURRENT_SCHEMA_VERSION || baseChance < 0 || restrictions.size() > MAX_RESTRICTIONS) {
            throw new IllegalArgumentException("Invalid Angling catch definition");
        }
    }
}
