package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentOfficeDefinition;
import panetina.elarion.addons.government.model.RealmGovernmentState;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentProfileContributorTest {
    @Test
    void roleLabelsUseActiveOfficeDisplayNames() {
        UUID citizen = UUID.randomUUID();
        RealmGovernmentState state = RealmGovernmentState.empty("realm1")
                .withOfficeHolder("monarch", citizen)
                .withOfficeHolder("officer", citizen);
        GovernmentFormDefinition form = new GovernmentFormDefinition(
                "monarchy",
                "Monarchy",
                "Crown authority.",
                true,
                "Kingdom of %realm%",
                List.of("monarch"),
                List.of(
                        new GovernmentOfficeDefinition("monarch", "Monarch", "Realm ruler.", 1),
                        new GovernmentOfficeDefinition("officer", "Officer", "Appointed office.", 3)),
                Map.of(),
                Map.of());

        assertEquals(List.of("Monarch", "Officer"),
                GovernmentProfileContributor.roleLabels(state, form, citizen));
    }

    @Test
    void roleLabelsFallBackToEmptyWhenNoOfficeIsHeld() {
        RealmGovernmentState state = RealmGovernmentState.empty("realm1");
        GovernmentFormDefinition form = new GovernmentFormDefinition(
                "republic",
                "Republic",
                "Ember rule.",
                true,
                "Republic of %realm%",
                List.of("president"),
                List.of(new GovernmentOfficeDefinition("president", "President", "Head of state.", 1)),
                Map.of(),
                Map.of());

        assertTrue(GovernmentProfileContributor.roleLabels(state, form, UUID.randomUUID()).isEmpty());
    }
}
