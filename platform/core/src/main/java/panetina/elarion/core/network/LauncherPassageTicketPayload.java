package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server-issued opaque receipt that lets the launcher read its own website passage snapshot. */
public record LauncherPassageTicketPayload(String uuid, String ticket) implements CustomPayload {
    public static final Id<LauncherPassageTicketPayload> ID =
            new Id<>(Identifier.of("elarion_core", "launcher_passage_ticket"));
    private static final int MAX_TICKET_LENGTH = 1024;

    public static final PacketCodec<PacketByteBuf, LauncherPassageTicketPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.uuid(), 36);
                ElarionPacketCodecs.writeString(buffer, payload.ticket(), MAX_TICKET_LENGTH);
            },
            buffer -> new LauncherPassageTicketPayload(
                    ElarionPacketCodecs.readString(buffer, 36),
                    ElarionPacketCodecs.readString(buffer, MAX_TICKET_LENGTH)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
