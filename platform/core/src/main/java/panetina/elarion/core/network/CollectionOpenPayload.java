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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record CollectionOpenPayload(ElarionCollectionSnapshot snapshot) implements CustomPayload {
    public static final Id<CollectionOpenPayload> ID =
            new Id<>(Identifier.of("elarion_core", "collection_open"));
    static final int MAX_TABS = 32;
    static final int MAX_ENTRIES_PER_TAB = 512;
    static final int MAX_ACTIONS_PER_ENTRY = 16;
    private static final int MAX_TAB_ID_LENGTH = 64;
    private static final int MAX_ENTRY_ID_LENGTH = 128;
    private static final int MAX_ACTION_ID_LENGTH = 64;
    private static final int MAX_RANK_LABEL_LENGTH = 32;
    public static final PacketCodec<PacketByteBuf, CollectionOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> writeSnapshot(payload.snapshot(), buffer),
            buffer -> new CollectionOpenPayload(readSnapshot(buffer)));

    static void writeSnapshot(ElarionCollectionSnapshot snapshot, PacketByteBuf buffer) {
        ElarionCollectionSnapshot wire = wireSnapshot(snapshot);
        ElarionPacketCodecs.writeString(buffer, wire.title(), 128);
        ElarionPacketCodecs.writeString(buffer, wire.subtitle(), 256);
        ElarionPacketCodecs.writeString(buffer, wire.selectedTabId(), MAX_TAB_ID_LENGTH);
        ElarionPacketCodecs.writeString(buffer, wire.message(), 256);
        buffer.writeVarInt(wire.tabs().size());
        wire.tabs().forEach(tab -> writeTab(tab, buffer));
    }

    static ElarionCollectionSnapshot readSnapshot(PacketByteBuf buffer) {
        String title = ElarionPacketCodecs.readString(buffer, 128);
        String subtitle = ElarionPacketCodecs.readString(buffer, 256);
        String selectedTabId = ElarionPacketCodecs.readString(buffer, 64);
        String message = ElarionPacketCodecs.readString(buffer, 256);
        int count = ElarionPacketCodecs.readBoundedCount(buffer, MAX_TABS);
        List<ElarionCollectionTab> tabs = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            tabs.add(readTab(buffer));
        }
        return new ElarionCollectionSnapshot(title, subtitle, selectedTabId, message, tabs);
    }

    private static void writeTab(ElarionCollectionTab tab, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, tab.id(), MAX_TAB_ID_LENGTH);
        ElarionPacketCodecs.writeString(buffer, tab.title(), 128);
        ElarionPacketCodecs.writeString(buffer, tab.subtitle(), 256);
        buffer.writeVarInt(tab.entries().size());
        tab.entries().forEach(entry -> writeEntry(entry, buffer));
    }

    private static ElarionCollectionTab readTab(PacketByteBuf buffer) {
        String id = ElarionPacketCodecs.readString(buffer, MAX_TAB_ID_LENGTH);
        String title = ElarionPacketCodecs.readString(buffer, 128);
        String subtitle = ElarionPacketCodecs.readString(buffer, 256);
        int count = ElarionPacketCodecs.readBoundedCount(buffer, MAX_ENTRIES_PER_TAB);
        List<ElarionCollectionEntry> entries = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            entries.add(readEntry(buffer));
        }
        return new ElarionCollectionTab(id, title, subtitle, entries);
    }

    private static void writeEntry(ElarionCollectionEntry entry, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, entry.id(), MAX_ENTRY_ID_LENGTH);
        ElarionPacketCodecs.writeString(buffer, entry.title(), 128);
        ElarionPacketCodecs.writeString(buffer, entry.subtitle(), 256);
        ElarionPacketCodecs.writeString(buffer, entry.body(), 1024);
        ElarionPacketCodecs.writeString(buffer, entry.state(), 128);
        ElarionPacketCodecs.writeString(buffer, entry.icon(), 256);
        buffer.writeBoolean(entry.unlocked());
        buffer.writeBoolean(entry.active());
        buffer.writeInt(entry.accentColor());
        ElarionPacketCodecs.writeString(buffer, entry.rankLabel(), MAX_RANK_LABEL_LENGTH);
        buffer.writeInt(entry.rankColor());
        buffer.writeVarInt(entry.actions().size());
        entry.actions().forEach(action -> writeAction(action, buffer));
    }

    private static ElarionCollectionEntry readEntry(PacketByteBuf buffer) {
        String id = ElarionPacketCodecs.readString(buffer, MAX_ENTRY_ID_LENGTH);
        String title = ElarionPacketCodecs.readString(buffer, 128);
        String subtitle = ElarionPacketCodecs.readString(buffer, 256);
        String body = ElarionPacketCodecs.readString(buffer, 1024);
        String state = ElarionPacketCodecs.readString(buffer, 128);
        String icon = ElarionPacketCodecs.readString(buffer, 256);
        boolean unlocked = buffer.readBoolean();
        boolean active = buffer.readBoolean();
        int accentColor = buffer.readInt();
        String rankLabel = ElarionPacketCodecs.readString(buffer, MAX_RANK_LABEL_LENGTH);
        int rankColor = buffer.readInt();
        int count = ElarionPacketCodecs.readBoundedCount(buffer, MAX_ACTIONS_PER_ENTRY);
        List<ElarionCollectionAction> actions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            actions.add(readAction(buffer));
        }
        return new ElarionCollectionEntry(
                id, title, subtitle, body, state, icon, unlocked, active, actions, accentColor, rankLabel, rankColor);
    }

    private static void writeAction(ElarionCollectionAction action, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, action.id(), MAX_ACTION_ID_LENGTH);
        ElarionPacketCodecs.writeString(buffer, action.label(), 128);
        buffer.writeBoolean(action.enabled());
    }

    private static ElarionCollectionAction readAction(PacketByteBuf buffer) {
        return new ElarionCollectionAction(
                ElarionPacketCodecs.readString(buffer, MAX_ACTION_ID_LENGTH),
                ElarionPacketCodecs.readString(buffer, 128),
                buffer.readBoolean());
    }

    static ElarionCollectionSnapshot wireSnapshot(ElarionCollectionSnapshot snapshot) {
        if (snapshot == null) return ElarionCollectionSnapshot.empty("");
        List<ElarionCollectionTab> tabs = new ArrayList<>();
        Set<String> tabIds = new LinkedHashSet<>();
        for (ElarionCollectionTab tab : snapshot.tabs()) {
            if (tabs.size() >= MAX_TABS) break;
            if (tab == null || !validId(tab.id(), MAX_TAB_ID_LENGTH) || !tabIds.add(tab.id())) continue;
            tabs.add(new ElarionCollectionTab(
                    tab.id(),
                    tab.title(),
                    tab.subtitle(),
                    wireEntries(tab.entries())));
        }
        String selected = snapshot.selectedTabId();
        if (!tabIds.contains(selected)) selected = tabs.isEmpty() ? "" : tabs.getFirst().id();
        return new ElarionCollectionSnapshot(
                snapshot.title(), snapshot.subtitle(), selected, snapshot.message(), tabs);
    }

    private static List<ElarionCollectionEntry> wireEntries(List<ElarionCollectionEntry> values) {
        if (values == null || values.isEmpty()) return List.of();
        List<ElarionCollectionEntry> entries = new ArrayList<>();
        Set<String> entryIds = new LinkedHashSet<>();
        for (ElarionCollectionEntry entry : values) {
            if (entries.size() >= MAX_ENTRIES_PER_TAB) break;
            if (entry == null || !validId(entry.id(), MAX_ENTRY_ID_LENGTH) || !entryIds.add(entry.id())) continue;
            entries.add(new ElarionCollectionEntry(
                    entry.id(),
                    entry.title(),
                    entry.subtitle(),
                    entry.body(),
                    entry.state(),
                    entry.icon(),
                    entry.unlocked(),
                    entry.active(),
                    wireActions(entry.actions()),
                    entry.accentColor(),
                    entry.rankLabel(),
                    entry.rankColor()));
        }
        return List.copyOf(entries);
    }

    private static List<ElarionCollectionAction> wireActions(List<ElarionCollectionAction> values) {
        if (values == null || values.isEmpty()) return List.of();
        List<ElarionCollectionAction> actions = new ArrayList<>();
        Set<String> actionIds = new LinkedHashSet<>();
        for (ElarionCollectionAction action : values) {
            if (actions.size() >= MAX_ACTIONS_PER_ENTRY) break;
            if (action == null || !validId(action.id(), MAX_ACTION_ID_LENGTH) || !actionIds.add(action.id())) continue;
            actions.add(action);
        }
        return List.copyOf(actions);
    }

    private static boolean validId(String value, int maxLength) {
        if (value == null || value.isBlank() || !value.equals(value.trim()) || value.length() > maxLength) {
            return false;
        }
        return value.codePoints().noneMatch(codePoint ->
                Character.isISOControl(codePoint) || Character.getType(codePoint) == Character.FORMAT);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
