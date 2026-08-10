package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

/** Bounded registrar request; fields are never packed into an action string. */
public record GuildRegistrarSubmitPayload(String name, String tag, boolean secret) implements CustomPayload {
    public static final Id<GuildRegistrarSubmitPayload> ID =
            new Id<>(Identifier.of("elarion_guilds", "registrar_submit"));
    public static final PacketCodec<PacketByteBuf, GuildRegistrarSubmitPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.name, 128);
                ElarionPacketCodecs.writeString(buffer, payload.tag, 16);
                buffer.writeBoolean(payload.secret);
            }, buffer -> new GuildRegistrarSubmitPayload(
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 16), buffer.readBoolean()));

    public GuildRegistrarSubmitPayload {
        name = name == null ? "" : name;
        tag = tag == null ? "" : tag;
    }

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
