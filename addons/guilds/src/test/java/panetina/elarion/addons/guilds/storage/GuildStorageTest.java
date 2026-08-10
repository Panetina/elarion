package panetina.elarion.addons.guilds.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.guilds.model.GuildInvite;
import panetina.elarion.addons.guilds.model.GuildRecord;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuildStorageTest {
    @TempDir
    Path root;

    @Test
    void stateRoundTrips() {
        UUID leader = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        UUID invited = UUID.randomUUID();

        GuildState state = new GuildState();
        state.guilds.put("merc", new GuildRecord(
                "merc",
                "Mercury Guild",
                "MERC",
                false,
                leader,
                Set.of(leader, member),
                1234L));
        state.playerGuilds.put(leader, "merc");
        state.playerGuilds.put(member, "merc");
        GuildInvite invite = new GuildInvite("merc", invited, leader, 5678L);
        state.invites.put(invite.key(), invite);

        GuildStorage storage = new GuildStorage(LoggerFactory.getLogger("guilds-test"), root);
        storage.save(root, state);
        GuildState loaded = storage.load(root);

        GuildRecord loadedGuild = loaded.guilds.get("merc");
        assertEquals("MERC", loadedGuild.tag());
        assertEquals(Set.of(leader, member), loadedGuild.members());
        assertEquals(1234L, loadedGuild.memberJoinedAt().get(leader));
        assertEquals(1234L, loadedGuild.memberJoinedAt().get(member));
        assertEquals(1, loadedGuild.roles().get("owner").position());
        assertEquals("merc", loaded.playerGuilds.get(leader));
        assertEquals(invited, loaded.invites.get(invite.key()).invitedPlayer());
    }

    @Test
    void migratesLegacyGroupsStateWithoutLosingMembershipsOrInvites() throws Exception {
        Path guildRoot = root.resolve("guilds");
        Path legacyRoot = root.resolve("groups");
        Files.createDirectories(legacyRoot);
        UUID leader = UUID.randomUUID();
        UUID invited = UUID.randomUUID();
        Files.writeString(legacyRoot.resolve("groups.json"), """
                {"groups":{"merc":{"id":"merc","displayName":"Mercury Guild","tag":"MERC","tagHidden":false,"leaderId":"%s","members":["%s"],"createdAt":1234}},"playerGroups":{"%s":"merc"},"invites":{"merc:%s":{"groupId":"merc","invitedPlayer":"%s","invitedBy":"%s","createdAt":5678}}}
                """.formatted(leader, leader, leader, invited, invited, leader));

        GuildStorage storage = new GuildStorage(LoggerFactory.getLogger("guilds-test"), guildRoot);
        GuildState loaded = storage.load(guildRoot);

        assertEquals("Mercury Guild", loaded.guilds.get("merc").displayName());
        assertEquals("merc", loaded.playerGuilds.get(leader));
        assertEquals("merc", loaded.invites.get("merc:" + invited).guildId());
        assertEquals(1234L, loaded.guilds.get("merc").memberJoinedAt().get(leader));
        assertEquals(1, loaded.guilds.get("merc").roles().get("owner").position());
        assertTrue(Files.exists(guildRoot.resolve("guilds.json")));
        assertFalse(Files.exists(legacyRoot.resolve("groups.json")));
        assertTrue(Files.exists(legacyRoot.resolve("groups.json.migrated-v1.bak")));
    }
}
