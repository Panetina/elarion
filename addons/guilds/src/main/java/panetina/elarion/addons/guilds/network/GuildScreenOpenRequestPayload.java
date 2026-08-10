package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Opens only the requesting player's canonical Guild view. */
public record GuildScreenOpenRequestPayload() implements CustomPayload {
    public static final GuildScreenOpenRequestPayload INSTANCE = new GuildScreenOpenRequestPayload();
    public static final Id<GuildScreenOpenRequestPayload> ID = new Id<>(Identifier.of("elarion_guilds", "screen_open_request"));
    public static final PacketCodec<PacketByteBuf, GuildScreenOpenRequestPayload> CODEC = PacketCodec.unit(INSTANCE);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
