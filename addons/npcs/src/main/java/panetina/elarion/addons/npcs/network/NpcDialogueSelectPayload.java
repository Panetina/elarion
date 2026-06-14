package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record NpcDialogueSelectPayload(UUID npcId, String nodeId, String optionId) implements CustomPayload {
    public static final Id<NpcDialogueSelectPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "dialogue_select"));

    public static final PacketCodec<PacketByteBuf, NpcDialogueSelectPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.npcId());
                buffer.writeString(payload.nodeId());
                buffer.writeString(payload.optionId());
            },
            buffer -> new NpcDialogueSelectPayload(
                    buffer.readUuid(),
                    buffer.readString(128),
                    buffer.readString(128))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
