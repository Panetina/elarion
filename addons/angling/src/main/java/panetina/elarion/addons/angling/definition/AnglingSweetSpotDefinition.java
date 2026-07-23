package panetina.elarion.addons.angling.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

/** Reload-time sweetspot definition; behavior is compiled through its type registry. */
public record AnglingSweetSpotDefinition(
        Identifier sweetspotType,
        Identifier texturePath,
        int hitboxSizePixels,
        int reward,
        boolean flip,
        float vanishingRate,
        float movingRate,
        int color,
        List<AnglingTypedNode> modifiers
) {
    private static final int MAX_MODIFIERS = 64;
    private static final Codec<List<AnglingTypedNode>> MODIFIERS_CODEC = AnglingTypedNode.CODEC.listOf()
            .validate(values -> values.size() <= MAX_MODIFIERS
                    ? DataResult.success(values)
                    : DataResult.error(() -> "Sweetspot modifier count exceeds " + MAX_MODIFIERS));

    public static final Codec<AnglingSweetSpotDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("sweetspot_type").forGetter(AnglingSweetSpotDefinition::sweetspotType),
            Identifier.CODEC.fieldOf("texture_path").forGetter(AnglingSweetSpotDefinition::texturePath),
            Codec.intRange(1, 512).fieldOf("hitbox_size_in_pixels").forGetter(AnglingSweetSpotDefinition::hitboxSizePixels),
            Codec.INT.fieldOf("reward").forGetter(AnglingSweetSpotDefinition::reward),
            Codec.BOOL.fieldOf("is_flip").forGetter(AnglingSweetSpotDefinition::flip),
            Codec.FLOAT.fieldOf("vanishing_rate").forGetter(AnglingSweetSpotDefinition::vanishingRate),
            Codec.FLOAT.fieldOf("moving_rate").forGetter(AnglingSweetSpotDefinition::movingRate),
            Codec.INT.fieldOf("color_as_int").forGetter(AnglingSweetSpotDefinition::color),
            MODIFIERS_CODEC.optionalFieldOf("add_modifiers_on_hit", List.of())
                    .forGetter(AnglingSweetSpotDefinition::modifiers)
    ).apply(instance, AnglingSweetSpotDefinition::new));

    public AnglingSweetSpotDefinition {
        Objects.requireNonNull(sweetspotType, "sweetspotType");
        Objects.requireNonNull(texturePath, "texturePath");
        modifiers = List.copyOf(Objects.requireNonNull(modifiers, "modifiers"));
        if (hitboxSizePixels < 1 || hitboxSizePixels > 512 || modifiers.size() > MAX_MODIFIERS
                || !Float.isFinite(vanishingRate) || !Float.isFinite(movingRate)) {
            throw new IllegalArgumentException("Invalid Angling sweetspot definition");
        }
    }
}
