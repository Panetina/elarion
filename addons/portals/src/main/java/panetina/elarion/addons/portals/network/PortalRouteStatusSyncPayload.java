package panetina.elarion.addons.portals.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.portals.model.PortalRouteSnapshot;

import java.util.ArrayList;
import java.util.List;

public record PortalRouteStatusSyncPayload(List<Entry> routes) implements CustomPayload {
    public static final Id<PortalRouteStatusSyncPayload> ID =
            new Id<>(Identifier.of("elarion_portals", "route_status_sync"));
    public static final PacketCodec<PacketByteBuf, PortalRouteStatusSyncPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeVarInt(payload.routes.size());
                payload.routes.forEach(entry -> entry.write(buffer));
            },
            buffer -> {
                int size = buffer.readVarInt();
                List<Entry> entries = new ArrayList<>(size);
                for (int index = 0; index < size; index++) entries.add(Entry.read(buffer));
                return new PortalRouteStatusSyncPayload(entries);
            });

    public PortalRouteStatusSyncPayload {
        routes = routes == null ? List.of() : List.copyOf(routes);
    }

    public static PortalRouteStatusSyncPayload from(Iterable<PortalRouteSnapshot> snapshots) {
        List<Entry> result = new ArrayList<>();
        snapshots.forEach(snapshot -> result.add(Entry.of(snapshot)));
        return new PortalRouteStatusSyncPayload(result);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record Entry(
            String routeId,
            String displayName,
            String mode,
            boolean unlocked,
            boolean complete,
            boolean active,
            long opensAt,
            long closesAt,
            String iconItem,
            String statusIconItem,
            int argb
    ) {
        static Entry of(PortalRouteSnapshot snapshot) {
            return new Entry(
                    snapshot.routeId(), snapshot.displayName(), snapshot.mode().configId(),
                    snapshot.unlocked(), snapshot.complete(), snapshot.active(),
                    snapshot.windowStart().toEpochMilli(), snapshot.windowEnd().toEpochMilli(),
                    snapshot.visual().iconItem(), snapshot.visual().statusIconItem(), snapshot.visual().argb());
        }

        void write(PacketByteBuf buffer) {
            buffer.writeString(routeId);
            buffer.writeString(displayName);
            buffer.writeString(mode);
            buffer.writeBoolean(unlocked);
            buffer.writeBoolean(complete);
            buffer.writeBoolean(active);
            buffer.writeLong(opensAt);
            buffer.writeLong(closesAt);
            buffer.writeString(iconItem);
            buffer.writeString(statusIconItem);
            buffer.writeInt(argb);
        }

        static Entry read(PacketByteBuf buffer) {
            return new Entry(
                    buffer.readString(128), buffer.readString(256), buffer.readString(64),
                    buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
                    buffer.readLong(), buffer.readLong(), buffer.readString(256),
                    buffer.readString(256), buffer.readInt());
        }
    }
}
