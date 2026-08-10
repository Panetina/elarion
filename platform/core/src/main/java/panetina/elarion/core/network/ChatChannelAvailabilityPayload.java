package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.ElarionChatChannel;

import java.util.ArrayList;
import java.util.List;

/** Server-authored list of channels the current player may select. */
public record ChatChannelAvailabilityPayload(List<ElarionChatChannel> channels) implements CustomPayload {
    public static final Id<ChatChannelAvailabilityPayload> ID = new Id<>(Identifier.of("elarion_core", "chat_channels"));
    public static final PacketCodec<PacketByteBuf, ChatChannelAvailabilityPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> { buffer.writeVarInt(payload.channels.size()); for (ElarionChatChannel channel : payload.channels) buffer.writeVarInt(channel.ordinal()); },
            buffer -> { int count = ElarionPacketCodecs.readBoundedCount(buffer, ElarionChatChannel.values().length); List<ElarionChatChannel> values = new ArrayList<>(count); for (int i = 0; i < count; i++) { int ordinal = buffer.readVarInt(); if (ordinal < 0 || ordinal >= ElarionChatChannel.values().length) throw new IllegalArgumentException("Invalid chat channel."); values.add(ElarionChatChannel.values()[ordinal]); } return new ChatChannelAvailabilityPayload(values); });
    public ChatChannelAvailabilityPayload { channels = channels == null ? List.of(ElarionChatChannel.LOCAL) : channels.stream().filter(java.util.Objects::nonNull).distinct().toList(); if (!channels.contains(ElarionChatChannel.LOCAL)) { List<ElarionChatChannel> withLocal = new ArrayList<>(); withLocal.add(ElarionChatChannel.LOCAL); withLocal.addAll(channels); channels = List.copyOf(withLocal); } }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
