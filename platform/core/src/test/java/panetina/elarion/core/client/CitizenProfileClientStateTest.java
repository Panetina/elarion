package panetina.elarion.core.client;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSnapshot;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CitizenProfileClientStateTest {
    @Test
    void storesAndClearsLatestSnapshot() {
        CitizenProfileClientState.clear();
        UUID targetId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CitizenProfileSnapshot snapshot = new CitizenProfileSnapshot(
                targetId,
                "Mara",
                List.of(new CitizenProfileSection(
                        "core.identity",
                        "Identity",
                        "elarion_core",
                        ProfileVisibility.PUBLIC,
                        List.of(new CitizenProfileField("display-name", "Name", "Mara", ProfileVisibility.PUBLIC)))));

        CitizenProfileClientState.update(snapshot);

        assertEquals(snapshot, CitizenProfileClientState.latest().orElseThrow());

        CitizenProfileClientState.clear();

        assertTrue(CitizenProfileClientState.latest().isEmpty());
    }
}
