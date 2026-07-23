package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.storage.DeferredRewardGrantStorage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class DeferredRewardGrantServiceTest {
    @Test
    void enqueueCreatesClaimablePersonalRewardNotificationWithoutDelivering() {
        DeferredRewardGrantService service = new DeferredRewardGrantService(
                new DeferredRewardGrantStorage(LoggerFactory.getLogger("test")),
                null,
                null);
        UUID recipient = UUID.randomUUID();

        service.enqueue("grant-1", recipient, "elarion_realms", "test_reward",
                List.of(new RewardAction("item", Map.of("id", "minecraft:diamond", "count", "1"))));

        var snapshot = service.snapshot(recipient);

        assertEquals(1, snapshot.entries().size());
        assertEquals(ElarionNotificationCategory.REWARD, snapshot.entries().getFirst().category());
        assertEquals("Rewards from the Realm Master.", snapshot.entries().getFirst().body());
        assertFalse(snapshot.entries().getFirst().actions().isEmpty());
        assertEquals("elarion_core:claim_reward", snapshot.entries().getFirst().actions().getFirst().id());
        assertEquals("item:minecraft:diamond", snapshot.entries().getFirst().rewards().getFirst().icon());
        assertEquals(1, snapshot.entries().getFirst().rewards().getFirst().count());
        assertEquals(1, service.pendingCount(recipient));
    }

    @Test
    void offeringRewardNotificationUsesShrineBody() {
        DeferredRewardGrantService service = new DeferredRewardGrantService(
                new DeferredRewardGrantStorage(LoggerFactory.getLogger("test")),
                null,
                null);
        UUID recipient = UUID.randomUUID();

        service.enqueue("grant-2", recipient, "elarion_offerings", "council_hall_blessing",
                List.of(new RewardAction("item", Map.of("id", "minecraft:emerald", "count", "3"))));

        assertEquals("Rewards from the Shrine of Foundation.",
                service.snapshot(recipient).entries().getFirst().body());
    }

    @Test
    void rewardPreviewIncludesConfiguredEnchantmentsForTooltip() {
        DeferredRewardGrantService service = new DeferredRewardGrantService(
                new DeferredRewardGrantStorage(LoggerFactory.getLogger("test")),
                null,
                null);
        UUID recipient = UUID.randomUUID();

        service.enqueue("grant-enchanted", recipient, "elarion_offerings", "enchanted_reward",
                List.of(new RewardAction("item", Map.of(
                        "id", "minecraft:diamond_sword",
                        "count", "1",
                        "enchants", "minecraft:sharpness:5,minecraft:unbreaking:3"))));

        var reward = service.snapshot(recipient).entries().getFirst().rewards().getFirst();

        assertEquals(List.of("Sharpness V", "Unbreaking III"), reward.tooltipLines());
    }

    @Test
    void snapshotContainsOnlyRewardEntriesOwnedByThisService() {
        DeferredRewardGrantService service = new DeferredRewardGrantService(
                new DeferredRewardGrantStorage(LoggerFactory.getLogger("test")),
                null,
                null);
        UUID recipient = UUID.randomUUID();

        var snapshot = service.snapshot(recipient);

        assertEquals(0, snapshot.entries().size());
    }

    @Test
    void idempotentEnqueueDistinguishesExactRetryFromConflictingGrantId() {
        DeferredRewardGrantService service = new DeferredRewardGrantService(
                new DeferredRewardGrantStorage(LoggerFactory.getLogger("test")), null, null);
        UUID recipient = UUID.randomUUID();
        List<RewardAction> actions = List.of(
                new RewardAction("item", Map.of("id", "minecraft:cod", "count", "1")));

        assertEquals(DeferredRewardGrantService.EnqueueResult.ENQUEUED,
                service.enqueueIdempotent("catch-1", recipient, "elarion_angling", "event", actions));
        assertEquals(DeferredRewardGrantService.EnqueueResult.EXACT_RETRY,
                service.enqueueIdempotent("catch-1", recipient, "elarion_angling", "event", actions));
        assertEquals(DeferredRewardGrantService.EnqueueResult.CONFLICT,
                service.enqueueIdempotent("catch-1", recipient, "elarion_angling", "different", actions));
        assertEquals(1, service.pendingCount(recipient));
    }
}
