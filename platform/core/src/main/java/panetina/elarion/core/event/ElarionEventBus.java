package panetina.elarion.core.event;

import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ElarionEventBus {
    public record CitizenChanged(UUID citizenId, CitizenRecord citizen, String reason) {}
    public record ProgressionEvent(String eventId, UUID actorId, String subjectId) {}

    private final List<Consumer<CitizenChanged>> citizenListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<ProgressionEvent>> progressionListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<CatchTelemetryEvent>> catchTelemetryListeners = new CopyOnWriteArrayList<>();

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

    public void emitCitizenChanged(CitizenChanged event) {
        citizenListeners.forEach(listener -> listener.accept(event));
    }

    public void emitProgression(ProgressionEvent event) {
        progressionListeners.forEach(listener -> listener.accept(event));
    }

    public void emitCatchTelemetry(CatchTelemetryEvent event) {
        catchTelemetryListeners.forEach(listener -> listener.accept(event));
    }
}
