package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

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
                buffer.writeString(payload.nodeId());
                buffer.writeString(payload.optionId());
                buffer.writeString(payload.value());
            },
            buffer -> new NpcDialoguePromptSubmitPayload(
                    buffer.readUuid(),
                    buffer.readString(128),
                    buffer.readString(128),
                    buffer.readString(16))
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
