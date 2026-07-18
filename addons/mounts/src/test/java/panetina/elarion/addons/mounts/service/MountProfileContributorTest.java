package panetina.elarion.addons.mounts.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MountProfileContributorTest {
    @Test
    void contributesOwnerMaintainedUnlockCount() {
        UUID player = UUID.randomUUID();
        MountProfileContributor contributor = new MountProfileContributor(ignored -> 4);

        var section = contributor.sections(CitizenProfileRequestContext.publicView(UUID.randomUUID(), player),
                new CitizenRecord(player, "Ember")).getFirst();

        assertEquals("4", section.fields().getFirst().value());
        assertEquals("mounts", contributor.id());
    }
}
