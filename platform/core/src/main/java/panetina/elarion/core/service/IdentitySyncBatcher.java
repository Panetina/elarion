package panetina.elarion.core.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

final class IdentitySyncBatcher {
    private boolean full;
    private final Set<UUID> viewers = new LinkedHashSet<>();
    private final Set<UUID> subjects = new LinkedHashSet<>();

    synchronized void requestFull() {
        full = true;
        viewers.clear();
        subjects.clear();
    }

    synchronized void requestViewer(UUID viewerId) {
        if (viewerId == null || full) return;
        viewers.add(viewerId);
    }

    synchronized void requestSubject(UUID subjectId) {
        if (subjectId == null || full) return;
        subjects.add(subjectId);
    }

    synchronized Intent drain() {
        Intent intent = new Intent(full, Set.copyOf(viewers), Set.copyOf(subjects));
        full = false;
        viewers.clear();
        subjects.clear();
        return intent;
    }

    record Intent(boolean full, Set<UUID> viewers, Set<UUID> subjects) {
        boolean empty() {
            return !full && viewers.isEmpty() && subjects.isEmpty();
        }
    }
}
