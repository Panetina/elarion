package panetina.elarion.addons.portals.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.RealmPresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PortalRealmTextTest {
    @Test
    void resolvesRuntimeRealmPresentationPlaceholders() {
        RealmPresentation realm = new RealmPresentation(
                "Oak", "Kingdom of Oak", "OAK", "[OAK]", "gold");

        assertEquals(
                "Oak / Kingdom of Oak / OAK",
                PortalRealmText.format(
                        "%realm_display% / %realm_official% / %realm_tag%",
                        realm));
    }
}
