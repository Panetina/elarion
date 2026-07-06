package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CollectionActionPayload(
        String tabId,
        String entryId,
        String actionId
) implements CustomPayload {
    public static final Id<CollectionActionPayload> ID =
            new Id<>(Identifier.of("elarion_core", "collection_action"));
    public static final PacketCodec<PacketByteBuf, CollectionActionPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.tabId(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.entryId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.actionId(), 64);
            },
            buffer -> new CollectionActionPayload(
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 64)));

    public CollectionActionPayload {
        tabId = tabId == null ? "" : tabId;
        entryId = entryId == null ? "" : entryId;
        actionId = actionId == null ? "" : actionId;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
