package panetina.elarion.core.service;

import panetina.elarion.core.model.DeferredRewardGrant;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Runtime-only pending-grant lookup projection. Persistent grant rows remain canonical. */
final class DeferredRewardGrantRuntimeIndex {
    private final Map<UUID, LinkedHashSet<String>> pendingByRecipient = new LinkedHashMap<>();
    private int pendingCount;

    void clear() {
        pendingByRecipient.clear();
        pendingCount = 0;
    }

    void add(DeferredRewardGrant grant) {
        if (!pending(grant)) return;
        if (pendingByRecipient.computeIfAbsent(grant.recipientId(), ignored -> new LinkedHashSet<>()).add(grant.id())) {
            pendingCount++;
        }
    }

    void remove(DeferredRewardGrant grant) {
        if (grant == null || grant.recipientId() == null || grant.id().isBlank()) return;
        LinkedHashSet<String> ids = pendingByRecipient.get(grant.recipientId());
        if (ids == null) return;
        if (ids.remove(grant.id())) pendingCount--;
        if (ids.isEmpty()) pendingByRecipient.remove(grant.recipientId());
    }

    void update(DeferredRewardGrant previous, DeferredRewardGrant current) {
        remove(previous);
        add(current);
    }

    List<String> pendingIds(UUID recipientId) {
        LinkedHashSet<String> ids = pendingByRecipient.get(recipientId);
        return ids == null || ids.isEmpty() ? List.of() : List.copyOf(ids);
    }

    int pendingCount(UUID recipientId) {
        return recipientId == null ? pendingCount : pendingIds(recipientId).size();
    }

    private static boolean pending(DeferredRewardGrant grant) {
        return grant != null && grant.recipientId() != null && !grant.id().isBlank() && !grant.delivered();
    }
}
