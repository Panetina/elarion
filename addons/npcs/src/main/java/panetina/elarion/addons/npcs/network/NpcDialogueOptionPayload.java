package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;

public record NpcDialogueOptionPayload(
        String id,
        String buttonText,
        String playerText,
        String promptType,
        String promptQuestion,
        int promptMaxDigits
) {
    public static void write(NpcDialogueOptionPayload option, PacketByteBuf buffer) {
        buffer.writeString(option.id());
        buffer.writeString(option.buttonText());
        buffer.writeString(option.playerText());
        buffer.writeString(option.promptType());
        buffer.writeString(option.promptQuestion());
        buffer.writeVarInt(option.promptMaxDigits());
    }

    public static NpcDialogueOptionPayload read(PacketByteBuf buffer) {
        return new NpcDialogueOptionPayload(
                buffer.readString(128),
                buffer.readString(512),
                buffer.readString(512),
                buffer.readString(64),
                buffer.readString(512),
                buffer.readVarInt());
    }

    public NpcDialogueOptionPayload {
        promptType = promptType == null ? "" : promptType;
        promptQuestion = promptQuestion == null ? "" : promptQuestion;
    }
}
