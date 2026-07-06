package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import panetina.elarion.core.network.ElarionPacketCodecs;

public record NpcDialogueCardPayload(
        String id,
        String label,
        String icon,
        int count,
        long currencyAmount,
        boolean disabled
) {
    public static void write(NpcDialogueCardPayload card, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, card.id(), 128);
        ElarionPacketCodecs.writeString(buffer, card.label(), 256);
        ElarionPacketCodecs.writeString(buffer, card.icon(), 256);
        buffer.writeVarInt(card.count());
        buffer.writeVarLong(card.currencyAmount());
        buffer.writeBoolean(card.disabled());
    }

    public static NpcDialogueCardPayload read(PacketByteBuf buffer) {
        return new NpcDialogueCardPayload(
                ElarionPacketCodecs.readString(buffer, 128),
                ElarionPacketCodecs.readString(buffer, 256),
                ElarionPacketCodecs.readString(buffer, 256),
                buffer.readVarInt(),
                buffer.readVarLong(),
                buffer.readBoolean());
    }
}
