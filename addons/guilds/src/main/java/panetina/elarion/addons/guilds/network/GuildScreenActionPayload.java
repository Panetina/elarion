package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.UUID;

/** A bounded client request. The server derives the guild and validates every action. */
public record GuildScreenActionPayload(String action, UUID target, String value, byte[] iconPixels)
        implements CustomPayload {
    public static final Id<GuildScreenActionPayload> ID =
            new Id<>(Identifier.of("elarion_guilds", "screen_action"));
    public static final PacketCodec<PacketByteBuf, GuildScreenActionPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.action, 48);
                buffer.writeBoolean(payload.target != null);
                if (payload.target != null) buffer.writeUuid(payload.target);
                ElarionPacketCodecs.writeString(buffer, payload.value, 512);
                buffer.writeVarInt(payload.iconPixels.length);
                buffer.writeBytes(payload.iconPixels);
            }, buffer -> {
                String action = ElarionPacketCodecs.readString(buffer, 48);
                UUID target = buffer.readBoolean() ? buffer.readUuid() : null;
                String value = ElarionPacketCodecs.readString(buffer, 512);
                int length = ElarionPacketCodecs.readBoundedCount(buffer, 256);
                byte[] pixels = new byte[length];
                buffer.readBytes(pixels);
                return new GuildScreenActionPayload(action, target, value, pixels);
            });

    public GuildScreenActionPayload {
        action = action == null ? "" : action;
        value = value == null ? "" : value;
        iconPixels = iconPixels == null ? new byte[0] : iconPixels.clone();
        if (iconPixels.length != 0 && iconPixels.length != 256) {
            throw new IllegalArgumentException("Guild icon payload must be blank or exactly 16x16 pixels.");
        }
    }

    @Override public byte[] iconPixels() { return iconPixels.clone(); }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
