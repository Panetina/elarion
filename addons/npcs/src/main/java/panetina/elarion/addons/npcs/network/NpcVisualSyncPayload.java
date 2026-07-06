package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record NpcVisualSyncPayload(List<Entry> entries) implements CustomPayload {
    public static final Id<NpcVisualSyncPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "visual_sync"));

    public static final PacketCodec<PacketByteBuf, NpcVisualSyncPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeVarInt(payload.entries().size());
                payload.entries().forEach(entry -> entry.write(buffer));
            },
            buffer -> {
                int count = ElarionPacketCodecs.readBoundedCount(buffer, 512);
                List<Entry> entries = new ArrayList<>();
                for (int index = 0; index < count; index++) {
                    entries.add(Entry.read(buffer));
                }
                return new NpcVisualSyncPayload(entries);
            }
    );

    public NpcVisualSyncPayload {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record Entry(
            UUID npcId,
            UUID entityId,
            String commandId,
            String displayName,
            String skinType,
            String skinTexture,
            String skinPlayerName,
            String skinFallbackType,
            String skinFallbackTexture,
            String portraitType,
            String portraitTexture,
            String portraitPlayerName,
            String portraitFallbackType,
            String portraitFallbackTexture
    ) {
        private void write(PacketByteBuf buffer) {
            buffer.writeUuid(npcId);
            buffer.writeUuid(entityId);
            ElarionPacketCodecs.writeString(buffer, commandId, 128);
            ElarionPacketCodecs.writeString(buffer, displayName, 128);
            ElarionPacketCodecs.writeString(buffer, skinType, 64);
            ElarionPacketCodecs.writeString(buffer, skinTexture, 256);
            ElarionPacketCodecs.writeString(buffer, skinPlayerName, 64);
            ElarionPacketCodecs.writeString(buffer, skinFallbackType, 64);
            ElarionPacketCodecs.writeString(buffer, skinFallbackTexture, 256);
            ElarionPacketCodecs.writeString(buffer, portraitType, 64);
            ElarionPacketCodecs.writeString(buffer, portraitTexture, 256);
            ElarionPacketCodecs.writeString(buffer, portraitPlayerName, 64);
            ElarionPacketCodecs.writeString(buffer, portraitFallbackType, 64);
            ElarionPacketCodecs.writeString(buffer, portraitFallbackTexture, 256);
        }

        private static Entry read(PacketByteBuf buffer) {
            return new Entry(
                    buffer.readUuid(),
                    buffer.readUuid(),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 256));
        }
    }
}
