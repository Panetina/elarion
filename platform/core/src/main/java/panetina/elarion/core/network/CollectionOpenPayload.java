package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.ElarionCollectionAction;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionSnapshot;
import panetina.elarion.core.model.ElarionCollectionTab;

import java.util.ArrayList;
import java.util.List;

public record CollectionOpenPayload(ElarionCollectionSnapshot snapshot) implements CustomPayload {
    public static final Id<CollectionOpenPayload> ID =
            new Id<>(Identifier.of("elarion_core", "collection_open"));
    public static final PacketCodec<PacketByteBuf, CollectionOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> writeSnapshot(payload.snapshot(), buffer),
            buffer -> new CollectionOpenPayload(readSnapshot(buffer)));

    private static void writeSnapshot(ElarionCollectionSnapshot snapshot, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, snapshot.title(), 128);
        ElarionPacketCodecs.writeString(buffer, snapshot.subtitle(), 256);
        ElarionPacketCodecs.writeString(buffer, snapshot.selectedTabId(), 64);
        ElarionPacketCodecs.writeString(buffer, snapshot.message(), 256);
        buffer.writeVarInt(snapshot.tabs().size());
        snapshot.tabs().forEach(tab -> writeTab(tab, buffer));
    }

    private static ElarionCollectionSnapshot readSnapshot(PacketByteBuf buffer) {
        String title = ElarionPacketCodecs.readString(buffer, 128);
        String subtitle = ElarionPacketCodecs.readString(buffer, 256);
        String selectedTabId = ElarionPacketCodecs.readString(buffer, 64);
        String message = ElarionPacketCodecs.readString(buffer, 256);
        int count = ElarionPacketCodecs.readBoundedCount(buffer, 32);
        List<ElarionCollectionTab> tabs = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            tabs.add(readTab(buffer));
        }
        return new ElarionCollectionSnapshot(title, subtitle, selectedTabId, message, tabs);
    }

    private static void writeTab(ElarionCollectionTab tab, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, tab.id(), 64);
        ElarionPacketCodecs.writeString(buffer, tab.title(), 128);
        ElarionPacketCodecs.writeString(buffer, tab.subtitle(), 256);
        buffer.writeVarInt(tab.entries().size());
        tab.entries().forEach(entry -> writeEntry(entry, buffer));
    }

    private static ElarionCollectionTab readTab(PacketByteBuf buffer) {
        String id = ElarionPacketCodecs.readString(buffer, 64);
        String title = ElarionPacketCodecs.readString(buffer, 128);
        String subtitle = ElarionPacketCodecs.readString(buffer, 256);
        int count = ElarionPacketCodecs.readBoundedCount(buffer, 512);
        List<ElarionCollectionEntry> entries = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            entries.add(readEntry(buffer));
        }
        return new ElarionCollectionTab(id, title, subtitle, entries);
    }

    private static void writeEntry(ElarionCollectionEntry entry, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, entry.id(), 128);
        ElarionPacketCodecs.writeString(buffer, entry.title(), 128);
        ElarionPacketCodecs.writeString(buffer, entry.subtitle(), 256);
        ElarionPacketCodecs.writeString(buffer, entry.body(), 1024);
        ElarionPacketCodecs.writeString(buffer, entry.state(), 128);
        ElarionPacketCodecs.writeString(buffer, entry.icon(), 256);
        buffer.writeBoolean(entry.unlocked());
        buffer.writeBoolean(entry.active());
        buffer.writeVarInt(entry.actions().size());
        entry.actions().forEach(action -> writeAction(action, buffer));
    }

    private static ElarionCollectionEntry readEntry(PacketByteBuf buffer) {
        String id = ElarionPacketCodecs.readString(buffer, 128);
        String title = ElarionPacketCodecs.readString(buffer, 128);
        String subtitle = ElarionPacketCodecs.readString(buffer, 256);
        String body = ElarionPacketCodecs.readString(buffer, 1024);
        String state = ElarionPacketCodecs.readString(buffer, 128);
        String icon = ElarionPacketCodecs.readString(buffer, 256);
        boolean unlocked = buffer.readBoolean();
        boolean active = buffer.readBoolean();
        int count = ElarionPacketCodecs.readBoundedCount(buffer, 16);
        List<ElarionCollectionAction> actions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            actions.add(readAction(buffer));
        }
        return new ElarionCollectionEntry(id, title, subtitle, body, state, icon, unlocked, active, actions);
    }

    private static void writeAction(ElarionCollectionAction action, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, action.id(), 64);
        ElarionPacketCodecs.writeString(buffer, action.label(), 128);
        buffer.writeBoolean(action.enabled());
    }

    private static ElarionCollectionAction readAction(PacketByteBuf buffer) {
        return new ElarionCollectionAction(
                ElarionPacketCodecs.readString(buffer, 64),
                ElarionPacketCodecs.readString(buffer, 128),
                buffer.readBoolean());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
