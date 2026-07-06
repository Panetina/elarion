package panetina.elarion.addons.angling.service;

import panetina.elarion.addons.angling.AnglingItems;
import panetina.elarion.addons.angling.model.AnglingCatchResult;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.service.DeferredRewardGrantService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AnglingRewardDeliveryService {
    public static final String SOURCE_SYSTEM = "elarion_angling";
    public static final String GRANT_PREFIX = "elarion_angling.catch.";
    public static final String SOURCE_ID_PREFIX = "catch:";

    private final RewardGrantSink grants;

    public AnglingRewardDeliveryService(DeferredRewardGrantService deferredRewards) {
        Objects.requireNonNull(deferredRewards, "deferredRewards");
        this.grants = grant -> deferredRewards.enqueue(
                grant.grantId(),
                grant.recipientId(),
                grant.sourceSystem(),
                grant.sourceId(),
                grant.actions());
    }

    AnglingRewardDeliveryService(RewardGrantSink grants) {
        this.grants = Objects.requireNonNull(grants, "grants");
    }

    static AnglingRewardDeliveryService noop() {
        return new AnglingRewardDeliveryService(grant -> true);
    }

    public void enqueue(AnglingCatchResult result) {
        AnglingRewardGrant grant = grantFor(result);
        grants.enqueue(grant);
    }

    public static AnglingRewardGrant grantFor(AnglingCatchResult result) {
        Objects.requireNonNull(result, "result");
        Map<String, String> parameters = rewardItemParameters(result);
        return new AnglingRewardGrant(
                grantId(result.eventId()),
                result.actorId(),
                SOURCE_SYSTEM,
                sourceId(result.eventId()),
                List.of(new RewardAction("item", parameters)));
    }

    private static Map<String, String> rewardItemParameters(AnglingCatchResult result) {
        var itemId = AnglingItems.rewardItemIdFor(result.fishDefinitionId());
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("id", itemId.toString());
        parameters.put("count", "1");
        if (AnglingItems.PLACEHOLDER_CATCH_ITEM_ID.equals(itemId)) {
            parameters.put("name", AnglingFishDisplayNames.displayName(result.fishDefinitionId()));
        }
        return Map.copyOf(parameters);
    }

    public static String grantId(UUID eventId) {
        return GRANT_PREFIX + Objects.requireNonNull(eventId, "eventId");
    }

    public static String sourceId(UUID eventId) {
        return SOURCE_ID_PREFIX + Objects.requireNonNull(eventId, "eventId");
    }

    @FunctionalInterface
    interface RewardGrantSink {
        boolean enqueue(AnglingRewardGrant grant);
    }

    public record AnglingRewardGrant(
            String grantId,
            UUID recipientId,
            String sourceSystem,
            String sourceId,
            List<RewardAction> actions
    ) {
        public AnglingRewardGrant {
            if (grantId == null || grantId.isBlank()) {
                throw new IllegalArgumentException("grantId must not be blank");
            }
            Objects.requireNonNull(recipientId, "recipientId");
            if (sourceSystem == null || sourceSystem.isBlank()) {
                throw new IllegalArgumentException("sourceSystem must not be blank");
            }
            if (sourceId == null || sourceId.isBlank()) {
                throw new IllegalArgumentException("sourceId must not be blank");
            }
            if (actions == null || actions.isEmpty()) {
                throw new IllegalArgumentException("actions must not be empty");
            }
            actions = List.copyOf(actions);
        }
    }
}
