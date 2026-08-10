package panetina.elarion.addons.guilds.model;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuildRecordTest {
    @Test
    void createAddsLeaderAsMember() {
        UUID leader = UUID.randomUUID();

        GuildRecord guild = GuildRecord.create("merc", "Mercury Guild", "MERC", leader);

        assertEquals(leader, guild.leaderId());
        assertEquals(Set.of(leader), guild.members());
        assertEquals(guild.createdAt(), guild.memberJoinedAt().get(leader));
        assertEquals(1, guild.roles().get("owner").position());
        assertEquals(2, guild.roles().get("officer").position());
        assertEquals(3, guild.roles().get("recruiter").position());
        assertTrue(guild.createdAt() > 0);
    }

    @Test
    void withLeaderAddsNewLeaderToMembers() {
        UUID oldLeader = UUID.randomUUID();
        UUID newLeader = UUID.randomUUID();
        GuildRecord guild = GuildRecord.create("merc", "Mercury Guild", "MERC", oldLeader);

        GuildRecord updated = guild.withLeader(newLeader);

        assertEquals(newLeader, updated.leaderId());
        assertTrue(updated.members().contains(oldLeader));
        assertTrue(updated.members().contains(newLeader));
    }

    @Test
    void transferDemotesTheFormerLeaderFromTheUniqueOwnerRole() {
        UUID oldLeader = UUID.randomUUID();
        UUID newLeader = UUID.randomUUID();
        GuildRecord guild = GuildRecord.create("merc", "Mercury Guild", "MERC", oldLeader)
                .withMembers(Set.of(oldLeader, newLeader));

        GuildRecord updated = guild.withLeader(newLeader);

        assertEquals("owner", updated.memberRoles().get(newLeader));
        assertEquals("member", updated.memberRoles().get(oldLeader));
    }

    @Test
    void membershipMetadataSurvivesExistingMembersAndRecordsOnlyNewJoins() {
        UUID leader = UUID.randomUUID();
        UUID existing = UUID.randomUUID();
        UUID joined = UUID.randomUUID();
        GuildRecord guild = GuildRecord.create("merc", "Mercury Guild", "MERC", leader)
                .withMembers(Set.of(leader, existing), 100L);

        GuildRecord updated = guild.withMembers(Set.of(leader, existing, joined), 200L);

        assertEquals(100L, updated.memberJoinedAt().get(existing));
        assertEquals(200L, updated.memberJoinedAt().get(joined));
    }
}
