package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.ElarionAdminPanelAction;
import panetina.elarion.core.model.ElarionAdminPanelRow;
import panetina.elarion.core.model.ElarionAdminPanelSnapshot;
import panetina.elarion.core.model.ElarionAdminPanelTab;

import java.util.ArrayList;
import java.util.List;

public record AdminPanelOpenPayload(ElarionAdminPanelSnapshot snapshot) implements CustomPayload {
    public static final Id<AdminPanelOpenPayload> ID =
            new Id<>(Identifier.of("elarion_core", "admin_panel_open"));
    static final int MAX_TABS = 16;
    static final int MAX_ROWS_PER_TAB = 512;
    static final int MAX_ACTIONS_PER_ROW = 32;
    static final int MAX_ACTION_SUGGESTIONS = 128;
    public static final PacketCodec<PacketByteBuf, AdminPanelOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> writeSnapshot(payload.snapshot(), buffer),
            buffer -> new AdminPanelOpenPayload(readSnapshot(buffer)));

    private static void writeSnapshot(ElarionAdminPanelSnapshot snapshot, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, snapshot.title(), 128);
        ElarionPacketCodecs.writeString(buffer, snapshot.subtitle(), 256);
        ElarionPacketCodecs.writeString(buffer, snapshot.selectedTabId(), 64);
        ElarionPacketCodecs.writeString(buffer, snapshot.selectedRowId(), 128);
        ElarionPacketCodecs.writeString(buffer, snapshot.message(), 512);
        List<ElarionAdminPanelTab> tabs = bounded(snapshot.tabs(), MAX_TABS);
        buffer.writeVarInt(tabs.size());
        tabs.forEach(tab -> writeTab(tab, buffer));
    }

    private static ElarionAdminPanelSnapshot readSnapshot(PacketByteBuf buffer) {
        String title = ElarionPacketCodecs.readString(buffer, 128);
        String subtitle = ElarionPacketCodecs.readString(buffer, 256);
        String selectedTabId = ElarionPacketCodecs.readString(buffer, 64);
        String selectedRowId = ElarionPacketCodecs.readString(buffer, 128);
        String message = ElarionPacketCodecs.readString(buffer, 512);
        int count = ElarionPacketCodecs.readBoundedCount(buffer, MAX_TABS);
        List<ElarionAdminPanelTab> tabs = new ArrayList<>(count);
        for (int index = 0; index < count; index++) tabs.add(readTab(buffer));
        return new ElarionAdminPanelSnapshot(title, subtitle, selectedTabId, selectedRowId, message, tabs);
    }

    private static void writeTab(ElarionAdminPanelTab tab, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, tab.id(), 64);
        ElarionPacketCodecs.writeString(buffer, tab.title(), 128);
        ElarionPacketCodecs.writeString(buffer, tab.subtitle(), 256);
        List<ElarionAdminPanelRow> rows = bounded(tab.rows(), MAX_ROWS_PER_TAB);
        buffer.writeVarInt(rows.size());
        rows.forEach(row -> writeRow(row, buffer));
    }

    private static ElarionAdminPanelTab readTab(PacketByteBuf buffer) {
        String id = ElarionPacketCodecs.readString(buffer, 64);
        String title = ElarionPacketCodecs.readString(buffer, 128);
        String subtitle = ElarionPacketCodecs.readString(buffer, 256);
        int count = ElarionPacketCodecs.readBoundedCount(buffer, MAX_ROWS_PER_TAB);
        List<ElarionAdminPanelRow> rows = new ArrayList<>(count);
        for (int index = 0; index < count; index++) rows.add(readRow(buffer));
        return new ElarionAdminPanelTab(id, title, subtitle, rows);
    }

    private static void writeRow(ElarionAdminPanelRow row, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, row.id(), 128);
        ElarionPacketCodecs.writeString(buffer, row.title(), 128);
        ElarionPacketCodecs.writeString(buffer, row.subtitle(), 256);
        ElarionPacketCodecs.writeString(buffer, row.body(), 2048);
        ElarionPacketCodecs.writeString(buffer, row.state(), 128);
        ElarionPacketCodecs.writeString(buffer, row.icon(), 256);
        ElarionPacketCodecs.writeString(buffer, row.kind(), 64);
        buffer.writeBoolean(row.active());
        buffer.writeBoolean(row.danger());
        List<ElarionAdminPanelAction> actions = bounded(row.actions(), MAX_ACTIONS_PER_ROW);
        buffer.writeVarInt(actions.size());
        actions.forEach(action -> writeAction(action, buffer));
    }

    private static ElarionAdminPanelRow readRow(PacketByteBuf buffer) {
        String id = ElarionPacketCodecs.readString(buffer, 128);
        String title = ElarionPacketCodecs.readString(buffer, 128);
        String subtitle = ElarionPacketCodecs.readString(buffer, 256);
        String body = ElarionPacketCodecs.readString(buffer, 2048);
        String state = ElarionPacketCodecs.readString(buffer, 128);
        String icon = ElarionPacketCodecs.readString(buffer, 256);
        String kind = ElarionPacketCodecs.readString(buffer, 64);
        boolean active = buffer.readBoolean();
        boolean danger = buffer.readBoolean();
        int count = ElarionPacketCodecs.readBoundedCount(buffer, MAX_ACTIONS_PER_ROW);
        List<ElarionAdminPanelAction> actions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) actions.add(readAction(buffer));
        return new ElarionAdminPanelRow(id, title, subtitle, body, state, icon, kind, active, danger, actions);
    }

    private static void writeAction(ElarionAdminPanelAction action, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, action.providerId(), 64);
        ElarionPacketCodecs.writeString(buffer, action.id(), 64);
        ElarionPacketCodecs.writeString(buffer, action.label(), 128);
        ElarionPacketCodecs.writeString(buffer, action.style(), 32);
        buffer.writeBoolean(action.enabled());
        buffer.writeBoolean(action.requiresConfirmation());
        ElarionPacketCodecs.writeString(buffer, action.parameterKey(), 64);
        ElarionPacketCodecs.writeString(buffer, action.parameterLabel(), 128);
        ElarionPacketCodecs.writeString(buffer, action.parameterPlaceholder(), 256);
        List<String> suggestions = bounded(action.parameterSuggestions(), MAX_ACTION_SUGGESTIONS);
        buffer.writeVarInt(suggestions.size());
        suggestions.forEach(suggestion -> ElarionPacketCodecs.writeString(buffer, suggestion, 128));
        ElarionPacketCodecs.writeString(buffer, action.confirmationTitle(), 128);
        ElarionPacketCodecs.writeString(buffer, action.confirmationBody(), 1024);
    }

    private static ElarionAdminPanelAction readAction(PacketByteBuf buffer) {
        String providerId = ElarionPacketCodecs.readString(buffer, 64);
        String id = ElarionPacketCodecs.readString(buffer, 64);
        String label = ElarionPacketCodecs.readString(buffer, 128);
        String style = ElarionPacketCodecs.readString(buffer, 32);
        boolean enabled = buffer.readBoolean();
        boolean requiresConfirmation = buffer.readBoolean();
        String parameterKey = ElarionPacketCodecs.readString(buffer, 64);
        String parameterLabel = ElarionPacketCodecs.readString(buffer, 128);
        String parameterPlaceholder = ElarionPacketCodecs.readString(buffer, 256);
        int count = ElarionPacketCodecs.readBoundedCount(buffer, MAX_ACTION_SUGGESTIONS);
        List<String> suggestions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            suggestions.add(ElarionPacketCodecs.readString(buffer, 128));
        }
        return new ElarionAdminPanelAction(
                providerId,
                id,
                label,
                style,
                enabled,
                requiresConfirmation,
                parameterKey,
                parameterLabel,
                parameterPlaceholder,
                suggestions,
                ElarionPacketCodecs.readString(buffer, 128),
                ElarionPacketCodecs.readString(buffer, 1024));
    }

    private static <T> List<T> bounded(List<T> values, int max) {
        if (values == null || values.isEmpty()) return List.of();
        if (values.size() <= max) return values;
        return values.subList(0, max);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
