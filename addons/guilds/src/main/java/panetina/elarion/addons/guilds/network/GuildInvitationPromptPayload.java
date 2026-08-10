package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

/** A bounded, server-authored invitation prompt. It conveys no authority. */
public record GuildInvitationPromptPayload(String guildId, String guildName, String guildTag, String inviterName)
        implements CustomPayload {
    public static final Id<GuildInvitationPromptPayload> ID =
            new Id<>(Identifier.of("elarion_guilds", "invitation_prompt"));
    public static final PacketCodec<PacketByteBuf, GuildInvitationPromptPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.guildId, 64);
                ElarionPacketCodecs.writeString(buffer, payload.guildName, 128);
                ElarionPacketCodecs.writeString(buffer, payload.guildTag, 16);
                ElarionPacketCodecs.writeString(buffer, payload.inviterName, 64);
            }, buffer -> new GuildInvitationPromptPayload(
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 16),
                    ElarionPacketCodecs.readString(buffer, 64)));

    public GuildInvitationPromptPayload {
        guildId = guildId == null ? "" : guildId;
        guildName = guildName == null ? "" : guildName;
        guildTag = guildTag == null ? "" : guildTag;
        inviterName = inviterName == null ? "" : inviterName;
        if (guildId.isBlank() || guildName.isBlank()) {
            throw new IllegalArgumentException("Guild invitation prompt requires a guild identity.");
        }
    }

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
