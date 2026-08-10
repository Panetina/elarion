package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

/** Result text for an already-open Guild-owned screen. */
public record GuildUiFeedbackPayload(boolean successful, String message) implements CustomPayload {
    public static final Id<GuildUiFeedbackPayload> ID =
            new Id<>(Identifier.of("elarion_guilds", "ui_feedback"));
    public static final PacketCodec<PacketByteBuf, GuildUiFeedbackPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeBoolean(payload.successful);
                ElarionPacketCodecs.writeString(buffer, payload.message, 256);
            },
            buffer -> new GuildUiFeedbackPayload(
                    buffer.readBoolean(), ElarionPacketCodecs.readString(buffer, 256)));

    public GuildUiFeedbackPayload {
        message = message == null ? "" : message;
    }

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
