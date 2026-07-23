package panetina.elarion.addons.angling.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.Objects;

/**
 * Versioned item component for server-computed fish properties. Core catch
 * telemetry remains canonical; this component is portable item presentation
 * data and cannot grant metrics or rewards by itself.
 */
public record AnglingCaughtFishComponent(
        int schemaVersion,
        Identifier definitionId,
        int sizeMillimetres,
        int weightGrams,
        int percentileBasisPoints,
        AnglingRarity rarity,
        boolean golden,
        boolean perfect
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public static final Codec<AnglingCaughtFishComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, CURRENT_SCHEMA_VERSION).optionalFieldOf("schema_version", CURRENT_SCHEMA_VERSION)
                    .forGetter(AnglingCaughtFishComponent::schemaVersion),
            Identifier.CODEC.fieldOf("definition_id").forGetter(AnglingCaughtFishComponent::definitionId),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("size_mm").forGetter(AnglingCaughtFishComponent::sizeMillimetres),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight_grams").forGetter(AnglingCaughtFishComponent::weightGrams),
            Codec.intRange(0, 10_000).optionalFieldOf("percentile_bps", 0)
                    .forGetter(AnglingCaughtFishComponent::percentileBasisPoints),
            AnglingRarity.CODEC.optionalFieldOf("rarity", AnglingRarity.COMMON)
                    .forGetter(AnglingCaughtFishComponent::rarity),
            Codec.BOOL.optionalFieldOf("golden", false).forGetter(AnglingCaughtFishComponent::golden),
            Codec.BOOL.optionalFieldOf("perfect", false).forGetter(AnglingCaughtFishComponent::perfect)
    ).apply(instance, AnglingCaughtFishComponent::new));

    public static final PacketCodec<PacketByteBuf, AnglingCaughtFishComponent> PACKET_CODEC = PacketCodec.of(
            (value, buffer) -> {
                buffer.writeVarInt(value.schemaVersion);
                ElarionPacketCodecs.writeString(buffer, value.definitionId.toString(), 256);
                buffer.writeVarInt(value.sizeMillimetres);
                buffer.writeVarInt(value.weightGrams);
                buffer.writeVarInt(value.percentileBasisPoints);
                ElarionPacketCodecs.writeString(buffer, value.rarity.serializedName(), 16);
                buffer.writeBoolean(value.golden);
                buffer.writeBoolean(value.perfect);
            },
            buffer -> {
                int schemaVersion = buffer.readVarInt();
                Identifier definitionId = Identifier.tryParse(ElarionPacketCodecs.readString(buffer, 256));
                if (definitionId == null) {
                    throw new IllegalArgumentException("Invalid caught-fish definition identifier");
                }
                int size = buffer.readVarInt();
                int weight = buffer.readVarInt();
                int percentile = buffer.readVarInt();
                AnglingRarity rarity = AnglingRarity.fromSerializedName(
                        ElarionPacketCodecs.readString(buffer, 16));
                return new AnglingCaughtFishComponent(schemaVersion, definitionId, size, weight, percentile,
                        rarity, buffer.readBoolean(), buffer.readBoolean());
            }
    );

    public AnglingCaughtFishComponent {
        Objects.requireNonNull(definitionId, "definitionId");
        Objects.requireNonNull(rarity, "rarity");
        if (schemaVersion != CURRENT_SCHEMA_VERSION || sizeMillimetres < 0 || weightGrams < 0
                || percentileBasisPoints < 0 || percentileBasisPoints > 10_000) {
            throw new IllegalArgumentException("Invalid caught-fish component values");
        }
    }
}
