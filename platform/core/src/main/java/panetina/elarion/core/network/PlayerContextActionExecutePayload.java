package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.UUID;

/** Client intent; the server re-resolves target, action and authorization. */
public record PlayerContextActionExecutePayload(UUID targetId, String actionId) implements CustomPayload {
    public static final Id<PlayerContextActionExecutePayload> ID = new Id<>(Identifier.of("elarion_core", "player_context_execute"));
    public static final PacketCodec<PacketByteBuf, PlayerContextActionExecutePayload> CODEC = PacketCodec.of(
            (payload, buffer) -> { buffer.writeUuid(payload.targetId); ElarionPacketCodecs.writeString(buffer, payload.actionId, 96); },
            buffer -> new PlayerContextActionExecutePayload(buffer.readUuid(), ElarionPacketCodecs.readString(buffer, 96)));
    public PlayerContextActionExecutePayload {
        if (targetId == null || actionId == null || !actionId.matches("[a-z0-9_.:-]{3,96}")) {
            throw new IllegalArgumentException("Invalid player context action request.");
        }
    }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
