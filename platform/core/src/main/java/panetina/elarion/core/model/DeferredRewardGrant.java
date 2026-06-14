package panetina.elarion.core.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record DeferredRewardGrant(
        String id,
        UUID recipientId,
        String sourceSystem,
        String sourceId,
        List<RewardAction> actions,
        Set<Integer> completedActions,
        long createdAt,
        long deliveredAt
) {
    public DeferredRewardGrant {
        id = id == null ? "" : id;
        sourceSystem = sourceSystem == null ? "" : sourceSystem;
        sourceId = sourceId == null ? "" : sourceId;
        actions = actions == null ? List.of() : List.copyOf(actions);
        completedActions = completedActions == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(completedActions));
        createdAt = createdAt <= 0 ? System.currentTimeMillis() : createdAt;
    }

    public DeferredRewardGrant completeAction(int index) {
        Set<Integer> completed = new LinkedHashSet<>(completedActions);
        completed.add(index);
        return new DeferredRewardGrant(
                id, recipientId, sourceSystem, sourceId, actions, completed, createdAt, deliveredAt);
    }

    public DeferredRewardGrant markDelivered(long time) {
        return new DeferredRewardGrant(
                id, recipientId, sourceSystem, sourceId, actions, completedActions, createdAt, time);
    }

    public boolean complete() {
        return completedActions.size() >= actions.size();
    }

    public boolean delivered() {
        return deliveredAt > 0;
    }
}
