package panetina.elarion.addons.groups.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.groups.model.GroupRecord;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GroupProfileContributorTest {
    @Test
    void contributesIndexedPublicMembership() {
        UUID player = UUID.randomUUID();
        GroupRecord group = GroupRecord.create("seekers", "The Seekers", "SEEK", player);
        GroupProfileContributor contributor = new GroupProfileContributor(ignored -> Optional.of(group));

        var section = contributor.sections(CitizenProfileRequestContext.publicView(UUID.randomUUID(), player),
                new CitizenRecord(player, "Ember")).getFirst();

        assertEquals("The Seekers - Leader", section.fields().getFirst().value());
        assertEquals("PUBLIC", section.visibility().name());
    }
}
