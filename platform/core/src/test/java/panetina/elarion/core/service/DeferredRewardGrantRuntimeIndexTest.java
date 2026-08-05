package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.DeferredRewardGrant;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DeferredRewardGrantRuntimeIndexTest {
    @Test
    void indexesOnlyPendingGrantsByRecipient() {
        DeferredRewardGrantRuntimeIndex index = new DeferredRewardGrantRuntimeIndex();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        DeferredRewardGrant pendingFirst = grant("first", first, 0L);
        DeferredRewardGrant delivered = grant("delivered", first, 10L);
        DeferredRewardGrant pendingSecond = grant("second", second, 0L);

        index.add(pendingFirst);
        index.add(delivered);
        index.add(pendingSecond);

        assertEquals(List.of("first"), index.pendingIds(first));
        assertEquals(List.of("second"), index.pendingIds(second));
        assertEquals(2, index.pendingCount(null));
        assertEquals(1, index.pendingCount(first));

        index.update(pendingFirst, grant("first", first, 20L));

        assertTrue(index.pendingIds(first).isEmpty());
        assertEquals(1, index.pendingCount(null));
    }

    private static DeferredRewardGrant grant(String id, UUID recipient, long deliveredAt) {
        return new DeferredRewardGrant(id, recipient, "test", "source", List.of(), Set.of(), 1L, deliveredAt);
    }
}
