package panetina.elarion.core.client;

import panetina.elarion.core.model.profile.CitizenProfileSnapshot;

import java.util.Optional;

public final class CitizenProfileClientState {
    private static CitizenProfileSnapshot latest;

    private CitizenProfileClientState() {
    }

    public static synchronized void update(CitizenProfileSnapshot snapshot) {
        latest = snapshot;
    }

    public static synchronized Optional<CitizenProfileSnapshot> latest() {
        return Optional.ofNullable(latest);
    }

    public static synchronized void clear() {
        latest = null;
    }
}
