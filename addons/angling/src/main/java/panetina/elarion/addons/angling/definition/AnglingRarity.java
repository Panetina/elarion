package panetina.elarion.addons.angling.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

/** Stable catch rarity identifiers used by definitions and item components. */
public enum AnglingRarity {
    NONE,
    TRASH,
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    GOLDEN;

    public static final Codec<AnglingRarity> CODEC = Codec.STRING.comapFlatMap(
            value -> {
                try {
                    return DataResult.success(fromSerializedName(value));
                } catch (IllegalArgumentException exception) {
                    return DataResult.error(() -> "Unknown Angling rarity: " + value);
                }
            },
            AnglingRarity::serializedName
    );

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static AnglingRarity fromSerializedName(String value) {
        return valueOf(value.toUpperCase(Locale.ROOT));
    }
}
