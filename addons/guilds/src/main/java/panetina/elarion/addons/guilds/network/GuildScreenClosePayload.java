package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Closes a stale Guild surface after the viewer no longer has membership access. */
public record GuildScreenClosePayload() implements CustomPayload {
    public static final GuildScreenClosePayload INSTANCE = new GuildScreenClosePayload();
    public static final Id<GuildScreenClosePayload> ID =
            new Id<>(Identifier.of("elarion_guilds", "screen_close"));
    public static final PacketCodec<PacketByteBuf, GuildScreenClosePayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
