package panetina.elarion.addons.underworld.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

public record GraveRecoverPayload(String corpseId) implements CustomPayload {
    public static final Id<GraveRecoverPayload> ID = new Id<>(Identifier.of("elarion_underworld", "grave_recover"));
    public static final PacketCodec<PacketByteBuf, GraveRecoverPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> ElarionPacketCodecs.writeString(buffer, payload.corpseId(), 64),
            buffer -> new GraveRecoverPayload(ElarionPacketCodecs.readString(buffer, 64)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
