package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AdminPanelOpenRequestPayload(String selectedTabId, String selectedRowId) implements CustomPayload {
    public static final Id<AdminPanelOpenRequestPayload> ID =
            new Id<>(Identifier.of("elarion_core", "admin_panel_open_request"));
    public static final PacketCodec<PacketByteBuf, AdminPanelOpenRequestPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.selectedTabId(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.selectedRowId(), 128);
            },
            buffer -> new AdminPanelOpenRequestPayload(
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 128)));

    public AdminPanelOpenRequestPayload {
        selectedTabId = selectedTabId == null ? "" : selectedTabId;
        selectedRowId = selectedRowId == null ? "" : selectedRowId;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
