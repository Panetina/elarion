package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.UUID;

public record NpcDialoguePromptSubmitPayload(
        UUID npcId,
        String nodeId,
        String optionId,
        String value
) implements CustomPayload {
    public static final Id<NpcDialoguePromptSubmitPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "dialogue_prompt_submit"));

    public static final PacketCodec<PacketByteBuf, NpcDialoguePromptSubmitPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.npcId());
                ElarionPacketCodecs.writeString(buffer, payload.nodeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.optionId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.value(), 16);
            },
            buffer -> new NpcDialoguePromptSubmitPayload(
                    buffer.readUuid(),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 16))
    );

    public NpcDialoguePromptSubmitPayload {
        nodeId = nodeId == null ? "" : nodeId;
        optionId = optionId == null ? "" : optionId;
        value = value == null ? "" : value;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
