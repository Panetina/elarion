package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.UUID;

public record NpcDialogueSelectPayload(UUID npcId, String nodeId, String optionId) implements CustomPayload {
    public static final Id<NpcDialogueSelectPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "dialogue_select"));

    public static final PacketCodec<PacketByteBuf, NpcDialogueSelectPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.npcId());
                ElarionPacketCodecs.writeString(buffer, payload.nodeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.optionId(), 128);
            },
            buffer -> new NpcDialogueSelectPayload(
                    buffer.readUuid(),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
