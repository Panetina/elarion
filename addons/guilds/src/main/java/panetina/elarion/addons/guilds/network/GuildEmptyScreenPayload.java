package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server-authorized empty Guild menu response for a player without membership. */
public record GuildEmptyScreenPayload() implements CustomPayload {
    public static final GuildEmptyScreenPayload INSTANCE = new GuildEmptyScreenPayload();
    public static final Id<GuildEmptyScreenPayload> ID = new Id<>(Identifier.of("elarion_guilds", "screen_empty"));
    public static final PacketCodec<PacketByteBuf, GuildEmptyScreenPayload> CODEC = PacketCodec.unit(INSTANCE);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
