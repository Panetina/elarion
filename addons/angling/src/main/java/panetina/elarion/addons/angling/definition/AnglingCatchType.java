package panetina.elarion.addons.angling.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

/** Stable categories from the frozen catch registry. */
public enum AnglingCatchType {
    FISH,
    TROPHY,
    SECRET,
    EXTRA;

    public static final Codec<AnglingCatchType> CODEC = Codec.STRING.comapFlatMap(
            value -> {
                try {
                    return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException exception) {
                    return DataResult.error(() -> "Unknown Angling catch type: " + value);
                }
            },
            AnglingCatchType::serializedName
    );

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
