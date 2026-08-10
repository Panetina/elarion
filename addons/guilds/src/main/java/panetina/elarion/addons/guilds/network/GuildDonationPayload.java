package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/** A retry-safe, bounded request to donate carried Sigils to the viewer's Guild. */
public record GuildDonationPayload(UUID operationId, long amount) implements CustomPayload {
    public static final Id<GuildDonationPayload> ID = new Id<>(Identifier.of("elarion_guilds", "donate"));
    public static final PacketCodec<PacketByteBuf, GuildDonationPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> { buffer.writeUuid(payload.operationId); buffer.writeLong(payload.amount); },
            buffer -> new GuildDonationPayload(buffer.readUuid(), buffer.readLong()));
    public GuildDonationPayload {
        if (operationId == null || amount < 1L || amount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Guild donation request is invalid.");
        }
    }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
