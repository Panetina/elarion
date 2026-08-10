package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.UUID;

/** Client request for actions visible for one currently targeted player. */
public record PlayerContextActionRequestPayload(UUID targetId) implements CustomPayload {
    public static final Id<PlayerContextActionRequestPayload> ID = new Id<>(Identifier.of("elarion_core", "player_context_request"));
    public static final PacketCodec<PacketByteBuf, PlayerContextActionRequestPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> buffer.writeUuid(payload.targetId), buffer -> new PlayerContextActionRequestPayload(buffer.readUuid()));
    public PlayerContextActionRequestPayload { if (targetId == null) throw new IllegalArgumentException("Player context target is required."); }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
