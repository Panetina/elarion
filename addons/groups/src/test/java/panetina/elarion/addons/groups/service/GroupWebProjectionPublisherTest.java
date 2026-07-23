package panetina.elarion.addons.groups.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.groups.model.GroupRecord;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GroupWebProjectionPublisherTest {
    @Test
    void publishesCurrentLeaderAsLoreAuthority() {
        UUID leader = UUID.randomUUID();
        GroupRecord group = GroupRecord.create("ember-court", "The Ember Court", "EMBER", leader);

        var payload = GroupWebProjectionPublisher.authorityPayload(group, true);

        assertEquals("true", payload.get("active"));
        assertEquals("ember-court", payload.get("resourceId"));
        assertEquals(leader.toString(), payload.get("ownerUuid"));
        assertEquals("leader", payload.get("authorityRole"));
    }

    @Test
    void tombstonesDeletedGroupAuthority() {
        GroupRecord group = GroupRecord.create(
                "ember-court", "The Ember Court", "EMBER", UUID.randomUUID());

        assertEquals("false", GroupWebProjectionPublisher.authorityPayload(group, false).get("active"));
    }

    @Test
    void publishesRecipientScopedMembershipWithResolvedRole() {
        UUID leader = UUID.randomUUID();
        GroupRecord group = GroupRecord.create("ember-court", "The Ember Court", "EMBER", leader);

        var payload = GroupWebProjectionPublisher.membershipPayload(group, leader, true);

        assertEquals("true", payload.get("active"));
        assertEquals("The Ember Court", payload.get("displayName"));
        assertEquals("Leader", payload.get("memberRole"));
    }
}
