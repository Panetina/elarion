package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HistoryEventTest {
    @Test
    void chronicleFactoryPersistsIntentAndCannotBeOverriddenByCallerMetadata() {
        HistoryEvent event = HistoryEvent.createChronicle("realm", "leader-set", null,
                "realm", "ash", "ash", Map.of(HistoryEvent.CHRONICLE_INTENT_METADATA_KEY, "false"), "A leader rose.");

        assertTrue(event.isChronicleIntentional());
        assertEquals("true", event.metadata().get(HistoryEvent.CHRONICLE_INTENT_METADATA_KEY));
    }

    @Test
    void ordinaryAuditEventIsNotChronicleIntentional() {
        assertFalse(HistoryEvent.create("realm", "leader-set", null,
                "realm", "ash", "ash", Map.of()).isChronicleIntentional());
    }
}
