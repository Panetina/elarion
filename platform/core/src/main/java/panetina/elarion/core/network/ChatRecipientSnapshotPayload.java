package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ChatRecipientSnapshotPayload(List<Entry> recipients) implements CustomPayload {
    public static final Id<ChatRecipientSnapshotPayload> ID = new Id<>(Identifier.of("elarion_core", "chat_recipients"));
    public static final PacketCodec<PacketByteBuf, ChatRecipientSnapshotPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> { buffer.writeVarInt(payload.recipients.size()); for (Entry entry : payload.recipients) { buffer.writeUuid(entry.id); ElarionPacketCodecs.writeString(buffer, entry.nickname, 128); } },
            buffer -> { int count = ElarionPacketCodecs.readBoundedCount(buffer, 256); List<Entry> values = new ArrayList<>(count); for (int i = 0; i < count; i++) values.add(new Entry(buffer.readUuid(), ElarionPacketCodecs.readString(buffer, 128))); return new ChatRecipientSnapshotPayload(values); });
    public ChatRecipientSnapshotPayload { recipients = recipients == null ? List.of() : recipients.stream().filter(java.util.Objects::nonNull).limit(256).toList(); }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
    public record Entry(UUID id, String nickname) { public Entry { nickname = nickname == null ? "" : nickname; } }
}
