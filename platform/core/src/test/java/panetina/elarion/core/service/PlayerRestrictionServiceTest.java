package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerRestrictionServiceTest {
    @Test
    void uuidAdmissionProvidersWorkWithoutAPlayerEntity() {
        PlayerRestrictionService restrictions = new PlayerRestrictionService();
        UUID banished = UUID.randomUUID();
        restrictions.registerAccountProvider((playerId, action) ->
                banished.equals(playerId) && PlayerRestrictionService.QUEUED_ADMISSION.equals(action)
                        ? Optional.of(new PlayerRestrictionService.PlayerRestriction("test", "queued denial"))
                        : Optional.empty());

        assertEquals("queued denial", restrictions.restriction(
                banished, PlayerRestrictionService.QUEUED_ADMISSION).orElseThrow().message());
        assertTrue(restrictions.restriction(UUID.randomUUID(),
                PlayerRestrictionService.QUEUED_ADMISSION).isEmpty());
        assertTrue(restrictions.restriction(banished, PlayerRestrictionService.CHAT).isEmpty());
    }
}
