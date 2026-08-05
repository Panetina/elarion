package panetina.elarion.addons.guilds.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.guilds.model.GuildRecord;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GuildWebProjectionPublisherTest {
    @Test
    void publishesCurrentLeaderAsLoreAuthority() {
        UUID leader = UUID.randomUUID();
        GuildRecord guild = GuildRecord.create("ember-court", "The Ember Court", "EMBER", leader);

        var payload = GuildWebProjectionPublisher.authorityPayload(guild, true);

        assertEquals("true", payload.get("active"));
        assertEquals("ember-court", payload.get("resourceId"));
        assertEquals(leader.toString(), payload.get("ownerUuid"));
        assertEquals("leader", payload.get("authorityRole"));
    }

    @Test
    void tombstonesDeletedGuildAuthority() {
        GuildRecord guild = GuildRecord.create(
                "ember-court", "The Ember Court", "EMBER", UUID.randomUUID());

        assertEquals("false", GuildWebProjectionPublisher.authorityPayload(guild, false).get("active"));
    }

    @Test
    void secretGuildAuthorityPayloadCanBeTombstoned() {
        GuildRecord guild = GuildRecord.create("hidden", "Hidden", "HIDE", true, UUID.randomUUID());
        assertEquals("false", GuildWebProjectionPublisher.authorityPayload(guild, false).get("active"));
    }

    @Test
    void publishesRecipientScopedMembershipWithResolvedRole() {
        UUID leader = UUID.randomUUID();
        GuildRecord guild = GuildRecord.create("ember-court", "The Ember Court", "EMBER", leader);

        var payload = GuildWebProjectionPublisher.membershipPayload(guild, leader, true);

        assertEquals("true", payload.get("active"));
        assertEquals("The Ember Court", payload.get("displayName"));
        assertEquals("Leader", payload.get("memberRole"));
    }
}
