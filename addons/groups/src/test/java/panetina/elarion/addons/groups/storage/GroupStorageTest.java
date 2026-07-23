package panetina.elarion.addons.groups.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.groups.model.GroupInvite;
import panetina.elarion.addons.groups.model.GroupRecord;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupStorageTest {
    @TempDir
    Path root;

    @Test
    void stateRoundTrips() {
        UUID leader = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        UUID invited = UUID.randomUUID();

        GroupState state = new GroupState();
        state.groups.put("merc", new GroupRecord(
                "merc",
                "Mercury Guild",
                "MERC",
                false,
                leader,
                Set.of(leader, member),
                1234L));
        state.playerGroups.put(leader, "merc");
        state.playerGroups.put(member, "merc");
        GroupInvite invite = new GroupInvite("merc", invited, leader, 5678L);
        state.invites.put(invite.key(), invite);

        GroupStorage storage = new GroupStorage(LoggerFactory.getLogger("groups-test"), root);
        storage.save(root, state);
        GroupState loaded = storage.load(root);

        GroupRecord loadedGroup = loaded.groups.get("merc");
        assertEquals("MERC", loadedGroup.tag());
        assertEquals(Set.of(leader, member), loadedGroup.members());
        assertEquals("merc", loaded.playerGroups.get(leader));
        assertEquals(invited, loaded.invites.get(invite.key()).invitedPlayer());
    }
}
