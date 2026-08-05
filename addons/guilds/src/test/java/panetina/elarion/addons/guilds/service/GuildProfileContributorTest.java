package panetina.elarion.addons.guilds.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.guilds.model.GuildRecord;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GuildProfileContributorTest {
    @Test
    void contributesIndexedPublicMembership() {
        UUID player = UUID.randomUUID();
        GuildRecord guild = GuildRecord.create("seekers", "The Seekers", "SEEK", player);
        GuildProfileContributor contributor = new GuildProfileContributor(ignored -> Optional.of(guild));

        var section = contributor.sections(CitizenProfileRequestContext.publicView(UUID.randomUUID(), player),
                new CitizenRecord(player, "Ember")).getFirst();

        assertEquals("The Seekers - Leader", section.fields().getFirst().value());
        assertEquals("PUBLIC", section.visibility().name());
    }
}
