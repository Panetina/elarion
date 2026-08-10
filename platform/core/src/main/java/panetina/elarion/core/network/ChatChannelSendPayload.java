package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.ElarionChatChannel;

import java.util.UUID;

/** Client request only; the server still resolves every recipient and permission. */
public record ChatChannelSendPayload(ElarionChatChannel channel, UUID recipientId, String message) implements CustomPayload {
    public static final Id<ChatChannelSendPayload> ID = new Id<>(Identifier.of("elarion_core", "chat_channel_send"));
    public static final PacketCodec<PacketByteBuf, ChatChannelSendPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> { buffer.writeVarInt(payload.channel.ordinal()); buffer.writeBoolean(payload.recipientId != null); if (payload.recipientId != null) buffer.writeUuid(payload.recipientId); ElarionPacketCodecs.writeString(buffer, payload.message, 256); },
            buffer -> { int ordinal = buffer.readVarInt(); ElarionChatChannel[] values = ElarionChatChannel.values(); if (ordinal < 0 || ordinal >= values.length) throw new IllegalArgumentException("Unknown chat channel"); UUID recipient = buffer.readBoolean() ? buffer.readUuid() : null; return new ChatChannelSendPayload(values[ordinal], recipient, ElarionPacketCodecs.readString(buffer, 256)); });
    public ChatChannelSendPayload { channel = channel == null ? ElarionChatChannel.LOCAL : channel; message = message == null ? "" : message.trim(); }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
