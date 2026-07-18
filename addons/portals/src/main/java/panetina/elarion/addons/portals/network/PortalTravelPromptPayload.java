package panetina.elarion.addons.portals.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.portals.model.PortalTravelDirection;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.Locale;

public record PortalTravelPromptPayload(
        String routeId,
        String gateName,
        PortalTravelDirection direction,
        long closesAt,
        String iconItem,
        String costKind,
        String requirement,
        int requirementColor,
        boolean allowed,
        String message,
        String themeVariant,
        int logicalWidth,
        int logicalHeight,
        int minimumScalePercent,
        int confirmButtonWidth,
        int closeButtonWidth
) implements CustomPayload {
    public static final String COST_FREE = "free";
    public static final String COST_TICKET = "ticket";
    public static final String COST_FEE = "fee";

    public static final Id<PortalTravelPromptPayload> ID =
            new Id<>(Identifier.of("elarion_portals", "travel_prompt"));
    public static final PacketCodec<PacketByteBuf, PortalTravelPromptPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.routeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.gateName(), 256);
                buffer.writeEnumConstant(payload.direction());
                buffer.writeLong(payload.closesAt());
                ElarionPacketCodecs.writeString(buffer, payload.iconItem(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.costKind(), 32);
                ElarionPacketCodecs.writeString(buffer, payload.requirement(), 1024);
                buffer.writeInt(payload.requirementColor());
                buffer.writeBoolean(payload.allowed());
                ElarionPacketCodecs.writeString(buffer, payload.message(), 1024);
                ElarionPacketCodecs.writeString(buffer, payload.themeVariant(), 64);
                buffer.writeVarInt(payload.logicalWidth());
                buffer.writeVarInt(payload.logicalHeight());
                buffer.writeVarInt(payload.minimumScalePercent());
                buffer.writeVarInt(payload.confirmButtonWidth());
                buffer.writeVarInt(payload.closeButtonWidth());
            },
            buffer -> new PortalTravelPromptPayload(
                    ElarionPacketCodecs.readString(buffer, 128), ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readEnumOrDefault(buffer, PortalTravelDirection.class,
                            PortalTravelDirection.OUTBOUND), buffer.readLong(),
                    ElarionPacketCodecs.readString(buffer, 256), ElarionPacketCodecs.readString(buffer, 32),
                    ElarionPacketCodecs.readString(buffer, 1024), buffer.readInt(),
                    buffer.readBoolean(), ElarionPacketCodecs.readString(buffer, 1024),
                    ElarionPacketCodecs.readString(buffer, 64), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt()));

    public PortalTravelPromptPayload {
        costKind = normalizeCostKind(costKind);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static String normalizeCostKind(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case COST_TICKET -> COST_TICKET;
            case COST_FEE -> COST_FEE;
            default -> COST_FREE;
        };
    }
}
