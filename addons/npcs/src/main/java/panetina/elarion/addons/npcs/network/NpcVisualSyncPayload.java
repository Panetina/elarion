package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

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
                int count = buffer.readVarInt();
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
            buffer.writeString(commandId);
            buffer.writeString(displayName);
            buffer.writeString(skinType);
            buffer.writeString(skinTexture);
            buffer.writeString(skinPlayerName);
            buffer.writeString(skinFallbackType);
            buffer.writeString(skinFallbackTexture);
            buffer.writeString(portraitType);
            buffer.writeString(portraitTexture);
            buffer.writeString(portraitPlayerName);
            buffer.writeString(portraitFallbackType);
            buffer.writeString(portraitFallbackTexture);
        }

        private static Entry read(PacketByteBuf buffer) {
            return new Entry(
                    buffer.readUuid(),
                    buffer.readUuid(),
                    buffer.readString(128),
                    buffer.readString(128),
                    buffer.readString(64),
                    buffer.readString(256),
                    buffer.readString(64),
                    buffer.readString(64),
                    buffer.readString(256),
                    buffer.readString(64),
                    buffer.readString(256),
                    buffer.readString(64),
                    buffer.readString(64),
                    buffer.readString(256));
        }
    }
}
