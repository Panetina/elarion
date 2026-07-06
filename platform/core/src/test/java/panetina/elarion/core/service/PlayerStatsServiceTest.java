package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.storage.PlayerStatsStorage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class PlayerStatsServiceTest {
    @Test
    void saveDirtyBeforeBindIsNoop() {
        PlayerStatsService service = new PlayerStatsService(
                new PlayerStatsStorage(LoggerFactory.getLogger("test")), null);

        assertDoesNotThrow(service::saveDirty);
    }
}
