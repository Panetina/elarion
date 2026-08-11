package panetina.elarion.addons.guilds.model;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void contributionAggregateIsGuildOwnedAndAttributedToTheMember() {
        UUID leader = UUID.randomUUID();
        GuildRecord guild = GuildRecord.create("merc", "Mercury Guild", "MERC", leader);

        GuildRecord updated = guild.withContribution(UUID.randomUUID(), leader, 75L)
                .withContribution(UUID.randomUUID(), leader, 25L);

        assertEquals(100L, updated.progression().totalContributed());
        assertEquals(100L, updated.progression().memberContributions().get(leader));
    }

    @Test
    void contributionOperationIsReplaySafeAndCannotChangeItsAmount() {
        UUID leader = UUID.randomUUID();
        UUID operation = UUID.randomUUID();
        GuildRecord guild = GuildRecord.create("merc", "Mercury Guild", "MERC", leader)
                .withContribution(operation, leader, 50L);

        GuildRecord replay = guild.withContribution(operation, leader, 50L);

        assertEquals(50L, replay.progression().totalContributed());
        assertThrows(IllegalArgumentException.class, () -> guild.withContribution(operation, leader, 51L));
    }

    @Test
    void secrecyMutationChangesOnlyTheSecrecyFlag() {
        UUID leader = UUID.randomUUID();
        GuildRecord guild = GuildRecord.create("merc", "Mercury Guild", "MERC", leader);

        GuildRecord secret = guild.withSecret(true);

        assertTrue(secret.secret());
        assertEquals(guild.leaderId(), secret.leaderId());
    }

    @Test
    void migratesLegacy32By32EmblemsToARevisioned16By16Projection() {
        UUID leader = UUID.randomUUID();
        byte[] legacy = new byte[1024];
        legacy[0] = 7;
        legacy[1] = 7;
        legacy[32] = 7;
        GuildRecord guild = new GuildRecord("merc", "Mercury Guild", "MERC", false, false, leader,
                Set.of(leader), null, null, null, null, null, 4L, legacy, 0L, 100L);

        assertEquals(256, guild.iconPaletteIndices().length);
        assertEquals(7, Byte.toUnsignedInt(guild.iconPaletteIndices()[0]));
        assertEquals(5L, guild.iconRevision());
    }
}
