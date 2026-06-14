package panetina.elarion.core.event;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionEventBusTest {
    @Test
    void catchTelemetryListenersReceiveEventsAndCanUnsubscribe() throws Exception {
        ElarionEventBus bus = new ElarionEventBus();
        List<CatchTelemetryEvent> received = new ArrayList<>();
        AutoCloseable subscription = bus.onCatchTelemetry(received::add);
        CatchTelemetryEvent event = event();

        bus.emitCatchTelemetry(event);
        subscription.close();
        bus.emitCatchTelemetry(event());

        assertEquals(List.of(event), received);
    }

    private static CatchTelemetryEvent event() {
        return new CatchTelemetryEvent(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                1,
                null,
                null,
                null,
                Map.of());
    }
}
