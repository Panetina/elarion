package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Bounded server-authored actions for a single online player target. */
public record PlayerContextActionSnapshotPayload(UUID targetId, String targetName, List<Entry> actions) implements CustomPayload {
    public record Entry(String id, String label) {
        public Entry { if (id == null || !id.matches("[a-z0-9_.:-]{3,96}") || label == null || label.isBlank() || label.length() > 64) throw new IllegalArgumentException("Invalid player context entry."); }
    }
    public static final Id<PlayerContextActionSnapshotPayload> ID = new Id<>(Identifier.of("elarion_core", "player_context_snapshot"));
    public static final PacketCodec<PacketByteBuf, PlayerContextActionSnapshotPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> { buffer.writeUuid(payload.targetId); ElarionPacketCodecs.writeString(buffer, payload.targetName, 64); buffer.writeVarInt(payload.actions.size()); for (Entry entry : payload.actions) { ElarionPacketCodecs.writeString(buffer, entry.id, 96); ElarionPacketCodecs.writeString(buffer, entry.label, 64); } },
            buffer -> { UUID target = buffer.readUuid(); String name = ElarionPacketCodecs.readString(buffer, 64); int count = ElarionPacketCodecs.readBoundedCount(buffer, 8); List<Entry> entries = new ArrayList<>(count); for (int i = 0; i < count; i++) entries.add(new Entry(ElarionPacketCodecs.readString(buffer, 96), ElarionPacketCodecs.readString(buffer, 64))); return new PlayerContextActionSnapshotPayload(target, name, entries); });
    public PlayerContextActionSnapshotPayload { if (targetId == null || targetName == null || targetName.isBlank()) throw new IllegalArgumentException("Player context target is required."); actions = actions == null ? List.of() : actions.stream().distinct().limit(8).toList(); }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
