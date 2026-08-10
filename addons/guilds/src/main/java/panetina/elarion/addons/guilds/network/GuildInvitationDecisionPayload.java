package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

/** Client intent only; the server rechecks the pending invite before mutation. */
public record GuildInvitationDecisionPayload(String guildId, boolean accepted) implements CustomPayload {
    public static final Id<GuildInvitationDecisionPayload> ID =
            new Id<>(Identifier.of("elarion_guilds", "invitation_decision"));
    public static final PacketCodec<PacketByteBuf, GuildInvitationDecisionPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.guildId, 64);
                buffer.writeBoolean(payload.accepted);
            }, buffer -> new GuildInvitationDecisionPayload(
                    ElarionPacketCodecs.readString(buffer, 64), buffer.readBoolean()));

    public GuildInvitationDecisionPayload {
        guildId = guildId == null ? "" : guildId;
        if (guildId.isBlank()) throw new IllegalArgumentException("Guild invitation decision requires a guild.");
    }

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
