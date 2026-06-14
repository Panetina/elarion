package panetina.elarion.addons.groups.model;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupRecordTest {
    @Test
    void createAddsLeaderAsMember() {
        UUID leader = UUID.randomUUID();

        GroupRecord group = GroupRecord.create("merc", "Mercury Guild", "MERC", leader);

        assertEquals(leader, group.leaderId());
        assertEquals(Set.of(leader), group.members());
        assertTrue(group.createdAt() > 0);
    }

    @Test
    void withLeaderAddsNewLeaderToMembers() {
        UUID oldLeader = UUID.randomUUID();
        UUID newLeader = UUID.randomUUID();
        GroupRecord group = GroupRecord.create("merc", "Mercury Guild", "MERC", oldLeader);

        GroupRecord updated = group.withLeader(newLeader);

        assertEquals(newLeader, updated.leaderId());
        assertTrue(updated.members().contains(oldLeader));
        assertTrue(updated.members().contains(newLeader));
    }
}
