package panetina.elarion.addons.offerings.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShrineContributionSubmitPayload(
        String instanceId,
        String requirementKey,
        String rawAmount
) implements CustomPayload {
    public static final Id<ShrineContributionSubmitPayload> ID =
            new Id<>(Identifier.of("elarion_offerings", "shrine_contribution_submit"));

    public static final PacketCodec<PacketByteBuf, ShrineContributionSubmitPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeString(payload.instanceId(), 128);
                buffer.writeString(payload.requirementKey(), 256);
                buffer.writeString(payload.rawAmount(), 10);
            },
            buffer -> new ShrineContributionSubmitPayload(
                    buffer.readString(128), buffer.readString(256), buffer.readString(10)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
