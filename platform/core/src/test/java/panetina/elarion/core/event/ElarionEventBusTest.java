package panetina.elarion.core.event;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CatchTelemetryEvent;
import panetina.elarion.core.model.ElarionDomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

final class ElarionEventBusTest {
    @Test
    void catchTelemetryListenersReceiveEventsAndCanUnsubscribe() throws Exception {
        ElarionEventBus bus = new ElarionEventBus();
        List<CatchTelemetryEvent> received = new ArrayList<>();
        AutoCloseable subscription = bus.onCatchTelemetry(received::add);
        CatchTelemetryEvent event = catchEvent();

        bus.emitCatchTelemetry(event);
        subscription.close();
        bus.emitCatchTelemetry(catchEvent());

        assertEquals(List.of(event), received);
    }

    @Test
    void domainListenersReceiveEventsAndCanUnsubscribe() throws Exception {
        ElarionEventBus bus = new ElarionEventBus();
        AtomicReference<ElarionDomainEvent> received = new AtomicReference<>();
        AutoCloseable subscription = bus.onDomainEvent(received::set);

        bus.emitDomainEvent(ElarionDomainEvent.of(
                "elarion_test", "feature-completed", null, "realm1",
                "feature", "test", Map.of("result", "success")));

        assertNotNull(received.get());
        assertEquals("elarion_test", received.get().sourceSystem());
        assertEquals("feature-completed", received.get().eventType());
        assertEquals("realm1", received.get().realmId());

        subscription.close();
        received.set(null);
        bus.emitDomainEvent(ElarionDomainEvent.of(
                "elarion_test", "ignored", null, "", "", "", Map.of()));
        assertNull(received.get());
    }

    private static CatchTelemetryEvent catchEvent() {
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
