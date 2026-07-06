package panetina.elarion.addons.offerings;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.offerings.model.OfferingInstance;
import panetina.elarion.addons.offerings.model.OfferingPresentation;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingProjectLevel;
import panetina.elarion.addons.offerings.model.OfferingScope;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionOfferingsAddonTest {
    @Test
    void shrineTitleUsesOverrideBeforeFoundationLevelText() {
        OfferingProjectDefinition project = new OfferingProjectDefinition(
                "council_hall", "Council Hall", "", true, OfferingScope.REALM, false, true,
                List.of(), List.of(), OfferingPresentation.defaults(),
                List.of(new OfferingProjectLevel("foundation_i", "Council Hall", "",
                        List.of(), List.of(), new OfferingPresentation("Foundation I", ""))));
        OfferingInstance instance = base().withDisplayNameOverride("Sorina's Stone");

        assertEquals("Sorina's Stone",
                ElarionOfferingsAddon.shrineTitle(instance, project, project.firstLevel()));
    }

    @Test
    void shrineTitleFallsBackToFoundationLevelTextAfterResetClearsOverride() {
        OfferingProjectDefinition project = new OfferingProjectDefinition(
                "council_hall", "Council Hall", "", true, OfferingScope.REALM, false, true,
                List.of(), List.of(), OfferingPresentation.defaults(),
                List.of(new OfferingProjectLevel("foundation_i", "Council Hall", "",
                        List.of(), List.of(), new OfferingPresentation("Foundation I", ""))));
        OfferingInstance instance = base().withDisplayNameOverride("Sorina's Stone").reset("foundation_i");

        assertEquals("Foundation I",
                ElarionOfferingsAddon.shrineTitle(instance, project, project.firstLevel()));
    }

    private static OfferingInstance base() {
        return new OfferingInstance("instance_1", "council_hall", OfferingScope.REALM, "realm1",
                "", 0, 0, 0, "", Map.of(), Map.of(), Set.of(), System.currentTimeMillis(), 0L);
    }
}
