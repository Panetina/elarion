package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChatRecipientRequestPayload() implements CustomPayload {
    public static final ChatRecipientRequestPayload INSTANCE = new ChatRecipientRequestPayload();
    public static final Id<ChatRecipientRequestPayload> ID = new Id<>(Identifier.of("elarion_core", "chat_recipients_request"));
    public static final PacketCodec<PacketByteBuf, ChatRecipientRequestPayload> CODEC = PacketCodec.unit(INSTANCE);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
