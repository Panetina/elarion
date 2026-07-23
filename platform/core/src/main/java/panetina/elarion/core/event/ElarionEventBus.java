package panetina.elarion.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.CatchTelemetryEvent;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.metric.MetricUpdatedEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ElarionEventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_core_events");

    public record CitizenChanged(UUID citizenId, CitizenRecord citizen, String reason) {}
    public record ProgressionEvent(String eventId, UUID actorId, String subjectId) {}

    private final List<Consumer<CitizenChanged>> citizenListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<ProgressionEvent>> progressionListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<CatchTelemetryEvent>> catchTelemetryListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<ElarionDomainEvent>> domainListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<MetricUpdatedEvent>> metricListeners = new CopyOnWriteArrayList<>();

    public AutoCloseable onCitizenChanged(Consumer<CitizenChanged> listener) {
        citizenListeners.add(listener);
        return () -> citizenListeners.remove(listener);
    }

    public AutoCloseable onProgression(Consumer<ProgressionEvent> listener) {
        progressionListeners.add(listener);
        return () -> progressionListeners.remove(listener);
    }

    public AutoCloseable onCatchTelemetry(Consumer<CatchTelemetryEvent> listener) {
        catchTelemetryListeners.add(listener);
        return () -> catchTelemetryListeners.remove(listener);
    }

    public AutoCloseable onDomainEvent(Consumer<ElarionDomainEvent> listener) {
        domainListeners.add(listener);
        return () -> domainListeners.remove(listener);
    }

    public AutoCloseable onMetricUpdated(Consumer<MetricUpdatedEvent> listener) {
        metricListeners.add(listener);
        return () -> metricListeners.remove(listener);
    }

    public void emitCitizenChanged(CitizenChanged event) {
        dispatch(citizenListeners, event, "citizen");
    }

    public void emitProgression(ProgressionEvent event) {
        dispatch(progressionListeners, event, "progression");
    }

    public void emitCatchTelemetry(CatchTelemetryEvent event) {
        dispatch(catchTelemetryListeners, event, "catch-telemetry");
    }

    public void emitDomainEvent(ElarionDomainEvent event) {
        if (event == null) return;
        dispatch(domainListeners, event, event.sourceSystem() + ":" + event.eventType());
    }

    public void emitMetricUpdated(MetricUpdatedEvent event) {
        dispatch(metricListeners, event, "metric-updated");
    }

    private static <T> void dispatch(List<Consumer<T>> listeners, T event, String eventType) {
        for (Consumer<T> listener : listeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException exception) {
                LOGGER.error("Elarion event listener failed for {}", eventType, exception);
            }
        }
    }
}
