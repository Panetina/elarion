package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;

public record NpcDialogueCardPayload(
        String id,
        String label,
        String icon,
        int count,
        long currencyAmount,
        boolean disabled
) {
    public static void write(NpcDialogueCardPayload card, PacketByteBuf buffer) {
        buffer.writeString(card.id());
        buffer.writeString(card.label());
        buffer.writeString(card.icon());
        buffer.writeVarInt(card.count());
        buffer.writeVarLong(card.currencyAmount());
        buffer.writeBoolean(card.disabled());
    }

    public static NpcDialogueCardPayload read(PacketByteBuf buffer) {
        return new NpcDialogueCardPayload(
                buffer.readString(128),
                buffer.readString(256),
                buffer.readString(256),
                buffer.readVarInt(),
                buffer.readVarLong(),
                buffer.readBoolean());
    }
}
