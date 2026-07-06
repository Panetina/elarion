package panetina.elarion.addons.underworld.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.ArrayList;
import java.util.List;

public record GraveOpenPayload(
        String corpseId,
        String title,
        String body,
        String ownerName,
        boolean error,
        String accessState,
        long protectedUntil,
        long publicLootStartedAt,
        long decaysAt,
        int totalItemCount,
        List<Entry> items
) implements CustomPayload {
    public static final Id<GraveOpenPayload> ID = new Id<>(Identifier.of("elarion_underworld", "grave_open"));
    public static final PacketCodec<PacketByteBuf, GraveOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.corpseId(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.title(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.body(), 512);
                ElarionPacketCodecs.writeString(buffer, payload.ownerName(), 128);
                buffer.writeBoolean(payload.error());
                ElarionPacketCodecs.writeString(buffer, payload.accessState(), 64);
                buffer.writeLong(payload.protectedUntil());
                buffer.writeLong(payload.publicLootStartedAt());
                buffer.writeLong(payload.decaysAt());
                buffer.writeVarInt(payload.totalItemCount());
                buffer.writeVarInt(payload.items().size());
                for (Entry entry : payload.items()) {
                    ElarionPacketCodecs.writeString(buffer, entry.itemId(), 256);
                    buffer.writeVarInt(entry.count());
                    ElarionPacketCodecs.writeString(buffer, entry.stackNbt(), 1_000_000);
                    ElarionPacketCodecs.writeString(buffer, entry.sourceType(), 64);
                    ElarionPacketCodecs.writeString(buffer, entry.sourceId(), 128);
                    ElarionPacketCodecs.writeString(buffer, entry.sourceLabel(), 128);
                    buffer.writeVarInt(entry.slotIndex());
                    ElarionPacketCodecs.writeString(buffer, entry.equipmentSlot(), 64);
                }
            },
            buffer -> {
                String corpseId = ElarionPacketCodecs.readString(buffer, 64);
                String title = ElarionPacketCodecs.readString(buffer, 128);
                String body = ElarionPacketCodecs.readString(buffer, 512);
                String ownerName = ElarionPacketCodecs.readString(buffer, 128);
                boolean error = buffer.readBoolean();
                String accessState = ElarionPacketCodecs.readString(buffer, 64);
                long protectedUntil = buffer.readLong();
                long publicLootStartedAt = buffer.readLong();
                long decaysAt = buffer.readLong();
                int totalItemCount = Math.max(0, Math.min(4096, buffer.readVarInt()));
                int count = ElarionPacketCodecs.readBoundedCount(buffer, 256);
                List<Entry> entries = new ArrayList<>(count);
                for (int index = 0; index < count; index++) {
                    entries.add(new Entry(ElarionPacketCodecs.readString(buffer, 256),
                            Math.max(0, Math.min(999, buffer.readVarInt())),
                            ElarionPacketCodecs.readString(buffer, 1_000_000),
                            ElarionPacketCodecs.readString(buffer, 64),
                            ElarionPacketCodecs.readString(buffer, 128),
                            ElarionPacketCodecs.readString(buffer, 128),
                            Math.max(-1, Math.min(255, buffer.readVarInt())),
                            ElarionPacketCodecs.readString(buffer, 64)));
                }
                return new GraveOpenPayload(corpseId, title, body, ownerName, error, accessState, protectedUntil,
                        publicLootStartedAt, decaysAt, totalItemCount, entries);
            });

    public GraveOpenPayload {
        corpseId = corpseId == null ? "" : corpseId;
        title = title == null ? "" : title;
        body = body == null ? "" : body;
        ownerName = ownerName == null ? "" : ownerName;
        accessState = accessState == null ? "" : accessState;
        totalItemCount = Math.max(0, totalItemCount);
        items = items == null ? List.of() : List.copyOf(items);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record Entry(
            String itemId,
            int count,
            String stackNbt,
            String sourceType,
            String sourceId,
            String sourceLabel,
            int slotIndex,
            String equipmentSlot
    ) {
        public Entry {
            itemId = itemId == null ? "" : itemId;
            count = Math.max(0, Math.min(999, count));
            stackNbt = stackNbt == null ? "" : stackNbt;
            sourceType = sourceType == null ? "" : sourceType;
            sourceId = sourceId == null ? "" : sourceId;
            sourceLabel = sourceLabel == null ? "" : sourceLabel;
            slotIndex = Math.max(-1, Math.min(255, slotIndex));
            equipmentSlot = equipmentSlot == null ? "" : equipmentSlot;
        }
    }
}
