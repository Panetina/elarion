package panetina.elarion.addons.portals.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.portals.model.PortalAxis;
import panetina.elarion.addons.portals.model.PortalBounds;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalRouteSnapshot;

import java.util.ArrayList;
import java.util.List;

public record PortalVisualSyncPayload(List<Entry> entries) implements CustomPayload {
    public static final Id<PortalVisualSyncPayload> ID =
            new Id<>(Identifier.of("elarion_portals", "visual_sync"));
    public static final PacketCodec<PacketByteBuf, PortalVisualSyncPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeVarInt(payload.entries().size());
                payload.entries().forEach((entry) -> entry.write(buffer));
            },
            buffer -> {
                int size = buffer.readVarInt();
                List<Entry> entries = new ArrayList<>(size);
                for (int index = 0; index < size; index++) entries.add(Entry.read(buffer));
                return new PortalVisualSyncPayload(entries);
            });

    public PortalVisualSyncPayload {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static PortalVisualSyncPayload from(Iterable<PortalRouteSnapshot> snapshots) {
        List<Entry> result = new ArrayList<>();
        for (PortalRouteSnapshot snapshot : snapshots) {
            if (snapshot.source() != null) result.add(Entry.of(snapshot, snapshot.source()));
            if (snapshot.returnEndpoint() != null) result.add(Entry.of(snapshot, snapshot.returnEndpoint()));
        }
        return new PortalVisualSyncPayload(result);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record Entry(
            String routeId,
            String worldId,
            PortalBounds bounds,
            boolean active,
            int argb,
            String texture
    ) {
        static Entry of(PortalRouteSnapshot route, PortalEndpoint endpoint) {
            return new Entry(route.routeId(), endpoint.worldId(), endpoint.bounds(),
                    route.active(), route.visual().argb(), route.visual().texture());
        }

        void write(PacketByteBuf buffer) {
            buffer.writeString(routeId);
            buffer.writeString(worldId);
            buffer.writeVarInt(bounds.minX());
            buffer.writeVarInt(bounds.minY());
            buffer.writeVarInt(bounds.minZ());
            buffer.writeVarInt(bounds.maxX());
            buffer.writeVarInt(bounds.maxY());
            buffer.writeVarInt(bounds.maxZ());
            buffer.writeEnumConstant(bounds.axis());
            buffer.writeBoolean(active);
            buffer.writeInt(argb);
            buffer.writeString(texture);
        }

        static Entry read(PacketByteBuf buffer) {
            String route = buffer.readString(128);
            String world = buffer.readString(256);
            int minX = buffer.readVarInt();
            int minY = buffer.readVarInt();
            int minZ = buffer.readVarInt();
            int maxX = buffer.readVarInt();
            int maxY = buffer.readVarInt();
            int maxZ = buffer.readVarInt();
            PortalAxis axis = buffer.readEnumConstant(PortalAxis.class);
            return new Entry(route, world, new PortalBounds(minX, minY, minZ, maxX, maxY, maxZ, axis),
                    buffer.readBoolean(), buffer.readInt(), buffer.readString(256));
        }
    }
}
