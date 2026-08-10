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
}
