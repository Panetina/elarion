package panetina.elarion.addons.offerings.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

public record ShrineContributionSubmitPayload(
        String instanceId,
        String requirementKey,
        String rawAmount
) implements CustomPayload {
    public static final Id<ShrineContributionSubmitPayload> ID =
            new Id<>(Identifier.of("elarion_offerings", "shrine_contribution_submit"));

    public static final PacketCodec<PacketByteBuf, ShrineContributionSubmitPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.instanceId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.requirementKey(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.rawAmount(), 10);
            },
            buffer -> new ShrineContributionSubmitPayload(
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 10)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
