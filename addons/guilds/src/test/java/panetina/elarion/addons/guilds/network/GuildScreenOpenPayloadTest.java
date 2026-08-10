package panetina.elarion.addons.guilds.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GuildScreenOpenPayloadTest {
    @Test void roundTripsMembersRolesAndAnnouncementsInTheirWireOrder() {
        UUID leader = UUID.randomUUID();
        GuildScreenOpenPayload payload = new GuildScreenOpenPayload("ember", "Ember Court", "EMBER", false,
                leader, 4L, new byte[0], List.of("INVITE", "EDIT_EMBLEM"),
                List.of(new GuildScreenOpenPayload.Member(leader, "Aster", "owner", 5L)),
                List.of(new GuildScreenOpenPayload.Role("scribe", "Scribe", 5, List.of("PUBLISH_ANNOUNCEMENTS"))),
                List.of(new GuildScreenOpenPayload.Announcement("a", "Aster", "Welcome", 5L)),
                List.of(new GuildScreenOpenPayload.InviteCandidate(UUID.randomUUID(), "Rowan")));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        GuildScreenOpenPayload.CODEC.encode(buffer, payload);
        GuildScreenOpenPayload decoded = GuildScreenOpenPayload.CODEC.decode(buffer);

        assertEquals(payload.members(), decoded.members());
        assertEquals(payload.roles(), decoded.roles());
        assertEquals(payload.announcements(), decoded.announcements());
        assertEquals(payload.viewerPermissions(), decoded.viewerPermissions());
        assertEquals(payload.inviteCandidates(), decoded.inviteCandidates());
    }
}
