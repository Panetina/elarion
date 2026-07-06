package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import panetina.elarion.core.network.ElarionPacketCodecs;

public record NpcDialogueOptionPayload(
        String id,
        String buttonText,
        String playerText,
        String promptType,
        String promptQuestion,
        int promptMaxDigits
) {
    public static void write(NpcDialogueOptionPayload option, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, option.id(), 128);
        ElarionPacketCodecs.writeString(buffer, option.buttonText(), 512);
        ElarionPacketCodecs.writeString(buffer, option.playerText(), 512);
        ElarionPacketCodecs.writeString(buffer, option.promptType(), 64);
        ElarionPacketCodecs.writeString(buffer, option.promptQuestion(), 512);
        buffer.writeVarInt(option.promptMaxDigits());
    }

    public static NpcDialogueOptionPayload read(PacketByteBuf buffer) {
        return new NpcDialogueOptionPayload(
                ElarionPacketCodecs.readString(buffer, 128),
                ElarionPacketCodecs.readString(buffer, 512),
                ElarionPacketCodecs.readString(buffer, 512),
                ElarionPacketCodecs.readString(buffer, 64),
                ElarionPacketCodecs.readString(buffer, 512),
                buffer.readVarInt());
    }

    public NpcDialogueOptionPayload {
        promptType = promptType == null ? "" : promptType;
        promptQuestion = promptQuestion == null ? "" : promptQuestion;
    }
}
