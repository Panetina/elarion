package panetina.elarion.addons.angling.service;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.AnglingItems;
import panetina.elarion.addons.angling.model.AnglingCatchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AnglingRewardDeliveryServiceTest {
    @Test
    void catchResultCreatesDeterministicFishItemGrant() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        AnglingCatchResult result = result(eventId, actorId);

        var grant = AnglingRewardDeliveryService.grantFor(result);

        assertEquals("elarion_angling.catch." + eventId, grant.grantId());
        assertEquals(actorId, grant.recipientId());
        assertEquals("elarion_angling", grant.sourceSystem());
        assertEquals("catch:" + eventId, grant.sourceId());
        assertEquals(1, grant.actions().size());
        assertEquals("item", grant.actions().getFirst().type());
        assertEquals(AnglingItems.PALE_BROOKLING_ID.toString(),
                grant.actions().getFirst().parameters().get("id"));
        assertEquals("1", grant.actions().getFirst().parameters().get("count"));
        assertFalse(grant.actions().getFirst().parameters().containsKey("name"));
    }

    @Test
    void fishDisplayNamesCoverCurrentWorkingDefinitionsAndFallback() {
        assertEquals("Pale Brookling", AnglingFishDisplayNames.displayName(
                Identifier.of("elarion_angling", "placeholder_fish_001")));
        assertEquals("Stormveil Koi", AnglingFishDisplayNames.displayName(
                Identifier.of("elarion_angling", "placeholder_fish_006")));
        assertEquals("Baitbright Perch", AnglingFishDisplayNames.displayName(
                Identifier.of("elarion_angling", "placeholder_fish_007")));
        assertEquals(AnglingFishDisplayNames.UNKNOWN_CATCH_NAME, AnglingFishDisplayNames.displayName(
                Identifier.of("elarion_angling", "placeholder_missing")));
    }

    @Test
    void unknownFishRewardUsesSafeGenericName() {
        var grant = AnglingRewardDeliveryService.grantFor(result(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "placeholder_missing")));

        assertEquals(
                AnglingItems.PLACEHOLDER_CATCH_ITEM_ID.toString(),
                grant.actions().getFirst().parameters().get("id"));
        assertEquals(
                AnglingFishDisplayNames.UNKNOWN_CATCH_NAME,
                grant.actions().getFirst().parameters().get("name"));
    }

    @Test
    void rewardItemIdsCoverCurrentWorkingDefinitionsAndFallback() {
        assertEquals(7, AnglingItems.rewardItemsByFish().size());
        assertEquals(
                AnglingItems.PALE_BROOKLING_ID,
                AnglingItems.rewardItemIdFor(Identifier.of("elarion_angling", "placeholder_fish_001")));
        assertEquals(
                AnglingItems.STORMVEIL_KOI_ID,
                AnglingItems.rewardItemIdFor(Identifier.of("elarion_angling", "placeholder_fish_006")));
        assertEquals(
                AnglingItems.BAITBRIGHT_PERCH_ID,
                AnglingItems.rewardItemIdFor(Identifier.of("elarion_angling", "placeholder_fish_007")));
        assertEquals(
                AnglingItems.PLACEHOLDER_CATCH_ITEM_ID,
                AnglingItems.rewardItemIdFor(Identifier.of("elarion_angling", "placeholder_missing")));
    }

    @Test
    void duplicateGrantRejectionIsTreatedAsIdempotentSuccess() {
        List<AnglingRewardDeliveryService.AnglingRewardGrant> grants = new ArrayList<>();
        AnglingRewardDeliveryService service = new AnglingRewardDeliveryService(grant -> {
            grants.add(grant);
            return false;
        });

        service.enqueue(result(UUID.randomUUID(), UUID.randomUUID()));

        assertEquals(1, grants.size());
    }

    @Test
    void transientGrantFailurePropagatesForSessionRetry() {
        AnglingRewardDeliveryService service = new AnglingRewardDeliveryService(grant -> {
            throw new IllegalStateException("test reward enqueue failure");
        });

        assertThrows(IllegalStateException.class,
                () -> service.enqueue(result(UUID.randomUUID(), UUID.randomUUID())));
    }

    private static AnglingCatchResult result(UUID eventId, UUID actorId) {
        return result(eventId, actorId, Identifier.of("elarion_angling", "placeholder_fish_001"));
    }

    private static AnglingCatchResult result(UUID eventId, UUID actorId, Identifier fishId) {
        return new AnglingCatchResult(
                eventId,
                1_000L,
                actorId,
                Identifier.of("elarion_angling", "fishing"),
                fishId,
                Identifier.of("elarion_angling", "placeholder_common"),
                1,
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"));
    }
}
