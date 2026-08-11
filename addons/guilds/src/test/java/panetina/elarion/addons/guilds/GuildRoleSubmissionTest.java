package panetina.elarion.addons.guilds;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.guilds.model.GuildPermission;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class GuildRoleSubmissionTest {
    @Test
    void keepsTheTrailingEmptyPermissionsField() {
        ElarionGuildsAddon.RoleSubmission submission = ElarionGuildsAddon.parseRoleSubmission("scribe\nScribe\n");
        assertEquals("scribe", submission.id());
        assertEquals("Scribe", submission.name());
        assertEquals(Set.of(), submission.permissions());
    }

    @Test
    void rejectsMalformedOrUnknownPermissions() {
        assertThrows(IllegalArgumentException.class, () -> ElarionGuildsAddon.parseRoleSubmission("scribe\nScribe"));
        assertThrows(IllegalArgumentException.class, () -> ElarionGuildsAddon.parseRoleSubmission("scribe\nScribe\nNOPE"));
        assertEquals(Set.of(GuildPermission.INVITE),
                ElarionGuildsAddon.parseRoleSubmission("scribe\nScribe\nINVITE").permissions());
    }
}
