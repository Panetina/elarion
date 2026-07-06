package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public record AdminPanelActionPayload(
        String selectedTabId,
        String providerId,
        String targetId,
        String actionId,
        Map<String, String> parameters,
        boolean confirmed
) implements CustomPayload {
    public static final Id<AdminPanelActionPayload> ID =
            new Id<>(Identifier.of("elarion_core", "admin_panel_action"));
    public static final PacketCodec<PacketByteBuf, AdminPanelActionPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.selectedTabId(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.providerId(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.targetId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.actionId(), 64);
                buffer.writeBoolean(payload.confirmed());
                buffer.writeVarInt(payload.parameters().size());
                payload.parameters().forEach((key, value) -> {
                    ElarionPacketCodecs.writeString(buffer, key, 64);
                    ElarionPacketCodecs.writeString(buffer, value, 256);
                });
            },
            buffer -> {
                String selectedTab = ElarionPacketCodecs.readString(buffer, 64);
                String provider = ElarionPacketCodecs.readString(buffer, 64);
                String target = ElarionPacketCodecs.readString(buffer, 128);
                String action = ElarionPacketCodecs.readString(buffer, 64);
                boolean confirmed = buffer.readBoolean();
                int count = ElarionPacketCodecs.readBoundedCount(buffer, 16);
                Map<String, String> parameters = new LinkedHashMap<>();
                for (int index = 0; index < count; index++) {
                    parameters.put(ElarionPacketCodecs.readString(buffer, 64),
                            ElarionPacketCodecs.readString(buffer, 256));
                }
                return new AdminPanelActionPayload(selectedTab, provider, target, action, parameters, confirmed);
            });

    public AdminPanelActionPayload {
        selectedTabId = clean(selectedTabId);
        providerId = clean(providerId);
        targetId = clean(targetId);
        actionId = clean(actionId);
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
