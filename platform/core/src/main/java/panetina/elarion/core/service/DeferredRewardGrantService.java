package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.model.DeferredRewardGrant;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.storage.DeferredRewardGrantStorage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DeferredRewardGrantService {
    private final DeferredRewardGrantStorage storage;
    private final RewardActionService rewards;
    private final HistoryService history;
    private final Map<String, DeferredRewardGrant> grants = new LinkedHashMap<>();
    private MinecraftServer server;

    public DeferredRewardGrantService(
            DeferredRewardGrantStorage storage,
            RewardActionService rewards,
            HistoryService history
    ) {
        this.storage = storage;
        this.rewards = rewards;
        this.history = history;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        grants.clear();
        grants.putAll(storage.load(server));
    }

    public synchronized boolean enqueue(
            String grantId,
            UUID recipientId,
            String sourceSystem,
            String sourceId,
            List<RewardAction> actions
    ) {
        if (grantId == null || grantId.isBlank() || recipientId == null || actions == null || actions.isEmpty()) {
            return false;
        }
        if (grants.containsKey(grantId)) return false;
        grants.put(grantId, new DeferredRewardGrant(
                grantId, recipientId, sourceSystem, sourceId, actions, java.util.Set.of(),
                System.currentTimeMillis(), 0L));
        save();
        ServerPlayerEntity online = server == null ? null : server.getPlayerManager().getPlayer(recipientId);
        if (online != null) deliverPending(online);
        return true;
    }

    public synchronized void deliverPending(ServerPlayerEntity player) {
        if (server == null || player == null) return;
        boolean changed = false;
        for (DeferredRewardGrant original : List.copyOf(grants.values())) {
            if (!player.getUuid().equals(original.recipientId()) || original.delivered()) continue;
            DeferredRewardGrant current = original;
            for (int index = 0; index < current.actions().size(); index++) {
                if (current.completedActions().contains(index)) continue;
                RewardAction action = current.actions().get(index);
                if (!rewards.executeAction(current.sourceId(), action, player)) break;
                current = current.completeAction(index);
                grants.put(current.id(), current);
                save();
                changed = true;
            }
            if (current.complete()) {
                current = current.markDelivered(System.currentTimeMillis());
                grants.put(current.id(), current);
                history.record("reward", "deferred-grant-delivered", player.getUuid(),
                        "reward_grant", current.id(), "",
                        Map.of("sourceSystem", current.sourceSystem(), "sourceId", current.sourceId()));
                changed = true;
            }
        }
        if (changed) save();
    }

    public synchronized int pendingCount(UUID recipientId) {
        return (int) grants.values().stream()
                .filter(grant -> !grant.delivered())
                .filter(grant -> recipientId == null || recipientId.equals(grant.recipientId()))
                .count();
    }

    public synchronized void save() {
        if (server != null) storage.save(server, grants);
    }
}
